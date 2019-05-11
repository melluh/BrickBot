package tech.mistermel.afkbot.util;

import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;

import tech.mistermel.afkbot.AFKBot;
import tech.mistermel.afkbot.packet.LocationPacket;

public class Player {

	private int entityId;
	private String uuid;
	private String username;
	private int ping;
	private GameMode gameMode;
	
	private double x, y, z;
	private float yaw, pitch;
	
	public Player(String uuid, String username, int ping, GameMode gameMode) {
		if(uuid == null || username == null || gameMode == null)
			throw new IllegalStateException("UUID, username or gamemode is null");
		
		this.uuid = uuid;
		this.username = username;
		this.ping = ping;
		this.gameMode = gameMode;
	}
	
	public int getEntityId() {
		return entityId;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getZ() {
		return z;
	}
	
	public float getYaw() {
		return yaw;
	}
	
	public float getPitch() {
		return pitch;
	}
	
	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}
	
	public void setLocation(double x, double y, double z, float yaw, float pitch, boolean relative) {
		if(relative) {
			this.x += x;
			this.y += y;
			this.z += z;
		} else {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		LocationPacket packet = new LocationPacket((int) this.getX(), (int) this.getY(), (int) this.getZ(), uuid);
		AFKBot.getInstance().getWebSocketHandler().sendPacket(packet);		
		
		this.yaw = yaw;
		this.pitch = pitch;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public String getUsername() {
		return username;
	}
	
	public int getPing() {
		return ping;
	}
	
	public void setPing(int ping) {
		this.ping = ping;
	}
	
	public GameMode getGameMode() {
		return gameMode;
	}
	
	public void setGameMode(GameMode gameMode) {
		this.gameMode = gameMode;
	}
	
}
