package tech.mistermel.afkbot.packet;

import org.json.JSONObject;

import tech.mistermel.afkbot.util.Player;

public class PlayerListPacket implements Packet {

	public static final String PACKET_NAME = "player_list";
	
	private PlayerListPacketAction action;
	private Player player;
	
	public PlayerListPacket(PlayerListPacketAction action, Player player) {
		this.action = action;
		this.player = player;
	}
	
	public JSONObject get() {
		JSONObject json = new JSONObject();
		json.put("type", PACKET_NAME);
		JSONObject payload = new JSONObject();
		json.put("payload", payload);
		payload.put("action", action.name());
		payload.put("uuid", player.getUuid());
		payload.put("name", player.getUsername());
		payload.put("gamemode", player.getGameMode());
		payload.put("ping", player.getPing());
		
		return json;
	}
	
	public static enum PlayerListPacketAction {
		
		ADD, REMOVE, UPDATE;
		
	}
	
}
