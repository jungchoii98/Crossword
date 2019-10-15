package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.Serializable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server implements Serializable{
	private static final long serialVersionUID = 1L;
	Socket s = null;
	ServerSocket ss = null;
	ServerThread st = null;
	Vector<ServerThread> serverThreads = new Vector<ServerThread>();
	Vector<Lock> locks = new Vector<Lock>();
	Vector<Condition> conditions = new Vector<Condition>();
	int[] playerScores;
	int numberOfPlayers = 0;
	int currentPlayer = 0;
	ArrayList<Word> words = new ArrayList<Word>();
	ArrayList<Word> placedWords = new ArrayList<Word>();
	ArrayList<String> prompts = new ArrayList<String>();
	Board solvedBoard = new Board();
	int requiredIntersections = 0;
	
	
	public Server(int port) {
		//parse file
		parseFile();
		//create board
		ArrayList<Word> remainingWords = new ArrayList<Word>();
		ArrayList<Word> placedWords = new ArrayList<Word>();
		for(int i=0; i<words.size(); i++) {
			remainingWords.add(words.get(i));
		}
		solvedBoard.initialize();
		solvedBoard.generateBoard(words, placedWords, remainingWords, requiredIntersections, 0);
		
		try {
			//receives the first user and gets the number of players
			System.out.println("binding to port: " + port);
			ss = new ServerSocket(port);
			System.out.println("successfully bound to port: " + port);
			Lock clientLock = new ReentrantLock();
			Condition lockCondition = clientLock.newCondition();
			locks.add(clientLock);
			conditions.add(lockCondition);
			s = ss.accept();
			System.out.println("connection from: " + s.getInetAddress());
			st = new ServerThread(s, this, clientLock, lockCondition, true);
			serverThreads.add(st);
			//give the client its player number
			ChatMessage cm = new ChatMessage("Player 1");
			broadcast(cm, st);
			//get the number of players that the client wants
			cm = returnClientMessage(st);
			numberOfPlayers = Integer.parseInt(cm.getMessage());
			System.out.println("this is number of players " + numberOfPlayers);
			playerScores = new int[numberOfPlayers];
			for(int j=0; j<numberOfPlayers; j++) {
				playerScores[j] = 0;
			}
			
			//wait until all of the players have connected to the game
			while(serverThreads.size() < numberOfPlayers) {
				Socket p = ss.accept();
				Lock playerLock = new ReentrantLock();
				Condition plockCondition = playerLock.newCondition();
				locks.add(playerLock);
				conditions.add(plockCondition);
				ServerThread stt = new ServerThread(p, this, playerLock, plockCondition, false);
				serverThreads.add(stt);
				//give player 2 and player 3 their player index
				ChatMessage cp = new ChatMessage("Player " + serverThreads.size());
				broadcast(cp, stt);
				//give the new client connected to player 1
				System.out.println("this is player 2 address: " + p.getInetAddress().getHostAddress());
				cp = new ChatMessage(p.getInetAddress().getHostAddress(), "Player " + Integer.toString(serverThreads.size()));
				broadcast(cp, serverThreads.get(0));
				//gives the new client connected to player 2
				if(serverThreads.size() == 3) {
					broadcast(cp, serverThreads.get(1)); 
				}
			}
			for(int i=0; i<serverThreads.size(); i++) {
				serverThreads.get(i).startThread();
			}
		} catch(IOException ioe) {
			System.out.println("ioe1: " + ioe.getMessage());
		}
	}
	
	public ChatMessage returnClientMessage(ServerThread st) {
		ChatMessage cm = st.returnMessageToServer();
		return cm;
	}
	
	public void broadcast(ChatMessage cm, ServerThread st) {
		if(cm != null) {
			System.out.println(cm.getMessage());
			for(ServerThread threads: serverThreads) {
				if(st == threads) {
					threads.sendMessage(cm);
				}
			}
		}
	}
	
	public void signalNextPlayer() {
		currentPlayer += 1;
		if(currentPlayer == locks.size()) {
			currentPlayer = 0;
		}
		locks.get(currentPlayer).lock();
		conditions.get(currentPlayer).signal();
		locks.get(currentPlayer).unlock();
		
	}
	
	public boolean validateNumber(String number, boolean across) {
		for(int i=0; i<words.size(); i++) {
			if(words.get(i).isHorz == across && words.get(i).number.equals(number)) {
				return true;
			}
			else if(words.get(i).isHorz != across && words.get(i).number.equals(number)) {
				return true;
			}
		}
		return false;
	}
	
	public void sendBoard(ServerThread st) {
//		char[][] literalBoard = solvedBoard.stringBoard();
		//System.out.println("this is singleLine : " + solvedBoard.stringBoard(8));
		for(ServerThread threads: serverThreads) {
			for(int i=0; i<16; i++) {
				String singleLine = solvedBoard.stringBoard(i);
				ChatMessage cm = new ChatMessage("board", singleLine, 0);
				broadcast(cm, threads);
			}
			for(int i=0; i<prompts.size(); i++) {
				ChatMessage cm = new ChatMessage("prompt", prompts.get(i));
				broadcast(cm, threads);
			}
		}
		//System.out.println("begin printing board from server");
//		for(int i=0; i<16; i++) {
//			for(int j=0; j<32; j++) {
//				System.out.print(literalBoard[i][j]);
//			}
//			System.out.println();
//		}
//		for(ServerThread thread: serverThreads) {
//			if(thread == st) {
//				ChatMessage cm = new ChatMessage("board", literalBoard);
//				broadcast(cm, thread);
//			}
//		}
	}
	
	public void parseFile() {
		boolean flag = true;
		//choose a random file in the gamedata folder
		File[] textFiles = new File("./gamedata").listFiles();
		ArrayList<String> validFiles = new ArrayList<String>();
		for(File txtFile: textFiles) {
			if(txtFile.isFile()) {
				validFiles.add(txtFile.getName());
			}
		}
		for(int i=0; i<validFiles.size(); i++) {
			System.out.println("this is a valid file " + validFiles.get(i));
		}
		for(int a=0; a<validFiles.size(); a++) {
			//String filename = "test.txt";
			int numIntersections = 0;
			
			FileReader fr = null;
			BufferedReader br = null;
			try {
				//read lines from the file
				fr = new FileReader("./gamedata/"+validFiles.get(a));
				br = new BufferedReader(fr);
				
				ArrayList<String> acrossNumbers = new ArrayList<String>();
				ArrayList<String> downNumbers = new ArrayList<String>();
				HashMap<String, String> acrossMap = new HashMap<String, String>();
				HashMap<String, String> downMap = new HashMap<String, String>();
				String line = br.readLine();
				boolean across = false;
				
				int acrossCounter = 0;
				int downCounter = 0;
				while(line != null) {
					String lineParse[] = line.split("\\|");
					System.out.println(lineParse.length);
					if(lineParse.length == 1) {
						prompts.add(line);
						if(lineParse[0].equals("ACROSS")) {
							if(acrossCounter > 1) {
								System.out.println("failed 1");
								flag = false;
							}
							across = true;
							acrossCounter++;
						} else if(lineParse[0].equals("DOWN")) {
							if(downCounter > 1) {
								System.out.println("failed 2");
								flag = false;
							}
							across = false;
							downCounter++;
							
						} else {
							System.out.println("failed 3");
							flag = false;
						}
					}
					else if(lineParse.length == 3) {
						prompts.add(lineParse[0] + lineParse[2]);
						//go through the parameters and make sure they are valid
						for(int i=0; i<3; i++) {
							//check if the first parameter is an integer
							if(i == 0) {
								Integer.parseInt(lineParse[0]);
							} 
							//checks if the second parameter has a whitespace 
							else if(i == 1){ 
								String temp[] = lineParse[1].split(" ");
								if(temp.length > 1) {
									System.out.println("failed 4");
									flag = false;
								}
							}
							//checks if the last parameter is a question
							else if(i == 2) {
								if( !(lineParse[2].charAt(lineParse[2].length()-1) == '?') ) {
									System.out.println("failed 5");
									flag = false;
								}
							}
						}
						//add the number and answer values to map
						if(across) {
							acrossNumbers.add(lineParse[0]);
							acrossMap.put(lineParse[0], lineParse[1]);
						} else {
							downNumbers.add(lineParse[0]);
							downMap.put(lineParse[0], lineParse[1]);
						}
					}
					else {
						System.out.println("failed 6");
						flag = false;
					}
					line = br.readLine();
				}
				//checks if across and down have the same number that start with the same letter answer
				for(int i=0; i<acrossNumbers.size(); i++) {
					String num = acrossNumbers.get(i);
					if(downMap.containsKey(num)) {
						String downNum = downMap.get(num);
						String acrossNum = acrossMap.get(num);
						char downChar = downNum.charAt(0);
						char acrossChar = acrossNum.charAt(0);
						if(downChar != acrossChar) {
							System.out.println("failed 7");
							flag = false;
							break;
						}
						numIntersections++;
					}
				}
				//add the words to the array of Words now
				for(int i=0; i<acrossNumbers.size(); i++) {
					Word temp = new Word(true, acrossNumbers.get(i), acrossMap.get(acrossNumbers.get(i)));
					words.add(temp);
				}
				for(int i=0; i<downNumbers.size(); i++){
					Word temp = new Word(false, downNumbers.get(i), downMap.get(downNumbers.get(i)));
					words.add(temp);
				}
				for(int i=0; i<words.size(); i++) {
					System.out.println(words.get(i).actualWord);
				}
				
			} catch(FileNotFoundException fnfe) {
				System.out.println("fnfe: " + fnfe.getMessage());
			} catch(IOException ioe) {
				System.out.println("ioe: " + ioe.getMessage());
			} catch(NumberFormatException nfe){
				System.out.println("nfe: " + nfe.getMessage());
			} finally {
				try {
					if(br != null) {
						br.close();
					} if(fr != null) {
						fr.close();
					}
				} catch(IOException ioe) {
					System.out.println("ioe: " + ioe.getMessage());
				}
				
			}
			
			if(flag) {
				requiredIntersections = numIntersections;
				System.out.println("success! file is good");
				break;
			}
			else {
				words = new ArrayList<Word>();
				prompts = new ArrayList<String>();
				flag = true;
				System.out.println("epic fail");
			}
		}
		
		return;
	}
	
	public boolean isFinished() {
		for(int i=0; i<words.size(); i++) {
			if(words.get(i).placedByPlayer == false) {
				return false;
			}
		}
		return true;
	}
	
	public void displayStats() {
		int playerIndex = 0;
		int winnerIndex = 0;
		boolean isTie = false;
		for(ServerThread threads: serverThreads) {
			String singleLine = "";
			for(int i=0; i<numberOfPlayers+1; i++) {
				if(i == 0) {
					singleLine += "Final Score";
					singleLine += "\n";
				}
				else {
					singleLine += "Player ";
					singleLine += (i-1);
					singleLine += " - ";
					singleLine += playerScores[playerIndex];
					singleLine += " correct answers.";
					singleLine += "\n";
					playerIndex++;
				}
			}
			ChatMessage cm = new ChatMessage("score", singleLine);
			broadcast(cm, threads);
			for(int i=1; i<numberOfPlayers; i++) {
				if(playerScores[winnerIndex] == playerScores[i]) {
					isTie = true;
				}
				else if(playerScores[winnerIndex] < playerScores[i]) {
					isTie = false;
					winnerIndex = i;
				}
			}
			if(isTie) {
				String newSingleLine = "Game is a tie.";
				ChatMessage cb = new ChatMessage("score", newSingleLine);
				broadcast(cb, threads);
			}
			else {
				String newSingleLine = "Player ";
				newSingleLine += winnerIndex;
				newSingleLine += " is the winner.";
				ChatMessage cb = new ChatMessage("score", newSingleLine);
				broadcast(cb, threads);
			}
		}
	}
	
	public boolean checkAnswer(String word, String across, String number, ServerThread st) {
		int playerIndex = 0;
		for(ServerThread threads: serverThreads) {
			if(st == threads) {
				break;
			}
			else {
				playerIndex++;
			}
		}
		for(int i=0; i<words.size(); i++) {
			if(word.equals(words.get(i).actualWord) && across.equals("a") && words.get(i).isHorz && number.equals(words.get(i).number) && words.get(i).placedByPlayer == false ) {
				words.get(i).placedByPlayer = true;
				playerScores[playerIndex]++;
				return true;
			}
			else if(word.equals(words.get(i).actualWord) && across.equals("d") && !words.get(i).isHorz && number.equals(words.get(i).number) && words.get(i).placedByPlayer == false ) {
				words.get(i).placedByPlayer = true;
				playerScores[playerIndex]++;
				return true;
			}
		}
		return false;
	}
	
	public void placeOnBoard(String word) {
		solvedBoard.placeWordOnBoard(word);
	}
	
	
	public static void main(String[] args) {
		
		//create a server
		Server server = new Server(6789);

		
			
	}
}
