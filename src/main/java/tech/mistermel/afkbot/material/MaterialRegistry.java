package tech.mistermel.afkbot.material;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class MaterialRegistry {

	public static final String BLOCKS_URL = "https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc/1.13.2/blocks.json";
	public static final String ITEMS_URL = "https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc/1.13.2/items.json";
	public static final String MATERIALS_URL = "https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc/1.13.2/materials.json";
	
	private static Set<Material> materials = new HashSet<Material>();
	
	private MaterialRegistry() {}
	
	public static void load() {
		loadItems();
		loadBlocks();
	}
	
	public static void loadItems() {
		JSONArray items = new JSONArray(fetch(ITEMS_URL));
		for(int i = 0; i < items.length(); i++) {
			JSONObject json = items.getJSONObject(i);
			long id = json.getLong("id");
			String name = json.getString("name");
			String displayName = json.getString("displayName");
			int stackSize = json.getInt("stackSize");
			
			materials.add(new ItemType(id, name, displayName, stackSize));
		}
	}
	
	public static void loadBlocks() {
		JSONArray blocks = new JSONArray(fetch(BLOCKS_URL));
		for(int i = 0; i < blocks.length(); i++) {
			JSONObject json = blocks.getJSONObject(i);
			long id = json.getLong("id");
			String name = json.getString("name");
			String displayName = json.getString("displayName");
			int stackSize = json.getInt("stackSize");
			double hardness = -1;
			if(!json.isNull("hardness"))
				hardness = json.getDouble("hardness");
			boolean diggable = json.getBoolean("diggable");
			
			materials.add(new BlockType(id, name, displayName, stackSize, hardness, diggable));
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
			BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
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
