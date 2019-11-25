package algorithms;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TimerTask;
import algorithms.Constants;
import algorithms.AscendingOrderNonStaticNodeComparator;
import util.EditPath;
import algorithms.NonStaticNode;
import algorithms.NonStaticTree;
import util.Node;

public class GEDDFSThread extends Thread{

	double idletime=0;	
	double starttime;
	double endtime;
	double cputime=0;
	NonStaticTree OPEN; // Tree that keeps track of unexplored nodes in the search tree
	private Node CurVertex; // Current node
	public int openCounterSize; // tracks the maximum size of the set OPEN
	public  int editPathCounter; // tracks number of edit paths
	public int nbexlporednode=0;
	public int maxopensize=0;
	public double noOfIterations = 0.0;
	public double varianceSum = 0.0;
	boolean debug;
	private int heuristicmethod;
	private boolean issolutionoptimal=false;
	private TimerTask task;
	public static int MunkresUB=0;
	public static int DummyUB=1;
	
	public double getIdletime() 
	{
		return idletime;
	}

	public double getCputime() 
	{
		return cputime;
	}

	public GEDDFSThread(int heuristic,boolean debug)
	{
		this.debug = debug;
		heuristicmethod = heuristic;
	}

	void SetRootNode(EditPath ROOT,AscendingOrderNonStaticNodeComparator MyNodeComparator)
	{
		OPEN = new NonStaticTree<EditPath>(ROOT,MyNodeComparator);	
	}

	public boolean isTimeconstraintover() 
	{
		return GlobalVar.timeconstraint;
	}

