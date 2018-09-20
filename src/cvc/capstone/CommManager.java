package cvc.capstone;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class CommManager {

	private Socket socket;

	public CommManager() throws GameException {
		if (!connect()) {
			throw new GameException("Unable to connect to server");
		}
	}

	public void resolveKeyPress(KeyEvent e) throws GameException {
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			return;
		}
		if (e.getKeyCode() == KeyEvent.VK_SPACE) { // Spacebar (speed up)
			sendCmd(1005, "");
		} else if (e.getKeyCode() == KeyEvent.VK_Z) { // Z (tag)
			sendCmd(1007, "");
		} else if (e.getKeyCode() == KeyEvent.VK_X) { // X (block)
			sendCmd(1008, "");
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) { // Left Non-numpad arrow key (change lane left)
			sendCmd(1003, "");
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) { // Right Non-numpad arrow key (change lane right)
			sendCmd(1004, "");
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) { // Down Non-numpad arrow key (turn around)
			sendCmd(1006, "");
		} else if (e.getKeyCode() == KeyEvent.VK_R) { // R (ready up)
			sendCmd(1001, "");
		}
	}

	private void sendCmd(int cmd, String extra) throws GameException {
		if (!socket.isConnected()) {
			if (!connect()) {
				throw new GameException("Unable to send command");
			}
		}
		try {
			SocketMessage msg = new SocketMessage(MainClass.MY_ID, cmd, extra);
			OutputStream os = socket.getOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(os);
			out.writeObject(msg);
			out.flush();
			os.flush();
			out.close();
			os.close();
		} catch (IOException e) {
			throw new GameException(e);
		}
	}

	private boolean connect() {
		try {
			socket = new Socket();
			socket.setSoTimeout(1000);
			socket.connect(new InetSocketAddress(MainClass.SERVER_NAME, MainClass.SERVER_PORT),
					MainClass.SERVER_TIMEOUT);
			if (!socket.isConnected()) {
				return false;
			}
			SocketMessage msg = new SocketMessage(MainClass.MY_ID, 1000, "");
			OutputStream os = socket.getOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(os);
			out.writeObject(msg);
			out.flush();
			os.flush();
			out.close();
			os.close();
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			msg = (SocketMessage) in.readObject();
			if (msg.cmd == 1009) {
				return true;
			} else if (msg.cmd == -1001) {
				return false;
			} else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
}