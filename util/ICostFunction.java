/**
 * 
 */
package util;

/**
 * @author kriesen
 *
 */
public interface ICostFunction 
{
	
	public double getCosts(GraphComponent start, GraphComponent end);

	public double getEdgeCosts();
	
	public double getNodeCosts();

}
