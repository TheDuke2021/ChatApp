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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Server2 implements Runnable{
	
	private ServerSocket serverSocket;
	private List<Socket> clientSockets;
	private Map<Socket, BufferedReader> serverInputs;
	private Map<Socket, BufferedWriter> serverOutputs;
	private BufferedReader serverInput;
	private BufferedWriter serverOutput;
	//TODO add keeping logs and sending last 10 minutes logs to new clients
	private File logs;
	private FileWriter fileWriter;
	private FileReader fileReader;

	
	Server2(){
		
		try {
			serverSocket = new ServerSocket(80);
			clientSockets = Collections.synchronizedList(new ArrayList<>());
			serverInputs = Collections.synchronizedMap(new HashMap<>());
			serverOutputs = Collections.synchronizedMap(new HashMap<>());
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
				String date = "[";
				for (Socket socket : clientSockets) {
					try {
						serverInput = serverInputs.get(socket);
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
				
				if(receivedMsg) {
					Calendar calendar = Calendar.getInstance();
					date += calendar.get(Calendar.DATE) + ".";
					date += calendar.get(Calendar.MONTH) + ".";
					date += calendar.get(Calendar.YEAR) + " ";
					
					date += calendar.get(Calendar.HOUR_OF_DAY) + ":";
					date += calendar.get(Calendar.MINUTE) + ":";
					date += calendar.get(Calendar.SECOND) + "]";
				}
				Iterator<Socket> iterator = clientSockets.iterator();
				while(iterator.hasNext()) {
					Socket socket = null;
					try {
						socket = iterator.next();
						serverOutput = serverOutputs.get(socket);
						if(receivedMsg)
							serverOutput.write(date + " " + name + ": " + msg);
						else
							serverOutput.write("");
						serverOutput.newLine();
						serverOutput.flush();
					}catch (SocketException e) {
						iterator.remove();
						serverInputs.remove(socket);
						serverOutputs.remove(socket);
						System.out.println("A user has disconencted!");
					}catch(IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	public static void main(String args[]){
		
		new Server2();
		
	}


	@Override
	public void run() {
		while (true) {
			try {
				Socket soc = serverSocket.accept();
				synchronized(this) {
					clientSockets.add(soc);
					serverInputs.put(soc, new BufferedReader(new InputStreamReader(soc.getInputStream())));
					serverOutputs.put(soc, new BufferedWriter(new OutputStreamWriter(soc.getOutputStream())));
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