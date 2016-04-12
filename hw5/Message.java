import java.util.ArrayList;
import java.io.Serializable;
public class Message implements Serializable{
	int timestamp;
	boolean changed;
	ArrayList<String> supplies;
    ArrayList<Integer> quantities;
    boolean okay;
    int id;
    boolean wantCS;
    Message(int timestamp, ArrayList<String> supplies, ArrayList<Integer> quantities, int id){
    	this.timestamp = timestamp;
    	this.supplies = supplies;
    	this.quantities = quantities;
    	this.id = id;
    	changed = false;
    	okay = false;
    }
    
    Message(boolean okay, ArrayList<Integer> quantities, int myID){
    	this.okay = okay;
    	timestamp = Integer.MAX_VALUE;
    	this.quantities = quantities;
    	this.id = myID;
    }
    
    public boolean getOkay(){
    	return okay;
    }
    
    public ArrayList<Integer> getQuantities(){
    	return quantities;
    }
    
    public int getID(){
    	return id;
    }
    
    public int getTime(){
    	return timestamp;
    }
    
    
    
    
}
