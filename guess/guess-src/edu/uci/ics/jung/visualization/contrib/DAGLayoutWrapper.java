package edu.uci.ics.jung.visualization.contrib;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.algorithms.transformation.EadesGreedyDAG;
import java.awt.Dimension;

public class DAGLayoutWrapper extends AbstractLayout {
    
    private Graph originalG = null;
    private Graph transformedG = null;
    private DAGLayout myDAG = null;

    public DAGLayoutWrapper(Graph originalG) {
	super(EadesGreedyDAG.eadesGreedyDAG(originalG));
	this.originalG = originalG;
	this.transformedG = super.getGraph();
	this.myDAG = new DAGLayout(transformedG);
    }	
    
    public void advancePositions() {
	myDAG.advancePositions();
    }

    protected void initialize_local_vertex(Vertex v) {
	//myDAG.initialize_local_vertex(v);
    }

    public void initialize_local() {
	myDAG.initialize_local();
    }

    public void initialize(Dimension c) {
	myDAG.initialize(c);
    }

    public boolean isIncremental() {
	return(myDAG.isIncremental());
    }

    public boolean incrementsAreDone() {
	return(myDAG.incrementsAreDone());
    }

    public double getX(Vertex v) {
	return(myDAG.getX((Vertex)v.getEqualVertex(transformedG)));
    }

    public double getY(Vertex v) {
	return(myDAG.getY((Vertex)v.getEqualVertex(transformedG)));
    }

}
