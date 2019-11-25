package algorithms;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import util.EditPath;

public class GlobalVar {


	//The id of most busy thread, the most heavy loaded thread
	static public int selectedthread;
	
	//The UBcost
	static public double UBCOST;

	// The Upper Bound Edit Path
	static public EditPath UB;
	
	/* The semaphore for the main thread
	 * it blocks the main thread
	 * it obliges the main thread to wait all the thread to be finished
	 */
	static public Semaphore sem;
	
	//the mutex waits until jobs have been stolen or given
	static public Semaphore mutex;

	//time constraint
	static public boolean timeconstraint=false;
	
	
	static public boolean memoryconstraint=false;
	
	//the table of theads
	static public GEDDFSThread[] tabthreads;

	
	// the table of thread variances
	static public double[] threadvariance;

	
	// the table of thread no of iterations
	static public double[] threadNoOfIterations;

	//the average work load
	public static double averageworkload;

	//the less busy thread
	public static int lightthread;


	//a lock when updating the UB
	public static final ReentrantLock ubupdatelock =  new ReentrantLock();
	
	//a lock when operating the loadbalancing
	public static final ReentrantLock loadbalancelock =  new ReentrantLock();
	


}
