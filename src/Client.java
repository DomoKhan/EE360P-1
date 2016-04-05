import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
  public static void main (String[] args) {
    String hostAddress;
    int tcpPort;
    int udpPort;
    int len = 2048;
    byte[] rbuffer = new byte[len];

    if (args.length != 3) {
      System.out.println("ERROR: Provide 3 arguments");
      System.out.println("\t(1) <hostAddress>: the address of the server");
      System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
      System.out.println("\t(3) <udpPort>: the port number for UDP connection");
      System.exit(-1);
    }

    hostAddress = args[0];
    tcpPort = Integer.parseInt(args[1]);
    udpPort = Integer.parseInt(args[2]);
    //overall use the same code over and over for all types...takes care of it in the server side.
    
    Scanner sc = new Scanner(System.in);
    try{
      //  System.out.println("Starting");
    	InetAddress ia = InetAddress.getByName(hostAddress);
        /* UDP socket and packets */
    	DatagramSocket datasocket = new DatagramSocket(); 
    	DatagramPacket sPacket, rPacket;
        /* TCP socket */
        
      //  System.out.println("Processing Lines:");
        Socket tcpSocket;
        DataOutputStream outToServer;
        BufferedReader inFromServer;
	    while(sc.hasNextLine()) {
	      
	      String cmd = sc.nextLine();
	      String[] tokens = cmd.split(" ");
	      
	      if (tokens[0].equals("purchase")) {
         //       System.out.println("Entered purchase");
	        // TODO: send appropriate command to the server and display the
	        // appropriate responses form the server
	    	  //convert the input string to bytes to send to server
	    	  byte[] buffer = new byte[cmd.length()];
	    	  buffer = cmd.getBytes("UTF-8");
	    	  //if UDP use the udp port number
	    	  if(tokens[4].equals("U")){
           //               System.out.println("Doing UDP");
	    		  sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
           //               System.out.println("Sending packet");
	    		  datasocket.send(sPacket); 
		//	  System.out.println("Packet Sent");
	    		  rPacket = new DatagramPacket(rbuffer, rbuffer.length);
                      
           //           System.out.println("Recieving packet");
	              datasocket.receive(rPacket);
          
	              String retstring = new String(rPacket.getData(), 0,
	            			rPacket.getLength());
	              System.out.println("Received from Server:" + retstring);
	    	  }
	    	  else{
	    		  tcpSocket = new Socket(hostAddress, tcpPort);
	    	      outToServer = new DataOutputStream(tcpSocket.getOutputStream());	
	    	      inFromServer = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream())); 
           //           System.out.println("Doing TCP");
                      
           //           System.out.println("Sending packet");
                      outToServer.writeBytes(cmd + '\n');
           //           System.out.println("Recieving packet");
                    String retstring = inFromServer.readLine();
	                System.out.println("Received from Server:" + retstring);
	    	  }
	      } else if (tokens[0].equals("cancel")) {
	        // TODO: send appropriate command to the server and display the
	        // appropriate responses form the server
	    	  byte[] buffer = new byte[cmd.length()];
	    	  buffer = cmd.getBytes("UTF-8");
	    	  if(tokens[2].equals("U")){
	    		  sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
	    		  datasocket.send(sPacket); 
	    		  rPacket = new DatagramPacket(rbuffer, rbuffer.length);
	              datasocket.receive(rPacket);
	              String retstring = new String(rPacket.getData(), 0,
	            			rPacket.getLength());
	              System.out.println("Received from Server:" + retstring);
	    	  }
	    	  else{
	    		  tcpSocket = new Socket(hostAddress, tcpPort);
	    	      outToServer = new DataOutputStream(tcpSocket.getOutputStream());	
	    	      inFromServer = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream())); 
	    	//	  System.out.println("Doing TCP");
                  
           //       System.out.println("Sending packet");
                  outToServer.writeBytes(cmd + '\n');
            //      System.out.println("Recieving packet");
                 
                  String retstring = inFromServer.readLine();
                System.out.println("Received from Server:" + retstring);
	    	  }
	      } else if (tokens[0].equals("search")) {
	        // TODO: send appropriate command to the server and display the
	        // appropriate responses form the server
	    	  byte[] buffer = new byte[cmd.length()];
	    	  buffer = cmd.getBytes("UTF-8");
	    	  if(tokens[2].equals("U")){
	    		  sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
	    		  datasocket.send(sPacket); 
	    		  rPacket = new DatagramPacket(rbuffer, rbuffer.length);
	              datasocket.receive(rPacket);
	              String retstring = new String(rPacket.getData(), 0,
	            			rPacket.getLength());
	              System.out.println("Received from Server:" + retstring);
	    	  }
	    	  else{
	    		  tcpSocket = new Socket(hostAddress, tcpPort);
	    	      outToServer = new DataOutputStream(tcpSocket.getOutputStream());	
	    	      inFromServer = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream())); 
	    //		  System.out.println("Doing TCP");
                  
         //         System.out.println("Sending packet");
                  outToServer.writeBytes(cmd + '\n');
         //         System.out.println("Recieving packet");
                 
                  String retstring = inFromServer.readLine();
                  if(! retstring.contains("No order found")){
	                  String[] breakRetString = retstring.split(" ");
	                  int size = breakRetString.length;
	                  retstring = new String();
	                  for(int i=0; i<size; i+=3){
	                  	retstring += breakRetString[i]+ " " + breakRetString[i+1] + " " + breakRetString[i+2] + "\n";
	                  }
                  }
                System.out.println("Received from Server:" + retstring);
	    	  }
	      } else if (tokens[0].equals("list")) {
	        // TODO: send appropriate command to the server and display the
	        // appropriate responses form the server
	    	  byte[] buffer = new byte[cmd.length()];
	    	  buffer = cmd.getBytes("UTF-8");
	    	  if(tokens[1].equals("U")){
	    		  sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
	    		  datasocket.send(sPacket); 
	    		  rPacket = new DatagramPacket(rbuffer, rbuffer.length);
	              datasocket.receive(rPacket);
	              String retstring = new String(rPacket.getData(), 0,
	            			rPacket.getLength());
	              System.out.println("Received from Server:" + retstring);
	    	  }
	    	  else{
	    		  tcpSocket = new Socket(hostAddress, tcpPort);
	    	      outToServer = new DataOutputStream(tcpSocket.getOutputStream());	
	    	      inFromServer = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream())); 
	    	//	  System.out.println("Doing TCP");
                  
          //        System.out.println("Sending packet");
                  outToServer.writeBytes(cmd + '\n');
          //        System.out.println("Recieving packet");
                String retstring = inFromServer.readLine();
                String[] breakRetString = retstring.split(" ");
                int size = breakRetString.length;
                retstring = new String();
                for(int i=0; i<size; i+=2){
                	retstring += breakRetString[i]+ " " + breakRetString[i+1] + "\n";
                }
                System.out.println("Received from Server:" + retstring);
	    	  }
	      } else {
	        System.out.println("ERROR: No such command");
	      }
	    }
	    datasocket.close();

    }
    catch (Exception e){
      e.printStackTrace();    	
    }

  }
}
