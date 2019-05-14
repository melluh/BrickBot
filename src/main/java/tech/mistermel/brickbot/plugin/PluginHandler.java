package tech.mistermel.brickbot.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import tech.mistermel.brickbot.BrickBot;
import tech.mistermel.core.logging.Logger;

public class PluginHandler {

	public static final String LOGGER_NAME = "PluginHandler";
	
	private Logger logger;
	private File folder;
	
	public PluginHandler(File folder) {
		this.logger = Logger.createBasic(LOGGER_NAME, BrickBot.DEBUG);
		
		if(!folder.exists())
			folder.mkdirs();
		
		if(!folder.isDirectory())
			logger.warn("Plugin folder is not a directory");
	}
	
	public void load() {
		for(File file : folder.listFiles()) {
			if(file.isDirectory()) continue;
			
			String extension = this.getExtension(file.getName());
			if(!extension.equals("jar")) {
				logger.warn(file.getName() + " is not a JAR file, skipping");
				continue;
			}
			
			String path = file.getAbsolutePath();
			try {
				JarFile jar = new JarFile(path);
				Enumeration<JarEntry> e = jar.entries();
				
				URL[] urls = {new URL("jar:file:" + path + "!/")};
				URLClassLoader cl = URLClassLoader.newInstance(urls);
				
				while(e.hasMoreElements()) {
					JarEntry entry = e.nextElement();
					if(entry.isDirectory() || !this.getExtension(entry.getName()).equals("class"))
						continue;
					
					String className = entry.getName().substring(0, entry.getName().length() - 6).replace("/", ".");
					Class<?> clazz = cl.loadClass(className);
					
					logger.debug("Discovered class {0}", className);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String getExtension(String filename) {
		int i = filename.lastIndexOf(".");
		if(i > 0) {
			return filename.substring(i+1);
		} else {
			return "";
		}
	}
	
}
