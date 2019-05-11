package tech.mistermel.brickbot.util;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import tech.mistermel.brickbot.material.MaterialRegistry;

public class Translator {

	public static final String LANG_URL = "https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc/1.13.2/language.json";
	
	private static Map<String, String> translations = new HashMap<String, String>();
	
	private Translator() {}
	
	public static void loadTranslations() {
		JSONObject json = new JSONObject(MaterialRegistry.fetch(LANG_URL));
		
		for(String key : json.keySet()) {
			translations.put(key, json.getString(key));
		}
	}
	
	public static String getTranslation(String key) {
		return translations.get(key);
	}
	
}
