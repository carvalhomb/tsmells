package com.hp.hpl.guess.layout;

import com.hp.hpl.guess.*;
import java.util.*;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.graph.Vertex;

/*
 * <P>
 * The basic way to run this is as follows <P>
 * <CODE> 
 *    SMDS myMDS = new SMDS();<P>
 *    int n = nrows;  // the size of the matrix (n X n) <P>
 *    int xdim = 2;  // the dimensions to scale down to <P>
 *    double[][] dp = new double[n][n]; // construct the initial matrix <P>
 *    // ... fill in the matrix ... // <P>
 *    double[][] xp = new double[n][xdim]; // where to stick in the results<P>
 *    myMDS.d_to_x(n, dp, xp, xdim); // calculate the MDS<P>
 *    myMDS.dumpmat(xp, n, xdim);  // pretty print the result<P>
 * </CODE>
 * <P>
 * @author Hacked by Eytan Adar
 * @version 0.1
 */
public class SMDS extends AbstractLayout {

    private static final double MIN_EIGENVALUE = 0.00001;
    
    private static int debug = 0;
    
    Graph g = null;

    HashMap locations = new HashMap();

    public SMDS(Graph g) {
	super(g);
	this.g = g;
	Iterator it = g.getNodes().iterator();
	while(it.hasNext()) {
	    Node n = (Node)it.next();
	    locations.put(n, new Coordinates(n.getX(),
					     n.getY()));
	}
    }

    public void advancePositions() {
	
	if (done) 
	    return;

	int nodecount = g.getNodes().size();
	double[][] dp = new double[nodecount][nodecount];

	// initialize to 0
	for (int i = 0 ; i < nodecount ; i++) {
	    for (int j = 0 ; j < nodecount ; j++) {
		dp[i][j] = 1;
	    }
	}

	Hashtable index = new Hashtable();
	ArrayList al = new ArrayList(nodecount);

	Iterator it = g.getEdges().iterator();
	while(it.hasNext()) {
	    Edge e = (Edge)it.next();
	    Node n1 = e.getNode1();
	    Node n2 = e.getNode2();
	    if (n1 == n2) {
		continue;
	    }

	    int i1 = 0;
	    if (!index.containsKey(n1)) {
		al.add(n1);
		i1 = al.size() - 1;
		index.put(n1,new Integer(i1));
	    } else {
		i1 = ((Integer)index.get(n1)).intValue();
	    }

	    int i2 = 0;
	    if (!index.containsKey(n2)) {
		al.add(n2);
		i2 = al.size() - 1;
		index.put(n2,new Integer(i2));
	    } else {
		i2 = ((Integer)index.get(n2)).intValue();
	    }

	    if (e instanceof UndirectedEdge) {
		dp[i1][i2] = e.edgeWeight();
		dp[i2][i1] = e.edgeWeight();
	    } else {
		dp[i1][i2] = e.edgeWeight();
	    }
	}

	//dumpmat(dp, nodecount,nodecount);

	double[][] xp = new double[nodecount][2];
	SMDS.d_to_x(nodecount,dp,xp,2);

	//dumpmat(xp, nodecount, 2);
	for (int i = 0 ; i < al.size() ; i++) {
	    double x = xp[i][0] * 1000;
	    double y = xp[i][1] * 1000;
	    Node n = (Node)al.get(i);
	    Coordinates c = new Coordinates(x,y);
	    locations.put(n,c);
	}
    }

    private static void eigensystem(double[][] ap, int n, 
				    double[][] vp, double[] dp) {
	int nrotations = jacobi(ap, n, dp, vp);
	eigsrt(dp, vp, n);
	return;
    }

    public static void d_to_b(int n, double[][] d, double b[][]) {
	int i,j, k;
	double aj, ai, d2, dsq;
    
	dsq = 0.0;
	for (i=0; i<n; i++) {
	    for (j=0; j<n; j++) {
		dsq += d[i][j]*d[i][j];
	    }
	}
	dsq = dsq/(n*n);
    
	for (i=0; i<n; i++)
	    for (j=0; j<n; j++) {
		ai = 0.0;
		for (k=0; k<n; k++)
		    ai += d[i][k]*d[i][k];
		aj = 0.0;
		for (k=0; k<n; k++)
		    aj += d[k][j]*d[k][j];
		d2 = d[i][j]*d[i][j];
		b[i][j] = -0.5*(d2 - ai/n - aj/n + dsq);
	    }
	//dumpmat(b,n,n);
	//dumpmat(d,n,n);
    }

    public static void b_to_x(int n, double[][] b, double[][] x, int xdim) {
	double[][] v;
	double[] d;
	double c;
	int i,j;
   
    
	v = new double[n][n];
	d = new double[n];
	eigensystem(b, n, v, d);
	//dumpmat(v,n,n);
	//dumpmat(b,n,n);
	//dumpmat(x,n,xdim);
	//dumpvec(d,n);
	for (j=0; j<n; j++) {
	    if (d[j] < MIN_EIGENVALUE) {
		if (debug > 0)
		    System.err.println("truncationg eigenvalue + " + d[j] + "\n");
		d[j] = 0.0;
	    }
	    d[j] = Math.sqrt(d[j]);
	}
	for (i=0; i<n; i++) {
	    for (j=0; j<xdim; j++) {
		x[i][j] = d[j]*v[i][j];
	    }
	}
    }


    public static void dumpmat(double[][] mat, int n, int m) {
	int i,j;
	String out = "";
	for (i=0; i < n; i++) {
	    for (j=0; j < m; j++) {
		out = ("" + mat[i][j]);
		if (mat[i][j] < 0) {
		    if (out.length() > 6)
			out = out.substring(0,6);
		} else {
		    if (out.length() > 5)
			out = out.substring(0,5);
		}
		System.out.print(out+"\t");
	    }
	    System.out.println("");
	}
	System.out.println("\n\n");
    }

