package tech.mistermel.brickbot.handler;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;

import tech.mistermel.brickbot.BrickBot;
import tech.mistermel.brickbot.packet.ItemPacket;
import tech.mistermel.brickbot.packet.LocationPacket;
import tech.mistermel.brickbot.packet.Packet;
import tech.mistermel.brickbot.packet.PlayerListPacket;
import tech.mistermel.brickbot.packet.PlayerListPacket.PlayerListPacketAction;
import tech.mistermel.brickbot.util.Player;
import tech.mistermel.core.logging.Logger;

public class WebSocketHandler extends WebSocketServer {
	
	public static final String LOGGER_NAME = "WebSocketHandler";

	private Logger logger;
	
	public WebSocketHandler() {
		this.logger = Logger.createBasic(LOGGER_NAME, BrickBot.DEBUG);
	}
	
	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		JSONObject json = new JSONObject();
		json.put("type", "init");
		JSONObject payload = new JSONObject();
		json.put("payload", payload);
		payload.put("difficulty", BrickBot.getInstance().getDifficulty());
		payload.put("health", BrickBot.getInstance().getHealth());
		payload.put("food", BrickBot.getInstance().getFood());
		payload.put("saturation", BrickBot.getInstance().getSaturation());
		payload.put("server_ip", BrickBot.getInstance().getServerIp());
		payload.put("username", BrickBot.getInstance().getBotProfile().getName());
		payload.put("uuid", BrickBot.getInstance().getBotProfile().getIdAsString());
		conn.send(json.toString());
		
		ItemStack[] inv = BrickBot.getInstance().getInventory();
		for(int i = 0; i < inv.length; i++) {
			ItemStack item = inv[i];
			
			if(item == null)
				continue;
			
			ItemPacket packet = new ItemPacket(i, item);
			this.sendPacket(packet);
		}
		
		for(Player p : BrickBot.getInstance().getPlayers()) {
			PlayerListPacket playerPacket = new PlayerListPacket(PlayerListPacketAction.ADD, p);
			this.sendPacket(playerPacket, conn);
		}
		
		LocationPacket locPacket = new LocationPacket(BrickBot.getInstance().getX(), BrickBot.getInstance().getY(), BrickBot.getInstance().getZ());
		this.sendPacket(locPacket);
		
		for(Player p : BrickBot.getInstance().getPlayers()) {
			LocationPacket packet = new LocationPacket((int) p.getX(), (int) p.getY(), (int) p.getZ(), p.getUuid());
			BrickBot.getInstance().getWebSocketHandler().sendPacket(packet);	
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
			BrickBot.getInstance().sendChatMessage(payload.getString("msg"));
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
