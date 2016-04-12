import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
  static boolean printStatements = false;
  static ArrayList<String> ips;
  static ArrayList<Integer> ports;
  public static void main (String[] args) {
    Scanner sc = new Scanner(System.in);
    int numServer = sc.nextInt();
    ips = new ArrayList<String>();
    ports = new ArrayList<Integer>();
   
    
    for (int i = 0; i < numServer; i++) {
	      // TODO: parse inputs to get the ips and ports of servers
	      String ip_port = sc.next();
	      String[] nums = ip_port.split(":");
	      assert nums.length == 2;
	      ips.add((nums[0]));
	      ports.add(Integer.parseInt(nums[1]));
	}

    BufferedReader inFromServer;
    while(sc.hasNextLine()) {
      String cmd = sc.nextLine();
      if (cmd.equals(""))
    	  continue;
      String[] tokens = cmd.split(" ");
      byte[] buffer = new byte[cmd.length()];
	  try {
		buffer = cmd.getBytes("UTF-8");
	  } catch (UnsupportedEncodingException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	  }
      
      if (tokens[0].equals("purchase")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
    	  String ret = tryToConnectToServer(numServer, cmd);
    	  System.out.println("Received from Server:\n" + ret);
      } else if (tokens[0].equals("cancel")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
    	  String ret = tryToConnectToServer(numServer, cmd);
    	  System.out.println("Received from Server:\n" + ret);
      } else if (tokens[0].equals("search")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
    	  String ret = tryToConnectToServer(numServer, cmd);
    	  if(! ret.contains("No order found")){
              String[] breakRetString = ret.split(" ");
              int size = breakRetString.length;
              ret = new String();
              for(int i=0; i<size; i+=3){
            	  ret += breakRetString[i]+ " " + breakRetString[i+1] + " " + breakRetString[i+2] + "\n";
              }
          }
    	  System.out.println("Received from Server:\n" + ret);
      } else if (tokens[0].equals("list")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
    	  String ret = tryToConnectToServer(numServer, cmd);
    	  String[] breakRetString = ret.split(" ");
          int size = breakRetString.length;
          ret = new String();
          for(int i=0; i<size; i+=2){
        	  ret += breakRetString[i]+ " " + breakRetString[i+1] + "\n";
          }
    	  System.out.println("Received from Server:\n" + ret);
      } else {
        System.out.println("ERROR: No such command" +"\n");
      }
    }
   
  }
  
  // Psuedo
 	// while( not connected)
  // 		connect to current choice
  //      if(100 ms pass))
  //         send 
  //      else 
  // li       restart with next one 
  private static String tryToConnectToServer(int numServer, String cmd){
	  int server = 0; // for going through closest ones
 	 boolean connected = false;
 	Socket tcpSocket;
 	DataOutputStream outToServer;
 	BufferedReader inFromServer;
 	 String retstring = "";
 	 while(server < numServer && !connected){
 		 try{
	    		 InetSocketAddress addr = new InetSocketAddress(ips.get(server), ports.get(server));
		   	  	 tcpSocket = new Socket();
		   	  	 
		   	  	 // 100 ms for reading from socket another 100ms for connect to socket
		   	  	// tcpSocket.setSoTimeout(100);
		   	  	 tcpSocket.connect(addr, 100);
		   	  	 
		   	  	 if(!tcpSocket.isConnected()){
                                      if(printStatements)
                                        System.out.println("TimeOut Occurred");
		   	  		 ++server;
		   	  		 continue;
		   	  	 }
		   	  	 // problem - say that it breaks while 
		   	  	 outToServer = new DataOutputStream(tcpSocket.getOutputStream());	
		   	  	 inFromServer = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream())); 
		//           System.out.println("Doing TCP");
		//           System.out.println("Sending packet");
		         outToServer.writeBytes(cmd + '\n');
		//           System.out.println("Recieving packet");
		         retstring = inFromServer.readLine();
		         connected = true;
	    	 }
 		 catch(SocketTimeoutException e ){
                      System.out.println("Socket Timeout Exception Occurred");
 			 connected = false;
 			 ++server;
 		 }
 		 catch(ConnectException ex){
 			connected = false;
			 ++server;
 		 }
 		 catch(IOException ex){
 			 connected = false;
 			 ++server;
 		 }
 	 }
       //System.out.println("Received from Server:" + retstring);
 	 return retstring;

  }
}