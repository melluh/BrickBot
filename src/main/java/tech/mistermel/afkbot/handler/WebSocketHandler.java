package tech.mistermel.afkbot.handler;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;

import tech.mistermel.afkbot.AFKBot;
import tech.mistermel.afkbot.packet.ItemPacket;
import tech.mistermel.afkbot.packet.LocationPacket;
import tech.mistermel.afkbot.packet.Packet;
import tech.mistermel.afkbot.packet.PlayerListPacket;
import tech.mistermel.afkbot.packet.PlayerListPacket.PlayerListPacketAction;
import tech.mistermel.afkbot.util.Player;
import tech.mistermel.core.logging.Logger;

public class WebSocketHandler extends WebSocketServer {
	
	public static final String LOGGER_NAME = "WebSocketHandler";

	private Logger logger;
	
	public WebSocketHandler() {
		this.logger = Logger.createBasic(LOGGER_NAME, AFKBot.DEBUG);
	}
	
	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		JSONObject json = new JSONObject();
		json.put("type", "init");
		JSONObject payload = new JSONObject();
		json.put("payload", payload);
		payload.put("difficulty", AFKBot.getInstance().getDifficulty());
		payload.put("health", AFKBot.getInstance().getHealth());
		payload.put("food", AFKBot.getInstance().getFood());
		payload.put("saturation", AFKBot.getInstance().getSaturation());
		payload.put("server_ip", AFKBot.getInstance().getServerIp());
		payload.put("username", AFKBot.getInstance().getBotProfile().getName());
		payload.put("uuid", AFKBot.getInstance().getBotProfile().getIdAsString());
		conn.send(json.toString());
		
		ItemStack[] inv = AFKBot.getInstance().getInventory();
		for(int i = 0; i < inv.length; i++) {
			ItemStack item = inv[i];
			
			if(item == null)
				continue;
			
			ItemPacket packet = new ItemPacket(i, item);
			this.sendPacket(packet);
		}
		
		for(Player p : AFKBot.getInstance().getPlayers()) {
			PlayerListPacket playerPacket = new PlayerListPacket(PlayerListPacketAction.ADD, p);
			this.sendPacket(playerPacket, conn);
		}
		
		LocationPacket locPacket = new LocationPacket(AFKBot.getInstance().getX(), AFKBot.getInstance().getY(), AFKBot.getInstance().getZ());
		this.sendPacket(locPacket);
		
		for(Player p : AFKBot.getInstance().getPlayers()) {
			LocationPacket packet = new LocationPacket((int) p.getX(), (int) p.getY(), (int) p.getZ(), p.getUuid());
			AFKBot.getInstance().getWebSocketHandler().sendPacket(packet);	
		}
	}
	
	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {}

	@Override
	public void onMessage(WebSocket conn, String msg) {
		JSONObject json = new JSONObject(msg);
		JSONObject payload = json.getJSONObject("payload");
		String type = json.getString("type");
		
		if(type.equals("chat_msg")) {
			AFKBot.getInstance().sendChatMessage(payload.getString("msg"));
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		logger.err("An error occured", ex);
	}

	@Override
	public void onStart() {
		logger.info("WebSocket server is now ready");
	}
	
	public void sendPacket(Packet packet) {
		this.broadcast(packet.get().toString());
	}
	
	public void sendPacket(Packet packet, WebSocket conn) {
		conn.send(packet.get().toString());
	}

}
