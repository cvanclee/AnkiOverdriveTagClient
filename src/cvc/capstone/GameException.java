package cvc.capstone;

public class GameException extends Exception {

	public GameException(String errorStatement) {
		super(errorStatement);
	}
	
	public GameException(Exception exc) {
		super(exc);
	}
}
