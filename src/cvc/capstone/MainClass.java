package cvc.capstone;

import java.awt.Dimension;
import java.util.UUID;

import javax.swing.SwingUtilities;

public class MainClass {

	public static final int SERVER_PORT = 4999;
	public static final String SERVER_NAME = "129.3.213.249";
	public static final int SERVER_TIMEOUT = 2000; //ms
	public static final String MY_ID = UUID.randomUUID().toString().replaceAll("-", "");
	private static GameGui gui = null;
	
	public static void main(String[] args) {
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
}
