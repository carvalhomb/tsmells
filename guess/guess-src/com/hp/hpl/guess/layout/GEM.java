// $Id: GEM.java,v 1.2 2006/09/22 20:43:26 eytanadar Exp $
package com.hp.hpl.guess.layout;

import java.util.*;

import com.hp.hpl.guess.*;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.graph.Vertex;

/**
 * Java implementation of the gem 2D layout.
 * <br>
 * The algorithm needs to get various subgraphs and traversals.
 * The recursive nature of the algorithm is totally captured
 * within those subgraphs and traversals. The main loop of the
 * algorithm is then expressed using the iterator feature, which makes it
 * look like a simple flat iteration over nodes.
 *
 * @author David Duke
 * @author Hacked by Eytan Adar for Guess
 */

public class GEM extends AbstractLayout {

    public final static String Name = "GEM Force-directed";
    
    private static Graph graph;
    
    private static Set nodes;
    private static Set edges;
    
    private static int nodeCount;
    private static int edgeCount;
    
    //
    // GEM Constants
    //
    private static final int ELEN = 128;
    private static final int ELENSQR = ELEN * ELEN;
    private static final int MAXATTRACT	= 1048576;
    
    //
    // GEM Defualt Parameter Values
    //
    private static final float IMAXTEMPDEF	   = (float)1.0;
    private static final float ISTARTTEMPDEF   = (float)0.3;
    private static final float IFINALTEMPDEF   = (float)0.05;
    private static final int   IMAXITERDEF	   = 10;
    private static final float IGRAVITYDEF	   = (float)0.05;
    private static final float IOSCILLATIONDEF = (float)0.4;
    private static final float IROTATIONDEF    = (float)0.5;
    private static final float ISHAKEDEF       = (float)0.2;
    private static final float AMAXTEMPDEF     = (float)1.5;
    private static final float ASTARTTEMPDEF   = (float)1.0;
    private static final float AFINALTEMPDEF   = (float)0.02;
    private static final int   AMAXITERDEF     = 3;
    private static final float AGRAVITYDEF     = (float)0.1;
    private static final float AOSCILLATIONDEF = (float)0.4;
    private static final float AROTATIONDEF    = (float)0.9;
    private static final float ASHAKEDEF       = (float)0.3;
    private static final float OMAXTEMPDEF     = (float)0.25;
    private static final float OSTARTTEMPDEF   = (float)1.0;
    private static final float OFINALTEMPDEF   = (float)1.0;
    private static final int   OMAXITERDEF     = 3;
    private static final float OGRAVITYDEF     = (float)0.1;
    private static final float OOSCILLATIONDEF = (float)0.4;
    private static final float OROTATIONDEF    = (float)0.9;
    private static final float OSHAKEDEF       = (float)0.3;

    //
    // GEM variables
    //
    private static long 	iteration;
    private static long		temperature;
    private static int		centerX, centerY;
    private static long		maxtemp;
    private static float    oscillation, rotation;

    // Following parameters can be initialised in the original GEM
    // from a configuration file.  Here they are hard-wired, but
    // this could be replaced by configuration from a royere file.
    // (NB how to make this compatible with the "optionality" of
    // modules in Royere?
    private float	i_maxtemp	= IMAXTEMPDEF;
    private float	a_maxtemp	= AMAXTEMPDEF;
    private float	o_maxtemp	= OMAXTEMPDEF;
    private float	i_starttemp	= ISTARTTEMPDEF;
    private float	a_starttemp	= ASTARTTEMPDEF;
    private float	o_starttemp	= OSTARTTEMPDEF;
    private float	i_finaltemp	= IFINALTEMPDEF;
    private float	a_finaltemp	= AFINALTEMPDEF;
    private float	o_finaltemp	= OFINALTEMPDEF;
    private int     i_maxiter	= IMAXITERDEF;
    private int     a_maxiter	= AMAXITERDEF;
    private int     o_maxiter	= OMAXITERDEF;
    private float	i_gravity	= IGRAVITYDEF;
    private float	i_oscillation	= IOSCILLATIONDEF;
    private float	i_rotation	= IROTATIONDEF;
    private float	i_shake		= ISHAKEDEF;
    private float	a_gravity	= AGRAVITYDEF;
    private float	a_oscillation	= AOSCILLATIONDEF;
    private float	a_rotation	= AROTATIONDEF;
    private float	a_shake		= ASHAKEDEF;
    private float	o_gravity	= OGRAVITYDEF;
    private float	o_oscillation	= OOSCILLATIONDEF;
    private float	o_rotation	= OROTATIONDEF;
    private float	o_shake		= OSHAKEDEF;

