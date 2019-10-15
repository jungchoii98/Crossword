package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Players extends Thread{
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private static int currNumPlayers = 0;
	
	public Players() {
		try {
			//retreive hostname and port number
			System.out.println("Welcome to 201 Crossword!");
			System.out.print("Enter the server hostname: ");
			Scanner scan = new Scanner(System.in);
			String hostname = scan.nextLine();
			System.out.print("Enter the server port: ");
			Scanner scan2 = new Scanner(System.in);
			int port = scan2.nextInt();
			//connect to server and start thread
			System.out.println("trying to connect to " + hostname + ": " + port);
			Socket s = new Socket(hostname, port);
			System.out.println("connected to " + hostname + ": " + port);
			ois = new ObjectInputStream(s.getInputStream());
			oos = new ObjectOutputStream(s.getOutputStream());
			
			//get the player number index
			ChatMessage cm = (ChatMessage)ois.readObject();
			System.out.println("I am " + cm.getMessage());
			
			//prompt the user accordingly
			int numberOfPlayers = 0;
			if(cm.getMessage().equals("Player 1")) {
				//get the number of players and send it to the server
				System.out.print("How many players will there be?");
				Scanner scan3 = new Scanner(System.in);
				numberOfPlayers = scan3.nextInt();
				cm = new ChatMessage(Integer.toString(numberOfPlayers));
				oos.writeObject(cm);
				
				for(int i=2; i<numberOfPlayers+1; i++) {
					System.out.println("Waiting for player " + i);
				}
				int counter = 1;
				while(counter < numberOfPlayers) {
					ChatMessage p1 = (ChatMessage)ois.readObject();
					System.out.println(p1.getMessage() + " has joined from " + p1.getHostname());
					counter++;
				}
				System.out.println("The game is beginning");
			}
			else if(cm.getMessage().equals("Player 2")) {
				System.out.println("There is a game waiting for you");
				System.out.println("Player 1 has already joined");
				int counter = 2;
				if(counter < numberOfPlayers) {
					System.out.println("Waiting for Player 3");
					ChatMessage p2 = (ChatMessage)ois.readObject();
					System.out.println(p2.getMessage() + " has joined from " + p2.getHostname());
				}
				System.out.println("The game is beginning");
				
			}
			else if(cm.getMessage().equals("Player 3")) {
				System.out.println("There is a game waiting for you");
				System.out.println("Player 1 has already joined");
				System.out.println("Player 2 has already joined");
				System.out.println("The game is beginning");
			}
			
			//start thread
			this.start();
			Scanner scan3 = new Scanner(System.in);
			String line = null;
			while(true) {
				line = scan3.nextLine();
				ChatMessage answer = new ChatMessage(line);
				oos.writeObject(answer);
			}
			
		} catch(IOException ioe) {
			//System.out.println("ioe: " + ioe.getMessage());
			ioe.printStackTrace();
		} catch(ClassNotFoundException cnfe) {
			//System.out.println("cnfe: " + cnfe.getMessage());
			cnfe.printStackTrace();
		}
	}
	
	public void run() {
		int row = 0;
		String[] board = new String[16];
		try {
			ChatMessage response = new ChatMessage();
			while(true) {
				response = (ChatMessage)ois.readObject();
				if(response.getMessage().equals("board")) {
					//response.getBoard().printBoard();
					//System.out.println("ooh we here");
					board[row] = response.getBoard();
					row++;
					if(row == 16) {
						for(int i=0; i<16; i++) {
							System.out.println(board[i]);
						}
						row = 0;
						board = new String[16];
					}
//					char[][] board = response.getBoard();
//					for(int i=0; i<16; i++) {
//						for(int j=0; j<32; j++) {
//							System.out.print(board[i][j]);
//						}
//						System.out.println();
//					}
				}
				else if(response.getMessage().equals("prompt")) {
					System.out.println(response.getMessage());
				}
				else if(response.getMessage().equals("score")) {
					System.out.println(response.getMessage());
				}
				else {
					System.out.println(response.getMessage());
				}
			}
		} catch(IOException ioe) {
			System.out.println("ioe player: " + ioe.getMessage());
			ioe.printStackTrace();
		} catch(ClassNotFoundException cnfe) {
			System.out.println("cnfe: " + cnfe.getMessage());
		}
	}
	
	public static void main(String[] args) {
		Players player = new Players();
	}
}
