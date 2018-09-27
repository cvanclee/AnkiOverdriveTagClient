package cvc.capstone;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.UUID;

import javax.swing.SwingUtilities;

public class MainClass {

	public static int SERVER_PORT = -1;
	public static String SERVER_NAME = null;
	public static int COMMUNICATION_TIMEOUT = -1; // ms
	public static final String MY_ID = UUID.randomUUID().toString().replaceAll("-", "");
	private static GameGui gui = null;

	public static void main(String[] args) throws GameException {
		String propPath;
		try {
			propPath = new File(MainClass.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent()
					+ "/res/clientProperties.properties";
		} catch (URISyntaxException e) {
			throw new GameException(e);
		}
		if (!readProperties(propPath)) {
			throw new GameException("Failed to read properties file");
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				gui = new GameGui();
				gui.setTitle("Anki Overdrive Tag Game");
				gui.setSize(new Dimension(800, 600));
				gui.setVisible(true);
			}
		});
	}

	public static boolean readProperties(String path) {
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(path));
			SERVER_PORT = Integer.parseInt(prop.getProperty("SERVER_PORT"));
			SERVER_NAME = prop.getProperty("SERVER_NAME");
			COMMUNICATION_TIMEOUT = Integer.parseInt(prop.getProperty("COMMUNICATION_TIMEOUT"));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
