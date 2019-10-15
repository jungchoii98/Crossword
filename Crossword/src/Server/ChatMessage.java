package Server;

import java.io.Serializable;

public class ChatMessage implements Serializable{
	
	public static final long serialVersionUID = 1;
	private String message = null;
	private String hostname = null;
	private String board = null;
	public ChatMessage() {
		
	}
	public ChatMessage(String message) {
		this.message = message;
	}
	public ChatMessage(String hostname, String message) {
		this.hostname = hostname;
		this.message = message;
	}
	public ChatMessage(String message, String board, int temp) {
		this.message = message;
		this.board = board;
	}
	public String getMessage() {
		return message;
	}
	public String getHostname() {
		return hostname;
	}
	public String getBoard() {
		return board;
	}
}
