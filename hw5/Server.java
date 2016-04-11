import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;


public class Server {
	final static boolean printStatements = true;
	static ArrayList<String> ips;
	static ArrayList<Integer> ports;
	static ArrayList<Integer> serverPorts; // only used for each server sending messages to each other
	static ArrayList<String> supplies;
    static ArrayList<Integer> quantities;
    static ArrayList<Integer> orderIDs = new ArrayList<Integer>();
    static ArrayList<String> userName = new ArrayList<String>();
    static ArrayList<String> myProductName = new ArrayList<String>();
    static ArrayList<String> myOrder = new ArrayList<String>();
    static AtomicInteger orderIDinit = new AtomicInteger(1);
    static DirectClock clock;
    static int myID;
    static int[] req_queue;
    static ArrayList<String> client_request;
    static ArrayList<Socket> sockets;
    public static void main (String[] args) {
	    Scanner sc = new Scanner(System.in);
	    myID = sc.nextInt();
	    int numServer = sc.nextInt();
	    String inventoryPath = sc.next();
	    ips = new ArrayList<String>();
	    ports = new ArrayList<Integer>();
	    serverPorts = new ArrayList<Integer>();
	    client_request = new ArrayList<String>();
	    sockets = new ArrayList<Socket>();
	    req_queue = new int[numServer];
	    for(int i = 0; i < numServer; i++) // means don't want the CS 
	    	req_queue[i] = Integer.MAX_VALUE;
	    
	    assert myID < numServer;
	    for (int i = 0; i < numServer; i++) {
	      // TODO: parse inputs to get the ips and ports of servers
	      String ip_port = sc.next();
	      String[] nums = ip_port.split(":");
	      
	      assert nums.length == 2;
	      ips.add((nums[0]));
	      ports.add(Integer.parseInt(nums[1]));
	    }
	    
	    // new ports for server-server connection
	    { // putting this in inner brackets in case I reuse i
	    	int i = 0;
	    	int possiblePort = ports.get(i) + 1;
	    	while(i < numServer){
	    		if(ports.contains(possiblePort) || serverPorts.contains(possiblePort)){ // prevent reuse of ports
	    			++possiblePort;
	    		}
	    		else{ // unused port - hopefully fine - tbh I don't really know if it is used by some other process
	    			serverPorts.add(possiblePort);
	    			++i;
	    			if(i >= numServer)
	    				break;
	    			possiblePort = ports.get(i) + 1;
	    		}
	    	} 	
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
	    parseInventory(inventoryPath);
	    
	    if(printStatements)
	   		System.out.println("Parsed Inventory");
	    // TODO: handle request from client
	    Thread serverListener = createServerRequestListener();
	    if(printStatements)
	    	System.out.println("Created Initial Server Listener");
	    Thread clientListener = createClientRequestListener(socket);
	    if(printStatements)
	    	System.out.println("Starting Threads");
	    serverListener.start();
	    clientListener.start();
	    if(printStatements)
	    	System.out.println("Entering Loop");
		while(true){
		    // get client command - makes its own thread
			// create listener for request 
			if(!clientListener.isAlive()){
				String request = client_request.remove(0); 
				Socket connectionSocket = sockets.remove(0);
				clientListener = createClientRequestListener(socket);
				clientListener.start();
				
				clientRequest(request, connectionSocket, numServer);			
			}
			
			if(!serverListener.isAlive()){
				serverListener = createServerRequestListener();	
				serverListener.start();
				sendOkays(numServer);
			}
	    }
	  }
    
    

    
   public static Thread createClientRequestListener(final ServerSocket socket){
    	Thread t = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Socket connectionSocket = null;
					connectionSocket = socket.accept();
					final BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
					String s = new String(inFromClient.readLine());
					client_request.add(s);
					sockets.add(connectionSocket);	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		return t;
    }
    
    // create listener for server -> server
    // Listen for if a message comes to this server - listens for this ports
    public static Thread createServerRequestListener(){
 	    Thread t = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					ServerSocket socket = null;
			    	Socket tcpsocket;
			    	int myPort = serverPorts.get(myID - 1);
			    	
		 			socket = new ServerSocket(myPort);
		 			tcpsocket = socket.accept();
		 			final ObjectInputStream ois = new ObjectInputStream(tcpsocket.getInputStream());
					Message msg = (Message)ois.readObject();
					req_queue[msg.getID()] = msg.getTime();
					// maybe update inventory
				} catch (ClassNotFoundException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		return t;
    }
    
    // parse Inventory file
    public static void parseInventory(String inventoryPath){
    	
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
	    } catch (NumberFormatException | IOException e1) {
	    	// TODO Auto-generated catch block
	    	e1.printStackTrace();
	    }	
    }
    
