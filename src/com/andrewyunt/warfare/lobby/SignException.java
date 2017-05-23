
package com.andrewyunt.warfare.lobby;

/**
 * The exception which is used when an error occurs with the SignDisplay object.
 * 
 * @author Andrew Yunt
 */
public class SignException extends Exception {

	private static final long serialVersionUID = 8364007684590051100L;

	public SignException() {
		
		super("An exception occured while conducting an operation on a sign.");
	}
	
	public SignException(String message) {
		
		super(message);
	}
}