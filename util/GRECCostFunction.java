//
//  GRECCostFunction.java
//  GraphMatching
//
//  Created by Miquel Ferrer Sumsi on 17/05/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

/**
 * 
 */
package util;

import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.Locale;
import java.util.Map.Entry;

import algorithms.Constants;
import algorithms.MunkresRec;

/**
 * @author kriesen
 * 
 */
public class GRECCostFunction implements ICostFunction{

	


	public GRECCostFunction()
	{

	}


	/**
	 * @return costs of a distortion between 
	 * @param start and @param end
	 */
	public double getCosts(GraphComponent start, GraphComponent end) 
	{	
		/**
		 * node handling
		 */ 
		if (start.isNode() || end.isNode()) 
		{
			double xStart;
			double yStart;
			String startType;
			double xEnd;
			double yEnd;
			String endType;
			// start is not empty
			if (!start.getComponentId().equals(Constants.EPS_ID)) 
			{
				startType = (String) start.getTable().get("type");
				String xStartString = (String) start.getTable().get("x");
				xStart = Double.parseDouble(xStartString);
				String yStartString = (String) start.getTable().get("y");
				yStart = Double.parseDouble(yStartString);				
			} 
			else 
			{
				// insertion
				return 45;
			}
			// end is not empty
			if (!end.getComponentId().equals(Constants.EPS_ID)) 
			{
				endType = (String) end.getTable().get("type");
				String xEndString = (String) end.getTable().get("x");
				xEnd = Double.parseDouble(xEndString);
				String yEndString = (String) end.getTable().get("y");
				yEnd = Double.parseDouble(yEndString);
			}
			else 
			{
				// deletion
				return 45;
			}
			if (startType.equals(endType))
			{
				double distance = Math.sqrt(Math.pow((xEnd - xStart), 2.)
						+ Math.pow((yEnd - yStart), 2.));
				DecimalFormat decFormat = (DecimalFormat) DecimalFormat
						.getInstance(Locale.ENGLISH);
				decFormat.applyPattern("0.00000");
				String distanceString = decFormat.format(distance);
				distance = Double.parseDouble(distanceString);
				return 0.5 * distance;
			} 
			else
			{
				return 90;
			}	

		}
		/**
		 * edge handling
		 */ 
		else 
		{
			int startFreq;
			int endFreq;
			
			if (start.getComponentId().equals(Constants.EPS_ID)) 
			{
				endFreq = Integer.parseInt((String) end.getTable().get("frequency"));
				return 7.5*endFreq;
			}
			if (end.getComponentId().equals(Constants.EPS_ID)) 
			{
				startFreq = Integer.parseInt((String) start.getTable().get("frequency"));
				return 7.5*startFreq;
			}
		
			
			startFreq = Integer.parseInt((String) start.getTable().get("frequency"));
			endFreq = Integer.parseInt((String) end.getTable().get("frequency"));
			if(startFreq==1 && endFreq==1)
			{
				String startType = (String) start.getTable().get("type0");
				
				String endType = (String) end.getTable().get("type0");
				if (startType.equals(endType))
				{
				     return 0;
				} 
				else 
				{
					return 15 ;
				}
				
			}
			
			else if(startFreq==2 && endFreq==2)
			{
				return 0;
			}
			
			else 
			{
				return 7.5;
			}
		
		}
	}

	private double precomputedcosts(GraphComponent start, GraphComponent end) 
	{		
		double[][] matrix=null;
		
		if (start.isNode() || end.isNode()) {
			matrix=Constants.nodecostmatrix;
		}else{
			matrix=Constants.edgecostmatrix;
		}
		
		int n1 = matrix.length;
		int n2 = matrix[0].length;
		int insertindexg1=n2-2;
		int insertindexg2=n1-2;
		int delindexg1=n2-1;
		int delindexg2=n1-1;


		if (start.getComponentId().equals(Constants.EPS_ID)) 
		{
			if(end.belongtosourcegraph)
			{
				return matrix[end.id][insertindexg1];
			}
			else
			{
				return matrix[insertindexg2][end.id];
			}
		}

		if (end.getComponentId().equals(Constants.EPS_ID)) 
		{
			if(start.belongtosourcegraph)
			{
				return matrix[start.id][delindexg1];
			}
			else
			{
				return matrix[delindexg2][start.id];
			}
		}
		
		if(start.belongtosourcegraph)
		{
			return matrix[start.id][end.id];
		}
		else
		{
			return matrix[end.id][start.id];
		}
	}


	private void printMatrix(double[][] matrix) 
	{
		for (int i = 0; i < matrix.length; i++)
		{
			for (int j = 0; j < matrix.length; j++)
			{
				System.out.print(matrix[i][j]+" ");
			}
			System.out.println();
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
