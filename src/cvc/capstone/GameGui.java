package cvc.capstone;

import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class GameGui extends JFrame {

	private CommManager commManager;
	private JPanel mainPanel;
	private JPanel topPanel;
	private JLabel statusLabel;
	private JLabel statusText;
	private JMenuBar menuBar;
	private JMenu menuHelp;
	private JMenuItem menuHelpControls;
	private String vehicleName;
	private AtomicBoolean isIt;

	public GameGui() {
		vehicleName = "";
		isIt = new AtomicBoolean();

		// Exit handling
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					commManager.notifyAndTerminate();
					commManager.interrupt();
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
		
		//Menu bar setup
		menuHelp = new JMenu();
		menuBar = new JMenuBar();
		menuHelpControls = new JMenuItem();
		menuHelp.setText("Help");
		menuHelpControls.setText("Controls");
		menuBar.add(menuHelp);
		menuHelp.add(menuHelpControls);
		menuHelpControls.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String h = "<html><body width='>'";
				String header = "<h1>Controls</h1>";
				String body = "<p>Left arrow key: change lane left."
						+ "<br>Right arrow key: change lane right."
						+ "<br>Space bar: increase speed."
						+ "<br>Down arrow key: turn around."
						+ "<br>Z key: attempt to tag (only as 'it'), if not on cooldown."
						+ "<br>X key: block for 3 seconds (only as 'tagger'), if not on cooldown.</p>";
				String full = h + header + body;
				JOptionPane.showMessageDialog(null, full);
			}
		});
		setJMenuBar(menuBar);

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
		commManager.start();

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
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				statusText.setText(status);
			}
		});
	}

	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}
	
	public AtomicBoolean isIt() {
		return isIt;
	}
	
	public void setIsIt(boolean isIt) {
		this.isIt.set(isIt);
	}
}
