import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCP implements Runnable {
	Protocol p;
	ServerSocket tcpSocket;
	TCP(Protocol p, ServerSocket datasocket){
		this.p = p;
		tcpSocket = datasocket;
		//this.run();
	}
	@Override
	public void run(){
		Socket connectionSocket;
		Integer orderID = p.orderIDinit.get();
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
				int indexSupplies = p.supplies.indexOf(tokens[2]);
				//checks if we sell this type of item
				if(indexSupplies < 0){
					rString = "Not Available - We do not sell this product";
				}
				
				//checks if we have enough of the item
				else if(p.quantities.get(indexSupplies) < Integer.parseInt(tokens[3])){
					rString = "Not Available - Not enough items";
				}
				
				//modifies the count, gives the userID, add to the order lists, write the appropriate return string
				else {
					int newQuant = p.quantities.get(indexSupplies) - Integer.parseInt(tokens[3]);
					p.quantities.set(indexSupplies, newQuant);
					p.orderIDinit.getAndIncrement();
					if(!p.orderIDs.contains(orderID)){
						p.orderIDs.add(orderID);
					}
					else{
						orderID ++;
						p.orderIDs.add(orderID);
					}
					rString = "You order has been placed, " + orderID + " " + rString;
				    p.userName.add(tokens[1]);
				    p.myProductName.add(tokens[2]);
				    p.myOrder.add(tokens[3]);
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
				int indexID = p.orderIDs.indexOf(Integer.parseInt(tokens[1]));
				
				//if orderID is not there return not found
				if(indexID < 0){
					rString = tokens[1] + " not found, no such order";
				}
				
				//else remove order from all of the lists...lol
				else {
					rString = "Order " + tokens[1] + " is canceled";
					int supplyIndex = p.supplies.indexOf(p.myProductName.get(indexID));
					p.quantities.set(supplyIndex, Integer.parseInt(p.myOrder.get(indexID)) + p.quantities.get(supplyIndex));
					p.orderIDs.remove(indexID);
				    p.userName.remove(indexID);
				    p.myProductName.remove(indexID);
				    p.myOrder.remove(indexID);
					
				}
				//System.out.println("Sending data");
				outToClient.writeBytes(rString + '\n');
				//System.out.println("Sent");
			}
	
			//if this is a search
			else if(tokens[0].equals("search")){
				//search <user-name> T|U
				String rString = new String();
				//checks if the user is in the database
				int indexName = p.userName.indexOf(tokens[1]);
				if(indexName < 0){
					rString = "No order found for " + tokens[1];
				}
				
				//if in the database...iterates through all users in order to see all instances of the user
				else {
					int i = 0;
					for(String currUser: p.userName){
						if(currUser.equals(tokens[1])){
							//return string: <order-id>, <product-name>, <quantity>
							rString += p.orderIDs.get(i) + ", " + p.myProductName.get(i) + ", " + p.myOrder.get(i) + " ";
							//rString = p.orderIDs.get(i) + ", " + p.myProductName.get(i) + ", " + p.myOrder.get(i) + " ";
							//outToClient.writeBytes(rString + '\n');
						}
						//outToClient.writeByte('\n');
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
				int sizeInv = p.supplies.size();
				String rString = new String();
				for(int i=0; i<sizeInv; i++){
					rString += p.supplies.get(i) + " " + p.quantities.get(i) + " ";
					//rString = p.supplies.get(i) + " " + p.quantities.get(i) + " ";
					//outToClient.writeBytes(rString + '\n');
				}
				//outToClient.writeByte('\n');
				//System.out.print(rString);
				//System.out.println("Sending data");
				outToClient.writeBytes(rString + '\n');
				//System.out.println("Sent");
			}
		}catch(IOException e){
			//System.out.println("Could not");
		}
	}
}

