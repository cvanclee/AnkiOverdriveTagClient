package cvc.capstone;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.ColorUIResource;

import net.miginfocom.swing.MigLayout;

public class GameGui {

	private JFrame frame;
	private CommManager commManager;
	private JListeningPanel mainPanel;
	private JPanel topPanel;
	private JLabel statusLabel; //Top left label
	private JLabel statusText;
	private JLabel scoreText; //Center label
	private JMenuBar menuBar;
	private JMenu menuHelp;
	private JMenu menuConnect;
	private JMenuItem menuConnectConnect;
	private JMenuItem menuConnectDisconnect;
	private JMenuItem menuHelpControls;
	private JMenuItem menuHelpSetup;
	private JMenuItem menuHelpRules;
	private JMenuItem menuHelpColors;
	private JMenuItem menuHelpIssues;
	private JButton readyButt;
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
	private GameGui me = this;
	
	public GameGui() {
		frame = new JFrame();
		vehicleName = "";
		isIt = new AtomicBoolean();
		myScore = 0;
		oppScore = 0;

		// Exit handling
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					if (commManager != null) {
						commManager.notifyAndTerminate();
						commManager.interrupt();
					}
				} catch (Exception ex) {
					System.out.println("Error while notifying server of disconnection");
					ex.printStackTrace();
				}
				frame.dispose();
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

		// Main JPanel setup - listens for keyboard presses if focused
		mainPanel = new JListeningPanel();
		mainPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		mainPanel.setOpaque(true);
		mainPanel.setFocusable(true);
		mainPanel.addKeyListener(mainPanel);
		
