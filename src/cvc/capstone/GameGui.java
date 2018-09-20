package cvc.capstone;

import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class GameGui extends JFrame {

	private CommManager commManager;

	public GameGui() {
		// Top JPanel setup
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		GroupLayout gl = new GroupLayout(getContentPane());
		getContentPane().setLayout(gl);
		gl.setHorizontalGroup(gl.createSequentialGroup().addComponent(mainPanel));
		gl.setVerticalGroup(gl.createSequentialGroup().addComponent(mainPanel));
		try {
			commManager = new CommManager();
		} catch (GameException e) {
			JOptionPane.showMessageDialog(this, "Unable to establish connection to server", "Connection Failed",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		add(mainPanel);

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
}
