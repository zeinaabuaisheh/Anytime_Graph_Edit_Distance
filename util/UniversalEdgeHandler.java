/**
 * 
 */
package util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

import algorithms.Constants;

/**
 * @author Romain
 * 
 */
public class UniversalEdgeHandler implements IEdgeHandler {

	/**
	 * handles the edge operations - for detail information 
	 * @see masters thesis of k. riesen p.17ff
	 */
	public void handleEdges(EditPath p, GraphComponent u_start,
			GraphComponent u_end) {
		/**
		 * node deletion
		 */
		if (u_end.getComponentId().equals(Constants.EPS_ID)) 
		{
			LinkedList edges = ((Node) u_start).getEdges();
			Node v;
			for (int i = 0; i < edges.size(); i++) 
			{
				Edge e = (Edge) edges.get(i);
				v = e.getOtherEnd((Node) u_start);
				//si noeud cibe de g1 est d�ja utilis� alors on surpprime l'arc
				if (!p.getUnUsedNodes1().contains(v)) 
				{
					p.addDistortion(e, Constants.EPS_COMPONENT);
				}
			}
		}
		/**
		 * node insertion
		 */
		if (u_start.getComponentId().equals(Constants.EPS_ID)) 
		{
			LinkedList edges = ((Node) u_end).getEdges();
			Node v;
			for (int i = 0; i < edges.size(); i++) 
			{
				Edge e = (Edge) edges.get(i);
				v = e.getOtherEnd((Node) u_end);
				// if the source vertex is already used, we insert the edge in G1
				if (!p.getUnUsedNodes2().contains(v)) 
				{
					p.addDistortion(Constants.EPS_COMPONENT, e);
				}
			}
		}
		/**
		 * node substitution
		 */
		if (!u_end.getComponentId().equals(Constants.EPS_ID)
				&& !u_start.getComponentId().equals(Constants.EPS_ID)) 
		{
			// Get all the edges of the node u_start in G1
			LinkedList edges = ((Node) u_start).getEdges();
			GraphComponent v_start;
			GraphComponent v_end;
			// For all the edges of G1
			for (int i = 0; i < edges.size(); i++){
				// Get the current edge of G1
				Edge e_start = (Edge) edges.get(i);
				// Get the target vertex of G1 (that corresponds to the current edge)
				v_start = e_start.getOtherEnd((Node) u_start);
				// If the distortion of the nodes has already been done, then let's deal with the edges
				  Enumeration enumeration = p.getDistortions().keys();
				    int containsKey=0;
					GraphComponent gc=new GraphComponent();
					while (enumeration.hasMoreElements())
					{
						GraphComponent key = (GraphComponent) enumeration.nextElement();
						GraphComponent value = (GraphComponent) p.getDistortions().get(key);
						if(v_start.getId().equals(key.getId()))
						{
							// Get the other end;
							gc = (GraphComponent) p.getDistortions().get(key);
							containsKey=1;
							break;
						}
					}
					
					if (containsKey==1){

					/*
					 * Get the vertex (v_end) of G2 that's matched with v_start
					 */
					v_end=gc;
					// If a vertex v_end has to be deleted, then we delete the arc as well
					if (v_end.getComponentId().equals(Constants.EPS_ID))
					{
						p.addDistortion(e_start, Constants.EPS_COMPONENT);
					} 
					else 
					{
						/* If a vertex v_end has to be substituted, then we substitute the arc as well
						 * Thus, we will get the edge between the two nodes of G2 (i.e., between u_end and v_end)
						 */
						Edge e_end;
						e_end = getEdgeBetween((Node) u_end, v_end);							
						if (e_end != null)
						{
							if(((Node) u_end).isDirected() == true)
							{
								boolean gooddirection =AreEdgesTheSameDirection(e_start,e_end,(Node)u_start,(Node)v_start,(Node)u_end,(Node)v_end);
								e_start.setInverted(!gooddirection);
								e_end.setInverted(!gooddirection);
							}
							p.addDistortion(e_start, e_end);
						} 
						else 
						{
						    /* 
						     * If there is no edge (uend_vend) between the two vertices of G2 
						     * then we delete the edge (ustart_vstart)
						     */
							p.addDistortion(e_start, Constants.EPS_COMPONENT);
						}
					}
				}
			}
			// Get the edges of the vertex uend of G2
			edges = ((Node) u_end).getEdges();
			Edge e_start;
			for (int i = 0; i < edges.size(); i++)
			{
				// Get the current edge of G2
				Edge e_end = (Edge) edges.get(i);
				// Get the source edge of G2
				v_end = e_end.getOtherEnd((Node) u_end);
				// Check if the target vertex was matched with a vertex of G1 or not
				Enumeration enumeration = p.getDistortions().keys();
				GraphComponent key = new GraphComponent();
				GraphComponent value = new GraphComponent();
			    int containsKey=0;
				GraphComponent gc=new GraphComponent();
				while (enumeration.hasMoreElements())
				{
					key = (GraphComponent) enumeration.nextElement();
					value = (GraphComponent) p.getDistortions().get(key);
					if(v_end.getId().equals(value.getId()))
					{
						//get the other end;
						gc = (GraphComponent) p.getDistortions().get(key);
						containsKey=1;
						break;
					}
				}

				if (containsKey==1)
				{
					// Get the vertex of G1 that wax matched 
					v_start =  key;
					// Get the edge of the vertex of G1
				    e_start = getEdgeBetween((Node) u_start, v_start);
				    /* 
				     * If there is no edge (vstart_ustart) between the two vertices of G1 but there is an edge (vend_uend)
				     * then we insert an edge (vstart_ustart)
				     */
					if (e_start == null){
						// Insert the edge in G1
						p.addDistortion(Constants.EPS_COMPONENT, e_end);
					}
				}
			}
		
		}
	}

	private boolean AreEdgesTheSameDirection(Edge e_start, Edge e_end,
			GraphComponent u_start, Node v_start,
			GraphComponent u_end, Node v_end) 
	{
		if(e_start !=null && e_end !=null){
			if(e_start.getStartNode()==v_start && e_end.getStartNode() == v_end) return true;
		}
		return false;
	}

	private Edge getDirectedEdgeBetween(Node n1, GraphComponent n2) 
	{
		Iterator iter = n1.getEdges().iterator();
		Node temp;
		while (iter.hasNext())
		{
			Edge e = (Edge) iter.next();
			temp =e.getStartNode();
			if (temp.equals(n2))
			{
				return e;
			}
		}
		return null;
	}

	/**
	 * @return the egde between two nodes: @param n1
	 * @param n2 
	 */
	private Edge getEdgeBetween(Node n1, GraphComponent n2) 
	{
		Iterator iter = n1.getEdges().iterator();
		Node temp;
		while (iter.hasNext())
		{
			Edge e = (Edge) iter.next();
			temp = e.getOtherEnd(n1);
			if(temp.getId().equals(n2.getId()))
			{
				return e;
			}	
		}
		return null;
	}
}