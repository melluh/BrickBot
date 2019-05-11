package tech.mistermel.afkbot.material;

public class Material {

	private long id;
	private String name, displayName;
	private int stackSize;
	
	public Material(long id, String name, String displayName, int stackSize) {
		this.id = id;
		this.name = name;
		this.displayName = displayName;
		this.stackSize = stackSize;
	}
	
	public long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public int getStackSize() {
		return stackSize;
	}
	
	public ItemType toItem() {
		if(this instanceof ItemType)
			return (ItemType) this;
		
		return null;
	}
	
	public BlockType toBlock() {
		if(this instanceof BlockType)
			return (BlockType) this;
		
		return null;
	}
	
}
