public class DirectClock {

	public int[] clock;
	int myId;

	public DirectClock (int numProc, int id ) {
		myId = id ;
		clock = new int [numProc];
		for ( int i = 0; i < numProc; i++) clock [ i] = 0;
		clock[myId] = 1;
	}
	
	public int getValue(int i) {
		return clock[i] ;
	}

	public void tick () {
		clock[myId]++;
	}
	
	public void seridAction () { 
		// sentvalue = clock [myld];
		tick ();
	}
	
	public void receiveAction (int sender , int sentvalue ) {
		clock[sender] = Math.max( clock[sender], sentvalue );
		clock[myId] = Math.max( clock[myId], sentvalue) + 1;
	}
}