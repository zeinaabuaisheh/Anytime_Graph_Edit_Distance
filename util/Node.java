package util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * @author   kriesen
 */
public class Node extends GraphComponent implements  java.io.Serializable
{
	
	/** the edges that belong to this node */
	private LinkedList edges;



	/** modes: directed, undirected */
	private boolean isDirected=false;

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
	public Node(String mode)
	{
		super();
		super.setNode(true);
	
		if(mode.equals("directed")==true)
		{
			this.isDirected = true;
		}
		
		this.edges = new LinkedList();
		
	}
	
	
	/**
	 * New constructor
	 * 
	 * Parsing a string a node
	 * 
	 * String Format = nodeId:x=value1,y=value2, etc.
	 * 
	 * @param str: a string to be parsed
	 * @param mode: directed, undirected
	 * @param exist: a variable that tests if the node has been created before
	 */
	public Node(String str, String mode, boolean exist, Graph graph)
	{
		super();
		super.setNode(true);
	
		String[] idNode1 = str.split(":");
		if(mode.equals("directed")==true)
		{
			this.isDirected = true;
		}
		
		this.setId(idNode1[0]);
		
		
		if(idNode1.length>1)
		{
			String[] node1Children = idNode1[1].split(",");
			for(int j=0; j<node1Children.length ; j++)
			{
				String[] node1Values = node1Children[j].split("=");
				this.put(node1Values[0], node1Values[1]);
			}
			
			// the Case of Distortion ...... Test if it is already exist
			if(exist==true)
			{
				Iterator nodeIterator = graph.iterator();
				exist=false;
				while (nodeIterator.hasNext()) 
				{
					Node node = (Node) nodeIterator.next();
					if (node.getComponentId().equals(this.getComponentId())) 
					{
						exist=true;
						//System.out.println("The node "+this.getId()+"is already in the graph "+graph.getId());
						break;
					}
				}
			}
			// if this is the first time that this node has been found
			if(exist==false)
			{
				this.edges = new LinkedList();
				graph.add(this);
			}
		}		
	}

	

	/**
	 * @return   Returns the edges.
	 * @uml.property   name="edges"
	 */
	public LinkedList getEdges() {
		return edges;
	}

	/**
	 * @param edges   The edges to set.
	 * @uml.property   name="edges"
	 */
	public void setEdges(LinkedList edges) {
		this.edges = edges;
	}

	  @Override
		/**
		 * @return the node as a string 
		 * Node To String converter
		 */
	  public String toString() {
		  String str=this.getComponentId()+":";
		  Enumeration enumeration = this.getTable().keys();
		  int i=0;
		  int size = this.getTable().size();
			while (enumeration.hasMoreElements()) {
				i=i+1;
				String key = (String) enumeration.nextElement();
				String value = (String) this.getTable().get(key);
				
				str=str+""+key+"="+value;
				if(i!=size)
				{
					str=str+",";
				}
			}
		
	       return str;
	  }
}
