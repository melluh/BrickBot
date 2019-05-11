package tech.mistermel.brickbot;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.auth.exception.request.InvalidCredentialsException;
import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.game.ClientRequest;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;
import com.github.steveice10.mc.protocol.data.game.entity.player.Hand;
import com.github.steveice10.mc.protocol.data.game.setting.Difficulty;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientRequestPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerSwingArmPacket;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;

import tech.mistermel.brickbot.handler.BlockHandler;
import tech.mistermel.brickbot.handler.PacketHandler;
import tech.mistermel.brickbot.handler.WebSocketHandler;
import tech.mistermel.brickbot.material.MaterialRegistry;
import tech.mistermel.brickbot.packet.ChatPacket;
import tech.mistermel.brickbot.packet.ClearItemPacket;
import tech.mistermel.brickbot.packet.DifficultyPacket;
import tech.mistermel.brickbot.packet.HealthPacket;
import tech.mistermel.brickbot.packet.ItemPacket;
import tech.mistermel.brickbot.packet.LocationPacket;
import tech.mistermel.brickbot.util.Player;
import tech.mistermel.brickbot.util.Translator;
import tech.mistermel.core.logging.Logger;

public class BrickBot {

	public static final String LOGGER_NAME = "AFKBot";
	public static final boolean DEBUG = true;

	private static BrickBot instance;

	private String ip;
	private int port;

	private Client client;
	private Logger logger;
	private MinecraftProtocol protocol;
	private WebSocketHandler webSocket;

	private float health;
	private float food;
	private float saturation;

	private double x;
	private double y;
	private double z;
	
	private float yaw;
	private float pitch;

	private Map<String, Player> players = new HashMap<String, Player>();
	private ItemStack[] inventory;

	private Difficulty difficulty;

	public BrickBot(String ip, int port) {
		this.ip = ip;
		this.port = port;
		this.inventory = new ItemStack[46];

		this.logger = Logger.createBasic(LOGGER_NAME, DEBUG);
		this.webSocket = new WebSocketHandler();
		
		MaterialRegistry.load();
		Translator.loadTranslations();
	}

	public void start(String email, String password) {
		try {
			logger.info("Logging in...");
			this.protocol = new MinecraftProtocol(email, password);

			this.client = new Client(ip, port, protocol, new TcpSessionFactory(Proxy.NO_PROXY));
			client.getSession().addListener(new PacketHandler());

			webSocket.start();
			client.getSession().connect();
		} catch (InvalidCredentialsException e) {
			logger.warn("Invalid credentials.");
		} catch (RequestException e) {
			e.printStackTrace();
		}
	}
	
	public void fall() {
		if(BlockHandler.getBlock(x, y - 1, z).getMaterial().getId() == 0) { // This should be adjusted to also be true for blocks that have no hitbox (e.g. signs).
			
		}
	}
	
	public void move(double rx, double ry, double rz) {
		int numberOfSteps = (int) (rx / 0.2);
		System.out.println(numberOfSteps);
		double sx = rx / numberOfSteps;
		double sy = ry / numberOfSteps;
		double sz = rz / numberOfSteps;
		
		for(int i = 0; i < numberOfSteps; i++) {
			x += sx;
			y += sy;
			z += sz;
			this.setLocation();
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		centerPosition();
	}

	public void swing() {
		client.getSession().send(new ClientPlayerSwingArmPacket(Hand.MAIN_HAND));
	}
	
	public void centerPosition() {
		this.x = x > 0 ? Math.floor(x) + 0.5d : Math.round(x) - 0.5d;
		this.y = Math.floor(y);
		this.z = z > 0 ? Math.floor(z) + 0.5d : Math.round(z) - 0.5d;
		
		this.setLocation();
	}
	
	public Map<String, Player> getPlayerMap() {
		return players;
	}

	public Player getPlayerByEntityId(int entityId) {
		for (Player p : players.values()) {
			if (p.getEntityId() == entityId)
				return p;
		}

		return null;
	}
	
	public void setLocation() {
		client.getSession().send(new ClientPlayerPositionRotationPacket(false, x, y, z, yaw, pitch));
		LocationPacket packet = new LocationPacket(x, y, z);
		webSocket.sendPacket(packet);
	}
	
	public void updateLocation(double x, double y, double z, float yaw, float pitch, boolean relative) {
		if(relative) {
			this.x += x;
			this.y += y;
			this.z += z;
		} else {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		this.yaw = yaw;
		this.pitch = pitch;

		LocationPacket packet = new LocationPacket(x, y, z);
		webSocket.sendPacket(packet);
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

	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;

		DifficultyPacket packet = new DifficultyPacket(difficulty);
		webSocket.sendPacket(packet);
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public void sendChatMessage(String msg) {
		client.getSession().send(new ClientChatPacket(msg));
	}

	public void setSlot(int slot, ItemStack item) {
		if (item != null) {
			ItemPacket packet = new ItemPacket(slot, item);
			webSocket.sendPacket(packet);
		} else if (inventory[slot] != null) {
			ClearItemPacket packet = new ClearItemPacket(slot);
			webSocket.sendPacket(packet);
		}

		inventory[slot] = item;
	}

	public ItemStack[] getInventory() {
		return inventory;
	}

	public void setHealth(float health, float food, float saturation) {
		this.health = health;
		this.food = food;
		this.saturation = saturation;

		HealthPacket packet = new HealthPacket(health, food, saturation);
		webSocket.sendPacket(packet);
		
		if(health <= 0) {
			ChatPacket chatPacket = new ChatPacket("-- Died, respawing.");
			webSocket.sendPacket(chatPacket);
			
			ClientRequestPacket respawnPacket = new ClientRequestPacket(ClientRequest.RESPAWN);
			client.getSession().send(respawnPacket);
		}
	}

	public float getHealth() {
		return health;
	}

	public float getFood() {
		return food;
	}

	public float getSaturation() {
		return saturation;
	}

	public Collection<Player> getPlayers() {
		return players.values();
	}

	public Player getPlayer(String uuid) {
		return players.get(uuid);
	}

	public String getServerIp() {
		return ip;
	}

	public WebSocketHandler getWebSocketHandler() {
		return webSocket;
	}
	
	public Session getSession() {
		return client.getSession();
	}

	public GameProfile getBotProfile() {
		return protocol.getProfile();
	}

	public static void main(String[] args) {
		try {
			File configFile = new File("config.json");
			if(!configFile.exists()) {
				if(configFile.createNewFile())
					System.out.println("Config file created");
				else
					System.out.println("Could not create config file");
				return;
			}
			JSONObject json = new JSONObject(new String(Files.readAllBytes(Paths.get("config.json"))));
			
			String email = json.getString("email");
			String password = json.getString("password");
			
			String ip = json.getString("ip");
			int port = json.getInt("port");
			
			instance = new BrickBot(ip, port);
			instance.start(email, password);
			
			/*try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			instance.move(10, 0, 0);*/
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static BrickBot getInstance() {
		return instance;
	}

}
