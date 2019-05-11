package tech.mistermel.afkbot.material;

public class BlockType extends Material {

	private double hardness;
	private boolean diggable;	
	
	public BlockType(long id, String name, String displayName, int stackSize, double hardness, boolean diggable) {
		super(id, name, displayName, stackSize);
		this.hardness = hardness;
		this.diggable = diggable;
	}
	
	public double getHardness() {
		return hardness;
	}
	
	public boolean isDiggable() {
		return diggable;
	}

}
