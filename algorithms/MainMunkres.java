/**
 * 
 */
package algorithms;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import util.CMUHouseCostFunction;
import util.GRECCostFunction;
import util.MutagenCostFunction;
import util.Graph;
import util.GraphCollection;
import util.UniversalEdgeHandler;
import xml.XMLParser;

/**
 * @author kriesen
 * 
 */
public class MainMunkres {

	/**
	 * the collections
	 */
	private GraphCollection source, target;

	/**
	 * the results of all matchings
	 */
	private double[][] distanceMatrix;

	/**
	 * the source and target graphs
	 */
	private Graph sourceGraph, targetGraph;

	/**
	 * boolean to check if swapped or not
	 */
	private boolean swapped = false;

	/**
	 * reads the graphs sets and triggers the individual matchings. results are
	 * stored in the distancematrix[][]
	 */
	private String toMatrixWithGraphId() {
		String s = new String();
		
		/*
		 * with file names
		 */
		s += "\t";
		for(int i = 0; i < this.source.size(); i++) {
			s += ((Graph)(this.source.get(i))).getClassId() + ((Graph)(this.source.get(i))).getId() + "\t";
		}
		s += "\n";
		
		for (int i = 0; i < this.target.size(); i++){
			s += ((Graph)(this.source.get(i))).getClassId() + ((Graph)(this.target.get(i))).getId() + "\t";
			for (int j = 0; j < this.source.size(); j++)
		   		s += distanceMatrix[i][j] + "\t";		   
			s += "\n";
		}
		return s;
	}

	private void toMatrixFile(String fileName) throws IOException{
		FileWriter output = new FileWriter(fileName);
		output.write(this.toMatrixWithGraphId());
		output.flush();
		output.close();
	}

	/**
	 * the main method -- where it all begins
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
	
		
		MatrixGenerator mgen = new MatrixGenerator();
		Munkres munkres = new Munkres();
		MunkresRec munkresRec = new MunkresRec();
		mgen.setMunkres(munkresRec);
		
		Constants.timer = new Timer();	
		
		Constants.timeconstraint = 30000;  // time limit: 30 seconds		
		Constants.edgeHandler = new UniversalEdgeHandler(); 
		int datasetChoice = Integer.parseInt(args[2]);
		// GREC dataset
		if(datasetChoice == 1)
		{
			Constants.costFunction = new GRECCostFunction();
		}
		// Mutagenicity dataset
		else if (datasetChoice == 2)
		{
			double nodeSubstitution = Double.parseDouble(args[3]);
			double nodeInsertionDeletion = Double.parseDouble(args[4]);
			double edgeSubstitution = Double.parseDouble(args[5]);
			double edgeInsertionDeletion = Double.parseDouble(args[6]);
			Constants.costFunction = new MutagenCostFunction(nodeSubstitution,nodeInsertionDeletion,
					edgeSubstitution,edgeInsertionDeletion);
		}
		// CMU dataset
		else if (datasetChoice == 3)
		{
			Constants.costFunction = new CMUHouseCostFunction();
		}

		XMLParser xmlParser = new XMLParser();
	    Graph g2,g1;
		g1=xmlParser.parseGXL(args[0]);
		g2=xmlParser.parseGXL(args[1]);
		long start = System.nanoTime();
		double[][] matrix = mgen.getMatrix(g1, g2);
		munkres.setGraphs(g1, g2);
		double distance = munkres.getCosts(matrix);
		long end= System.nanoTime();
		long executionTime_NanoSeconds = end - start;
		double executionTime_seconds = (double)executionTime_NanoSeconds / (double)1000000000;
		System.out.println("The distance is: "+munkres.getBestEditPath().getTotalCosts());
		System.out.println("The best editpath is: "+munkres.getBestEditPath().bestMatchingNodesMapping());
		System.out.println("The execution time in seconds is: "+new DecimalFormat("##.########").format(executionTime_seconds));
		System.exit(0);

	}
}
