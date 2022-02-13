package duke.chatapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileReader;
//import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Server implements Runnable{
	
	private ServerSocket serverSocket;
	private Map<String, Socket> clients;
	private BufferedReader serverInput;
	private BufferedWriter serverOutput;
	//TODO add keeping logs and sending last 10 minutes logs to new clients
//	private File logs;
//	private FileWriter fileWriter;
//	private FileReader fileReader;

	
	
	
	Server(){
		
		try {
			serverSocket = new ServerSocket(80);
			clients = Collections.synchronizedMap(new HashMap<>());
			System.out.println("Server started.");
			new Thread(this).start();
		}catch(IOException e) {
			System.err.println("An IOException occured while starting the server");
			e.printStackTrace();
			System.exit(1);
		}
		
		while(true) {
			synchronized(this){
				
				boolean receivedMsg = false;
				String name = "";
				String msg = "";
				for (Socket socket : clients.values()) {
					try {
						serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						//TODO maybe bc of that if, IOException will never occur
						if(!serverInput.ready())
							continue;
						name = serverInput.readLine();
						msg = serverInput.readLine();
						if(name != null && !name.equals("")) {
							receivedMsg = true;
							break;
						}
					} catch (IOException e) {
						//TODO
						e.printStackTrace();
					} 
				}
				
				
				if(receivedMsg) 
					sendMsg(name, msg, Message_Type.MESSAGE);
				else
					sendMsg("", "", Message_Type.CHECK);
			}
			
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	
	private void sendMsg(String name, String msg, Message_Type msgType) {
		String date = String.format("[%1$td.%1$tm.%1$tY %1$tT]", Calendar.getInstance());
		Set<Map.Entry<String, Socket>> set = clients.entrySet();
		Iterator<Map.Entry<String, Socket>> iterator = set.iterator();
		String socketName = "";
		Socket socket;
		try {
			while(iterator.hasNext()) {
				Map.Entry<String, Socket> entry = iterator.next();
				socketName = entry.getKey();
				socket = entry.getValue();
				serverOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				switch(msgType) {
				case MESSAGE:
					serverOutput.write(date + " " + name + ": " + msg);
					break;
				case CHECK:
					serverOutput.write("");
					break;
				case DISCONNECTED:
					serverOutput.write(date + " " + name + " has disconnected!");
					break;
				case GREETING:
					serverOutput.write(date + " " + name + " has entered the chat!");
					break;
				}
				serverOutput.newLine();
				serverOutput.flush();
			}
		}catch (SocketException e) {
			clients.remove(socketName);
			System.out.println(socketName + " has disconnected!");
			sendMsg(socketName, "", Message_Type.DISCONNECTED);
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String args[]){
		
		new Server();
		
	}


	@Override
	public void run() {
		while (true) {
			try {
				Socket soc = serverSocket.accept();
				synchronized(this) {
					try {
						serverInput = new BufferedReader(new InputStreamReader(soc.getInputStream()));
						serverOutput = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream()));
						String name = serverInput.readLine();
						if (clients.containsKey(name)) {
							serverOutput.write(0);
							serverOutput.flush();
							continue;
						}
						serverOutput.write(1);
						serverOutput.flush();
						clients.put(name, soc);
						System.out.println("New connection established!");
						sendMsg(name, "", Message_Type.GREETING);
					}catch(IOException e) {
						System.err.println("User unexpectedly disconnected");
						continue;
					}
				}
			} catch (IOException e) {
				System.err.println("An IOException occured while trying to establish contact");
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

}