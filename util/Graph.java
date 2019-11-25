package util;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author   kriesen
 * Graph is a list with nodes
 * and there is a list with all edges
 * of this graph
 */
public class Graph extends LinkedList
{
	/** the class of this graph */
	private String classId;
	/** the identifier of the graph */
	private String id;
	/** labeled edges: true/false */
	private String edgeId;
	/** modes: directed, undirected */
	private String edgeMode;
	/** the edges of the graph */
	private LinkedList edges;
	
	/**
	 * the constructor
	 */
	public Graph()
	{
		super();
		this.edges = new LinkedList();
	}
	
	/**
	 * some getters and setters
	 */
	public String getEdgeId() 
	{
		return edgeId;
	}
	public String getEdgeMode() 
	{
		return edgeMode;
	}
	public String getId() 
	{
		return id;
	}
	public LinkedList getEdges() 
	{
		return edges;
	}
	public void setEdges(LinkedList edges) 
	{
		this.edges = edges;
	}
	public void setEdgeId(String edgeids) 
	{
		this.edgeId = edgeids;
	}
	public void setEdgeMode(String edgemode) 
	{
		this.edgeMode = edgemode;
	}
	public void setId(String id) 
	{
		this.id = id;
	}
	public String getClassId() 
	{
		return classId;
	}
	public void setClassId(String classId) 
	{
		this.classId = classId;
	}
	
	public String toString()
	{
		String nodes = "Nodes\n";
		Iterator iter = this.iterator();
		while (iter.hasNext()){
			Node n = (Node) iter.next();
			nodes += n.getComponentId()+" ";
			nodes += n.getTable().get("chem") +" ; ";
		}
		nodes += "\n";
		nodes += "Edges\n";
		Iterator edgeIter = this.edges.iterator();
		while (edgeIter.hasNext())
		{
			Edge e = (Edge) edgeIter.next();
			nodes += e.getTable().get("from") +"<-"+e.getTable().get("valence")+"->";
			nodes += e.getTable().get("to")+" ";
			nodes+="\n";
		}
		return nodes;
	}
}
