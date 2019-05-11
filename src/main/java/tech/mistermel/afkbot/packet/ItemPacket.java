package tech.mistermel.afkbot.packet;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;

import tech.mistermel.afkbot.material.MaterialRegistry;
import tech.mistermel.afkbot.util.Constants;
import tech.mistermel.afkbot.util.Constants.Attribute;

/**
 * Packet that is sent when the inventory of the
 * bot has a new item or replaces an existing item.
 * 
 * Packet identifier: item
 * 
 * Parameters:
 * - slot (int, required): Contains the number of the slot that the item is in.
 * - name (string, required): Contains the Minecraft internal item name.
 * - custom_name (string, optional): Only present if the item was renamed. Contains the custom name of the item.
 * - amount (int, required): Contains the number of items in the stack.
 * - attributes (string array, required): Contains a list of attributes (e.g. "CONSUMABLE"). This may be empty.
 * 
 * @author Melle Moerkerk
 */
public class ItemPacket implements Packet {
	
	public static final String PACKET_NAME = "item";

	private int slot;
	private String name, customName;
	private int amount;
	private List<Attribute> attributes;
	
	public ItemPacket(int slot, ItemStack item) {
		this.slot = slot;
		this.name = MaterialRegistry.getItem(item.getId()).getDisplayName();
		this.amount = item.getAmount();
		this.attributes = Constants.getAttributes(item.getId());
		
		if(item.getNBT() != null && item.getNBT().get("display") != null) {
			CompoundTag tag = item.getNBT().get("display");
			if(tag.get("Name") != null) {
				StringTag name = tag.get("Name");
				JSONObject nameJson = new JSONObject(name.getValue());
				if(nameJson.has("text")) {
					this.customName = nameJson.getString("text");
				}
			}
		}
	}
	
	public JSONObject get() {
		JSONObject json = new JSONObject();
		json.put("type", PACKET_NAME);
		JSONObject payload = new JSONObject();
		json.put("payload", payload);
		payload.put("slot", slot);
		payload.put("name", name);
		if(customName != null)
			payload.put("custom_name", customName);
		payload.put("amount", amount);
		
		JSONArray jsonAttributes = new JSONArray();
		for(Attribute attribute : attributes) {
			jsonAttributes.put(attribute.name());
		}
		payload.put("attributes", jsonAttributes);
		
		return json;
	}
	
}
