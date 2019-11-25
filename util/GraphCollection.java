/**
 * 
 */
package util;

import java.util.LinkedList;

/**
 * @author kriesen GraphCollection holds several graphs in a list
 */
public class GraphCollection extends LinkedList 
{
	/** the name of the collection */
	private String collectionName;

	/**
	 * the constructor
	 */
	public GraphCollection() 
	{
		super();
	}

	/**
	 * getter and setter
	 */
	public String getCollectionName() 
	{
		return collectionName;
	}

	public void setCollectionName(String collectionName) 
	{
		this.collectionName = collectionName;
	}

}
