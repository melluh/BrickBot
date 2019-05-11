package tech.mistermel.afkbot.packet;

import org.json.JSONObject;

/**
 * Packet that is sent when the health,
 * food or saturation of the bot changes.
 * 
 * Packet identifier: health
 * 
 * Parameters:
 * - health (float, required): The new health value
 * - food (float, required): The new food value
 * - saturation (float, required): The saturation value
 * 
 * @author Melle Moerkerk
 */
public class HealthPacket implements Packet {

	public static final String PACKET_NAME = "health";
	
	private float health;
	private float food;
	private float saturation;
	
	public HealthPacket(float health, float food, float saturation) {
		this.health = health;
		this.food = food;
		this.saturation = saturation;
	}
	
	public JSONObject get() {
		JSONObject json = new JSONObject();
		json.put("type", PACKET_NAME);
		JSONObject payload = new JSONObject();
		json.put("payload", payload);
		payload.put("health", health);
		payload.put("food", food);
		payload.put("saturation", saturation);
		
		return json;
	}

}