		//Menu bar setup
		menuHelp = new JMenu();
		menuConnect = new JMenu();
		menuBar = new JMenuBar();
		menuConnectConnect = new JMenuItem();
		menuConnectDisconnect = new JMenuItem();
		menuHelpControls = new JMenuItem();
		menuHelpSetup = new JMenuItem();
		menuHelpRules = new JMenuItem();
		menuHelpColors = new JMenuItem();
		menuHelpIssues = new JMenuItem();
		menuHelp.setText("Help");
		menuConnect.setText("Connect");
		menuConnectConnect.setText("Connect to server");
		menuConnectDisconnect.setText("Disconnect from server");
		menuHelpControls.setText("Controls");
		menuHelpSetup.setText("Setup");
		menuHelpRules.setText("Rules");
		menuHelpColors.setText("Colors");
		menuHelpIssues.setText("Issues");
		menuBar.add(menuConnect);
		menuBar.add(menuHelp);
		menuConnect.add(menuConnectConnect);
		menuConnectConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (commManager == null || commManager.isClosed()) {
					connectDialog();
					frame.revalidate();
					frame.repaint();
				} else {
					JOptionPane.showMessageDialog(frame,
						    "You already are connected to a server. Try disconnecting first.",
						    "Connect",
						    JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		menuConnect.add(menuConnectDisconnect);
		menuConnectDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (commManager != null && !commManager.isClosed()) {
					disconnectDialog();
				} else {
					JOptionPane.showMessageDialog(frame,
						    "Not connected to a server.",
						    "Disconnect",
						    JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		menuHelp.add(menuHelpControls);
		menuHelpControls.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String h = "<html><body width='>'";
				String header = "<h1>Controls</h1>";
				String body = "<p>Left arrow key: change lane left."
						+ "<br>Right arrow key: change lane right."
						+ "<br>Space bar: increase speed. You will gradually lose speed without pressing this!"
						+ "<br>Down arrow key: turn around."
						+ "<br>Z key: attempt to tag (only as 'hunter'), if not on cooldown."
						+ "<br>X key: block for 3 seconds (only as 'hunted'), if not on cooldown."
						+ "<br>Note: Block cooldown is 10 seconds.</p>";
				String full = h + header + body;
				JOptionPane.showMessageDialog(frame, full);
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
				JOptionPane.showMessageDialog(frame, full);
			}
		});
		menuHelp.add(menuHelpRules);
		menuHelpRules.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String h = "<html><body width='>'";
				String header = "<h1>Rules</h1>";
				String body = "<p>Never modify the track or interfere with the cars during normal gameplay. "
						+ "<br>Each player (or client) begins with 0 points, and is randomly assigned"
						+ "<br>a role ('hunted' or 'hunter') and an Anki car. Once the cars scan the track, "
						+ "<br>each player may control their respective car. The goal of the 'hunted' is to avoid "
						+ "<br>being tagged by the 'hunter'."
						+ "<br>Every consecutive 30 seconds a player is 'hunted' without being tagged, they "
						+ "<br>gain 7 points. Every time the 'hunter' tags the 'hunted', the players swap roles, "
						+ "<br>and the new 'hunted' is given 3 seconds to get away and is given 5 points. The game "
						+ "<br> ends when a player reaches the max points (server decides), or a player disconnects. "
						+ "<br>Note: You will lose 1 point for turning or trying to turn.</p>";
				String full = h + header + body;
				JOptionPane.showMessageDialog(frame, full);
			}
		});
		menuHelp.add(menuHelpColors);
		menuHelpColors.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String h = "<html><body width='>'";
				String header = "<h1>Colors</h1>";
				String body = "<p>Red represents who is the 'hunter', and green represents who is 'hunted'."
						+ "<br>The headlights of your assigned car and the background of this interface"
						+ "<br>will display either green or red to match your current role during gameplay."
						+ "<br>The headlights will flash green while 'hunted' is blocking, signaling"
						+ "<br>that they cannot be tagged.</p>";
				String full = h + header + body;
				JOptionPane.showMessageDialog(frame, full);
			}
		});
		menuHelp.add(menuHelpIssues);
		menuHelpIssues.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String h = "<html><body width='>'";
				String header = "<h1>Issues</h1>";
				String body = "<p>Occasionnaly cars may go off the track. The scores and control for "
						+ "<br>both players will freeze, and you may simply manually put the "
						+ "<br>car back on track, and give it a push and move command to get it going."
						+ "<br><br>Occasionally, after a game has ended, one or both cars may not"
						+ "<br>disconnect. In this case, the car LED will stay blue while the"
						+ "<br>server complains it cannot find the car. In this case, simply"
						+ "<br>power the car off and on.</p>";
				String full = h + header + body;
				JOptionPane.showMessageDialog(frame, full);
			}
		});
		frame.setJMenuBar(menuBar);
		
		//Buttons for car control setup
		readyButt = new JButton();
		leftButt = new JButton();
		rightButt = new JButton();
		speedButt = new JButton();
		turnButt = new JButton();
		tagButt = new JButton();
		blockButt = new JButton();
		readyButt.setFocusable(false);
		leftButt.setFocusable(false); //Prevents spacebar binding to the most recently pressed button
		rightButt.setFocusable(false);
		speedButt.setFocusable(false);
		turnButt.setFocusable(false);
		tagButt.setFocusable(false);
		blockButt.setFocusable(false);
		readyButt.setBackground(Color.DARK_GRAY);
		leftButt.setBackground(Color.DARK_GRAY);
		rightButt.setBackground(Color.DARK_GRAY);
		speedButt.setBackground(Color.DARK_GRAY);
		turnButt.setBackground(Color.DARK_GRAY);
		tagButt.setBackground(Color.DARK_GRAY);
		blockButt.setBackground(Color.DARK_GRAY);
		UIManager.put("Button.focus", new ColorUIResource(new Color(0, 0, 0, 0))); //Remove icon focus outline
		readyButt.setToolTipText("Notify server you are ready to play");
		leftButt.setToolTipText("Turn left");
		rightButt.setToolTipText("Turn right");
		speedButt.setToolTipText("Speed up");
		turnButt.setToolTipText("Turn around (180)");
		tagButt.setToolTipText("Attempt to tag");
		blockButt.setToolTipText("Attempt to block");
		try {
			String resPath = new File(MainClass.class.getProtectionDomain().getCodeSource().getLocation().toURI())
					.getParent() + "/res/";
			readyButt.setIcon(resizeImg(resPath + "start.png", 30, 30));
			leftButt.setIcon(resizeImg(resPath + "back.png", 30, 30));
			rightButt.setIcon(resizeImg(resPath + "forward.png", 30, 30));
			speedButt.setIcon(resizeImg(resPath + "racing.png", 30, 30));
			turnButt.setIcon(resizeImg(resPath + "download.png", 30, 30));
			tagButt.setIcon(resizeImg(resPath + "hand-gesture.png", 30, 30));
			blockButt.setIcon(resizeImg(resPath + "shield.png", 30, 30));
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		readyButt.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		leftButt.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		rightButt.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		speedButt.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		turnButt.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		tagButt.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		blockButt.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		MigLayout mgl = new MigLayout("", "[20%][60%][20%]", "[20%][60%][20%]"); //column, row
		mainPanel.setLayout(mgl);
		mainPanel.add(readyButt, "cell 2 0");
		mainPanel.add(turnButt, "cell 1 0");
		mainPanel.add(leftButt, "cell 0 1");
		mainPanel.add(rightButt, "cell 2 1");
		mainPanel.add(tagButt, "cell 0 2");
		mainPanel.add(speedButt, "cell 1 2");
		mainPanel.add(blockButt, "cell 2 2");
		mainPanel.add(scoreText, "align center, cell 1 1"); //center
		readyButt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					commManager.resolveKeyPress(new FakeKeyEvents(readyButt, -1, (long) -1.0, -1, KeyEvent.VK_R));
				} catch (GameException ex) {
					ex.printStackTrace();
				}
			}
		});
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
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(topPanel, BorderLayout.NORTH);
		frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
		frame.setMinimumSize(new Dimension(600, 600));		
		frame.setTitle("Anki Overdrive Tag Game");
		frame.setSize(new Dimension(800, 600));
		frame.setVisible(true);
	}
	
	public synchronized void endGame(String reason) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				JOptionPane optionPane = new JOptionPane(reason, JOptionPane.INFORMATION_MESSAGE);
				JDialog dialog = new JDialog(frame, "Game over", true);
				dialog.setContentPane(optionPane);
				dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				optionPane.addPropertyChangeListener(new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent e) {
						String prop = e.getPropertyName();
						if (dialog.isVisible() && (e.getSource() == optionPane)
								&& (prop.equals(JOptionPane.VALUE_PROPERTY))) {
							dialog.setVisible(false);
							dialog.dispose();
						}
					}
				});
				dialog.addWindowListener( new WindowAdapter() {
				    public void windowOpened( WindowEvent e ){
				        dialog.requestFocus();
				    }
				}); 
				dialog.setLocationRelativeTo(frame);
				dialog.pack();
				dialog.setVisible(true);
			}
		});
	}

	public synchronized void setGameStatus(String status) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				statusText.setText(status);
				scoreText.setText("<html>"
						+ "<h3><center>Status:</center></h3>"
						+ "<center>" + statusText.getText() + "</center>"
						+ "<br><h3><center>Score:</center></h3>"
						+ "<center>Me: " + myScore + "<br>Opponent: " + oppScore + "</center></html>");
				frame.revalidate();
				frame.repaint();
			}
		});
	}

	public synchronized void setScoreStatus(int me, int opponent) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				myScore = me;
				oppScore = opponent;
				scoreText.setText("<html>"
						+ "<h3><center>Status:</center></h3>"
						+ statusText.getText()
						+ "<br><h3><center>Score:</center></h3>"
						+ "<center>Me: " + myScore + "<br>Opponent: " + oppScore + "</center></html>");
				frame.revalidate();
				frame.repaint();
			}
		});
	}
	
	public synchronized void setFrameColor(Color c) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				mainPanel.setBackground(c);
				frame.revalidate();
				frame.repaint();
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
	
	private void newComm() {
		try {
			if (commManager != null && commManager.isAlive()) {
				System.out.print("A communication manager is running. Ignoring request for new connection.");
				JOptionPane.showMessageDialog(frame,
						"A communication manager is running. Ignoring request.", "Connection Failed",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			commManager = new CommManager(this);
			commManager.start();
		} catch (GameException e) {
			JOptionPane.showMessageDialog(frame, "Unable to establish connection to server", "Connection Failed",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	private void disconnectDialog() {
		int dialogResult = JOptionPane.showConfirmDialog (frame, "Are you sure you want to disconnect?",
				"Disconnect", JOptionPane.YES_NO_OPTION);
		if(dialogResult == JOptionPane.YES_OPTION){
			try {
				commManager.notifyAndTerminate();
			} catch (Exception e) {
				e.printStackTrace();
			}
			commManager.interrupt();
			try {
				commManager.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			return;
		}
	}
	
	private void connectDialog() {
		MigLayout gl = new MigLayout("", "[][]", "[100%][]");
		MigLayout ml = new MigLayout("", "[100%]", "[][]");
		JPanel topPanel = new JPanel();
		topPanel.setLayout(gl);
		JLabel hostLabel = new JLabel();
		JLabel portLabel = new JLabel();
		hostLabel.setText("Hostname/IP: ");
		portLabel.setText("Port: ");
		JTextField hostField = new JTextField();
		JTextField portField = new JTextField();
		JDialog conDialog = new JDialog();
		conDialog.setTitle("Connect to server");
		conDialog.setLayout(ml);
		topPanel.add(hostLabel, "cell 0 0, shrink");
		topPanel.add(hostField, "cell 1 0, growx, pushx");
		topPanel.add(portLabel, "cell 0 1, shrink");
		topPanel.add(portField, "cell 1 1, growx, pushx");
		JButton conButt = new JButton();
		conButt.setText("Connect");
		if (MainClass.SERVER_NAME != null) {
			hostField.setText(MainClass.SERVER_NAME);
			portField.setText(String.valueOf(MainClass.SERVER_PORT));
		} else {
			portField.setText("4999");
		}
		conButt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				conDialog.dispose();
				frame.revalidate();
				frame.repaint();
				MainClass.SERVER_NAME = hostField.getText();
				MainClass.SERVER_PORT = Integer.parseInt(portField.getText());
				newComm();
			}
		});
		conDialog.setLayout(ml);
		conDialog.add(topPanel, "cell 0 0, center, grow, push");
		conDialog.add(conButt, "cell 0 1, center");
		conDialog.setSize(new Dimension(350, 200));
		conDialog.setResizable(true);
		conDialog.setVisible(true);
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
	
	private class JListeningPanel extends JPanel implements KeyListener {
		public void keyPressed(KeyEvent e) {
			;
		}

		@Override
		public void keyReleased(KeyEvent e) {
			try {
				if (commManager == null) {
					return;
				}
				commManager.resolveKeyPress(e);
			} catch (GameException ex) {
				ex.printStackTrace();
			}
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			;
		}
	}
}
