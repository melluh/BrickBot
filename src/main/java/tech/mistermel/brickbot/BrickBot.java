package tech.mistermel.brickbot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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
import tech.mistermel.brickbot.util.Vector3d;
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

	private Vector3d pos;
	
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

	public void start(MinecraftProtocol protocol) {
		if(protocol == null) {
			logger.warn("Protocol is null");
			return;
		}
		this.protocol = protocol;

		this.client = new Client(ip, port, protocol, new TcpSessionFactory(Proxy.NO_PROXY));
		client.getSession().addListener(new PacketHandler());

		webSocket.start();
		client.getSession().connect();
	}
	
	public void fall() {
		// TODO: Also check for blocks without a hitbox
		if(BlockHandler.getBlock(pos.getX(), pos.getY() - 1, pos.getZ()).getMaterial().getId() == 0) {
			move(0, -1, 0);
		}
	}
	
	public void moveTo(double tx, double ty, double tz) {
		moveTo(new Vector3d(tx, ty, tz));
	}
	
	public void moveTo(Vector3d to) {
		Vector3d v = to.clone().subtract(pos).floor();
		this.move(v.getX(), v.getY(), v.getZ());
	}
	
	public void move(double rx, double ry, double rz) {
		double c = Math.sqrt(rx * rx + rz * rz);
        double a1 = -Math.asin(rx / c) / Math.PI * 180;
        double a2 = Math.acos(rz / c) / Math.PI * 180;
        if(a2 > 90) 
        	this.yaw = (float) (180 - a1);
        else
        	this.yaw = (float) a1;
		this.pitch = (float) Math.atan(ry / c);
        
		int numberOfSteps = (int) ((int) 4.0 * Math.floor(Math.sqrt(Math.pow(rx, 2) + Math.pow(ry, 2) + Math.pow(rz, 2))));
		double sx = rx / numberOfSteps;
		double sy = ry / numberOfSteps;
		double sz = rz / numberOfSteps;
		
		for(int i = 0; i < numberOfSteps; i++) {
			pos.setX(pos.getX() + sx);
			pos.setY(pos.getY() + sy);
			pos.setZ(pos.getZ() + sz);
			this.setLocation();
			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		centerPosition();
	}
	
	public void swing() {
		client.getSession().send(new ClientPlayerSwingArmPacket(Hand.MAIN_HAND));
	}
	
	public void centerPosition() {
		pos.setX(pos.getX() > 0 ? Math.floor(pos.getX()) + 0.5d : Math.ceil(pos.getX()) - 0.5d);
		pos.setY(Math.floor(pos.getY()));
		pos.setZ(pos.getZ() > 0 ? Math.floor(pos.getZ()) + 0.5d : Math.ceil(pos.getZ()) - 0.5d);
		
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
		client.getSession().send(new ClientPlayerPositionRotationPacket(false, pos.getX(), pos.getY(), pos.getZ(), yaw, pitch));
		LocationPacket packet = new LocationPacket(pos.getX(), pos.getY(), pos.getZ());
		webSocket.sendPacket(packet);
	}
	
	public void updateLocation(double x, double y, double z, float yaw, float pitch, boolean relative) {
		if(relative) {
			pos.setX(pos.getX() + x);
			pos.setY(pos.getY() + y);
			pos.setZ(pos.getZ() + z);
		} else {
			pos.setX(x);
			pos.setY(y);
			pos.setZ(z);
		}
		
		this.yaw = yaw;
		this.pitch = pitch;

		LocationPacket packet = new LocationPacket(pos.getX(), pos.getY(), pos.getZ());
		webSocket.sendPacket(packet);
	}

	public Vector3d getPosition() {
		return pos;
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
			JSONObject config = new JSONObject(new String(Files.readAllBytes(Paths.get("config.json"))));
			
			String ip = config.getString("ip");
			int port = config.getInt("port");
			
			MinecraftProtocol protocol = null;
			File authFile = new File("auth.json");
			if(!authFile.exists()) {
				if(!authFile.createNewFile()) {
					System.out.println("Could not create auth file. Exiting.");
					return;
				}
				
				Scanner scan = new Scanner(System.in);
				String email;
				while(true) {
					System.out.println("Please enter your email address:");
					email = scan.nextLine();
					System.out.println("Please enter your password:");
					String password = scan.nextLine();
					
					try {
						protocol = new MinecraftProtocol(email, password);
						break;
					} catch (InvalidCredentialsException e) {
						System.out.println("Invalid credentials. Please try again.");
						break;
					} catch (RequestException e) {
						e.printStackTrace();
					}
				}
				scan.close();
				
				JSONObject jsonOut = new JSONObject();
				jsonOut.put("email", email);
				jsonOut.put("clientToken", protocol.getClientToken());
				jsonOut.put("accessToken", protocol.getAccessToken());
				PrintWriter writer = new PrintWriter(new FileOutputStream(authFile));
				writer.println(jsonOut.toString());
				writer.close();
			} else {
				JSONObject auth = new JSONObject(new String(Files.readAllBytes(Paths.get("auth.json"))));
				String email = auth.getString("email");
				String clientToken = auth.getString("clientToken");
				String accessToken = auth.getString("accessToken");
				try {
					protocol = new MinecraftProtocol(email, clientToken, accessToken);
				} catch (InvalidCredentialsException e) {
					System.out.println("Invalid tokens.");
					return;
				} catch (RequestException e) {
					e.printStackTrace();
				}
			}
			
			instance = new BrickBot(ip, port);
			instance.start(protocol);
			
			Thread.sleep(3000);
			instance.move(10, 0, 20);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static BrickBot getInstance() {
		return instance;
	}

}
