package algorithms;
import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import util.AscendingOrderMyNodeComparator;
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

public class GEDDFS 
{
	MyTree OPEN; // Tree that keeps track of unexplored nodes in the search tree
	private Graph G1; // Graph 1	
	private Graph G2; // Graph 2
	private Node CurVertex; // Current node
	private int G2NbNodes; // Number of nodes of G2
	boolean debug;
	private double UBCOST;
	public EditPath UB;
	private int heuristicmethod;
	private AscendingOrderMyNodeComparator MyNodeComparator;
	private int ubmethod;
	
	public GEDDFS(Graph G1, Graph G2,ICostFunction costfunction, IEdgeHandler edgehandler,int heuristic, int ubmethod,boolean variableordering,boolean debug)
	{
		inittimer();
		this.debug = debug;
		this.G1 =G1;
		this.G2=G2;
		G2NbNodes = G2.size(); // Number of nodes of G2
		UBCOST = Double.MAX_VALUE;
		heuristicmethod = heuristic;
		this.ubmethod = ubmethod; // Selected method for upper bound
		EditPath ROOT = new EditPath(G1,G2,this.heuristicmethod,variableordering);
		MyNodeComparator =new AscendingOrderMyNodeComparator(heuristicmethod);
		OPEN = new MyTree<EditPath>(ROOT,MyNodeComparator);
		
		if(Constants.FirstUB==null || ubmethod!=MunkresUB)
		{
			UB = ComputeUpperBound(ubmethod);
		}
		else
		{
			UB = Constants.FirstUB;
			this.UBCOST=UB.getTotalCosts();
			Constants.FirstUB=null;
		}
		
		init();
		UB = loop();
		task.cancel();
		Constants.timer.purge();
		Constants.edgecostmatrix=null;
		Constants.nodecostmatrix=null;
	}

	protected boolean timeconstraint=false;
	private Timer timer;
	private boolean issolutionoptimal=false;
	private TimerTask task;
	public static int MunkresUB=0;
	public static int DummyUB=1;

	public boolean isTimeconstraintover() 
	{
		return timeconstraint;
	}

	private void inittimer() 
	{
		task = new TimerTask()
		{
			@Override
			public void run() 
			{
				timeconstraint=true;
			}	
		};
		
		Constants.timer.scheduleAtFixedRate(task, Constants.timeconstraint, Constants.timeconstraint);
	}

