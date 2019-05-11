package tech.mistermel.afkbot.handler;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntryAction;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;
import com.github.steveice10.mc.protocol.data.game.world.block.BlockChangeRecord;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerDifficultyPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPlayerListEntryPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityTeleportPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerHealthPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerSetSlotPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerBlockChangePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerMultiBlockChangePacket;
import com.github.steveice10.packetlib.event.session.ConnectedEvent;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;

import tech.mistermel.afkbot.AFKBot;
import tech.mistermel.afkbot.material.BlockType;
import tech.mistermel.afkbot.material.MaterialRegistry;
import tech.mistermel.afkbot.packet.ChatPacket;
import tech.mistermel.afkbot.packet.PlayerListPacket;
import tech.mistermel.afkbot.packet.PlayerListPacket.PlayerListPacketAction;
import tech.mistermel.afkbot.util.Player;
import tech.mistermel.core.logging.Logger;

public class PacketHandler extends SessionAdapter {

	public static final String LOGGER_NAME = "PacketHandler";
	
	private Logger logger;
	
	public PacketHandler() {
		this.logger = Logger.createBasic(LOGGER_NAME, AFKBot.DEBUG);
	}
	
	@Override
	public void packetReceived(PacketReceivedEvent event) {
		if (event.getPacket() instanceof ServerJoinGamePacket) {
			ServerJoinGamePacket packet = (ServerJoinGamePacket) event.getPacket();
			AFKBot.getInstance().setDifficulty(packet.getDifficulty());
		} else if (event.getPacket() instanceof ServerDifficultyPacket) {
			ServerDifficultyPacket packet = (ServerDifficultyPacket) event.getPacket();
			AFKBot.getInstance().setDifficulty(packet.getDifficulty());
		} else if (event.getPacket() instanceof ServerChatPacket) {
			ServerChatPacket packet = (ServerChatPacket) event.getPacket();
			ChatPacket chatPacket = new ChatPacket(packet.getMessage().getFullText());
			AFKBot.getInstance().getWebSocketHandler().sendPacket(chatPacket);
		} else if (event.getPacket() instanceof ServerPlayerHealthPacket) {
			ServerPlayerHealthPacket packet = (ServerPlayerHealthPacket) event.getPacket();
			AFKBot.getInstance().setHealth(packet.getHealth(), packet.getFood(), packet.getSaturation());
		} else if (event.getPacket() instanceof ServerPlayerPositionRotationPacket) {
			ServerPlayerPositionRotationPacket packet = (ServerPlayerPositionRotationPacket) event.getPacket();
			AFKBot.getInstance().setLocation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch(), true);
		} else if (event.getPacket() instanceof ServerSetSlotPacket) {
			ServerSetSlotPacket packet = (ServerSetSlotPacket) event.getPacket();

			int windowId = packet.getWindowId();
			int slot = packet.getSlot();
			ItemStack item = packet.getItem();
			if (windowId == 0)
				AFKBot.getInstance().setSlot(slot, item);
		} else if (event.getPacket() instanceof ServerPlayerListEntryPacket) {
			ServerPlayerListEntryPacket packet = (ServerPlayerListEntryPacket) event.getPacket();
			PlayerListEntryAction action = packet.getAction();

			if (action == PlayerListEntryAction.ADD_PLAYER) {
				for (PlayerListEntry entry : packet.getEntries()) {
					GameProfile profile = entry.getProfile();
					if (profile.getId().equals(AFKBot.getInstance().getBotProfile().getId()))
						continue;

					Player p = new Player(profile.getIdAsString(), profile.getName(), entry.getPing(),
							entry.getGameMode());
					AFKBot.getInstance().getPlayerMap().put(profile.getIdAsString(), p);

					PlayerListPacket packetOut = new PlayerListPacket(PlayerListPacketAction.ADD, p);
					AFKBot.getInstance().getWebSocketHandler().sendPacket(packetOut);
				}
			}
			if (action == PlayerListEntryAction.REMOVE_PLAYER) {
				for (PlayerListEntry entry : packet.getEntries()) {
					Player p = AFKBot.getInstance().getPlayerMap().get(entry.getProfile().getIdAsString());
					if (p != null) {
						PlayerListPacket packetOut = new PlayerListPacket(PlayerListPacketAction.REMOVE, p);
						AFKBot.getInstance().getWebSocketHandler().sendPacket(packetOut);

						AFKBot.getInstance().getPlayerMap().remove(entry.getProfile().getIdAsString());
					}
				}
			}
			if (action == PlayerListEntryAction.UPDATE_LATENCY) {
				for (PlayerListEntry entry : packet.getEntries()) {
					Player p = AFKBot.getInstance().getPlayerMap().get(entry.getProfile().getIdAsString());
					if (p != null) {
						p.setPing(entry.getPing());

						PlayerListPacket packetOut = new PlayerListPacket(PlayerListPacketAction.UPDATE, p);
						AFKBot.getInstance().getWebSocketHandler().sendPacket(packetOut);
					}
				}
			}
			if (action == PlayerListEntryAction.UPDATE_GAMEMODE) {
				for (PlayerListEntry entry : packet.getEntries()) {
					Player p = AFKBot.getInstance().getPlayerMap().get(entry.getProfile().getIdAsString());
					if (p != null) {
						p.setGameMode(entry.getGameMode());

						PlayerListPacket packetOut = new PlayerListPacket(PlayerListPacketAction.REMOVE, p);
						AFKBot.getInstance().getWebSocketHandler().sendPacket(packetOut);
					}
				}
			}
		} else if (event.getPacket() instanceof ServerSpawnPlayerPacket) {
			ServerSpawnPlayerPacket packet = (ServerSpawnPlayerPacket) event.getPacket();
			Player p = AFKBot.getInstance().getPlayerMap().get(packet.getUUID().toString());
			if (p != null) {
				p.setEntityId(packet.getEntityId());
				p.setLocation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch(), false);
			}
		} else if (event.getPacket() instanceof ServerEntityPositionRotationPacket) {
			ServerEntityPositionRotationPacket packet = (ServerEntityPositionRotationPacket) event.getPacket();

			Player p = AFKBot.getInstance().getPlayerByEntityId(packet.getEntityId());
			if (p != null)
				p.setLocation(packet.getMovementX(), packet.getMovementY(), packet.getMovementZ(), packet.getYaw(), packet.getPitch(), true);
		} else if (event.getPacket() instanceof ServerEntityPositionPacket) {
			ServerEntityPositionPacket packet = (ServerEntityPositionPacket) event.getPacket();

			Player p = AFKBot.getInstance().getPlayerByEntityId(packet.getEntityId());
			if (p != null)
				p.setLocation(packet.getMovementX(), packet.getMovementY(), packet.getMovementZ(), packet.getYaw(), packet.getPitch(), true);
		} else if (event.getPacket() instanceof ServerEntityTeleportPacket) {
			ServerEntityTeleportPacket packet = (ServerEntityTeleportPacket) event.getPacket();

			Player p = AFKBot.getInstance().getPlayerByEntityId(packet.getEntityId());
			if (p != null)
				p.setLocation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch(), false);
		} else if (event.getPacket() instanceof ServerChunkDataPacket) {
			ServerChunkDataPacket packet = (ServerChunkDataPacket) event.getPacket();
			BlockHandler.addColumn(packet.getColumn());
		} else if (event.getPacket() instanceof ServerBlockChangePacket) {
			ServerBlockChangePacket packet = (ServerBlockChangePacket) event.getPacket();
			BlockChangeRecord record = packet.getRecord();
			
			BlockType block = MaterialRegistry.getBlock(record.getBlock().getId());
			if(block == null) {
				logger.warn("Unknown block with ID {0}", record.getBlock().getId());
				return;
			}
			
			System.out.println(record.getPosition() + " ::: " + block.getName());
		} else if (event.getPacket() instanceof ServerMultiBlockChangePacket) {
			ServerMultiBlockChangePacket packet = (ServerMultiBlockChangePacket) event.getPacket();
			for(BlockChangeRecord record : packet.getRecords()) {
				BlockType block = MaterialRegistry.getBlock(record.getBlock().getId());
				if(block == null) {
					logger.warn("Unknown block with ID {0}", record.getBlock().getId());
					continue;
				}
				
				System.out.println(record.getPosition() + " :::: " + block.getName());
			}
		}
	}
	
	@Override
	public void connected(ConnectedEvent event) {
		logger.info("AFKBot is ready");
	}

	@Override
	public void disconnected(DisconnectedEvent event) {
		String reason = Message.fromString(event.getReason()).getFullText();
		logger.warn("Disconnected: " + reason);
		
		ChatPacket packet = new ChatPacket("-- Disconnected: " + reason);
		AFKBot.getInstance().getWebSocketHandler().sendPacket(packet);

		if (event.getCause() != null)
			event.getCause().printStackTrace();
	}
	
}
