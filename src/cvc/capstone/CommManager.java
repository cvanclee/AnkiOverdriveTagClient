package cvc.capstone;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class CommManager extends Thread {

	private Socket socket;
	private GameGui parent;
	private OutputStream os;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String vehicleName;

	public CommManager(GameGui parent) throws GameException {
		this.parent = parent;

		if (!connect()) {
			throw new GameException("Unable to connect to server");
		}
	}

	/**
	 * Runs after receiving the game start from server, until the serve notifies the
	 * game ends
	 */
	@Override
	public void run() {
		while (!isInterrupted() || !socket.isClosed()) {
			try {
				SocketMessage msg = (SocketMessage) in.readObject();
				switch (msg.cmd) {
				case 1010:
					parent.setIsIt(true);
					parent.setGameStatus("YOU ARE IT! DRIVING " + vehicleName);
					parent.setFrameColor(new Color(1f, 0f, 0f, .5f));
					break;
				case 1011:
					parent.setIsIt(false);
					parent.setGameStatus("YOU ARE THE TAGGER! DRIVING " + vehicleName);
					parent.setFrameColor(new Color(0f, 1f, 0f, .5f));
					break;
				case 1012:
					parent.setIsIt(!(parent.isIt().get()));
					if (parent.isIt().get()) {
						parent.setGameStatus("YOU ARE IT! DRIVING " + vehicleName);
						parent.setFrameColor(new Color(1f, 0f, 0f, .5f));
					} else {
						parent.setGameStatus("YOU ARE THE TAGGER! DRIVING " + vehicleName);
						parent.setFrameColor(new Color(0f, 1f, 0f, .5f));
					}
					break;
				case 1016:
					int myInc = Integer.parseInt(msg.extra.split(";")[0]);
					int oppInc = Integer.parseInt(msg.extra.split(";")[1]);
					parent.setScoreStatus(myInc, oppInc);
				default:
					break;
				}
			} catch (SocketTimeoutException e) {
				continue;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
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
			parent.setGameStatus("CONNECTED AND READY AS " + vehicleName + ", WAITING FOR SERVER TO START GAME");
		}
	}

	public void sendCmd(int cmd, String extra) throws GameException {
		if (!socket.isConnected()) {
			throw new GameException("Unable to send command");
		}
		try {
			SocketMessage msg = new SocketMessage(MainClass.MY_ID, cmd, extra);
			out.reset();
			out.writeObject(msg);
			out.flush();
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
			throw new GameException(e);
		}
	}

	public void notifyAndTerminate() throws Exception {
		if (!socket.isConnected()) {
			return;
		}
		sendCmd(1002, "");
		socket.close();
	}

	public boolean isConnected() {
		return socket.isConnected();
	}

	private boolean connect() throws GameException {
		try {
			socket = new Socket();
			socket.setSoTimeout(2000);
			socket.connect(new InetSocketAddress(MainClass.SERVER_NAME, MainClass.SERVER_PORT),
					MainClass.COMMUNICATION_TIMEOUT);
			if (!socket.isConnected()) {
				return false;
			}
			os = socket.getOutputStream();
			out = new ObjectOutputStream(os);
			sendCmd(1000, "");
			in = new ObjectInputStream(socket.getInputStream());
			SocketMessage msg = (SocketMessage) in.readObject();
			if (msg.cmd == 1009) {
				vehicleName = msg.extra;
				parent.setVehicleName(vehicleName);
				parent.setGameStatus("CONNECTED AS " + vehicleName + ", WAITING FOR GAME START");
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