package tech.mistermel.brickbot.material;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class MaterialRegistry {

	public static final String BLOCKS_URL = "https://apimon.de/mcdata/1.13.2/blocks.json";
	public static final String ITEMS_URL = "https://apimon.de/mcdata/1.13.2/items.json";
	
	private static Set<Material> materials = new HashSet<Material>();
	
	private MaterialRegistry() {}
	
	public static void load() {
		loadItems();
		loadBlocks();
	}
	
	public static void loadItems() {
		JSONObject items = new JSONObject(fetch(ITEMS_URL));
		for(String key : items.keySet()) {
			JSONObject item = items.getJSONObject(key);
			int id = item.getInt("protocol_id");
			
			materials.add(new ItemType(id, key));
		}
	}
	
	public static void loadBlocks() {
		JSONObject blocks = new JSONObject(fetch(BLOCKS_URL));
		for(String key : blocks.keySet()) {
			JSONObject block = blocks.getJSONObject(key);
			JSONArray states = block.getJSONArray("states");
			
			for(int i = 0; i < states.length(); i++) {
				JSONObject state = states.getJSONObject(i);
				int id = state.getInt("id");
				boolean isDefault = state.has("default") ? state.getBoolean("default") : false;
				
				BlockType blockType = new BlockType(id, key, isDefault);
				if(state.has("properties")) {
					JSONObject properties = state.getJSONObject("properties");
					
					for(String property : properties.keySet()) {
						blockType.setProperty(property, properties.getString(property));
					}
				}
				
				materials.add(blockType);
			}
		}
	}
	
	public static ItemType getItem(int id) {
		for(Material material : materials) {
			if(material.getId() == id && material instanceof ItemType)
				return (ItemType) material;
		}
		return null;
	}
	
	public static BlockType getBlock(int id) {
		for(Material material : materials) {
			if(material.getId() == id && material instanceof BlockType)
				return (BlockType) material;
		}
		return null;
	}
	
	public static String fetch(String url) {
		try {
			URLConnection conn = new URL(url).openConnection();
			conn.setRequestProperty("User-Agent", "BrickBot/1.0");
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder builder = new StringBuilder();
			
			String line;
			while((line = reader.readLine()) != null) {
				builder.append(line);
			}
			
			return builder.toString();
		} catch (MalformedURLException e) {
			System.out.println("Malformed URL: " + url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