	private EditPath loop() throws FileNotFoundException 
	{
		boolean condition1, condition2;
		String threadFile = ""+Thread.currentThread().getName()+".csv";
		// Search in the OPEN tree the node that has the minimum cost (Pmin) and then delete it
		EditPath pmin=null;
		NonStaticNode<EditPath> pminNode=null;
		NonStaticNode<EditPath> CurNode=OPEN.root;
	
		// Main loop of the tree search
		while(true){

			if(GlobalVar.timeconstraint==true)
			{
				return FinishThread(false);			
			}
			
			ProcessHeavyThread(CurNode);
			pminNode=OPEN.pollFirstLowestCost(CurNode);	

			/* Condition1: Test if there is any child to explore from CurNode
			 * Condition2: Test if CurNode is ROOT node 
			*/
			condition1 = (pminNode == null);
			condition2 = ((pminNode == null)&&(CurNode.parent != null));
			while( condition1 && condition2)
			{
				// pminNode is null so CurNode has no children, thus get the parent of CurNode
				CurNode =OPEN.BackTrack(CurNode);
				// Get the cheapest Child Node. The node is removed from the tree
				pminNode=OPEN.pollFirstLowestCost(CurNode);	
				condition1 = (pminNode == null);
				condition2 = ((pminNode == null)&&(CurNode.parent != null));
			}

				if((pminNode == null) && (CurNode.parent == null) )
				{
					/* All the tree has been explored since pminNode is null and its CurNode is root.
					 * No more children to explore in the sequential program, it is the case of optimality.
					 * We have fully explore the tree. In this multi threaded  version, it means we need 
					 * to steal some jobs.
					 * Here we check if the thread elected to be stolen is the current thread.
					 * The selected threas is the most heavy loaded thread
					 * The selected thread should not enter here caus it risks to be block by the lock
					 */
					if(  GlobalVar.selectedthread  != this.IdThread )
					{
						// Only one thread at the time can steal jobs		
						if(GlobalVar.timeconstraint==true) return this.FinishThread(false);
						GlobalVar.loadbalancelock.lock();
						// Start a timer to see how long it takes to steal jobs
						this.starttime=System.currentTimeMillis();					
						// The lighted thread is a thread with no jobs
						GlobalVar.lightthread =this.IdThread;
						// Selected thread is the most busy thread
						GlobalVar.selectedthread  = findthemostloadedthread();
						// If the most busy and the less busy thread are the same then there is no more jobs to do
						if(GlobalVar.lightthread ==GlobalVar.selectedthread || GlobalVar.selectedthread==-1 ){
							return FinishWaitingThreadOptimality(true);					
						}
						// Compute the average load for all threads
						double avg =  GlobalVar.tabthreads[GlobalVar.selectedthread].OPEN.globalworkload; 
						//if the average load is 0, then we must end the thread
						if(avg == 0)
						{
							return FinishWaitingThreadOptimality(true);
						}
						
						try 
						{
							/* Now we know the most busy thread
							 * let us wait untill it gets the notification and it gives us some jobs
							 */
							GlobalVar.mutex.acquire();
						} 
						catch (InterruptedException e) 
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						/* No more jobs to do?
						 * It might be because the others threads have finished everything
						 * OR it might be because the selected thread has been stopped by the time constraint
						 */
						if(GlobalVar.timeconstraint==true)
						{
							/*we fail to exchange jobs
							 * Probably there is no jobs to do
							 */
							return FinishWaitingThreadOptimality(false);
						}
						

						if(GlobalVar.memoryconstraint==true)
						{
							/*we fail to exchange jobs
							 * Probably there is no jobs to do
							 */
							return FinishWaitingThreadOptimality(false);
						}
						
						// Get the next jobs
						pminNode=OPEN.pollFirstLowestCost(this.OPEN.root);	

						if(pminNode==null && this.ComputerAverageWorkLoad(false)==0)
						{
							/*we fail to exchange jobs
							 * Probably there is no jobs to do
							 */
							return FinishWaitingThreadOptimality(true);
						}
			
						if(pminNode==null)
						{
							/* We fail to exchange jobs
							 * Probably there is no jobs to do
							 */
							return FinishWaitingThreadOptimality(false);
						}
						
						/* The job stealing is over so let us 
						 * Check how long it takes
						 */
						this.endtime=System.currentTimeMillis();
						this.idletime+=endtime-starttime;
						
						/* The mutex was released I might have some jobs to do now
						 * Reset of the variable for the next
						 */
						GlobalVar.lightthread=-1;
						GlobalVar.selectedthread=-1;
						// We release the lock for the next thread
						GlobalVar.loadbalancelock.unlock();
					}
				}

				if(pminNode==null)
				{
					if( this.ComputerAverageWorkLoad(false)==0)
					{
						
						return FinishThread(true);
					}
					else
					{
						System.out.println("error= "+this.IdThread);
						System.out.println(" balancing=");
						ComputerAverageWorkLoad(true);		
					}				
				}
				if((pminNode == null) && (CurNode.parent == null))
				{
					return FinishThread(true);

				}
			
			pmin = pminNode.data;

			// Prune the search tree if pmin is higher than UBCOST
			if(pmin.getTotalCosts()+pmin.ComputeHeuristicCosts(heuristicmethod)<GlobalVar.UBCOST)
			{
				/* Generates all successors of node u in the search tree
				 * and add them to open
				 */
				this.nbexlporednode++;	
				maxopensize = Math.max(pminNode.children.size(), this.maxopensize);

				if(pmin.getUnUsedNodes1().size() > 0)
				{
					this.CurVertex=pmin.getNext();
					if(debug==true) System.out.println("Current Node="+this.CurVertex.getId());

					/* For all the nodes (w) that are not already explored in Pmin, add to (pmin) all 
					 * the substitutions of uk+1 with w 
					 */					
					LinkedList<Node>UnUsedNodes2= pmin.getUnUsedNodes2();
					for(int i=0;i<UnUsedNodes2.size();i++){
						EditPath newpath = new EditPath(pmin);
						Node w = UnUsedNodes2.get(i);
						newpath.addDistortion(this.CurVertex, w);
						AddTreeNode(pminNode,newpath,GlobalVar.UBCOST);
					}
					// Put in (pmin) the deletion of uk+1
					EditPath newpath = new EditPath(pmin);
					newpath.addDistortion(this.CurVertex, Constants.EPS_COMPONENT);
					AddTreeNode(pminNode,newpath,GlobalVar.UBCOST);
				}
				else
				{
					/* If k equals the size of G1, put in (pmin) all the insertions of nodes of G2 that were not used 
					 * during the matching
					 */
					EditPath newpath = new EditPath(pmin);
					newpath.complete();	
					double g = newpath.getTotalCosts();
					double h = newpath.ComputeHeuristicCosts(heuristicmethod);
					double f= g+h;

					/* Critical Section
					 * only best UB can be modified
					 * so condition must be check by only one thread at the time
					 */
					if(f<GlobalVar.UBCOST)
					{
						GlobalVar.ubupdatelock.lock();
						if(f<GlobalVar.UBCOST)
						{
							// Update the upper bound
							GlobalVar.UBCOST = f;
							GlobalVar.UB = newpath;
						}
						/* Release the lock for the next thread to check if the next thread will
						 *  have a better solution.
						 */
						GlobalVar.ubupdatelock.unlock();
					}
				}
			}
			// Next node to be explored
			CurNode=pminNode;
		}
	}



