package util;

import java.text.DecimalFormat;
import java.util.Locale;

import algorithms.Constants;


public class CMUHouseCostFunction implements ICostFunction
{

	public CMUHouseCostFunction()
	{

	}
	
	@Override
	public double getCosts(GraphComponent start, GraphComponent end) 
	{		
		/**
		 * node handling
		 */ 
		if (start.isNode() || end.isNode()) 
		{
			// start is not empty
			if (start.getComponentId().equals(Constants.EPS_ID)) 
			{
				
				// insertion
				return Double.MAX_VALUE;
			}
			// end is not empty
			if (end.getComponentId().equals(Constants.EPS_ID)) 
			{
				
				// deletion
				return Double.MAX_VALUE;
			}
			
			return 0;
				

		}/**
		 * edge handling
		 */ 
		else 
		{
			double label1=0;
			double label2=0;;
			String stringlabel1;
			String stringlabel2;
			// start is not empty
			if (!start.getComponentId().equals(Constants.EPS_ID) && (start.isNode()==false)) 
			{
				stringlabel1 = (String) start.getTable().get("dist");
				label1 = Double.parseDouble(stringlabel1);						
			} 
			// end is not empty
			if (!end.getComponentId().equals(Constants.EPS_ID)) 
			{
				stringlabel2 = (String) end.getTable().get("dist");
				label2 = Double.parseDouble(stringlabel2);
			} 
			
			// insertion
			if( (start.getComponentId().equals(Constants.EPS_ID) ==true) )
			{
				return 0.5*label2;
			}
			
			// deletion
			if( (end.getComponentId().equals(Constants.EPS_ID) ==true) )
			{
				return 0.5*label1;
			}
			
			double distance = Math.abs(label1 - label2);
			DecimalFormat decFormat = (DecimalFormat) DecimalFormat
					.getInstance(Locale.ENGLISH);
			decFormat.applyPattern("0.00000");
			String distanceString = decFormat.format(distance);
			distance = Double.parseDouble(distanceString);
			return  0.5 * distance;

		}
	}
	
	


	@Override
	public double getEdgeCosts() 
	{
		return 0;
	}

	@Override
	public double getNodeCosts() 
	{
		return 0;
	}

	

}
