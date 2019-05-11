package tech.mistermel.afkbot.packet;

import org.json.JSONObject;

import com.github.steveice10.mc.protocol.data.game.setting.Difficulty;

/**
 * Packet that is sent when the difficulty specified by the
 * Minecraft server is changed.
 * 
 * Packet identifier: difficulty
 * 
 * Parameters:
 * - difficulty (string, required): The new difficulty
 * 
 * @author Melle Moerkerk
 */
public class DifficultyPacket implements Packet {

	public static final String PACKET_NAME = "difficulty";
	
	private Difficulty difficulty;
	
	public DifficultyPacket(Difficulty difficulty) {
		this.difficulty = difficulty;
	}
	
	public JSONObject get() {
		JSONObject json = new JSONObject();
		json.put("type", PACKET_NAME);
		JSONObject payload = new JSONObject();
		json.put("payload", payload);
		payload.put("difficulty", difficulty.name());
		
		return json;
	}
	
}
