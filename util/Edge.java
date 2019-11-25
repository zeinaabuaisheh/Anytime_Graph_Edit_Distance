package util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Edge represents an edge between two nodes.
 * Edge extends of GraphComponent
 * 
 * @author   kriesen
 * 
 */
public class Edge extends GraphComponent implements  java.io.Serializable
{

	/** the start-node of an edge*/
	private Node startNode;
	
	/** the end-node of an edge*/
	private Node endNode;
	
	boolean isInverted;
	public boolean isInverted() 
	{
		return isInverted;
	}

	public void setInverted(boolean isInverted) 
	{
		this.isInverted = isInverted;
	}

	public boolean isIDirected() 
	{
		return isIDirected;
	}

	public void setIDirected(boolean isIDirected) 
	{
		this.isIDirected = isIDirected;
	}

	boolean isIDirected;

	private boolean isDirected;
	
	public boolean isDirected() 
	{
		return isDirected;
	}

	public void setDirected(boolean isDirected) 
	{
		this.isDirected = isDirected;
	}

	/**
	 * the constructor
	 */
	public Edge(String mode)
	{
		super();
		super.setNode(false);
		
		if(mode.equals("directed")==true)
		{
			this.isDirected = true;
		}
		this.setComponentId("-1");
	}
	
	
	/**
	 * New constructor
	 * 
	 * Parsing a string an edge
	 * 
	 * String Format = node1@node2#Attributes
	 */
	public Edge(String str ,String mode, Graph graph)
	{
		super();
		super.setNode(false);

		String[] nodesFromEdges1 = str.split("@");
		Node nd1=new Node(nodesFromEdges1[0],mode,true,graph); // first assumption : the node "node1" has been added to the Graph "graph" before
		String[] getValues = nodesFromEdges1[1].split("#");
		Node nd2=new Node(getValues[0],mode,true,graph); // second assumption : the node "node2" has been added to the Graph "graph" before
		this.put("from", nd1.getId());
		this.put("to", nd2.getId());

		for(int m=1; m<getValues.length;m++)
		{
			this.put("attr", ""+getValues[m]);
		}

		this.setId(nd1.getId() + "_<>" + nd2.getId());
		this.setComponentId(nd1.getId() + "_<>" + nd2.getId());
		this.setStartNode(nd1);
		this.setEndNode(nd2);
		
		int found1=0; // testing if node1 has been found in the Graph graph or not
		int found2=0;  // testing if node2 has been found in the Graph graph or not
		Iterator nodeIterator = graph.iterator();
		while (nodeIterator.hasNext()) 
		{
			Node node = (Node) nodeIterator.next();
			if (node.getComponentId().equals(nd1.getComponentId())) 
			{
				found1=1;
				this.setStartNode(node);
				node.getEdges().add(this);
			}
			if (node.getComponentId().equals(nd2.getComponentId())) 
			{
				found2=1;
				this.setEndNode(node);
				node.getEdges().add(this);
			}
		}
		
		// if node1 had not been found, then create a new node and add it to the graph
		if(found1==0)
		{
			nd1=new Node(nodesFromEdges1[0],mode,false,graph); // Create nd1 and add it to the Graph "graph"
			nd1.getEdges().add(this);
			this.setStartNode(nd1);
			//graph.add(nd1);
		}
		// if node2 had not been found, then create a new node and add it to the graph
		if(found2==0)
		{
			nd2=new Node(nodesFromEdges1[1],mode,false,graph);  // Create nd1 and add it to the Graph "graph"
			nd2.getEdges().add(this);
			this.setEndNode(nd2);
		//	graph.add(nd2);
		}
		
		
		if(mode.equals("directed")==true){
			this.isDirected = true;
		}
		
	}

	
	
	/**
	 * @return endNode
	 * returns the end-node.
	 */
	public Node getEndNode() 
	{
		return endNode;
	}

	/**
	 * @param endNode   
	 * the end-node to set
	 */
	public void setEndNode(Node endNode)
	{
		this.endNode = endNode;
	}

	/**
	 * @return startNode
	 * returns the start-node.
	 */
	public Node getStartNode()
	{
		return startNode;
	}

	/**
	 * @param startNode   
	 * the start-node to set.
	 */
	public void setStartNode(Node startNode) 
	{
		this.startNode = startNode;
	}
	
	/**
	 * @return the other end of the 
	 * edge that belongs to @param n
	 */
	public Node getOtherEnd(Node n){
		if (n.equals(this.startNode)){
			return this.endNode;
		}
		return this.startNode;
	}
	
	  @Override
		/**
		 * @return the edge as a string 
		 * Edge To String converter
		 */
	  public String toString() 
	  {
		  
		  //1st step: converting nodes:
		  String str = ""+this.startNode.toString()+"@"+this.endNode.toString()+"";
		  Enumeration values1 = this.getTable().elements();
			int i=0;
			int size= this.getTable().size();
			String v1;
			Double d1;
			int ecart = 2;
			
			// 2nd step: converting attributes
			while(values1.hasMoreElements()==true)
			{
				i++;
				v1 =(String)values1.nextElement();
				if(size-i >=ecart)
				{
				str=str+"#"+v1;
				}
			}
			
		  
	       return str;
	  }
	
}