    private Graph g = null;

    /** 
     * Constructor  
     */
    public GEM(Graph g, long seed) {
	this(g);
	rand.setSeed(seed);
    }

    /** 
     * Constructor  
     */
    public GEM(Graph g) {
	super(g);
	if (Guess.getGPLFreeMode()) {
	    throw(new Error("Running in GPL Free Mode, you will not be able to use this class"));
	}
	this.g = g;
    }

    protected double defaultNodeSize = 1.0;
  
    protected double setDefaultNodeSize() {
	return 1.0;
    }

    public String getName() {
	return Name;
    }
  
 
    private static final String GEM2D = "GEM 2D Parameters";

    static class GemP {

	public int x, y;       // position
	public int in;
    
	public int iX, iY;    // impulse
	public float dir;    // direction
	public float heat;        // heat
	public float mass;      // weight = nr edges
	public boolean mark;

	public GemP(int m) {
	    x  = 0;
	    y  = 0;
	    iX = iY = 0;
	    dir  = (float)0.0;
	    heat = 0;
	    mass = m;
	    mark = false;
	}
    }

    private GemP gemProp[];
    private Node invmap[];
    private ArrayList adjacent[];
    private HashMap nodeNumbers;
    
    private Random rand = new Random();

    public int rand() {
	return (int)(rand.nextDouble() * Integer.MAX_VALUE);
    }


    private int map[];
  
    private int select() {

	int	u;
	int 	n, v;
 
	if (iteration == 0) {
	    // System.out.print( "New map for " + nodeCount );
	    map = new int[nodeCount];
	    for (int i = 0; i < nodeCount; i++)
		map[i] = i;
	}
	n = (int)(nodeCount - iteration % nodeCount);
	v = rand () % n;  // was 1 + rand() % n due to numbering in GEM
	if (v == nodeCount) v--;
	if (n == nodeCount) n--;
	// System.out.println( "Access n = " + n + " v = " + v );
	u = map[v]; map[v] = map[n]; map[n] = u;
	return u;
    }

    private LinkedList q;

    private int bfs(int root) {

	Integer uint;
	Iterator nodeSet;
	int  v, ui;
 
	if (root >= 0) {
	    q = new LinkedList();
	    if (!gemProp[root].mark) {    // root > 0
		for (int vi = 0; vi < nodeCount; vi++) {
		    gemProp[vi].in = 0;
		}
	    }
	    else
		gemProp[root].mark = true;  // root = -root;
	    q.addFirst(new Integer(root));
	    gemProp[root].in = 1;
	}
	if (q.size() == 0)
	    return -1;    // null
	v = (((Integer)(q.removeLast())).intValue());

	nodeSet = adjacent[v].iterator();
	while (nodeSet.hasNext()) {
	    uint = (Integer)nodeSet.next();
	    ui = uint.intValue();
	    if (gemProp[ui].in != 0) {
		q.addFirst(uint);
		gemProp[ui].in = gemProp[v].in + 1;
	    }
	}
	return v;
    }

    private int graph_center() {

	Iterator nodeSet;
	GemP p;
	int  c, u, v, w;  // nodes
	int  h;

	c = -1;       // for a contented compiler.
	u = -1;
    
	h = nodeCount + 1;
	for (w = 0; w < nodeCount; w++) {
	    v = bfs (w);
	    while (v >= 0 && gemProp[v].in < h) {
		u = v;
		v = bfs(-1);  // null
	    }
	    p = gemProp[u];
	    if (p.in < h) {
		h = p.in;
		c = w;
	    }
	}
	return c;
    }

    private void vertexdata_init (final float starttemp) {

	Iterator nodeSet;
	GemP p;

	temperature = 0;
	centerX = centerY = 0;

	for (int v = 0; v < nodeCount; v++) {
	    p = gemProp[v];
	    p.heat = starttemp * ELEN;
	    temperature += p.heat * p.heat;
	    p.iX = p.iY = 0;
	    p.dir = 0;
	    p.mass = 1 + gemProp[v].mass / 3;
	    centerX += p.x;
	    centerY += p.y;
	}
	// srand ((unsigned) time (NULL));
    }


    /*
   * INSERT code from GEM
   */

  /*
   * Nasty using global variables to handle return params,
   * but there are too many vectors in this code!
   */
   
    private int i_impulseX, i_impulseY;
  
