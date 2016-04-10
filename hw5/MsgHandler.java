import java.io.*; 
import java.util.*;


public interface MsgHandler {
	public void handleMsg(Msg m, int srcId , String tag ); 
	
	public Msg receiveMsg ( int fromId ) throws IOException ; 
	
	public void mySignal ( ) ; 
	
	public void startListening ( ) ;

	public void executeMsg(Msg m);
}