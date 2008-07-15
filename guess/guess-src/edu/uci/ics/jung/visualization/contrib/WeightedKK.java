package edu.uci.ics.jung.visualization.contrib;
import java.awt.Dimension;
import java.util.Iterator;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import com.hp.hpl.guess.jung.*;
import com.hp.hpl.guess.Guess;

public class WeightedKK extends AbstractLayout {

    //private static final Object KK_KEY = "KK_Visualization_Key";
    
    private double EPSILON = 0.1d;
    
    private int currentIteration;
    private int maxIterations = 2000;
    private String status = "KKLayout";
    //private Pair key;
    
    private double L;			// the ideal length of an edge
    private double K = 1;		// arbitrary const number
    private DoubleMatrix2D dm;	// distance matrix
    
    private boolean adjustForGravity = true;
    private boolean exchangeVertices = true;
    
    private Vertex[] vertices;
    private Coordinates[] xydata;
    
    private Graph g = null;
    /**
     * Stores graph distances between vertices of the visible graph
     */
    protected DijkstraShortestPath dsp = null;

    /**
     * The diameter of the visible graph. In other words, length of
     * the longest shortest path between any two vertices of the visible graph.
     */
    protected double diameter;
    
    public WeightedKK(Graph g) {
	super(g);
	this.g = g;
	//key = new Pair(this, KK_KEY);
    }
    
