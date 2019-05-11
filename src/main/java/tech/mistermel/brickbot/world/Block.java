package tech.mistermel.brickbot.world;

import tech.mistermel.brickbot.material.BlockType;

public class Block {

	private int x;
	private int y;
	private int z;
	private BlockType material;
	
	public Block(int x, int y, int z, BlockType material) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.material = material;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	public BlockType getMaterial() {
		return material;
	}
	
}
