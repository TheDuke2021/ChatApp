package duke2021.chatapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class Server{
	
	private ServerSocket serverSocket;
	private Map<Socket, String> clients;
	
	
	public Server(){
		
		try {
			serverSocket = new ServerSocket(80);
			clients = Collections.synchronizedMap(new HashMap<>());
			System.out.println("Server started.");
		}catch(IOException e) {
			System.err.println("An IOException occurred while starting the server");
			e.printStackTrace();
			System.exit(1);
		}
		
		while(true) {
			try {
				Socket soc = serverSocket.accept();
				BufferedReader input = new BufferedReader(new InputStreamReader(soc.getInputStream()));
				BufferedWriter output = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream()));
				String name = input.readLine();
				synchronized(this) {
					try {
						//If a user with the duplicate name exists
						//Deny permission
						if (clients.containsValue(name)) {
							output.write(0);
							output.flush();
							continue;
						}
						output.write(1);
						output.flush();
						clients.put(soc, name);
						System.out.println("New connection established!");
						sendMessage(name + " has entered the chat!");
						listenToMessages(soc);
					}catch(IOException e) {
						System.err.println("User unexpectedly disconnected");
						continue;
					}
				}
			} catch (IOException e) {
				System.err.println("An IOException occurred while trying to establish contact");
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	
	private void sendMessage(String message) {
		String date = String.format("[%1$td.%1$tm.%1$tY %1$tT]", Calendar.getInstance());
		Iterator<Socket> iterator = clients.keySet().iterator();
		while(iterator.hasNext()) {
			try {
				Socket socket = iterator.next();
				BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				output.write(date + " " + message);
				output.newLine();
				output.flush();
			}catch(IOException e) {
				//If IOException occurs here, it means the user has disconnected
				//Just ignore it
				//We'll notify that the user left in the other thread
			}
		}
	}
	
	
	public static void main(String args[]){
		
		new Server();
		
	}


	private void listenToMessages(Socket socket) {
		new Thread(()->{
			try {
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while (true) {
					String msg = input.readLine();
					synchronized(this) {
						String name = clients.get(socket);
						sendMessage(name + ": " + msg);
					}
				}
			}catch(IOException e) {
				synchronized(this) {
					String name = clients.remove(socket);
					sendMessage(name + " has disconnected!");
				}
			}

		}).start();
	}
}