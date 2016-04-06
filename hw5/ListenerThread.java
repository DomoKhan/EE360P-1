import java.io.IOException;

public class ListenerThread extends Thread {
 	int channel; 
	MsgHandler process; 
	
	public ListenerThread (int channel , MsgHandler process ) {
		this.channel = channel; 
		this.process = process ;
	} 
	
	public void run () {
		while (true ) { 
			try {
				Msg m = process.receiveMsg( channel );
				process.handleMsg(m, m. getSrcId () , m.getTag()) ;
				process.mySignal() ; // automatic notification after every message
			} catch (IOException e ) {
				System . err . println ( e );
			} 
		} 
	} 
}