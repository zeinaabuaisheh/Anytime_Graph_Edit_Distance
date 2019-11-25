/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

/**
 *
 * @author mferrer
 */

import util.GraphComponent;
import algorithms.Constants;

public class MutagenCostFunction implements ICostFunction 
{
	double node_sub;
	double node_del_insert;
	double edge_sub;
	double edge_del_insert;


	public MutagenCostFunction(double node_sub, double node_del_insert, double edge_sub, double edge_del_insert ) 
	{
	
		this.node_sub = node_sub;
		this.node_del_insert = node_del_insert;
		this.edge_sub = edge_sub;
		this.edge_del_insert = edge_del_insert;
	}

	/**
	 * @return costs of a distortion between
	 * @param start
	 *            and
	 * @param end
	 */
	public double getCosts(GraphComponent start, GraphComponent end) 
	{
		/**
		 * node handling
		 */
		if (start.isNode() || end.isNode()) 
		{
			String chemSym1;
			String chemSym2;
			// start is not empty
			if (!start.getComponentId().equals(Constants.EPS_ID)) 
			{
				chemSym1 = (String) start.getTable().get("chem");
			} 
			else 
			{
				// insertion
				return node_del_insert;
			}
			// end is not empty
			if (!end.getComponentId().equals(Constants.EPS_ID)) 
			{
				chemSym2 = (String) end.getTable().get("chem");
			} 
			else 
			{
				// deletion
				return node_del_insert;
			}
			
			if(chemSym1.equals(chemSym2)==true)
			{
				return 0;
			}
			else
			{
				return node_sub;
			}
		
		}
		/**
		 * edge handling
		 */
		else 
		{
			if (start.getComponentId().equals(Constants.EPS_ID)) 
			{
				return edge_del_insert ;
			}
			if (end.getComponentId().equals(Constants.EPS_ID)) 
			{
				return edge_del_insert ;
			}
			
			String startValence = (String) start.getTable().get("valence");
			String endValence = (String) end.getTable().get("valence");
			
			if(startValence.equals(endValence))
			{
				return 0;
			}
			else 
			{
				return edge_sub;
			}

		}
	}



	/**
	 * @return the cost of an edge operation
	 */
	public double getEdgeCosts() 
	{
		return 0;
	}

	/**
	 * @return the cost of a node operation
	 */
	public double getNodeCosts() 
	{
		return 0;
	}

}

