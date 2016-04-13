import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;


public class Server {
	final static boolean printStatements = false;
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
    static boolean inCS = false;
    static int okaySent = 0;
    static int releasedReceived = 0;
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
	    clock = new DirectClock(numServer, myID - 1);
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
			if(!clientListener.isAlive() && clientListener.getState() != Thread.State.NEW){
				if(printStatements)
					System.out.println("Heard Client Request");
				final String request = client_request.remove(0); 
				final Socket connectionSocket = sockets.remove(0);
				final int tempServer = numServer;
				clientListener = createClientRequestListener(socket);
				clientListener.start();
				new Thread(new Runnable(){
					@Override
					public void run() {
						// TODO Auto-generated method stub
						clientRequest(request, connectionSocket, tempServer);
					}
					
				}).start();
							
			}		
			if(!serverListener.isAlive() && serverListener.getState() != Thread.State.NEW){
				if(printStatements)
					System.out.println("Heard Server Request");
				printQueue();
				serverListener = createServerRequestListener();	
				serverListener.start();
				final int tempServer = numServer;
				new Thread(new Runnable(){
					@Override
					public void run() {
						// TODO Auto-generated method stub
						sendOkays(tempServer);
					}
				}).start();
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
		 			if(printStatements)
		 				System.out.println("Server Socket Awaiting acceptance");
		 			tcpsocket = socket.accept();
		 			if(printStatements)
		 				System.out.println("Server Socket Accepted");
		 			final ObjectInputStream ois = new ObjectInputStream(tcpsocket.getInputStream());
					Message msg = (Message)ois.readObject();
					req_queue[msg.getID() - 1] = msg.getTime();
					// maybe update inventory
					quantities = msg.getQuantities();
					if(msg.getOkay() == true) // release is received
						++releasedReceived;
					socket.close();
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

	    try {
	    	Socket tcpSocket = new Socket(InetAddress.getByName(ips.get(id)), serverPorts.get(id));
		    ObjectOutputStream oos = new ObjectOutputStream(tcpSocket.getOutputStream());
			oos.writeObject(req);
			tcpSocket.close();
		} 
	    catch(ConnectException ex){
	    	req_queue[id] = Integer.MAX_VALUE; // can't connect so ignore it 
	    	System.out.println("Can't Connect to server");
	    }
	    catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
    }
    
    public static void sendOkays(int numServer){
    	if(printStatements)
    		System.out.println("Entering okay");
    	printQueue();
    	int myTime = req_queue[myID - 1];
    	
    	for(int i = 0; i < numServer; ++i){
    		
    		if(i == (myID - 1))
    			continue;
    		if(req_queue[i] < myTime){
    			if(printStatements)
    				System.out.println("Sending okay");
    			++okaySent;
    			boolean okay = true;
    			Message msg = new Message(okay, quantities, myID);
    			try {
					sendMsg(i, msg);
				} 
    			catch(ConnectException ex){
    		    	System.out.println("Can't Connect to server");
    		    }
    			catch (IOException e) {
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
    			if(i == (myID - 1))
    				continue;
    			Message req = new Message(req_queue[myID - 1], supplies, quantities, myID);
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
    	if(printStatements){
	    	System.out.println("okaySent " + okaySent);
	    	System.out.println("Released " + releasedReceived);
    	}
		for(int i = 0; i < numServer; ++i){
			if(req_queue[i] < clock.getValue(myID - 1)){
				System.out.println("Greater Clock");
				return false;
			}
			if(req_queue[i] == clock.getValue(myID - 1) && i < (myID - 1)){
				System.out.println("Same Clock");
				return false;
			}
			if(okaySent > releasedReceived){
				System.out.println("Not enough releases");
				return false;
			}
		}
		if(printStatements)
			System.out.println("Entering CS");
		return true;
    }
    

    /* resets the req_queue so that the queue will be filled with
     * achknowledgement time stamps rather than local copies
     */
    public static void resetForOkays(int numServer){
    	for(int i = 0; i < numServer; ++i){
    		if(i == (myID - 1))
    			continue;
    		if(req_queue[i] == Integer.MAX_VALUE)
    			req_queue[i] = -1;
    	}
    }
    
    // send a message to all servers 
	// wait for acceptance from all
    public synchronized static void requestCS(int numServer){
    	if(printStatements)
    		System.out.println("Requesting CS");
    	clock.tick();
    	req_queue[myID - 1] = clock.getValue(myID - 1);
    	resetForOkays(numServer);
    	broadcastMsg(numServer);
    	if(printStatements)
    		System.out.println("Broadcasted Request");
    	printQueue();
    	while(!okayCS(numServer)){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	inCS = true;
    }
    
    
    public synchronized static void releaseCS(int numServer){
    	if(printStatements)
    		System.out.println("Releasing CS");
    	req_queue[myID - 1] = Integer.MAX_VALUE;
    	inCS = false;
    	broadcastMsg(numServer);
    	if(printStatements)
    		System.out.println("Broadcasted release");
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
    		requestCS(numServer);
    		
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
				printQueue();
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
				printQueue();
				requestCS(numServer);
				rString = "Order " + tokens[1] + " is canceled";
				int supplyIndex = supplies.indexOf(myProductName.get(indexID));
				quantities.set(supplyIndex, Integer.parseInt(myOrder.get(indexID)) + quantities.get(supplyIndex));
				orderIDs.remove(indexID);
			    userName.remove(indexID);
			    myProductName.remove(indexID);
			    myOrder.remove(indexID);
			    releaseCS(numServer);
				
			}
			System.out.println("Sending data");
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
    
    public static void printQueue(){
    	/*for(int i = 0; i < req_queue.length; ++i){
    		System.out.print(req_queue[i] + " ");
    	}
    	System.out.println();*/
    }
}