    private void i_impulse(int v) {

	Iterator nodeSet;
 
	int	iX, iY, dX, dY, pX, pY;
	int	n;
	GemP	p, q;

	p  = gemProp[v];
	pX = p.x;
	pY = p.y;

	n  = (int)(i_shake * ELEN);
	iX = rand () % (2 * n + 1) - n;
	iY = rand () % (2 * n + 1) - n;
	iX += (centerX / nodeCount - pX) * p.mass * i_gravity;
	iY += (centerY / nodeCount - pY) * p.mass * i_gravity;

	for (int u = 0; u < nodeCount; u++) {
	    q = gemProp[u];
	    if (q.in > 0) {
		dX = pX - q.x;
		dY = pY - q.y;
		n = dX * dX + dY * dY;
		if (n > 0) {
		    iX += dX * ELENSQR / n;
		    iY += dY * ELENSQR / n;
		}
	    }
	}
	nodeSet = adjacent[v].iterator();
	int u;
	while (nodeSet.hasNext()) {
	    u = ((Integer)nodeSet.next()).intValue();
	    q = gemProp[u];
	    if (q.in > 0) {
		dX = pX - q.x;
		dY = pY - q.y;
		n = (int)((dX * dX + dY * dY) / p.mass);
		n = Math.min(n, MAXATTRACT);
		iX -= dX * n / ELENSQR;
		iY -= dY * n / ELENSQR;
	    }
	}
	i_impulseX = iX;
	i_impulseY = iY;
    }

    public void insert() {

	Iterator nodeSet2;
	GemP p, q;
	int startNode;
    
	int 	v, w;

	int		d;

	//      System.out.println( "insert phase" );
    
	vertexdata_init(i_starttemp);
     
	oscillation = i_oscillation;
	rotation  = i_rotation;
	maxtemp   = (int)(i_maxtemp * ELEN);

	v = graph_center();


	for (int ui = 0; ui < nodeCount; ui++) {
	    gemProp[ui].in = 0;
	}

	gemProp[v].in = -1;

	startNode = -1;
	for (int i = 0; i < nodeCount; i++) {
	    d = 0;
	    for (int u = 0; u < nodeCount; u++) {
		if (gemProp[u].in < d) {
		    d = gemProp[u].in;
		    v = u;
		}
	    }
	    gemProp[v].in = 1;

	    nodeSet2 = adjacent[v].iterator();
	    int u;
	    while (nodeSet2.hasNext()) {
		u = ((Integer)nodeSet2.next()).intValue();
		if (gemProp[u].in <= 0)
		    gemProp[u].in--;
	    }
	    p = gemProp[v];
	    p.x = p.y = 0;

	    if (startNode >= 0) {
		d = 0;
		p = gemProp[v];
		nodeSet2 = adjacent[v].iterator();
		while (nodeSet2.hasNext()) {
		    w = ((Integer)nodeSet2.next()).intValue();
		    q = gemProp[w];
		    if (q.in > 0) {
			p.x += q.x;
			p.y += q.y;
			d++;
		    }
		}
		if (d > 1) {
		    p.x /= d;
		    p.y /= d;
		}
		d = 0;
		while ((d++ < i_maxiter) && (p.heat > i_finaltemp * ELEN)) {
		    i_impulse(v);
		    displace( v, i_impulseX, i_impulseY );
		}

	    }
	    else {
		startNode = i;
	    }
	}
    }

    private void displace(int v, int iX, int iY) {

	int t;
	int		n;
	GemP p;

	if (iX != 0 || iY != 0) {
	    n = Math.max ( Math.abs(iX), Math.abs(iY)) / 16384;
	    if (n > 1) {
		iX /= n;
		iY /= n;
	    }
	    p  = gemProp[v];
	    t  = (int)p.heat;
	    n  = (int)Math.sqrt( iX*iX + iY*iY );
	    iX = iX * t / n;
	    iY = iY * t / n;
	    p.x += iX;
	    p.y += iY;
	    centerX += iX;
	    centerY += iY;
	    // imp = &vi[v].imp;
	    n = t * (int)Math.sqrt(p.iX*p.iX + p.iY*p.iY);
	    if (n > 0) {
		temperature -= t * t;
		t += t * oscillation * (iX * p.iX + iY * p.iY) / n;
		t = (int)Math.min(t, maxtemp);
		p.dir += rotation * (iX * p.iY - iY * p.iX) / n;
		t -= t * Math.abs(p.dir) / nodeCount;
		t = Math.max(t, 2);
		temperature += t * t;
		p.heat = t;
	    }
	    p.iX = iX;
	    p.iY = iY;
	}
    }

