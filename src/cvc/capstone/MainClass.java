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
	public static int COMMUNICATION_TIMEOUT = 2000; // ms
	public static final String MY_ID = UUID.randomUUID().toString().replaceAll("-", "");
	private static GameGui gui = null;

	public static void main(String[] args) throws GameException {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				gui = new GameGui();
			}
		});
	}
}