	/**
	 * calculates the first upper bound
	 * @return the editpath of the upper bound
	 */
	private EditPath ComputeUpperBound(int upperboundmethod)
	{
		EditPath res=null;
		
		if(upperboundmethod ==-1)
		{
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
			EditPath ROOT = new EditPath(this.G1,this.G2); 
			ROOT.setHeuristicmethod(this.heuristicmethod);
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
		
		if(p.isComplete() == false)
		{
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
				int noOfDeletedNodes = G1NbNodes - G2NbNodes; 
				for(int i=0; i<noOfDeletedNodes; i++)
				{
					Node u = ptmp.getNext();
					ptmp.addDistortion(u, Constants.EPS_COMPONENT);
				}
			}
			// Insertion case
			else if(G1NbNodes < G2NbNodes)
			{
				int noOfInsertedNodes = G2NbNodes - G1NbNodes; 
				for(int i=0; i<noOfInsertedNodes; i++)
				{
					Node u = ptmp.getNextG2();
					ptmp.addDistortion(Constants.EPS_COMPONENT, u);	
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
	 * explores the search tree
	 * @return the optimal solution or the one found so far after the timeout
	 */

	private EditPath loop() 
	{
		boolean condition1;
		boolean condition2;

		if(OPEN.isEmpty() == true){
			issolutionoptimal=true;
			return UB;
		}

		// Search in the OPEN tree the node that has the minimum cost (Pmin) and then delete it
		EditPath pmin=null;
		MyNode<EditPath> pminNode=null;
		MyNode<EditPath> CurNode=OPEN.root;
	
		while(true)
		{
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
			if(this.timeconstraint==true)
			{
				return UB; 	// Reached the time constraint
			}
			
			if((pminNode == null) && (CurNode.parent == null))
			{
				/* All the tree has been explored since pminNode is null and its CurNode is root.
				   No more children to explore */
				issolutionoptimal=true;
				return UB;
			}

			pmin = pminNode.data;
		
			// Cut the tree if pmin is higher than UBCOST
			if(pmin.getTotalCosts()+pmin.ComputeHeuristicCosts(heuristicmethod)<UBCOST)
			{
				// Generates all successors of node u in the search tree and add them to open				
				if(pmin.getUnUsedNodes1().size() > 0)
				{
					this.CurVertex=pmin.getNext();
					if(debug==true) System.out.println("Current Node="+this.CurVertex.getId());
					/* For all the nodes (w) that are not already explored in Pmin, add to (pmin) all 
					 * the substitutions of uk+1 with w 
					 */
					LinkedList<Node>UnUsedNodes2= pmin.getUnUsedNodes2();

					for(int i=0;i<UnUsedNodes2.size();i++)
					{
						EditPath newpath = new EditPath(pmin);
						Node w = UnUsedNodes2.get(i);
						newpath.addDistortion(this.CurVertex, w);
						AddTreeNode(pminNode,newpath,this.UBCOST);
					}
					// Put in (pmin) the deletion of uk+1
					EditPath newpath = new EditPath(pmin);
					newpath.addDistortion(this.CurVertex, Constants.EPS_COMPONENT);
					AddTreeNode(pminNode,newpath,this.UBCOST);
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

					if(f<UBCOST)
					{
						// Update the upper bound
						UBCOST = f;
						UB = newpath;	
					}	
				}
			}
			// Next node to be explored
			CurNode=pminNode;
		}
	}


	/**
	 * The initialisation function
	 */
	private void init() 
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

		for(int i=0;i<G2NbNodes;i++)
		{
			// Put all the substitutions of u1 with all the vertices of G2 inside OPEN
			EditPath p = new EditPath((EditPath) OPEN.root.data); 
			Node v = (Node)G2.get(i);
			this.CurVertex = p.getNext();
			
			/* Add distortion between the current node u1 and each node in G2 
			 * distortion means insertion or substitution but not deletion as we are sure that
			 * G2 has nodes as we are inside the for loop of G2 */
			
			p.addDistortion(this.CurVertex, v);
			AddTreeNode(OPEN.root,p,this.UBCOST);
		}
		
		// Put the deletion of u1 inside OPEN
		EditPath p = new EditPath((EditPath) OPEN.root.data);
		this.CurVertex = p.getNext();
		p.addDistortion(this.CurVertex, Constants.EPS_COMPONENT);
		AddTreeNode(OPEN.root,p,this.UBCOST);
	}

	/**
	 * adds a node to the tree if its cost is lower than UBCOST
	 */
	private void AddTreeNode(MyNode<EditPath> parent, EditPath p, double ubcost) 
	{
		double g=p.getTotalCosts();
		double h=p.ComputeHeuristicCosts(heuristicmethod);
		double f = g+h;
		
		if(f<ubcost)
		{
			OPEN.Add(parent,p);	
		}
	}
	

	private static FilenameFilter gxlFileFilter = new FilenameFilter() 
	{
		public boolean accept(File dir, String name) 
		{
			return name.endsWith(".gxl");
		}
	};

	public EditPath getBestEditpath() 
	{
			return UB;		
	}
	
	public boolean isSolutionoptimal() 
	{
		return issolutionoptimal;
	}
	
	private boolean memoryconstraint=false;
	
	public boolean isMemoryconstraintover() 
	{
		return memoryconstraint;
	}
	

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception 
	{
		Graph g2,g1;
		Constants.timer = new Timer();	
		Constants.timeconstraint = Integer.parseInt(args[0]);  // time limit: e.g. 30 seconds
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
		GEDDFS HGED = new GEDDFS(g1,g2,Constants.costFunction,Constants.edgeHandler,
									EditPath.MunkresAssigmentHeuristic,0,true,false);
		long end =  System.nanoTime();
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


}
