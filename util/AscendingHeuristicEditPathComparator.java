package util;

import java.util.Comparator;

public class AscendingHeuristicEditPathComparator  implements
Comparator < EditPath > {
	  double g1;
	  double h1;
	  double f1;
	  
	  double g2;
	  double h2;
	  double f2;
	private int heuristicmethod;
	  
	 public AscendingHeuristicEditPathComparator(int heuristicmethod){
		 this.heuristicmethod=heuristicmethod;
	 }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
  /**
   * {@inheritDoc}
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare(final EditPath o1, final EditPath o2) {
	  g1=o1.getTotalCosts();
	  h1=o1.ComputeHeuristicCosts(heuristicmethod);
	  f1=g1+h1;
	  
	  g2=o2.getTotalCosts();
	  h2=o2.ComputeHeuristicCosts(heuristicmethod);
	  f2=g2+h2;
	
    if (f2 > f1) {
      return -1;
    }
    if (f2 < f1) {
      return 1;
    } else {
      return 0;
    }
  }

}