    public static void sendMsg(int id, Message req) throws IOException{
    	Socket tcpSocket = new Socket(InetAddress.getByName(ips.get(id)), serverPorts.get(id));
	    ObjectOutputStream oos = new ObjectOutputStream(tcpSocket.getOutputStream());
	    try {
			oos.writeObject(req);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    tcpSocket.close();
    }
    
    public static void sendOkays(int numServer){
    	int myTime = req_queue[myID];
    	for(int i = 0; i < numServer; ++i){
    		if(i == myID)
    			continue;
    		if(req_queue[i] < myTime){
    			boolean okay = true;
    			Message msg = new Message(okay, quantities);
    			try {
					sendMsg(i, msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    }
    
    // send requests to all the servers
    public static void broadcastMsg(int numServer){
    	try {
            // broadcast message
    		for(int i = 0; i < numServer; i++){
    			if(i == myID)
    				continue;
    			Message req = new Message(req_queue[myID], supplies, quantities, myID);
    			sendMsg(i, req);
    		}
    		// probably do it differently
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    // check if you can access the CS
    public static boolean okayCS(int numServer){
		for(int i = 0; i < numServer; ++i){
			if(req_queue[i] < clock.getValue(myID))
				return false;
			if(req_queue[i] == clock.getValue(myID) && i < myID)
				return false;
		}
		return true;
    }
    
    public static void listenForOkays(int numServer){
    	for(int i = 0; i < numServer; ++i){
    		final Socket tcpSocket;
			try {
				tcpSocket = new Socket(InetAddress.getByName(ips.get(i)), serverPorts.get(i));
				final ObjectInputStream ois = new ObjectInputStream(tcpSocket.getInputStream());
		    	new Thread(new Runnable(){
					public void run() {
						try {
							Message msg = (Message)ois.readObject();
							req_queue[msg.getID()] = msg.getTime();
							// requestCS is used only for changes in inventory
							quantities = msg.getQuantities();
						} catch (ClassNotFoundException | IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}		
		    	});
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
    	}
    }
    
    // send a message to all servers 
	// wait for acceptance from all
    public synchronized static void requestCS(int numServer){
    	clock.tick();
    	req_queue[myID] = clock.getValue(myID);
    	broadcastMsg(numServer);
    	listenForOkays(numServer);
    	while(!okayCS(numServer)){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    public synchronized static void releaseCS(int numServer){
    	req_queue[myID] = Integer.MAX_VALUE;
    	broadcastMsg(numServer);
    }
    
    public static String clientRequest(String order, Socket connectionSocket, int numServer){
    	// TODO: handle request from client
    	if(printStatements)
    		System.out.println("Dealing with Client Request");
	    
    	DataOutputStream outToClient = null;
		try {
			outToClient = new DataOutputStream(connectionSocket.getOutputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	String rString = order;
    	String[] tokens = order.split(" ");
    	Integer orderID = orderIDinit.get();
    	if(tokens[0].equals("purchase")){
    	//	requestCS(numServer);
    		
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
				releaseCS(numServer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
				rString = "Order " + tokens[1] + " is canceled";
				int supplyIndex = supplies.indexOf(myProductName.get(indexID));
				quantities.set(supplyIndex, Integer.parseInt(myOrder.get(indexID)) + quantities.get(supplyIndex));
				orderIDs.remove(indexID);
			    userName.remove(indexID);
			    myProductName.remove(indexID);
			    myOrder.remove(indexID);
				
			}
			//System.out.println("Sending data");
			try {
				outToClient.writeBytes(rString + '\n');
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(printStatements)
				System.out.println("Sent");
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
        if(printStatements)
          System.out.println("Completed Client Request Processing");
		return rString;
    }
}
