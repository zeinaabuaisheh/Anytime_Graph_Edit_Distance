package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import util.MyTree.MyNode;

public class MyTree<T> 
{
    public MyNode<T> root;
    public static Comparator compar;
    public static int MaxNodeByFloor=200;//160;
    public MyTree(T rootData, Comparator cmp) 
    {
        root = new MyNode<T>();
        root.parent=null;
        root.data = rootData;
        compar=cmp;
        root.children = new ArrayList<MyNode<T>>(MaxNodeByFloor);     
    }

    
	public static class MyNode<T> 
	{
        public T data;
        public MyNode<T> parent;
        public ArrayList<MyNode<T>> children = new ArrayList<MyNode<T>>(MaxNodeByFloor);
        public boolean issorted=false;
    }
    
	
  
    public MyNode<T> Add(MyNode<T> parent, T  childdata) 
    {
    	MyNode<T> child = new MyNode<T>();
    	child.parent=parent;
    	child.data = childdata;
    	parent.children.add(child);
   	
    	return child;
    }
    
   
    
    public MyNode<T> pollFirstLowestCost(MyNode<T> parent) 
    {
    	
    	if(parent.issorted==false){
    		Collections.sort(parent.children,this.compar);
    		parent.issorted=true;
    	}
    	if(parent.children.size()>0)
    	{
    		MyNode<T> res = parent.children.get(0);
    		
    		parent.children.remove(0);
    		return res;
    	}
    	return null;
    }
    
    
    public MyNode<T> BackTrack(MyNode<T> child) 
    {
    	return child.parent;
    }



	public boolean isEmpty() 
	{
		if(this.root.children.size()==0)
		{
			return true;
		}
		return false;
	}
	
	

	/**
	 * deletes some edit paths in order to only have s nodes in the OPEN set
	 */
	private void removeSomeEditPaths(ArrayList children, int j) 
	{
		int size = children.size();
		int delta =size-j;
		if(delta>0)
		{
			for(int i=0; i<delta ; i++)
			{
				children.remove(size-i-1);
			}
		}

	}



	



	
    
}