    public static void dumpvec(double[] vec, int n) {
	int i;
    
	for (i=0; i<n; i++)
	    System.out.print(vec[i]+"\t");
	System.out.println("\n\n");
    }
  
    public static void d_to_x(int n, double[][] d, double[][] x, int xdim) {
	double[][] b = new double[n][n];
    
	if (debug > 1)
	    System.err.println("In d_to_x\n");
	d_to_b(n, d, b);
	if (debug > 1) {
	    System.err.println("B:\n");
	    dumpmat(b, n, n);
	}
	b_to_x(n, b, x, xdim);
    }

    private static int jacobi(double[][] a, int n, double[] d, double[][] v) {
	int j,iq,ip,i;
	double tresh,theta,tau,t,sm,s,h,g,c;
	double[] b;
	double[] z;
	int nrot;
    
	b = new double[n];
	z = new double[n];

	for (ip=0;ip<n;ip++) {
	    for (iq=0;iq<n;iq++) v[ip][iq]=0.0;
	    v[ip][ip]=1.0;
	}
	//dumpmat(v,n,n);
	for (ip=0;ip<n;ip++) {
	    b[ip]=d[ip]=a[ip][ip];
	    z[ip]=0.0;
	}
	//dumpvec(b,n);
	//dumpvec(d,n);
	//dumpvec(z,n); 
	nrot=0;

	for (i=0;i<50;i++) {
	    if (debug > 1) {
		System.out.println("a");
		dumpmat(a,n,n);
		System.out.println("v");
		dumpmat(v,n,n);
	    }
	    sm=0.0;
      
	    for (ip=0;ip<n-1;ip++) {
		for (iq=ip+1;iq<n;iq++)
		    sm += Math.abs(a[ip][iq]);
	    }

	    if (sm == 0.0) {
		return(nrot);
	    }

	    if (i < 4) {
		tresh = 0.2*sm / (n*n);
	    } else {
		tresh=0.0;
	    }

	    //dumpmat(a,n,n);
	    for (ip=0;ip<=n-1;ip++) {
		for (iq=ip+1;iq<n;iq++) {
		    g=100.0*Math.abs(a[ip][iq]);
		    if ((i > 4) &&
			(Math.abs(d[ip])+g == Math.abs(d[ip])) && 
			Math.abs(d[iq])+g == Math.abs(d[iq])) {
			a[ip][iq]=0.0;
		    }
		    else if (Math.abs(a[ip][iq]) > tresh) {
			h=d[iq]-d[ip];
			if (Math.abs(h)+g == Math.abs(h))
			    t=(a[ip][iq])/h;
			else {
			    theta=0.5*h/(a[ip][iq]);
			    t=1.0/(Math.abs(theta)+Math.sqrt(1.0+theta*theta));
			    if (theta < 0.0) t = -t;
			}
			c=1.0/Math.sqrt(1+t*t);
			s=t*c;
			tau=s/(1.0+c);
			h=t*a[ip][iq];
			z[ip] -= h;
			z[iq] += h;
			d[ip] -= h;
			d[iq] += h;
			a[ip][iq]=0.0;
			for (j=0;j<ip-1;j++) {
			    g=a[j][ip];
			    h=a[j][iq];
			    a[j][ip]=g-s*(h+g*tau);
			    a[j][iq]=h+s*(g-h*tau);
			}
			for (j=ip+1;j<iq-1;j++) {
			    g=a[ip][j];
			    h=a[j][iq];
			    a[ip][j]=g-s*(h+g*tau);
			    a[j][iq]=h+s*(g-h*tau);
			}

			for (j=iq+1;j<n;j++) {
			    g=a[ip][j];
			    h=a[iq][j];
			    a[ip][j]=g-s*(h+g*tau);
			    a[iq][j]=h+s*(g-h*tau);
			}
	    
			for (j=0;j<n;j++) {
			    g=v[j][ip];
			    h=v[j][iq];
			    v[j][ip]=g-s*(h+g*tau);
			    v[j][iq]=h+s*(g-h*tau);
			}
			nrot++;
		    }
		}
	    }
	    for (ip=0;ip<n;ip++) {
		b[ip] += z[ip];
		d[ip]=b[ip];
		z[ip]=0.0;
	    }
	}
	System.err.println("Too many iterations in routine JACOBI\n");
	return(nrot);
    }


    private static void eigsrt(double[] d, double[][] v, int n) {
	int k,j,i;
	double p;
    
	for (i=0;i<n;i++) {
	    p=d[k=i];
	    for (j=i+1;j<n;j++)
		if (d[j] >= p) p=d[k=j];
	    if (k != i) {
		d[k]=d[i];
		d[i]=p;
		for (j=0;j<n;j++) {
		    p=v[j][i];
		    v[j][i]=v[j][k];
		    v[j][k]=p;
		}
	    }
	}
    }

    public double getX(Vertex n) {
	Coordinates d2d = (Coordinates)locations.get(n);
	return(d2d.getX());
    }

    public double getY(Vertex n) {
	Coordinates d2d = (Coordinates)locations.get(n);
	return(d2d.getY());
    }

    public Coordinates getCoordinates(Node v) {
	return((Coordinates)locations.get(v));
    }

    public boolean done = false;

    public boolean incrementsAreDone() {
	return(done);
    }

    public void initialize_local_vertex(edu.uci.ics.jung.graph.Vertex v) {
    }

    public void initialize_local() {
    }

    public boolean isIncremental() {
	return(false);
    }    
}

