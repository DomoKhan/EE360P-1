import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Protocol{
	private int udpPort;
	private int tcpPort;
	private ArrayList<ArrayList<String>> s_list;
	private ArrayList<ArrayList<Integer>> i_list;
    ArrayList<String> supplies = new ArrayList<String>();
    ArrayList<Integer> quantities = new ArrayList<Integer>();
    
    //to keep track of what orders people have placed (from client)
    ArrayList<Integer> orderIDs = new ArrayList<Integer>();
    ArrayList<String> userName = new ArrayList<String>();
    ArrayList<String> myProductName = new ArrayList<String>();
    ArrayList<String> myOrder = new ArrayList<String>();
    AtomicInteger orderIDinit;
    
	public Protocol(int udpPort, int tcpPort, ArrayList<ArrayList<String>> s_list, ArrayList<ArrayList<Integer>> i_list){
		this.udpPort = udpPort;
		this.tcpPort = tcpPort;
		this.s_list = s_list;
		this.i_list = i_list;
		
		supplies= s_list.get(0);
		quantities= i_list.get(0);
		orderIDs= i_list.get(1);
		userName= s_list.get(1);
		myProductName= s_list.get(2);
		myOrder= s_list.get(3);
		orderIDinit = new AtomicInteger(1);
	}
	
	/*
	public synchronized void udp( DatagramSocket datasocket){
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		
		class udpRunning implements Runnable{
			public void run(){
				DatagramPacket datapacket,returnpacket;
				int len = 2048;
				byte[] buf = new byte[len];
				Integer orderID = orderIDinit.get();
				try {
					datapacket = new DatagramPacket(buf, buf.length);
				    // System.out.println("Recieving Packet");		
					datasocket.receive(datapacket);
					String s = new String(buf, "UTF-8");
					//System.out.println("Packet Recieved");	
					String[] tokens = s.split(" ");
				    // System.out.println("running");	
					if(tokens[0].equals("purchase")){
			            //  System.out.println("Processing purchase"); 
						//How we get the data:
						//purchase <user-name> <product-name> <quantity> T|U 
						//tokens[0] = 'purchase'     tokens[1] = user name
						//tokens[2] = product		 tokens[2] = quantity
						String rString = s;
						int indexSupplies = supplies.indexOf(tokens[2]);
						
						//checks if we sell this type of item
						if(indexSupplies < 0){
							rString = "Not Available - We do not sell this product";
						}
						
						//checks if we have enough of the item
						else if(quantities.get(indexSupplies) < Integer.parseInt(tokens[3])){
							rString = "Not Available - Not enough items";
						}
						
						//modifies the count, gives the userID, add to the order lists, write the appropriate return string
						else {
							int newQuant = quantities.get(indexSupplies) - Integer.parseInt(tokens[3]);
							quantities.set(indexSupplies, newQuant);
							orderIDinit.getAndIncrement();
							rString = "You order has been placed, " + orderID + " " + rString;
							orderIDs.add(orderID);
						    userName.add(tokens[1]);
						    myProductName.add(tokens[2]);
						    myOrder.add(tokens[3]);
						}
						
						//send the data back to the client
						byte[] buffer = rString.getBytes("UTF-8");
						//System.out.println("Sending packet");
						returnpacket = new DatagramPacket(
								buffer,
								buffer.length,
								datapacket.getAddress(),
								datapacket.getPort());
						datasocket.send(returnpacket);
						//System.out.println("Sent");
					}
		
					//if this is a cancel
					else if(tokens[0].equals("cancel")){
						//cancel <order-id> T|U
						String rString = s;
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
						byte[] buffer = rString.getBytes("UTF-8");
						returnpacket = new DatagramPacket(
								buffer,
								buffer.length,
								datapacket.getAddress(),
								datapacket.getPort());
						datasocket.send(returnpacket);
					}
		
					//if this is a search
					else if(tokens[0].equals("search")){
						//search <user-name> T|U
						String rString = new String();
						//checks if the user is in the database
						int indexName = userName.indexOf(tokens[1]);
						if(indexName < 0){
							rString = "No order found for " + tokens[1];
						}
						//if in the database...iterates through all users in order to see all instances of the user
						else {
							int i = 0;
							//System.out.println(tokens[1] + "b");
							for(String currUser: userName){
						   //     System.out.println(currUser+"x");
								if(currUser.equals(tokens[1])){
							//		System.out.println("Entered");
									//return string: <order-id>, <product-name>, <quantity>
									rString += orderIDs.get(i) + ", " + myProductName.get(i) + ", " + myOrder.get(i) + "\n";
								}
								i++;
							}
						}
						byte[] buffer = rString.getBytes("UTF-8");
						System.out.println(rString);
						returnpacket = new DatagramPacket(
								buffer,
								buffer.length,
								datapacket.getAddress(),
								datapacket.getPort());
						datasocket.send(returnpacket);
					}
		
					//if this is a list
					else if(tokens[0].equals("list")){
						//list T|U
						//goes through all items in supplies and quantities to list out the current inventory
						int sizeInv = supplies.size();
						String rString = new String();
						for(int i=0; i<sizeInv; i++){
							rString += supplies.get(i) + " " + quantities.get(i) + "\n";
						}
						System.out.print(rString);
						byte[] buffer = rString.getBytes("UTF-8");
						returnpacket = new DatagramPacket(
								buffer,
								buffer.length,
								datapacket.getAddress(),
								datapacket.getPort());
						datasocket.send(returnpacket);
					}
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
			
		
	
	
	public synchronized void tcp(ServerSocket tcpSocket) {
		//	System.out.println("begin tcp");
		
		class tcpRunning implements Runnable{
			public void run(){
				Socket connectionSocket;
				Integer orderID = orderIDinit.get();
				try {
					connectionSocket = tcpSocket.accept();
				   	BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			    	DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			    	String s = new String(inFromClient.readLine());
			    	//System.out.println("Packet Recieved");	
					String[] tokens = s.split(" ");
			      //  System.out.println("running");	
					if(tokens[0].equals("purchase")){
				         //       System.out.println("Processing purchase"); 
						//How we get the data:
						//purchase <user-name> <product-name> <quantity> T|U 
						//tokens[0] = 'purchase'     tokens[1] = user name
						//tokens[2] = product		 tokens[2] = quantity
						String rString = s;
						int indexSupplies = supplies.indexOf(tokens[2]);
						//checks if we sell this type of item
						if(indexSupplies < 0){
							rString = "Not Available - We do not sell this product";
						}
						
						//checks if we have enough of the item
						else if(quantities.get(indexSupplies) < Integer.parseInt(tokens[3])){
							rString = "Not Available - Not enough items";
						}
						
						//modifies the count, gives the userID, add to the order lists, write the appropriate return string
						else {
							int newQuant = quantities.get(indexSupplies) - Integer.parseInt(tokens[3]);
							quantities.set(indexSupplies, newQuant);
							orderIDinit.getAndIncrement();
							rString = "You order has been placed, " + orderID + " " + rString;
							orderIDs.add(orderID);
						    userName.add(tokens[1]);
						    myProductName.add(tokens[2]);
						    myOrder.add(tokens[3]);
						}
				
						//send the data back to the client
						//System.out.println("Sending data");
						outToClient.writeBytes(rString + '\n');
						//System.out.println("Sent");
						//if this is a cancel
					}
					else if(tokens[0].equals("cancel")){
						//cancel <order-id> T|U
						String rString = s;
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
						outToClient.writeBytes(rString + '\n');
						//System.out.println("Sent");
					}
			
					//if this is a search
					else if(tokens[0].equals("search")){
						//search <user-name> T|U
						String rString = s;
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
								}
								i++;
							}
						}
						//System.out.println("Sending data");
						outToClient.writeBytes(rString + '\n');
						//System.out.println("Sent");
					}
			
					//if this is a list
					else if(tokens[0].equals("list")){
						//list T|U
						//goes through all items in supplies and quantities to list out the current inventory
						int sizeInv = supplies.size();
						String rString = new String();
						for(int i=0; i<sizeInv; i++){
							rString += supplies.get(i) + " " + quantities.get(i) + " ";
						}
						System.out.print(rString);
						//System.out.println("Sending data");
						outToClient.writeBytes(rString + '\n');
						//System.out.println("Sent");
					}
				}catch(IOException e){
				System.out.println("Could not");
				}
			}
		}
	}

*/
}
				
		
		
		
