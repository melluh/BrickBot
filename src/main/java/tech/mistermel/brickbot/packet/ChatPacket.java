package tech.mistermel.brickbot.packet;

import org.json.JSONObject;

/**
 * Packet that is sent when the bot receives
 * a chat message.
 * 
 * Packet identifier: chat
 * 
 * Parameters:
 * - msg (string, required): Contains the chat message
 * 
 * @author Melle Moerkerk
 */
public class ChatPacket implements Packet {

	public static final String PACKET_NAME = "chat";
	
	private String msg;
	
	public ChatPacket(String msg) {
		this.msg = msg;
	}
	
	public JSONObject get() {
		JSONObject json = new JSONObject();
		json.put("type", PACKET_NAME);
		JSONObject payload = new JSONObject();
		json.put("payload", payload);
		payload.put("msg", msg);
		
		return json;
	}
	
}
