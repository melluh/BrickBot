package tech.mistermel.brickbot.material;

public class Material {

	private long id;
	private String name;
	
	public Material(long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public long getId() {
		return id;
	}
	
	public String getName() {
		return name;
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
