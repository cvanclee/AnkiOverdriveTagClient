package cvc.capstone;

import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class GameGui extends JFrame {

	private CommManager commManager;
	private JPanel mainPanel;
	private JPanel topPanel;
	private JLabel statusLabel;
	private JLabel statusText;
	private String vehicleName;

	public GameGui() {
		vehicleName = "";

		// Exit handling
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					commManager.notifyAndTerminate();
				} catch (Exception ex) {
					System.out.println("Error while notifying server of disconnection");
					ex.printStackTrace();
				}
				dispose();
				System.exit(0);
			}
		});

		// Top JPanel setup
		topPanel = new JPanel();
		topPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		statusLabel = new JLabel();
		statusText = new JLabel();
		statusLabel.setText("STATUS: ");
		statusText.setText("DISCONNECTED");
		GroupLayout tgl = new GroupLayout(topPanel);
		topPanel.setLayout(tgl);
		tgl.setHorizontalGroup(tgl.createSequentialGroup().addComponent(statusLabel).addComponent(statusText));
		tgl.setVerticalGroup(tgl.createParallelGroup().addComponent(statusLabel).addComponent(statusText));

		// Main JPanel setup
		mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		// Frame setup
		GroupLayout gl = new GroupLayout(getContentPane());
		getContentPane().setLayout(gl);
		gl.setHorizontalGroup(gl.createParallelGroup().addComponent(topPanel).addComponent(mainPanel));
		gl.setVerticalGroup(gl.createSequentialGroup().addComponent(topPanel).addComponent(mainPanel));
		try {
			commManager = new CommManager(this);
		} catch (GameException e) {
			JOptionPane.showMessageDialog(this, "Unable to establish connection to server", "Connection Failed",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

		// Key listener
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				try {
					commManager.resolveKeyPress(e);
				} catch (GameException ex) {
					ex.printStackTrace();
				}
				return true;
			}
		});
	}

	public synchronized void setGameStatus(String status) {
		statusText.setText(status);
	}

	public void setVehicleName(String vehicleName) {

	}
}
