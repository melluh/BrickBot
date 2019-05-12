package tech.mistermel.brickbot.material;

import java.util.HashMap;
import java.util.Map;

public class BlockType extends Material {
	
	private boolean isDefault;
	private Map<String, String> properties = new HashMap<String, String>();
	
	public BlockType(long id, String name, boolean isDefault) {
		super(id, name);
		this.isDefault = isDefault;
	}
	
	public void setProperty(String key, String value) {
		properties.put(key, value);
	}
	
	public String getProperty(String key) {
		return properties.get(key);
	}
	
	public boolean isDefault() {
		return isDefault;
	}

}
