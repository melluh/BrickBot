package tech.mistermel.brickbot.packet;

import org.json.JSONObject;

/**
 * Packet that is sent when a inventory slot
 * should be cleared of any items.
 * 
 * Packet identifier: clear-item
 * 
 * Parameters:
 * - slot (int, required): Contains the number of the slot that should be cleared.
 * 
 * @author Melle Moerkerk
 */
public class ClearItemPacket implements Packet {
	
	public static final String PACKET_NAME = "clear_item";

	private int slot;
	
	public ClearItemPacket(int slot) {
		this.slot = slot;
	}
	
	public JSONObject get() {
		JSONObject json = new JSONObject();
		json.put("type", PACKET_NAME);
		JSONObject payload = new JSONObject();
		json.put("payload", payload);
		payload.put("slot", slot);
		
		return json;
	}
	
}
