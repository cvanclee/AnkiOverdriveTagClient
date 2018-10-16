package cvc.capstone;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import net.miginfocom.swing.MigLayout;

public class GameGui extends JFrame {

	private CommManager commManager;
	private JPanel mainPanel;
	private JPanel topPanel;
	private JLabel statusLabel; //Top left label
	private JLabel statusText;
	private JLabel scoreText; //Center label
	private JMenuBar menuBar;
	private JMenu menuHelp;
	private JMenuItem menuHelpControls;
	private JMenuItem menuHelpSetup;
	private JMenuItem menuHelpRules;
	private JButton leftButt;
	private JButton rightButt;
	private JButton speedButt;
	private JButton turnButt;
	private JButton tagButt;
	private JButton blockButt;
	private String vehicleName;
	private volatile AtomicBoolean isIt;
	private int myScore;
	private int oppScore;

	public GameGui() {
		vehicleName = "";
		isIt = new AtomicBoolean();
		myScore = 0;
		oppScore = 0;

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
		scoreText = new JLabel();
		setScoreStatus(0, 0);
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
		menuHelpSetup = new JMenuItem();
		menuHelpRules = new JMenuItem();
		menuHelp.setText("Help");
		menuHelpControls.setText("Controls");
		menuHelpSetup.setText("Setup");
		menuHelpRules.setText("Rules");
		menuBar.add(menuHelp);
		menuHelp.add(menuHelpControls);
		menuHelpControls.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String h = "<html><body width='>'";
				String header = "<h1>Controls</h1>";
				String body = "<p>Left arrow key: change lane left."
						+ "<br>Right arrow key: change lane right."
						+ "<br>Space bar: increase speed. You will gradually lose speed without pressing this!"
						+ "<br>Down arrow key: turn around."
						+ "<br>Z key: attempt to tag (only as 'it'), if not on cooldown."
						+ "<br>X key: block for 3 seconds (only as 'tagger'), if not on cooldown."
						+ "<br>Note: tag cooldown is half a second, block cooldown is 10 seconds.</p>";
				String full = h + header + body;
				JOptionPane.showMessageDialog(null, full);
			}
		});
		menuHelp.add(menuHelpSetup);
		menuHelpSetup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String h = "<html><body width='>'";
				String header = "<h1>Game Setup</h1>";
				String body = "<p>A properly setup game is composed of two cars, two clients, and a track. "
						+ "<br>The track must have exactly ONE starting piece (piece with four arrows), and "
						+ "<br>form a complete circuit. Before both clients ready up, the cars should "
						+ "<br>be placed about one inch behind the starting arrows, in seperate lanes. "
						+ "<br>Do not modify the track or interfere with the cars during gameplay.</p>";
				String full = h + header + body;
				JOptionPane.showMessageDialog(null, full);
			}
		});
		menuHelp.add(menuHelpRules);
		menuHelpRules.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String h = "<html><body width='>'";
				String header = "<h1>Rules</h1>";
				String body = "<p>Never modify the track or interfere with the cars during normal gameplay. "
						+ "<br>Each player (or client) begins with 0 points, and is randomly assigned"
						+ "<br>a role ('it' or 'tagger') and an Anki car. Once the cars scan the track, "
						+ "<br>each player may control their respective car. The goal of 'it' is to avoid "
						+ "<br>being tagged by the 'tagger'."
						+ "<br>Every consecutive 30 seconds a player is 'it' without being tagged, they "
						+ "<br>gain 2 points. Every time the 'tagger' tags 'it', the players swap roles, "
						+ "<br>and the new 'it' is given 3 seconds to get away and given 1 point. The game "
						+ "<br> ends after 3 minutes, a player reaches 6 points, or an Anki car is "
						+ "<br>detected off the track.</p>";
				String full = h + header + body;
				JOptionPane.showMessageDialog(null, full);
			}
		});
		setJMenuBar(menuBar);
		
		//Buttons for car control setup
		leftButt = new JButton();
		rightButt = new JButton();
		speedButt = new JButton();
		turnButt = new JButton();
		tagButt = new JButton();
		blockButt = new JButton();
		leftButt.setBackground(Color.DARK_GRAY);
		rightButt.setBackground(Color.DARK_GRAY);
		speedButt.setBackground(Color.DARK_GRAY);
		turnButt.setBackground(Color.DARK_GRAY);
		tagButt.setBackground(Color.DARK_GRAY);
		blockButt.setBackground(Color.DARK_GRAY);
		UIManager.put("Button.focus", new ColorUIResource(new Color(0, 0, 0, 0))); //Remove icon focus outline
		leftButt.setToolTipText("Turn left");
		rightButt.setToolTipText("Turn right");
		speedButt.setToolTipText("Speed up");
		turnButt.setToolTipText("Turn around (180)");
		tagButt.setToolTipText("Attempt to tag");
		blockButt.setToolTipText("Attempt to block");
		try {
			String resPath = new File(MainClass.class.getProtectionDomain().getCodeSource().getLocation().toURI())
					.getParent() + "/res/";
			leftButt.setIcon(resizeImg(resPath + "back.png", 30, 30));
			rightButt.setIcon(resizeImg(resPath + "forward.png", 30, 30));
			speedButt.setIcon(resizeImg(resPath + "racing.png", 30, 30));
			turnButt.setIcon(resizeImg(resPath + "download.png", 30, 30));
			tagButt.setIcon(resizeImg(resPath + "hand-gesture.png", 30, 30));
			blockButt.setIcon(resizeImg(resPath + "shield.png", 30, 30));
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		leftButt.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		rightButt.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		speedButt.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		turnButt.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		tagButt.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		blockButt.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		MigLayout mgl = new MigLayout("", "[20%][60%][20%]", "[20%][60%][20%]"); //column, row
		mainPanel.setLayout(mgl);
		mainPanel.add(turnButt, "cell 1 0");
		mainPanel.add(leftButt, "cell 0 1");
		mainPanel.add(rightButt, "cell 2 1");
		mainPanel.add(tagButt, "cell 0 2");
		mainPanel.add(speedButt, "cell 1 2");
		mainPanel.add(blockButt, "cell 2 2");
		mainPanel.add(scoreText, "align center, cell 1 1"); //center
		leftButt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					commManager.resolveKeyPress(new FakeKeyEvents(leftButt, -1, (long) -1.0, -1, KeyEvent.VK_LEFT));
				} catch (GameException ex) {
					ex.printStackTrace();
				}
			}
		});
		rightButt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					commManager.resolveKeyPress(new FakeKeyEvents(rightButt, -1, (long) -1.0, -1, KeyEvent.VK_RIGHT));
				} catch (GameException ex) {
					ex.printStackTrace();
				}
			}
		});
		speedButt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					commManager.resolveKeyPress(new FakeKeyEvents(speedButt, -1, (long) -1.0, -1, KeyEvent.VK_SPACE));
				} catch (GameException ex) {
					ex.printStackTrace();
				}
			}
		});
		turnButt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					commManager.resolveKeyPress(new FakeKeyEvents(turnButt, -1, (long) -1.0, -1, KeyEvent.VK_DOWN));
				} catch (GameException ex) {
					ex.printStackTrace();
				}
			}
		});
		tagButt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					commManager.resolveKeyPress(new FakeKeyEvents(tagButt, -1, (long) -1.0, -1, KeyEvent.VK_Z));
				} catch (GameException ex) {
					ex.printStackTrace();
				}
			}
		});
		blockButt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					commManager.resolveKeyPress(new FakeKeyEvents(blockButt, -1, (long) -1.0, -1, KeyEvent.VK_X));
				} catch (GameException ex) {
					ex.printStackTrace();
				}
			}
		});

		// Frame setup
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		this.setMinimumSize(new Dimension(400, 400));
		
		//Comm setup
		try {
			commManager = new CommManager(this);
			commManager.start();
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
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				statusText.setText(status);
			}
		});
	}

	public synchronized void setScoreStatus(int me, int opponent) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				myScore = myScore + me;
				oppScore = oppScore + opponent;
				scoreText.setText("<html><h2>Score</h2><br>Me: " + myScore + "<br>Opponent: " + oppScore + "</html>");
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
	
	private ImageIcon resizeImg(String path, int x, int y) {
		ImageIcon imageIcon = new ImageIcon(path); // load the image to a imageIcon
		Image image = imageIcon.getImage(); // transform it 
		Image newimg = image.getScaledInstance(x, y,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
		imageIcon = new ImageIcon(newimg);  // transform it back
		return imageIcon;
	}
	
	private class FakeKeyEvents extends KeyEvent {

		private int fakeKeyCode;
		
		public FakeKeyEvents(Component source, int id, long when, int modifiers, int keyCode) {
			super(source, id, when, modifiers, keyCode);
			this.fakeKeyCode = keyCode;
		}

		@Override
		public int getID() {
			return KeyEvent.KEY_RELEASED;
		}
		
		@Override
		public int getKeyCode() {
			return fakeKeyCode;
		}
	}
}
