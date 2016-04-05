import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class Server {
  public static void main (String[] args) throws IOException{
    //port numbers from args
	int tcpPort = Integer.parseInt(args[0]);
    int udpPort = Integer.parseInt(args[1]);
    String fileName = args[2];
    //to keep track of the store inventory (read in from inventory.txt and modified)
    ArrayList<String> supplies = new ArrayList<String>();
    ArrayList<Integer> quantities = new ArrayList<Integer>();
    
    //to keep track of what orders people have placed (from client)
    ArrayList<Integer> orderIDs = new ArrayList<Integer>();
    ArrayList<String> userName = new ArrayList<String>();
    ArrayList<String> myProductName = new ArrayList<String>();
    ArrayList<String> myOrder = new ArrayList<String>();

	//ensures there are the 3 arguments provided
    if (args.length != 3) {
      System.out.println("ERROR: Provide 3 arguments");
      System.out.println("\t(1) <tcpPort>: the port number for TCP connection");
      System.out.println("\t(2) <udpPort>: the port number for UDP connection");
      System.out.println("\t(3) <file>: the file of inventory");

      System.exit(-1);
    }
    
   

    // parse the inventory file
    try {
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    String line;
		while(null != (line = br.readLine())){
			String[] tokens = line.split(" ");
			if(tokens.length != 2){
				throw new IOException();
			}
			supplies.add(tokens[0]);
			quantities.add(Integer.parseInt(tokens[1]));
		}
	} catch (NumberFormatException | IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
    ArrayList<ArrayList<String>> s_lists = new ArrayList<ArrayList<String>>();
    s_lists.add(supplies);
    s_lists.add(userName);
    s_lists.add(myProductName);
    s_lists.add(myOrder);
    ArrayList<ArrayList<Integer>> i_lists= new ArrayList<ArrayList<Integer>>();
    i_lists.add(quantities);
    i_lists.add(orderIDs);
    Protocol socket = new Protocol(udpPort, tcpPort, s_lists, i_lists);
	
    boolean first = true;
    // TODO: handle request from clients
    //do this infinitely
    ServerSocket tcpSocket = new ServerSocket(tcpPort);
    DatagramSocket datasocket = new DatagramSocket(udpPort);
    Thread t1 = new Thread();
    Thread t2 = new Thread();
    while(true){
		try {
			if(first){
			    t1 = new Thread(new TCP(socket, tcpSocket));
			    t2 = new Thread(new UDP(socket, datasocket));
				t1.start();
				t2.start(); 
				first = false;
			}
			if(!t1.isAlive()){
				//socket.tcp(tcpSocket);
				t1 = new Thread(new TCP(socket, tcpSocket));
				t1.start();
			}
			if(!t2.isAlive()){
				//socket.udp(datasocket);
				t2 = new Thread(new UDP(socket, datasocket));
				t2.start();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   	   // System.out.println("1 round done");
    } 
    
  }
}