	private void ProcessHeavyThread(NonStaticNode<EditPath> CurNode) 
	{
		/* the current thread is the selected thread
		 * The one chosen to be the most busy
		 */
		if( this.IdThread==GlobalVar.selectedthread ){
			
			/* Let us start a timer to compute the idl cpu time
			 * If we are here it means we waste time
			 * The thread is not doing any tree exploration
			 */

			this.starttime=System.currentTimeMillis();

			/* a sleep to prevent change of mind from the light thread light thread can decide 
			 * to not steal jobs but the heavy thread has already caaught the notification
			 * to avoid that a small sleep
			 */
			try {
				this.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/* we check if since we notice that the thread was loaded and since it gets the 
			 * notification we check if the current thread is still busy.
			 * it might have finish everything.
			 * we must keep in mind that between the moment we compute the function find the most loaded thread below 
			 * because it might have change since we arrive here.
			 * 1- We check if selectedthread is not any more the current thread. well it is not the most busy anymore but it can give jobs?
			 * 2- GlobalVar.selectedthread==-1  (so there no jobs to do anymore)
			 * 3- GlobalVar.lightthread==-1; Weird it should not happend cause this value can only be modified in lock function
			 * 4- GlobalVar.selectedthread==GlobalVar.lightthread in that case the most busy is the less busy so there is no more jobs to do.
			 */
			if( GlobalVar.memoryconstraint==true || GlobalVar.timeconstraint==true || GlobalVar.selectedthread==-1 || GlobalVar.lightthread==-1
					|| GlobalVar.selectedthread==GlobalVar.lightthread || GlobalVar.tabthreads[GlobalVar.selectedthread].OPEN.globalworkload==0)
			{
				// Reset variables
				GlobalVar.selectedthread=-1;
				GlobalVar.lightthread=-1;
				// Release the mutex so the less busy thread can go on
				GlobalVar.mutex.release();
			}
			else
			{
				/* Computer the average workload it might have change because the delay between sending 
				 * the notification and getting the notification.  the average load here is the most heavy 
				 * thread divided by two the less busy thread will receive half of the most busy
				 */
			     GlobalVar.averageworkload=this.ComputerAverageWorkLoad(false);
				// Start the loadblancing
				loadbalance(GlobalVar.lightthread,GlobalVar.selectedthread ,CurNode);
				GlobalVar.lightthread=-1;
				GlobalVar.selectedthread=-1;
				// Release the mutex so the less busy thread can go on
				GlobalVar.mutex.release();
			}
			
			/* This is the end of the load balancing so we can compute the time spent doing loadblancing, 
			 * we add this time to idle time
			 * 
			 */
			this.endtime=System.currentTimeMillis();
			this.idletime+=endtime-starttime;
		}
	}

	private EditPath FinishThread(boolean optimal) 
	{
		/* if the thread that is dying if the most busy thread then we might
		 * release the mutex to let the less busy thread go
		 * Reset the variable
		 * if time out is true and the heaviest thread can die so we put the workload to 0
		 */
		GlobalVar.tabthreads[this.IdThread].OPEN.globalworkload=0;
		GlobalVar.threadNoOfIterations[this.IdThread]= 	noOfIterations;
		GlobalVar.threadvariance[this.IdThread]= varianceSum;
		if(this.IdThread==GlobalVar.selectedthread) GlobalVar.mutex.release();
		/* Reached the time constraint
		 * We notify to the main thread that the current thread is dead
		 */
		this.issolutionoptimal=optimal;
		GlobalVar.sem.release();
		return GlobalVar.UB;
	}

	private EditPath FinishWaitingThreadOptimality(boolean optimal) 
	{
		/* Reset the variable
		 * If time out is true and the heaviest thread can die so we put the workload to 0
		 */
		GlobalVar.tabthreads[this.IdThread].OPEN.globalworkload=0;
		if(this.IdThread==GlobalVar.selectedthread) GlobalVar.mutex.release();
		GlobalVar.selectedthread=-1;
		// The branch has been explored fully might be optimal
		GlobalVar.threadNoOfIterations[this.IdThread]= 	noOfIterations;
		GlobalVar.threadvariance[this.IdThread]= varianceSum;
		issolutionoptimal=optimal;
		// Able to compute the idle time.
		this.endtime=System.currentTimeMillis();
		this.idletime+=endtime-starttime;
		// Give back the lock
		GlobalVar.lightthread=-1;
		GlobalVar.loadbalancelock.unlock();
		// Say to the main thread that this thread is over
		GlobalVar.sem.release();	
		return GlobalVar.UB;
	}

	private void loadbalance(int lightthread,int hevyidthread,NonStaticNode CurNode) 
	{
		/* The thread requested jobs has been set to -1. This is not normal!
		 * Well it can happen because first the say yes this thread is selected then 
		 * We change our mind but the selected thread caught the notification so to limit 
		 * This i put a sleep when the notification is caught and before checking 
		 * The light thread still needs jobs
		 */
		NonStaticNode CurNode2 =CurNode; 
		double halfworkload = GlobalVar.tabthreads[hevyidthread].OPEN.globalworkload/2.0;
		if(lightthread ==-1)
		{
			System.out.println("The thread requested jobs has been set to -1. This is not normal ? "
					+ "Well it can happen cause first the say yes this thread is selected then we change "
					+ "our mind but the selected thread caught the notification so to limit this i put"
					+ " a sleep when the notification is caucght and before checking the light thread "
					+ "still needs jobs");
			return;
		}
				
		// The light thread is under the average
		int isLightThread = 1;				

		while(GlobalVar.tabthreads[lightthread].OPEN.globalworkload<halfworkload)
		{
			ArrayList nodes;
			
			// Get one job
			if(isLightThread == 1)
			{
				nodes  = GlobalVar.tabthreads[hevyidthread].OPEN.searchLowestCost(CurNode2, 1);
				isLightThread = 0;
			}
			else
			{
				nodes  = GlobalVar.tabthreads[hevyidthread].OPEN.searchLowestCost(CurNode2, 0);
				isLightThread = 1;
			}
			
			/* Failed to retreive one job so we go away
			 *  Probably we need to steal everything from the heavy thread
			 */
			if(nodes == null || nodes.size()==0)
			{
				break;
			}
			
			// Get the job
			NonStaticNode node = (NonStaticNode) nodes.get(0);
			CurNode2 = (NonStaticNode) nodes.get(1);
			// Add the job to the light thread
			GlobalVar.tabthreads[lightthread].addJob((EditPath) node.data);

		}
	}

	private int findthemostloadedthread() 
	{
		double maxvalue=-1;
		int maxindex=-1;
		for(int i=0;i<GlobalVar.tabthreads.length;i++){
			if(maxvalue<GlobalVar.tabthreads[i].OPEN.globalworkload 
					){
				maxvalue=GlobalVar.tabthreads[i].OPEN.globalworkload;
				maxindex=i;
			}
		}
		return maxindex;
	}

	private double ComputerAverageWorkLoad(boolean b) 
	{
		double avg=0;
		for(int i=0;i<GlobalVar.tabthreads.length;i++){
			if (b==true )System.out.print(GlobalVar.tabthreads[i].OPEN.globalworkload+"  ");
			avg+=GlobalVar.tabthreads[i].OPEN.globalworkload;
		}
		if (b==true ) System.out.println();
		return avg/(double)GlobalVar.tabthreads.length;

	}

	/* Add a node to the tree if it is worth. if it is lower than UBCOST
	 * The new node is linked to its parent node.
	 */
	private void AddTreeNode(NonStaticNode<EditPath> parent, EditPath p, double ubcost2) 
	{
		double g=p.getTotalCosts();
		double h=p.ComputeHeuristicCosts(heuristicmethod);
		double f = g+h;
		
		if(f<ubcost2)
		{
			OPEN.Add(parent,p);	
		}
	}


	public EditPath getBestEditpath() 
	{
		return GlobalVar.UB;
	}

	public int getNbExploredNode() 
	{
		return this.nbexlporednode;
	}

	public int getMaxSizeOpen() 
	{
		return this.maxopensize;
	}

	public boolean isSolutionoptimal() 
	{
		return issolutionoptimal;
	}

	private GEDMultiThread motherclass;

	private int IdThread;

	public boolean isMemoryconstraintover() 
	{
		return GlobalVar.memoryconstraint;
	}


	@Override
	public void run() 
	{

		this.cputime=System.currentTimeMillis();
		try 
		{
			loop();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		this.cputime=System.currentTimeMillis()-cputime;
	}

	public void addJob(EditPath editPath) 
	{
		AddTreeNode(this.OPEN.root,editPath,Double.MAX_VALUE);
		this.OPEN.root.issorted=false;
	}

	public void setMotherClass(GEDMultiThread gedMultiThread) 
	{
		this.motherclass = gedMultiThread;
	}

	public void SetIdThead(int i) 
	{
		this.IdThread=i;
	}

}