    void a_round() {

	Iterator nodeSet;
	int v;

	int   iX, iY, dX, dY;
	int	n;
	int		pX, pY;
	GemP p, q;


	for (int i = 0; i < nodeCount; i ++) {
	    v = select();
	    p = gemProp[v];

	    pX = p.x;
	    pY = p.y;

	    n = (int)(a_shake * ELEN);
	    iX = rand () % (2 * n + 1) - n;
	    iY = rand () % (2 * n + 1) - n;
	    iX += (centerX / nodeCount - pX) * p.mass * a_gravity;
	    iY += (centerY / nodeCount - pY) * p.mass * a_gravity;

      
	    for (int u = 0; u < nodeCount; u++) {
		q  = gemProp[u];
		dX = pX - q.x;
		dY = pY - q.y;
		n  = dX * dX + dY * dY;
		if (n > 0) {
		    iX += dX * ELENSQR / n;
		    iY += dY * ELENSQR / n;
		}
	    }
	    nodeSet = adjacent[v].iterator();
	    int u;
	    while (nodeSet.hasNext()) {
		u  = ((Integer)nodeSet.next()).intValue();
		q  = gemProp[u];
		dX = pX - q.x;
		dY = pY - q.y;
		n  = (int)((dX * dX + dY * dY) / p.mass);
		n  = (int)Math.min(n, MAXATTRACT);
		iX -= dX * n / ELENSQR;
		iY -= dY * n / ELENSQR;
	    }
	    displace(v, iX, iY);
	    iteration++;
	}
    }


    private void arrange() {

	long stop_temperature;
	long stop_iteration;

	vertexdata_init(a_starttemp);

	oscillation    = a_oscillation;
	rotation     = a_rotation;
	maxtemp      = (int)(a_maxtemp * ELEN);
	stop_temperature = (int)(a_finaltemp * a_finaltemp * ELENSQR * nodeCount);
	stop_iteration   = a_maxiter * nodeCount * nodeCount;
	iteration    = 0;
     
	//      System.out.print( "arrange phase -- temp " );
	//      System.out.print( stop_temperature + " iter ");
	//      System.out.println ( stop_iteration );
    


	while (temperature > stop_temperature && iteration < stop_iteration) {
	    //com.hp.hpl.guess.ui.StatusBar.setValue((int)stop_iteration,
	    //			       (int)iteration);
	    a_round();
	}
	//com.hp.hpl.guess.ui.StatusBar.setValue(100,0);
    }


    /*
   * Optimisation Code
   */


    private int EVdistX, EVdistY;
  
    private void EVdistance (int thisNode, int thatNode,  int v) {

	GemP thisGP = gemProp[thisNode];
	GemP thatGP = gemProp[thatNode];
	GemP nodeGP = gemProp[v];

	int aX = thisGP.x;
	int aY = thisGP.y;
	int bX = thatGP.x;
	int bY = thatGP.y;
	int cX = nodeGP.x;
	int cY = nodeGP.y;
    
	long m, n;

	bX -= aX; bY -= aY;           /* b' = b - a */
	m = bX * (cX - aX) + bY * (cY - aY);  /* m = <b'|c-a> = <b-a|c-a> */
	n = bX * bX + bY * bY;          /* n = |b'|^2 = |b-a|^2 */
	if (m < 0) m = 0;
	if (m > n) m = n = 1;
	if ((m >> 17) > 0)	{  	/* prevent integer overflow */
	    n /= m >> 16;
	    m /= m >> 16;
	}
	if (n != 0) {
	    aX += (int)(bX * m / n);	  /* a' = m/n b' = a + m/n (b-a) */
	    aY += (int)(bY * m / n);
	}
	EVdistX = aX;
	EVdistY = aY;
    }


    private int o_impulseX, o_impulseY;

    private void o_impulse (int v) {

	Iterator edgeSet;
	int		u, w;
	Edge			e;
	int	iX, iY, dX, dY;
	int	n;
	GemP p, up, wp;
	int pX, pY;

	p = gemProp[v];
	pX = p.x; pY = p.y;

	n  =  (int)(o_shake * ELEN);
	iX =  rand () % (2 * n + 1) - n;
	iY =  rand () % (2 * n + 1) - n;
	iX += (centerX / nodeCount - pX) * p.mass * o_gravity;
	iY += (centerY / nodeCount - pY) * p.mass * o_gravity;

	edgeSet = edges.iterator();
	while (edgeSet.hasNext()) {
	    e = (Edge)edgeSet.next();
	    u = ((Integer)nodeNumbers.get(e.getNode1())).intValue();
	    w = ((Integer)nodeNumbers.get(e.getNode2())).intValue();
	    if (u != v && w != v) {
		up = gemProp[u];
		wp = gemProp[w];
		dX = (up.x + wp.x) / 2 - pX;
		dY = (up.y + wp.y) / 2 - pY;
		n = dX * dX + dY * dY;
		if (n < 8 * ELENSQR) {
		    EVdistance(u,w, v);  // source, dest, vert
		    dX = EVdistX;
		    dY = EVdistY;
		    dX -= pX;
		    dY -= pY;
		    n = dX * dX + dY * dY;
		}
		if (n > 0) {
		    iX -= dX * ELENSQR / n;
		    iY -= dY * ELENSQR / n;
		}
	    }
	    else {
		if (u == v)
		    u = w;
		up = gemProp[u];
		dX = pX - up.x;
		dY = pY - up.y;
		n = (int)((dX * dX + dY * dY) / p.mass);
		n = Math.min (n, MAXATTRACT);
		iX -= dX * n / ELENSQR;
		iY -= dY * n / ELENSQR;
	    }
	}
	o_impulseX = iX;
	o_impulseY = iY;
    }


