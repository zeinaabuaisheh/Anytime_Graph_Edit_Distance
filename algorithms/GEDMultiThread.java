package algorithms;
import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import algorithms.Constants;
import algorithms.MatrixGenerator;
import algorithms.Munkres;
import algorithms.MunkresRec;
import algorithms.AscendingOrderNonStaticNodeComparator;
import algorithms.HeuristicGEDPartialMatcher;
import util.AscendingHeuristicEditPathComparator;
import util.EditPath;
import util.GRECCostFunction;
import util.MutagenCostFunction;
import util.CMUHouseCostFunction;
import util.Graph;
import util.ICostFunction;
import util.IEdgeHandler;
import util.MyTree;
import util.UniversalEdgeHandler;
import util.MyTree.MyNode;
import util.Node;
import xml.XMLParser;

public class GEDMultiThread {

	MyTree OPEN; // Tree that keeps track of unexplored nodes in the search tree
	private Graph G1; // Graph 1	
	private Graph G2; // Graph 2
	private Node CurVertex; // Current node
	private int G2NbNodes; // Number of nodes of G2
	boolean debug;
	private double UBCOST;
	private int heuristicmethod;
	private AscendingOrderNonStaticNodeComparator MyNodeComparator;
	private int ubmethod;

	public GEDMultiThread(Graph G1, Graph G2,ICostFunction costfunction, IEdgeHandler edgehandler,int heuristic, int ubmethod,boolean variableordering,int nbthread,int numberofInitialEditPaths,boolean debug){
		
		// Start the timer
		inittimer();
		GlobalVar.selectedthread=-1;
		GlobalVar.lightthread=-1;
		GlobalVar.timeconstraint=false;
		GlobalVar.memoryconstraint=false;
		GlobalVar.averageworkload=0;
		GlobalVar.tabthreads=null;
		GlobalVar.UBCOST=Double.MAX_VALUE;
		GlobalVar.UB=null;
		this.debug = debug;
		this.G1 =G1;
		this.G2=G2;
		G2NbNodes = G2.size(); // Number of nodes of G2
		UBCOST = Double.MAX_VALUE;
		heuristicmethod = heuristic;
		this.ubmethod = ubmethod;
			
		if(numberofInitialEditPaths<G1.size() || numberofInitialEditPaths<G2.size())
		{
			numberofInitialEditPaths = Math.max(G1.size(), G2.size())+1;

		}
				
		/* Set up the semaphore to -nbthread+1
		 * The goal is to make the main thread wait until all the thread have finished
		 */
		GlobalVar.sem = new Semaphore(-nbthread+1);
		
		// The mutex ready to block when the load balancing will be needed
		GlobalVar.mutex = new Semaphore(0);
		
		/* First tree to decompose the load
		 * EditPath ROOT = new EditPath(G1,G2,this.heuristicmethod,variableordering);
		 * Each thread has its own local tree search with a root node		
		 */	
		MyNodeComparator =new AscendingOrderNonStaticNodeComparator(heuristicmethod);			
		EditPath ROOT1 = new EditPath(G1,G2,this.heuristicmethod,variableordering);
		HeuristicGEDPartialMatcher HGED =  new HeuristicGEDPartialMatcher(G1, G2, numberofInitialEditPaths ,Constants.costFunction, Constants.edgeHandler,heuristicmethod,ROOT1,ubmethod,variableordering,debug);
		AscendingHeuristicEditPathComparator EDComparator = new AscendingHeuristicEditPathComparator(heuristicmethod);
		Collections.sort(HGED.OPEN,EDComparator);
		ArrayList<EditPath> somejobs= HGED.OPEN;

		// The table of thread is created
		GlobalVar.tabthreads = new GEDDFSThread[nbthread];
		GlobalVar.threadNoOfIterations = new double[nbthread];
		GlobalVar.threadvariance = new double[nbthread];

		// Initialisation of thread
		for(int i=0;i<nbthread;i++){
			GlobalVar.threadNoOfIterations[i]=0.0;
			GlobalVar.threadvariance[i] = 0.0;
			GlobalVar.tabthreads[i]= new GEDDFSThread(heuristicmethod,debug);
			// Set the root node
			GlobalVar.tabthreads[i].SetRootNode(ROOT1, MyNodeComparator);
			// Set the id of the thread
			GlobalVar.tabthreads[i].SetIdThead(i);
		}
		
		// Dispatch the jobs amaong all the thread using modulo
		DispatchJobs(GlobalVar.tabthreads,somejobs);
		
		// Start all the threads
		for(int i=0;i<nbthread;i++)
		{
			GlobalVar.tabthreads[i].start();		
		}
		
		// A small sleep because a thread could have already release the semaphore
		try 
		{
			Thread.currentThread().sleep(1);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		
		// Block here waiting for  all the threads to finish
		try 
		{
			GlobalVar.sem.acquire();
		} 
		catch (InterruptedException e1) 
		{
			e1.printStackTrace();
		}
	
		//we finish the timer
		task.cancel();
		Constants.timer.purge();
		//we clean variables
		Constants.edgecostmatrix=null;
		Constants.nodecostmatrix=null;
	}
	
	

	private void DispatchJobs(GEDDFSThread[] tabthreads, ArrayList<EditPath> somejobs) 
	{
		for(int i=0;i<somejobs.size();i++)
		{
			int index = i%tabthreads.length;
			tabthreads[index].addJob(somejobs.get(i));
		}
	}

	private Timer timer;
	private boolean issolutionoptimal=false;
	private TimerTask task;
	public static int MunkresUB=0;
	public static int DummyUB=1;

	public boolean isTimeconstraintover() 
	{
		return GlobalVar.timeconstraint;
	}

	private void inittimer() 
	{
		task = new TimerTask()
		{
			@Override
			public void run() 
			{
				GlobalVar.timeconstraint=true;
			}	
		};
		
		Constants.timer.scheduleAtFixedRate(task, Constants.timeconstraint, Constants.timeconstraint);
	}


	private EditPath ComputeUpperBound(int upperboundmethod) 
	{
		EditPath res=null;
		
		if(upperboundmethod ==-1){
			UBCOST = Double.MAX_VALUE;
			res = null;
		}
		
		if(upperboundmethod == MunkresUB)
		{
			MatrixGenerator mgen = new MatrixGenerator();
			Munkres munkres = new Munkres();
			MunkresRec munkresRec = new MunkresRec();
			mgen.setMunkres(munkresRec);
			double[][] matrix = mgen.getMatrix(this.G1, G2);
			munkres.setGraphs(G1, G2);
			UBCOST = munkres.getCosts(matrix);
			res = munkres.ApproximatedEditPath();
			UBCOST = res.getTotalCosts();
		}

		if(upperboundmethod == this.DummyUB)
		{
			EditPath ROOT = new EditPath(this.G1,this.G2,this.heuristicmethod,false); 
			res = this.dummyUpperBound(ROOT);
			UBCOST = res.getTotalCosts();

		}
		return res;
	}

	/**
	 * calculates an upper bound
	 * @return an editpath that is based on a heuristic
	 */
	private EditPath dummyUpperBound(EditPath p) 
	{
		EditPath ptmp=null;
		if(p.isComplete() == false){
			ptmp = new EditPath(p);
			int G1NbNodes = ptmp.getUnUsedNodes1().size();
			int G2NbNodes = ptmp.getUnUsedNodes2().size();
			
			// Substitution case
			for(int i=0; i<G1NbNodes; i++)
			{
				Node u = (Node) ptmp.getUnUsedNodes1().getFirst();
				if(i<G2NbNodes) 
				{
					Node v = (Node) ptmp.getUnUsedNodes2().getFirst();
					ptmp.addDistortion(u, v);
				}

			}

			// Deletion case
			if(G1NbNodes > G2NbNodes)
			{
				int noOfDeletedNodes = G1NbNodes- G2NbNodes; 
				for(int i=0;i<noOfDeletedNodes;i++)
				{

					Node u = ptmp.getNext();
					ptmp.addDistortion(u, Constants.EPS_COMPONENT);

				}
			}

			// Insertion case
			else if(G1NbNodes < G2NbNodes)
			{
				int noOfInsertedNodes = G2NbNodes- G1NbNodes; 
				for(int i=0;i<noOfInsertedNodes;i++)
				{
					Node u = ptmp.getNextG2();
					ptmp.addDistortion( Constants.EPS_COMPONENT, u);	
				}
			}
		}
		else
		{
			return p;
		}

		return ptmp;
	}


	/**
	 * The initialisation frunction
	 */
	private ArrayList<EditPath> init() 
	{
		if(G1.size() ==0 && G2.size() ==0)
		{
			System.out.println("G1 has no node inside");
			System.out.println("G2 has no node inside");
			System.out.println("I cannot work !!!");
			System.exit(0);
		}
		
		if(G1.size() ==0 )
		{
			System.out.println("G1 has no node inside");
			this.G1=G2;
			this.G2=G1;
			G2NbNodes = G2.size();
		}

		ArrayList<EditPath> res = new  ArrayList<EditPath>();
		for(int i=0;i<G2NbNodes;i++)
		{
			// Put all the substitutions of u1 with all the vertices of G2 inside OPEN
			EditPath p = new EditPath((EditPath) OPEN.root.data); // An intialization for both G1 and G2 "to show that we did not use neither edges nor nodes of both G1 and G2"
			Node v = (Node)G2.get(i);
			this.CurVertex = p.getNext();
			
			/* Add distortion between the current node u1 and each node in G2 
			 * distortion means insertion or substitution but not deletion as we are sure that
			 * G2 has nodes as we are inside the for loop of G2 */
			
			p.addDistortion(this.CurVertex, v);
			if(debug==true) System.out.println("Substitution CurNode G1 and ith node of G2= ("+this.CurVertex.getId()+"  "+v.getId()+")   Cost ="+p.getTotalCosts());
			res.add(p);
			AddTreeNode(OPEN.root,p,this.UBCOST);
		}
		
		// Put the deletion of u1 inside OPEN
		EditPath p = new EditPath((EditPath) OPEN.root.data);
		this.CurVertex = p.getNext();
		p.addDistortion(this.CurVertex, Constants.EPS_COMPONENT);
		res.add(p);
		AddTreeNode(OPEN.root,p,this.UBCOST);
		return res;
	}

	/**
	 * adds a node to the tree if its cost is lower than UBCOST
	 */
	private void AddTreeNode(MyNode<EditPath> parent, EditPath p, double ubcost2) 
	{
		double g=p.getTotalCosts();
		double h=p.ComputeHeuristicCosts(heuristicmethod);
		double f = g+h;
		
		if(f<ubcost2){
			OPEN.Add(parent,p);	
		}
	}


	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception 
	{
		Graph g2,g1;
		Constants.timer = new Timer();	
		Constants.timeconstraint = Integer.parseInt(args[0]);  // time limit: 30 seconds
		Constants.edgeHandler = new UniversalEdgeHandler(); 
		XMLParser xmlParser = new XMLParser();
		g1=xmlParser.parseGXL(args[1]);
		g2=xmlParser.parseGXL(args[2]);
		int datasetChoice = Integer.parseInt(args[3]);
		
		// GREC dataset
		if(datasetChoice == 1)
		{
			Constants.costFunction = new GRECCostFunction();
		}
		// Mutagenicity dataset
		else if (datasetChoice == 2)
		{
			double nodeSubstitution = Double.parseDouble(args[4]);
			double nodeInsertionDeletion = Double.parseDouble(args[5]);
			double edgeSubstitution = Double.parseDouble(args[6]);
			double edgeInsertionDeletion = Double.parseDouble(args[7]);
			Constants.costFunction = new MutagenCostFunction(nodeSubstitution,nodeInsertionDeletion,
					edgeSubstitution,edgeInsertionDeletion);
		}
		// CMU dataset
		else if (datasetChoice == 3)
		{
			Constants.costFunction = new CMUHouseCostFunction();
		}
		
		long start = System.nanoTime();
		GEDMultiThread HGED = new GEDMultiThread(g1,g2,Constants.costFunction,Constants.edgeHandler,
											EditPath.MunkresAssigmentHeuristic,MunkresUB,true,4,-1,false);
		long end = System.nanoTime();
		long executionTime_NanoSeconds = end - start;
		double executionTime_seconds = (double)executionTime_NanoSeconds / (double)1000000000;
		System.out.println("The distance is: "+HGED.getBestEditpath().getTotalCosts());
		System.out.println("The best editpath is: "+HGED.getBestEditpath().bestMatchingNodesMapping());
		if(HGED.issolutionoptimal)
		{
			System.out.println("The solution is optimal");
		}
		else
		{
			System.out.println("The solution is not optimal");
		}
		System.out.println("The execution time in seconds is: "+new DecimalFormat("##.########").format(executionTime_seconds));
		System.exit(0);


	}

	private static FilenameFilter gxlFileFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(".gxl");
		}
	};


	public EditPath getBestEditpath() 
	{
		return GlobalVar.UB;
	}
	
	
	public double getVariance() 
	{
		double maxNoOfIterations = -1;
		int threadIndex = -1;
		for(int i=0;i<GlobalVar.tabthreads.length;i++)
		{
			if(GlobalVar.threadNoOfIterations[i]>maxNoOfIterations)
			{
				maxNoOfIterations = GlobalVar.threadNoOfIterations[i];
				threadIndex = i;
			}
		}
		double averageVariance = GlobalVar.threadvariance[threadIndex]/maxNoOfIterations;
		return averageVariance;
	}
	
	
	public double getMaxIteration() 
	{
		double maxNoOfIterations = -1;
		int threadIndex = -1;
		for(int i=0;i<GlobalVar.tabthreads.length;i++)
		{
			if(GlobalVar.threadNoOfIterations[i]>maxNoOfIterations)
			{
				maxNoOfIterations = GlobalVar.threadNoOfIterations[i];
				threadIndex = i;
			}
		}
		return maxNoOfIterations;
	}

	
	public double getIDLEtime() 
	{
		double res=0;
		for(int i=0;i<GlobalVar.tabthreads.length;i++)
		{
			res+=GlobalVar.tabthreads[i].getIdletime();
		}
		return res;
	}
	
	public int getNbExploredNode() 
	{
		int res=0;
		for(int i=0;i<GlobalVar.tabthreads.length;i++)
		{
			res+=GlobalVar.tabthreads[i].getNbExploredNode();
		}
		return res;
		
	}

	public double getCputime() 
	{
		double res=0;
		for(int i=0;i<GlobalVar.tabthreads.length;i++)
		{
			res+=GlobalVar.tabthreads[i].getCputime();
		}
		return res;
	}
	
	public int getMaxSizeOpen() 
	{
		int res=0;
		for(int i=0;i<GlobalVar.tabthreads.length;i++)
		{
			res+=GlobalVar.tabthreads[i].getMaxSizeOpen();
		}
		return res;
	}

	public boolean isSolutionoptimal() 
	{
	     if(isTimeconstraintover()==true)
         {
                 return false;
         }
         else
         {
                 return true;

         }

	}
	
	
}
