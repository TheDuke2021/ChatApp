package duke.chatapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Server implements Runnable{
	
	private ServerSocket serverSocket;
	private List<Socket> clientSockets;
	private BufferedReader serverInput;
	private BufferedWriter serverOutput;
	//TODO add keeping logs and sending last 10 minutes logs to new clients
	private File logs;
	private FileWriter fileWriter;
	private FileReader fileReader;

	
	Server(){
		
		try {
			serverSocket = new ServerSocket(80);
			clientSockets = Collections.synchronizedList(new ArrayList<>());
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
				String date = "";
				for (Socket socket : clientSockets) {
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
					date += String.format("[%1$td.%1$tm.%1$tY %1$tT]", Calendar.getInstance());
				
				Iterator<Socket> iterator = clientSockets.iterator();
				while(iterator.hasNext()) {
					try {
						Socket socket = iterator.next();
						serverOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
						if(receivedMsg)
							serverOutput.write(date + " " + name + ": " + msg);
						else
							serverOutput.write("");
						serverOutput.newLine();
						serverOutput.flush();
					}catch (SocketException e) {
						iterator.remove();
						System.out.println("A user has disconencted!");
					}catch(IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
					clientSockets.add(soc);
					System.out.println("New connection established!");
				}
			} catch (IOException e) {
				System.err.println("An IOException occured while trying to establish contact");
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

}