package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ServerThread extends Thread{
	
	ObjectOutputStream oos = null;
	ObjectInputStream ois = null;
	Server server = null;
	Lock lock = null;
	Condition condition = null;
	boolean isFirst = false;
	
	public ServerThread(Socket s, Server server, Lock lock, Condition condition, boolean isFirst) {
		try {
			this.server = server;
			this.lock = lock;
			this.condition = condition;
			this.isFirst = isFirst;
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
		} catch(IOException ioe) {
			System.out.println("ioe: " + ioe.getMessage());
		}
	}
	public void startThread() {
		this.start();
	}
	
	public ChatMessage returnMessageToServer() {
		ChatMessage cm = null;
		try {
			cm = (ChatMessage)ois.readObject();
		} catch(ClassNotFoundException cnfe) {
			System.out.println("cnfe: " + cnfe.getMessage());
		} catch(IOException ioe) {
			System.out.println("ioe: " + ioe.getMessage());
		}
		return cm;
	}
	
	public void sendMessage(ChatMessage cm) {
		try {
			oos.writeObject(cm);
			oos.flush();
		} catch(IOException ioe) {
			System.out.println("ioe: " + ioe.getMessage());
		}
	}
	
	public void run() {
		try {
			while(true) {
				lock.lock();
				if(isFirst == false) {
					condition.await();
				}
				else {
					isFirst = false;
				}
				if(server.isFinished()) {
					server.displayStats();
					return;
				}
				server.sendBoard(this);
				boolean flag = false;
				String acrossOrDown = "";
				String number = "";
				String answer = "";
				while(!flag) {
					ChatMessage toClient = new ChatMessage("Would you like to answer a question across (a) or down (d)?");
					server.broadcast(toClient, this);
					ChatMessage clientAnswer = (ChatMessage)ois.readObject();
					if(clientAnswer.getMessage().equals("a") || clientAnswer.getMessage().equals("d")) {
						acrossOrDown = clientAnswer.getMessage();
						flag = true;
					}
					else {
						server.broadcast(new ChatMessage("That is not a valid option."), this);
					}
				}
				flag = false;
				while(!flag) {
					ChatMessage toClient = new ChatMessage("Which number?");
					server.broadcast(toClient, this);
					ChatMessage clientAnswer = (ChatMessage)ois.readObject();
					if(acrossOrDown.equals("a")) {
						if(server.validateNumber(clientAnswer.getMessage(), true)) {
							flag = true;
						}
					}
					else {
						if(server.validateNumber(clientAnswer.getMessage(), false)) {
							flag = true;
						}
					}
					if(!flag) {
						server.broadcast(new ChatMessage("That is not a valid option."), this);
					}
					else {
						number = clientAnswer.getMessage();
					}
				}
				flag = false;
				while(!flag) {
					ChatMessage toClient;
					if(acrossOrDown.equals("a")) {
						toClient = new ChatMessage("What is your guess for " + number + "across");
					}
					else {
						toClient = new ChatMessage("What is your guess for " + number + "down");
					}
					server.broadcast(toClient, this);
					ChatMessage clientAnswer = (ChatMessage)ois.readObject();
					if(server.checkAnswer(clientAnswer.getMessage(), acrossOrDown, number, this)) {
						server.broadcast(new ChatMessage("That is correct!"), this);
					}
					else {
						server.broadcast(new ChatMessage("That is incorrect!"), this);
					}
					flag = true;
				}
				lock.unlock();
				server.signalNextPlayer();
//				ChatMessage cm = (ChatMessage)ois.readObject();
//				server.broadcast(cm, this);
			}
		} catch(IOException ioe) {
			System.out.println("ioe: in ServerThread" + ioe.getMessage());
		} catch(ClassNotFoundException cnfe) {
			System.out.println("cnfe: in ServerThread" + cnfe.getMessage());
		} catch(InterruptedException ie) {
			System.out.println("ie: " + ie.getMessage());
		}
	}

}