    private void o_round() {

	int v;
	for (int i = 0; i < nodeCount; i ++) {
	    v = select();
	    o_impulse(v);
	    displace (v, o_impulseX, o_impulseY );
	    iteration ++;
	}
    }


    private void optimize () {

	long	stop_temperature;
	long	stop_iteration;

	vertexdata_init(o_starttemp);
	oscillation = o_oscillation;
	rotation  = o_rotation;
	maxtemp   = (int)(o_maxtemp * ELEN);
	stop_temperature = (int)(o_finaltemp * o_finaltemp * ELENSQR * nodeCount);
	stop_iteration   = o_maxiter * nodeCount * nodeCount;

	//      System.out.print( "optimise phase -- temp " );
	//      System.out.print( stop_temperature + " iter ");
	//      System.out.println ( stop_iteration );
    
	while (temperature > stop_temperature && iteration < stop_iteration) {
	    o_round();
	    if ((iteration % 20000) == 0) {
		//            System.out.println( iteration + "\t" + temperature );
	    }
	}
    }

    /*
   * Royere main layout method
   */
  
    public void computePositions() {
      
	long startTime, endTime;
      
	startTime = System.currentTimeMillis();
      
	Integer nodeNr;
	Node n;
	GemP p;
      
	graph = g;
      
	nodes = graph.getNodes();
	edges = graph.getEdges();
      
	nodeCount = nodes.size();
	edgeCount = edges.size();
      
	gemProp = new GemP[nodeCount];
	invmap  = new Node[nodeCount];
	adjacent = new ArrayList[nodeCount];
	nodeNumbers = new HashMap();
    
	Iterator nodeSet = nodes.iterator();
	for (int i = 0; nodeSet.hasNext(); i++) {
	    n = (Node)nodeSet.next();
	    gemProp[i] = new GemP(n.getOutEdges().size());
	    invmap[i]  = n;
	    nodeNumbers.put(n,new Integer(i));
	}
	Iterator neighbors;
	Set nset;
	for (int i = 0; i < nodeCount; i++) {
	    nset = invmap[i].getNeighbors();
	    neighbors = nset.iterator();
	    adjacent[i] = new ArrayList( nset.size() );
	    for (int j=0; neighbors.hasNext(); j++) {
		n = (Node)neighbors.next();
		nodeNr = (Integer)nodeNumbers.get(n);
		adjacent[i].add( nodeNr );
	    }
	}  


	if (i_finaltemp < i_starttemp)
	    insert();
	if (a_finaltemp < a_starttemp)
	    arrange();
	if (o_finaltemp < o_starttemp)
	    optimize();
    
    
    
	for (int i = 0; i < nodeCount; i++) {
	    p = gemProp[ i ];
	    n = invmap[i];

	    myDone.put(n,new Coordinates(p.x,p.y));

	}     
	Rescale.rescalePositions(graph.getNodes(),
				 .25,
				 0,
				 myDone);

	endTime = System.currentTimeMillis();
	done = true;
    }

    private HashMap myDone = new HashMap();

    public double getX(Vertex n) {
	Coordinates d2d = (Coordinates)myDone.get(n);
	return(d2d.getX());
    }

    public double getY(Vertex n) {
	Coordinates d2d = (Coordinates)myDone.get(n);
	return(d2d.getY());
    }

    public Coordinates getCoordinates(Node v) {
	return((Coordinates)myDone.get(v));
    }

    public boolean done = false;

    public boolean incrementsAreDone() {
	return(done);
    }

    public void advancePositions() {
	if (!done)
	    computePositions();
    }

    public void initialize_local_vertex(edu.uci.ics.jung.graph.Vertex v) {
    }

    public void initialize_local() {
    }

    public boolean isIncremental() {
	return(false);
    }
}


