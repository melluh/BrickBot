package tech.mistermel.afkbot.packet;

import org.json.JSONObject;

/**
 * Packet that is sent when the location of the
 * bot or a player changes.
 * 
 * Packet identifier: loc
 * 
 * Parameters:
 * - x (double, required): The new X coördinate
 * - y (double, required): The new X coördinate
 * - z (double, required): The new X coördinate
 * - uuid (string, optional): The UUID of the player. Not present if it is the bot itself.
 * 
 * @author Melle Moerkerk
 */
public class LocationPacket implements Packet {

	public static final String PACKET_NAME = "loc";
	
	private String uuid;
	private double x;
	private double y;
	private double z;
	
	public LocationPacket(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public LocationPacket(double x, double y, double z, String uuid) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.uuid = uuid;
	}
	
	public JSONObject get() {
		JSONObject json = new JSONObject();
		json.put("type", PACKET_NAME);
		JSONObject payload = new JSONObject();
		json.put("payload", payload);
		payload.put("x", x);
		payload.put("y", y);
		payload.put("z", z);
		payload.put("uuid", uuid);
		
		return json;
	}

}
