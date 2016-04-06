import java.util.ArrayList;
import java.util.Scanner;

public class Client {
  public static void main (String[] args) {
    Scanner sc = new Scanner(System.in);
    int numServer = sc.nextInt();
    ArrayList<Integer> ips = new ArrayList<Integer>();
    ArrayList<Integer> ports = new ArrayList<Integer>();
    
    for (int i = 0; i < numServer; i++) {
      // TODO: parse inputs to get the ips and ports of servers
	  String ip_port = sc.next();
      String[] nums = ip_port.split(":");
      assert nums.length == 2;
      ips.add(Integer.parseInt(nums[0]));
      ports.add(Integer.parseInt(nums[1]));
    }

    while(sc.hasNextLine()) {
      String cmd = sc.nextLine();
      String[] tokens = cmd.split(" ");

      if (tokens[0].equals("purchase")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
    	  // Psuedo
    	   	// while( not connected)
    	    // 		connect to current choice
    	    //      if(100 ms pass))
    	    //         send 
    	    //      else 
    	    //        restart with next one 
      } else if (tokens[0].equals("cancel")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
      } else if (tokens[0].equals("search")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
      } else if (tokens[0].equals("list")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
      } else {
        System.out.println("ERROR: No such command");
      }
    }
  }
}
