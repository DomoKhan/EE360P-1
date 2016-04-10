import java.io.*; 
import java.lang.*; 
import java.util.*;

public class Process implements MsgHandler {
	int N, myId ; 
	Linker comm;
	public Process (Linker initComm) {
		comm = initComm ; 
		myId = comm.getMyId ( );
		N= comm.getNumProc ( ) ;
	} 
	public synchronized void handleMsg(Msg m, int src , String tag ) { 
		
	} 
	public void sendMsg( int destId , Object... objects ) {
		Util.println ("Sending msg to"+ destId ) ; 
		comm.sendMsg( destId , Util.getArrayList ( objects ) ) ;
	} 
	public void broadcastMsg ( String tag , int msg) {
		for ( int i= 0; i < N; i++) if (i != myId) sendMsg( i , tag , msg ) ;
	} 
	public void sendToNeighbors ( String tag , int msg) {
		for ( int i: comm.neighbors )
			sendMsg( i , tag , msg ) ;
	} 
	public void relayToNeighbors ( int src , String tag , int msg) {
		for ( int i: comm.neighbors ) 
			if (i != src ) 
				sendMsg( i , tag , msg );
	} 
	public void multicast ( LinkedList<Integer> destIds , String tag , String msg) {
		for (int i: destIds)
			sendMsg(i,tag,msg) ;
	} 
	public boolean isNeighbor (int i) {
		return (comm.neighbors.contains(i)) ;
	} 
	public Msg receiveMsg (int fromId) {
		List<Object> recvdMssage = receiveMsgAsObjectList ( fromId );
		return new Msg(fromId, myId,(String) recvdMssage.get(0), Util.getLinkedList(recvdMssage));
	}
	public List<Object> receiveMsgAsObjectList(int fromId){
		return comm.receiveMsg(fromId).getMsgBuf();
	} 
	public synchronized void myWait() { 
		try {
			wait();
		}
		catch (InterruptedException e) {
			System.err.println(e);
		} 
	}
	public synchronized void mySignal(){
		notifyAll() ; 
	} 
	public void startListening(){
		for(int i=0; i<N; i++)
			if (i != myId)
				(new ListenerThread (i,comm)).start();
	}
	public static void println(String s){
		System.out.println(s);
	}
	@Override
	public void executeMsg(Msg m) {
		// TODO Auto-generated method stub
		
	}
}