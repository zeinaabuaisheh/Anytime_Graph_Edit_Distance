package xml;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class XMLWriter {
	
	private String resultName;
	
	private String source;
	
	private String target;

	public void setResultName(String resultName) {
		this.resultName = resultName;
	}

	public void  writeDXL(double [][] distances){
		PrintWriter out;
		try {
			out = new PrintWriter(new FileOutputStream(resultName));
			out.println("<?xml version=\"1.0\"?>");
			out.println("<Matrix xmlns:ns=\"http://www.iam.unibe.ch/%7Emneuhaus/FAN/1.0\">");
			out.print("<content sourceset=\""+this.source+";\" ");
			out.print("targetset=\""+this.target+";\" ");
			out.print("costfunction=\"\" ");
			out.print("cols=\""+ distances[0].length+ "\" ");
			out.print("rows=\""+ distances.length+ "\">");
			for (int i = 0; i < distances.length; i++){
				for (int j = 0; j < distances[0].length; j++){
					double d = distances[i][j];
					d = this.round(d);
					out.print(d+";");
				}
			}
			out.println("</content>");
			out.print("</Matrix>");
			out.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	private double round(double d) {
		d *= 100000.0;
		d = Math.round(d);
		d /= 100000.0;
		return d;
	}

	
}
