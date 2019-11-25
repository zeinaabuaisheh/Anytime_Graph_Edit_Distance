package xml;



import java.io.FileReader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import nanoxml.XMLElement;

import util.Edge;
import util.Graph;
import util.GraphCollection;
import util.Node;


public class XMLParser {

	private String graphPath;

	public String getGraphPath() {
		return graphPath;
	}

	public void setGraphPath(String graphPath) {
		this.graphPath = graphPath;
	}

	public GraphCollection parseCXL(String filename) throws Exception {

		XMLElement xml = new XMLElement();
		FileReader reader = new FileReader(filename);
		xml.parseFromReader(reader);

		GraphCollection graphCollection = new GraphCollection();
		graphCollection.setCollectionName(filename);
		Vector children = xml.getChildren();
		XMLElement root = (XMLElement) children.get(0);
		Enumeration enumerator = root.enumerateChildren();
		while (enumerator.hasMoreElements()) {
			XMLElement child = (XMLElement) enumerator.nextElement();
			Graph g = this.parseGXL(this.graphPath
					+ child.getAttribute("file", null)+"");
			g.setClassId((String) child.getAttribute("class", "NO_CLASS"));
			graphCollection.add(g);
		}
		return graphCollection;
	}

	public Graph parseGXL(String filename) throws Exception {

		XMLElement xml = new XMLElement();
		FileReader reader = new FileReader(filename);
		xml.parseFromReader(reader);
		reader.close();
		Graph graph1 = new Graph();
		Vector children = xml.getChildren();
		XMLElement root = (XMLElement) children.get(0);
		String id = (String) root.getAttribute("id", null);
		String edgeids = (String) root.getAttribute("edgeids", null);
		String edgemode = (String) root.getAttribute("edgemode", "undirected");
		graph1.setId(id);
		graph1.setEdgeId(edgeids);
		graph1.setEdgeMode(edgemode);

		Enumeration enumerator = root.enumerateChildren();
		while (enumerator.hasMoreElements()) {
			XMLElement child = (XMLElement) enumerator.nextElement();
			if (child.getName().equals("node")) {
				String nodeId = (String) (child.getAttribute("id", null));

				Node node = new Node(edgemode);
				node.setId(nodeId);

				Enumeration enum1 = child.enumerateChildren();
				while (enum1.hasMoreElements()) {
					XMLElement child1 = (XMLElement) enum1.nextElement();
					if (child1.getName().equals("attr")) {
						String key = (String) child1.getAttribute("name", null);
						Vector children2 = child1.getChildren();
						XMLElement child2 = (XMLElement) children2.get(0);

						String value = child2.getContent();
						node.put(key, value);
					}

				}
				graph1.add(node);
			}
			if (child.getName().equals("edge")) {
				Edge edge = new Edge(edgemode);
				String from = (String) child.getAttribute("from", null);
				String to = (String) child.getAttribute("to", null);
				edge.put("from", from);
				edge.put("to", to);
				edge.setId(from + "_<>" + to);
				// *******************************
				Enumeration enum1 = child.enumerateChildren();
				while (enum1.hasMoreElements()) {
					XMLElement child1 = (XMLElement) enum1.nextElement();
					if (child1.getName().equals("attr")) {
						String key = (String) child1.getAttribute("name",
								"key failed!");
						Vector children2 = child1.getChildren();
						XMLElement child2 = (XMLElement) children2.get(0);
						String value = child2.getContent();
						edge.put(key, value);
					}

				}
				Iterator nodeIterator = graph1.iterator();
				while (nodeIterator.hasNext()) {
					Node node = (Node) nodeIterator.next();
					if (node.getComponentId().equals(from)) {
						edge.setStartNode(node);
						node.getEdges().add(edge);
					}
					if (node.getComponentId().equals(to)) {
						edge.setEndNode(node);
						node.getEdges().add(edge);
					}
				}
				graph1.getEdges().add(edge);
			}
		}
		
	
		return graph1;
	}
	
	

	private void printGraph(Graph g) {
		System.out.println("The Graph: "+g.getId()+" (Label="+g.getClassId()+")\n");
		System.out.println("Nodes:");
		for (int i = 0; i < g.size(); i++){
			Node n = (Node) g.get(i);
			System.out.println(n.getComponentId());
			System.out.println(n.getTable().get("FREQUENCY"));
		}
		System.exit(0);
	}

	
}
