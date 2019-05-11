package tech.mistermel.afkbot.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {

	private Constants() {}
	
	public static final List<Integer> CONSUMABLE_ITEMS = Arrays.asList(
			536, // Porkchop
			537, // Cooked porkchop
			612, // Cookie
			615, // Melon slice
			619, // Beef
			620, // Cooked beef
			
			622  // Cooked chicken
	);
	
	public static final List<Integer> WEAPON_ITEMS = Arrays.asList(
			484, // Iron sword
			485, // Wooden sword
			489, // Stone sword
			493, // Diamond sword
			500 // Golden sword
	);
	
	public static enum Attribute {
		CONSUMABLE(CONSUMABLE_ITEMS),
		WEAPON(WEAPON_ITEMS);
		
		private List<Integer> list;
		
		private Attribute(List<Integer> list) {
			this.list = list;
		}
		
		public List<Integer> getList() {
			return list;
		}
	}
	
	public static List<Attribute> getAttributes(int id) {
		List<Attribute> result = new ArrayList<Attribute>();
		
		for(Attribute attribute : Attribute.values()) {
			if(attribute.getList().contains(id))
				result.add(attribute);
		}
		
		return result;
	}
	
}