    public String getStatus() {
	return status + this.getCurrentSize();
    }
    
    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }
    
    /**
     * This one is an incremental visualization.
     */
    public boolean isIncremental() {
	return true;
    }
    
    /**
     * Returns true once the current iteration has passed the maximum count.
     */
    public boolean incrementsAreDone() {
	if (currentIteration > maxIterations) {
	    return true;
	}
	return false;
    }
    
    protected void initialize_local() {
    }
    
    protected void initializeLocations() {
	super.initializeLocations();
	
	//Random random = new Random(12345L);
	
	Dimension d = getCurrentSize();
	double height = d.getHeight();
	double width = d.getWidth();
	
	//System.out.println("v=" + getGraph().getVertices());
	//int n = getVisibleGraph().numVertices();
        int n = getVisibleVertices().size();
	dm = new DenseDoubleMatrix2D(n, n);
	vertices = new Vertex[n];
	xydata = new Coordinates[n];
	dsp = 
	    new DijkstraShortestPath(g,
				     new NEVMap(Guess.getGraph().getEdgeSchema().getField("weight")));
	
	// assign IDs to all visible vertices
	int index = 0;
	for (Iterator iter = getVisibleVertices().iterator();
	     iter.hasNext(); ) {
	    Vertex v = (Vertex) iter.next();
	    Coordinates xyd = getCoordinates(v);
	    
	    //xyd.setX(random.nextDouble() * width);
	    //xyd.setY(random.nextDouble() * height);
	    
	    vertices[index] = v;
	    xydata[index] = xyd;
	    index++;
	}
	
        // This is practically fast, but it would be the best if we have an
        // implementation of All Pairs Shortest Paths(APSP) algorithm.
	diameter = 0;
	for (int i = 0; i < n - 1; i++) {
	    for (int j = i + 1; j < n; j++) {
                Number dist = dsp.getDistance(vertices[i], vertices[j]);
                if (dist != null && dist.intValue() > diameter)
		    diameter = dist.intValue();
	    }
	}
	
	double L0 = height > width ? width : height;
	L = L0 / diameter * 0.9;
	//L = 0.75 * Math.sqrt(height * width / n);
	
	for (int i = 0; i < n - 1; i++) {
	    for (int j = i + 1; j < n; j++) {
		double dist = getDistance(vertices[i], vertices[j]);
		dm.setQuick(i, j, dist);
		dm.setQuick(j, i, dist);
	    }
	}
    }

	/**
	 * Gets a distance (a length of the shortest path) between
     * the specified vertices.
     * Returned value is used for computing the strength of an embedded spring.
     * You may override this method to visualize a graph with weighted edges.
     * <p>
     * The original Kamada-Kawai algorithm requires a connected graph.
     * That is, pathes must be exist between
     * every pair of vertices in the graph. To visualize a non-connected graph,
     * this method returns (diameter + 1) for vertices that are not connected.
     * <p>
     * The default implementation is as follows:
	 * <pre>
	 *   int dist = unweightedShortestPaths.getShortestPath(v1, v2);
     *   if (dist < 0)
     *      return diameter + 1;
     *   else
     *      return dist;
	 * </pre>
	 */
	protected double getDistance(Vertex v1, Vertex v2) 
    {
        Number n = dsp.getDistance(v1, v2);
        if (n == null)
            return diameter + 1;
        else
            return n.intValue();
	}

	protected void initialize_local_vertex(Vertex v) {
	}

    public void advancePositions() {
		currentIteration++;
		double energy = calcEnergy();
		status = "Kamada-Kawai V=" + getVisibleVertices().size()
            + "(" + getGraph().numVertices() + ")"
			+ " IT: " + currentIteration
			+ " E=" + energy
			;

		int n = getVisibleGraph().numVertices();
        if (n == 0)
            return;

		double maxDeltaM = 0;
		int pm = -1;            // the node having max deltaM
		for (int i = 0; i < n; i++) {
            if (dontMove(vertices[i]))
                continue;
			double deltam = calcDeltaM(i);
			//System.out.println("* i=" + i + " deltaM=" + deltam);
			if (maxDeltaM < deltam) {
				maxDeltaM = deltam;
				pm = i;
			}
		}
		if (pm == -1)
            return;

        for (int i = 0; i < 100; i++) {
			double[] dxy = calcDeltaXY(pm);
			xydata[pm].add(dxy[0], dxy[1]);
			double deltam = calcDeltaM(pm);
            if (deltam < EPSILON)
                break;
            //if (dxy[0] > 1 || dxy[1] > 1 || dxy[0] < -1 || dxy[1] < -1)
            //    break;
		}

		if (adjustForGravity)
			adjustForGravity();

		if (exchangeVertices && maxDeltaM < EPSILON) {
            energy = calcEnergy();
			for (int i = 0; i < n - 1; i++) {
                if (dontMove(vertices[i]))
                    continue;
				for (int j = i + 1; j < n; j++) {
                    if (dontMove(vertices[j]))
                        continue;
					double xenergy = calcEnergyIfExchanged(i, j);
					if (energy > xenergy) {
						double sx = xydata[i].getX();
						double sy = xydata[i].getY();
						xydata[i].setX(xydata[j].getX());
						xydata[i].setY(xydata[j].getY());
						xydata[j].setX(sx);
						xydata[j].setY(sy);
						//System.out.println("SWAP " + i + " with " + j +
						//				   " maxDeltaM=" + maxDeltaM);
						return;
					}
				}
			}
		}
	}

	/**
	 * Shift all vertices so that the center of gravity is located at
	 * the center of the screen.
	 */
	public void adjustForGravity() {
		Dimension d = getCurrentSize();
		double height = d.getHeight();
		double width = d.getWidth();
		double gx = 0;
		double gy = 0;
		for (int i = 0; i < xydata.length; i++) {
			gx += xydata[i].getX();
			gy += xydata[i].getY();
		}
		gx /= xydata.length;
		gy /= xydata.length;
		double diffx = width / 2 - gx;
		double diffy = height / 2 - gy;
		for (int i = 0; i < xydata.length; i++) {
			xydata[i].add(diffx, diffy);
		}
	}

	/**
	 * Enable or disable gravity point adjusting.
	 */
	public void setAdjustForGravity(boolean on) {
		adjustForGravity = on;
	}

	/**
	 * Returns true if gravity point adjusting is enabled.
	 */
	public boolean getAdjustForGravity() {
		return adjustForGravity;
	}

	/**
	 * Enable or disable the local minimum escape technique by
	 * exchanging vertices.
	 */
	public void setExchangeVertices(boolean on) {
		exchangeVertices = on;
	}

	/**
	 * Returns true if the local minimum escape technique by
	 * exchanging vertices is enabled.
	 */
	public boolean getExchangeVertices() {
		return exchangeVertices;
	}

	/**
	 * Determines a step to new position of the vertex m.
	 */
	private double[] calcDeltaXY(int m) {
		double dE_dxm = 0;
		double dE_dym = 0;
		double d2E_d2xm = 0;
		double d2E_dxmdym = 0;
		double d2E_dymdxm = 0;
		double d2E_d2ym = 0;

		for (int i = 0; i < vertices.length; i++) {
			if (i != m) {
				double dist = dm.getQuick(m, i);
				double l_mi = L * dist;
				double k_mi = K / (dist * dist);
				double dx = xydata[m].getX() - xydata[i].getX();
				double dy = xydata[m].getY() - xydata[i].getY();
				double d = Math.sqrt(dx * dx + dy * dy);
				double ddd = d * d * d;

				dE_dxm += k_mi * (1 - l_mi / d) * dx;
				dE_dym += k_mi * (1 - l_mi / d) * dy;
				d2E_d2xm += k_mi * (1 - l_mi * dy * dy / ddd);
				d2E_dxmdym += k_mi * l_mi * dx * dy / ddd;
				//d2E_dymdxm += k_mi * l_mi * dy * dx / ddd;
				d2E_d2ym += k_mi * (1 - l_mi * dx * dx / ddd);
			}
		}
		// d2E_dymdxm equals to d2E_dxmdym.
		d2E_dymdxm = d2E_dxmdym;

		double denomi = d2E_d2xm * d2E_d2ym - d2E_dxmdym * d2E_dymdxm;
		double deltaX = (d2E_dxmdym * dE_dym - d2E_d2ym * dE_dxm) / denomi;
		double deltaY = (d2E_dymdxm * dE_dxm - d2E_d2xm * dE_dym) / denomi;
		return new double[]{deltaX, deltaY};
	}

	/**
	 * Calculates the gradient of energy function at the vertex m.
	 */
	private double calcDeltaM(int m) {
		double dEdxm = 0;
		double dEdym = 0;
		for (int i = 0; i < vertices.length; i++) {
			if (i != m) {
				double dist = dm.getQuick(m, i);
				double l_mi = L * dist;
				double k_mi = K / (dist * dist);

				double dx = xydata[m].getX() - xydata[i].getX();
				double dy = xydata[m].getY() - xydata[i].getY();
				double d = Math.sqrt(dx * dx + dy * dy);

				double common = k_mi * (1 - l_mi / d);
				dEdxm += common * dx;
				dEdym += common * dy;
			}
		}
		return Math.sqrt(dEdxm * dEdxm + dEdym * dEdym);
	}

	/**
	 * Calculates the energy function E.
	 */
	private double calcEnergy() {
		double energy = 0;
		for (int i = 0; i < vertices.length - 1; i++) {
			for (int j = i + 1; j < vertices.length; j++) {
				double dist = dm.getQuick(i, j);
				double l_ij = L * dist;
				double k_ij = K / (dist * dist);
				double dx = xydata[i].getX() - xydata[j].getX();
				double dy = xydata[i].getY() - xydata[j].getY();
				double d = Math.sqrt(dx * dx + dy * dy);


				energy += k_ij / 2 * (dx * dx + dy * dy + l_ij * l_ij -
									  2 * l_ij * d);
			}
		}
		return energy;
	}

	/**
	 * Calculates the energy function E as if positions of the
	 * specified vertices are exchanged.
	 */
	private double calcEnergyIfExchanged(int p, int q) {
		if (p >= q)
			throw new RuntimeException("p should be < q");
		double energy = 0;		// < 0
		for (int i = 0; i < vertices.length - 1; i++) {
			for (int j = i + 1; j < vertices.length; j++) {
				int ii = i;
				int jj = j;
				if (i == p) ii = q;
				if (j == q) jj = p;

				double dist = dm.getQuick(i, j);
				double l_ij = L * dist;
				double k_ij = K / (dist * dist);
				double dx = xydata[ii].getX() - xydata[jj].getX();
				double dy = xydata[ii].getY() - xydata[jj].getY();
				double d = Math.sqrt(dx * dx + dy * dy);
				
				energy += k_ij / 2 * (dx * dx + dy * dy + l_ij * l_ij -
									  2 * l_ij * d);
			}
		}
		return energy;
	}
}

