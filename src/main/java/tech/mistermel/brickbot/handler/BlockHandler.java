package tech.mistermel.brickbot.handler;

import java.util.HashSet;
import java.util.Set;

import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;

import tech.mistermel.brickbot.material.MaterialRegistry;
import tech.mistermel.brickbot.world.Block;

public class BlockHandler {

	private static Set<Column> columns = new HashSet<Column>();
	
	public static void addColumn(Column column) {
		Column existingColumn = getColumn(column.getX(), column.getZ());
		if(existingColumn != null)
			columns.remove(existingColumn);
		
		columns.add(column);
	}
	
	public static Block getBlock(double x, double y, double z) {
		return getBlock((int) x, (int) y, (int) z);
	}
	
	public static Block getBlock(int x, int y, int z) {
		int chunkX = x > 0 ? (int) Math.ceil(x / 16) : (int) Math.floor((double) x / 16);
		int chunkY = y > 0 ? (int) Math.ceil(y / 16) : (int) Math.floor((double) y / 16);
		int chunkZ = z > 0 ? (int) Math.ceil(z / 16) : (int) Math.floor((double) z / 16);
		int blockX = x > 0 ? x % 16 : 16 - (Math.abs(x) % 16);
		int blockY = y > 0 ? y % 16 : 16 - (Math.abs(y) % 16);
		int blockZ = z > 0 ? z % 16 : 16 - (Math.abs(z) % 16);
		
		Column column = getColumn(chunkX, chunkZ);
		if(column == null) {
			System.out.println("Column " + chunkX + " " + chunkZ + " not loaded");
			return null;
		}
		
		Chunk chunk = column.getChunks()[chunkY];
		if(chunk == null) {
			System.out.println("Chunk " + chunkX + " " + chunkY + " " + chunkZ + " not loaded");
			return null;
		}
		
		int blockId = chunk.getBlocks().get(blockX, blockY, blockZ).getId();
		System.out.println(blockId + " :: " + MaterialRegistry.getBlock(blockId).getName() + " " + MaterialRegistry.getItem(blockId).getName());
		return new Block(x, y, z, MaterialRegistry.getBlock(blockId));
	}
	
	public static Chunk getChunk(Position pos) {
		return getChunk(pos.getX(), pos.getY(), pos.getZ());
	}
	
	public static Chunk getChunk(int x, int y, int z) {
		int chunkX = x > 0 ? (int) Math.ceil(x / 16) : (int) Math.floor((double) x / 16);
		int chunkY = y > 0 ? (int) Math.ceil(y / 16) : (int) Math.floor((double) y / 16);
		int chunkZ = z > 0 ? (int) Math.ceil(z / 16) : (int) Math.floor((double) z / 16);
		
		Column column = getColumn(chunkX, chunkZ);
		if(column == null) {
			System.out.println("Column " + chunkX + " " + chunkZ + " not loaded");
			return null;
		}
		
		Chunk chunk = column.getChunks()[chunkY];
		return chunk;
	}
	
	public static Position toChunkCoords(Position pos) {
		int blockX = pos.getX() > 0 ? pos.getX() % 16 : 16 - (Math.abs(pos.getX()) % 16);
		int blockY = pos.getY() > 0 ? pos.getY() % 16 : 16 - (Math.abs(pos.getY()) % 16);
		int blockZ = pos.getZ() > 0 ? pos.getZ() % 16 : 16 - (Math.abs(pos.getZ()) % 16);
		
		return new Position(blockX, blockY, blockZ);
	}
	
	public static Column getColumn(int x, int z) {
		for(Column column : columns) {
			if(column.getX() == x && column.getZ() == z)
				return column;
		}
		return null;
	}
	
}
