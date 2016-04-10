import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
	final static String baseName = "HW5";
	static ArrayList<String> ips;
	static ArrayList<Integer> ports;
	static ArrayList<String> supplies;
    static ArrayList<Integer> quantities;
    static ArrayList<Integer> orderIDs = new ArrayList<Integer>();
    static ArrayList<String> userName = new ArrayList<String>();
    static ArrayList<String> myProductName = new ArrayList<String>();
    static ArrayList<String> myOrder = new ArrayList<String>();
    static AtomicInteger orderIDinit;
    
    public static void main (String[] args) {

	    Scanner sc = new Scanner(System.in);
	    int myID = sc.nextInt();
	    int numServer = sc.nextInt();
	    String inventoryPath = sc.next();
	    ips = new ArrayList<String>();
	    ports = new ArrayList<Integer>();
	    assert myID < numServer;
	    for (int i = 0; i < numServer; i++) {
	      // TODO: parse inputs to get the ips and ports of servers
	      String ip_port = sc.next();
	      String[] nums = ip_port.split(":");
	      assert nums.length == 2;
	      ips.add((nums[0]));
	      ports.add(Integer.parseInt(nums[1]));
	    }
	    
	    
	    
	    // TODO: start server socket to communicate with clients and other servers
	    ServerSocket socket = null;
	    try {
	        int myPort = ports.get(myID - 1);
                System.out.println(myPort);
			socket = new ServerSocket(myPort);
			
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    parseInventory(inventoryPath, socket);
	    
	    // create mutex
	    String[] linkerInfo = {baseName, Integer.toString(myID), Integer.toString(numServer)};
	    Linker link;
	    LamportMutex mutex = null;
		try {
			link = new Linker(linkerInfo);
			mutex = new LamportMutex(link);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	    
	    // TODO: handle request from client
	    Socket connectionSocket;
	    while(true){
		    try{
	    		connectionSocket = socket.accept();
	    		//awaitOkay(connectionSocket, numServer);
	    		
	    		BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		    	DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
		    	String s = new String(inFromClient.readLine());
		    	String[] tokens = s.split(" ");
		    	clientRequest(tokens, mutex, s, outToClient);
		    	
	    	}catch(IOException e){
				//System.out.println("Could not");
			}
	    }
	  }
  
    
    // parse Inventory file
    public static void parseInventory(String inventoryPath, ServerSocket socket){
    	
	    supplies = new ArrayList<String>();
	    quantities = new ArrayList<Integer>();
	    try {
	        BufferedReader br = new BufferedReader(new FileReader(inventoryPath));
	        String line;
	    	while(null != (line = br.readLine())){
				String[] tokens = line.split(" ");
				if(tokens.length != 2){
					throw new IOException();
				}
				supplies.add(tokens[0]);
				quantities.add(Integer.parseInt(tokens[1]));
			}
	    	br.close();
	    	socket.close();
	    } catch (NumberFormatException | IOException e1) {
	    	// TODO Auto-generated catch block
	    	e1.printStackTrace();
	    }	
    }
    
    
    // send a message to all servers 
	// wait for acceptance from all
    public static void awaitOkay(Socket connectionSocket, int numServer){
    	try {
            Socket tcpSocket;
            DataOutputStream outToServer;
            BufferedReader inFromServer;
    		for(int i = 0; i < numServer; i++){
    			
    			tcpSocket = new Socket(InetAddress.getByName(ips.get(i)), ports.get(i));
	    	    outToServer = new DataOutputStream(tcpSocket.getOutputStream());	
	    	    inFromServer = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream())); 
			
    		}
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static String clientRequest(String[] tokens, LamportMutex mutex, String order, DataOutputStream outToClient){
    	String rString = order;
    	Integer orderID = orderIDinit.get();
    	if(tokens[0].equals("purchase")){
    		mutex.requestCS();
    		
    		int indexSupplies = supplies.indexOf(tokens[2]);
    		if(indexSupplies < 0){
				rString = "Not Available - We do not sell this product";
			}
    		else if(quantities.get(indexSupplies) < Integer.parseInt(tokens[3])){
				rString = "Not Available - Not enough items";
			}
    		else{
				int newQuant = quantities.get(indexSupplies) - Integer.parseInt(tokens[3]);
				quantities.set(indexSupplies, newQuant);
				orderIDinit.getAndIncrement();
				if(!orderIDs.contains(orderID)){
					orderIDs.add(orderID);
				}
				else{
					orderID++;
					orderIDs.add(orderID);
				}
				rString = "You order has been placed, " + orderID + " " + rString;
			    userName.add(tokens[1]);
			    myProductName.add(tokens[2]);
			    myOrder.add(tokens[3]);
    		}
    		try {
				outToClient.writeBytes(rString + '\n');
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		mutex.releaseCS();
    	}
    	else if(tokens[0].equals("cancel")){
			//checks if the orderID is present
			int indexID = orderIDs.indexOf(Integer.parseInt(tokens[1]));
		
			//if orderID is not there return not found
			if(indexID < 0){
				rString = tokens[1] + " not found, no such order";
			}
			
			//else remove order from all of the lists...lol
			else {
				mutex.requestCS();
				rString = "Order " + tokens[1] + " is canceled";
				int supplyIndex = supplies.indexOf(myProductName.get(indexID));
				quantities.set(supplyIndex, Integer.parseInt(myOrder.get(indexID)) + quantities.get(supplyIndex));
				orderIDs.remove(indexID);
			    userName.remove(indexID);
			    myProductName.remove(indexID);
			    myOrder.remove(indexID);
			    mutex.releaseCS();
				
			}
			//System.out.println("Sending data");
			try {
				outToClient.writeBytes(rString + '\n');
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("Sent");
    	}
    	else if(tokens[0].equals("search")){
    		rString = new String();
			//checks if the user is in the database
			int indexName = userName.indexOf(tokens[1]);
			if(indexName < 0){
				rString = "No order found for " + tokens[1];
			}
			
			//if in the database...iterates through all users in order to see all instances of the user
			else {
				int i = 0;
				for(String currUser: userName){
					if(currUser.equals(tokens[1])){
						//return string: <order-id>, <product-name>, <quantity>
						rString += orderIDs.get(i) + ", " + myProductName.get(i) + ", " + myOrder.get(i) + " ";
						//rString = orderIDs.get(i) + ", " + myProductName.get(i) + ", " + myOrder.get(i) + " ";
						//outToClient.writeBytes(rString + '\n');
					}
					//outToClient.writeByte('\n');
					i++;
				}
			}
			//System.out.println("Sending data");
			try {
				outToClient.writeBytes(rString + '\n');
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("Sent");
    	}
    	else if(tokens[0].equals("list")){
    		int sizeInv = supplies.size();
			rString = new String();
			for(int i=0; i<sizeInv; i++){
				rString += supplies.get(i) + " " + quantities.get(i) + " ";
				//rString = supplies.get(i) + " " + quantities.get(i) + " ";
				//outToClient.writeBytes(rString + '\n');
			}
			//outToClient.writeByte('\n');
			//System.out.print(rString);
			//System.out.println("Sending data");
			try {
				outToClient.writeBytes(rString + '\n');
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
		return rString;
    }
}
