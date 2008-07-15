/*
 * Copyright (c) 2004, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 * 
 * Created on Aug 12, 2004
 */
package edu.uci.ics.jung.algorithms.cluster;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.uci.ics.jung.algorithms.cluster.KMeansClusterer.NotEnoughClustersException;
import edu.uci.ics.jung.algorithms.cluster.VoltageClusterer;
import edu.uci.ics.jung.algorithms.importance.VoltageRanker;
import edu.uci.ics.jung.graph.ArchetypeGraph;
import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.UserDatumNumberVertexValue;
import edu.uci.ics.jung.statistics.DiscreteDistribution;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.decorators.ConstantEdgeValue;
import edu.uci.ics.jung.graph.decorators.Indexer;
import edu.uci.ics.jung.graph.decorators.NumberEdgeValue;
import edu.uci.ics.jung.graph.decorators.NumberVertexValue;

/**
 * <p>Clusters vertices of a <code>Graph</code> based on their ranks as 
 * calculated by <code>VoltageRanker</code>.  This algorithm is based on
 * the method described in the paper below.
 * 
 * <p>The algorithm proceeds as follows:
 * <ul>
 * <li/>for each weakly connected component above the average cluster size: 
 * <li/>first, generate a set of candidate clusters as follows:
 *      <ul>
 *      <li/>pick random vertex pair, run VoltageRanker
 *      <li/>rank order vertices by voltages 
 *      <li/>identify high and low 'candidate' clusters starting from the source and sink
 *           by looking for the largest voltage drop between vertices within 
 *           50%-200% of the desired cluster size from either end of the voltage spectrum
 *      <li/> 
 *      <li/>store resulting candidate clusters
 *      </ul>
 *
 * <li/>second, generate clusters as follows:
 *      <ul>
 *      <li/>pick a vertex v as a cluster 'seed'
 *           <br>(Wu/Huberman: most frequent vertex in candidate clusters)
 *      <li/>calculate co-occurrence over all candidate clusters of v with each other
 *           vertex
 *      <li/>add all vertices to v's cluster if they occur in 50% or more of 
 *            the same candidate clusters as v
 *      <li/>if resulting cluster size is within 50-200% of the desired cluster size, remove v's vertices from candidate clusters; 
 *           continue
 *      <li/> repeat to find smaller clusters from the remaining vertices
 *      </ul>
 * <li/>finally, remaining unassigned vertices are assigned to the kth ("garbage")
 * cluster.
 * </ul></p>
 * 
 * <p><b>NOTE</b>: Depending on how the co-occurrence data splits the data into
 * clusters, the number of clusters returned by this algorithm may be less or more than the
 * number of clusters requested. </p>
 * 
 * @author Lada Adamic
 * @modified from Joshua O'Madadhain's VoltageClusterer
 * @see <a href="http://www.hpl.hp.com/research/idl/papers/linear/">'Finding communities in linear time: a physics approach', Fang Wu and Bernardo Huberman</a>
 * @see VoltageRanker
 * @see KMeansClusterer
 */
public class VoltageClustererL extends VoltageClusterer
{
    
    private int volt_rank_iterations;
    private double volt_rank_convergence;
    private NumberEdgeValue e_weight_map = new ConstantEdgeValue(1);

    /**
     * this will replace the voltageranker with one that reads
     * edge weights from a specific place
     */
    public void setEdgeWeight(NumberEdgeValue edge_weights) {
	e_weight_map = edge_weights;
	super.vr = new VoltageRanker(edge_weights,
				     super.vv,
				     volt_rank_iterations,
				     volt_rank_convergence);
    }

    /**
     * Creates an instance of a VoltageClusterL with the specified parameters.
     * 
     * @param num_candidates    the number of candidate clusters to create
     * @param rank_iterations   the number of iterations to run VoltageRanker
     */
    public VoltageClustererL(int num_candidates, int rank_iterations, 
			     double rank_convergence)
    {
        super(num_candidates, rank_iterations, rank_convergence, 100, 0.01);   
	volt_rank_iterations = rank_iterations;
	volt_rank_convergence = rank_convergence;
    }
    
    
    /**
     * Returns a community (cluster) centered around <code>v</code>.
     * @param v the vertex whose community we wish to discover
     */
    public Collection getCommunity(ArchetypeGraph g, ArchetypeVertex v)
    {
        return cluster_internal(g, v, 1, -1,num_candidates);
    }

			/**
     * Returns a community (cluster) centered around <code>v</code>.
     * @param v the vertex whose community we wish to discover
     */
    public Collection getCommunity(ArchetypeGraph g, 
				   ArchetypeVertex v, 
				   int communitysize)
    {
        return cluster_internal(g, v, 1, communitysize,num_candidates);
    }
    
    /**
     * Clusters the vertices of <code>g</code> into 
     * <code>num_clusters</code> clusters, based on their connectivity.
     * @param g             the graph whose vertices are to be clustered
     * @param num_clusters  the number of clusters to identify
     */
    public Collection cluster(ArchetypeGraph g, int num_clusters)
    {
        return cluster_internal(g, null, num_clusters,-1,num_candidates);
    }

    protected Collection cluster_internal(ArchetypeGraph g, 
					  ArchetypeVertex origin, 
					  int num_clusters, 
					  int communitysize, 
					  int num_candidates)
    {
        // generate candidate clusters
        // repeat the following 'samples' times:
        // * pick random vertex pair, run VoltageRanker
        // * identify 2 communities in ranked graph
        // * store resulting candidate communities
	
	// breakup graph into connected components disregard
	// components smaller than the minimum cluster size return
	// those between minimum and average cluster size as a
	// complete cluster separately break up weak components other
	// than the giant one that are larger than the average cluster
	// size
	//
	
	//get weak components
	WeakComponentClusterer wcc = new WeakComponentClusterer();
	ClusterSet wcs = wcc.extract(((Graph)(g)));
	
	//sort them by size
	wcs.sort();

	//the first one is the 'giant' component
	Graph gg = wcs.getClusterAsNewSubGraph(0);

	//clusters to be returned by algorithm
        Collection clusters = new LinkedList();

	//vertices not placed in clusters, initially consists of all the vertices
        Collection remaining = new HashSet(g.getVertices());
				
	//if we want the community for one vertex, consider just the weak component it belongs to
	if (origin != null) {

	    //if desired vertex is not int the giant component
	    if (!((wcs.getCluster(0)).contains(origin))) {

		//check all other components to see if they have the vertex
		for (int i = 1; i < wcs.size(); i++) {
		    Set weakcomponent = wcs.getCluster(i);

		    //if component contains origin vertex
		    if (weakcomponent.contains(origin)) {

			//check if it's already about the right size
			if ((weakcomponent.size() <= 
			     (communitysize*0.5))||
			    (weakcomponent.size() <=6)) { 
			    
			    //add to the set of clusters
			    clusters.add((Collection)weakcomponent);	

			    //put all other vertices into a second cluster
			    Set allvertices = g.getVertices();
			    Collection allothervertices = new LinkedList();
			    for (Iterator iter = allvertices.iterator(); 
				 iter.hasNext();) {
				ArchetypeVertex othervertex = 
				    (ArchetypeVertex)iter.next();
				if (!(weakcomponent.contains(othervertex))) {
				    allothervertices.add(othervertex);
				}
			    }
			    clusters.add(allothervertices);

			    //return the two clusters
			    return clusters;
			} else {
			    //otherwise get the component that
			    //contains the vertex as the graph we're
			    //clustering
			    gg = wcs.getClusterAsNewSubGraph(i);
			}
		    }
		}
	    }
	} else {
	    // we're clustering the whole graph, not just finding the
	    // community around a node look at each connected
	    // component
	    for (int i = 1; i < wcs.size(); i++) {
           	Set weakcomponent = wcs.getCluster(i);

		// if connected component is about the right size, add
		// it as a cluster
		if ((weakcomponent.size() >= 
		     (g.numVertices()/num_clusters/2)) && 
		    (weakcomponent.size() <= 
		     (g.numVertices()/num_clusters*2))) { 

		    clusters.add((Collection)weakcomponent);	
		    remaining.removeAll((Collection)weakcomponent);
		    // otherwise component might need to be broken up
		} else if (weakcomponent.size() > 
			   (g.numVertices()/num_clusters)) {
		    
		    ArchetypeGraph mysubgraph = 
			wcs.getClusterAsNewSubGraph(i);	
		    int numclustersforsubgraph = 
			(int)Math.floor(((double)wcs.size()/
					 (double)g.numVertices())*
					num_clusters);

		    int numcandidatesforsubgraph = 
			(int)Math.ceil(((double)wcs.size()/
					(double)g.numVertices())*
				       num_candidates);
		    if (numcandidatesforsubgraph < 20) 
			{numcandidatesforsubgraph = 20;}
		    //make a separate call to clustering algorithm for
		    //this component
		    Collection weakclusters = 
			cluster_internal(mysubgraph,null,
					 numclustersforsubgraph,0,
					 numcandidatesforsubgraph);
		    
		    //add all but the 'junk cluster' from clustering
		    //the component to the set of clusters
		    for (Iterator iter = weakclusters.iterator(); 
			 iter.hasNext();) {
			Collection clustertoadd = (Collection)iter.next();
			if ((iter.hasNext())||(weakclusters.size() == 1)) {
			    clusters.add(clustertoadd);
			    remaining.removeAll(clustertoadd);
			}
		    }
		}
	    }
	}

	//get set of vertices of largest component (or component
	//containing origin vertex)
	Set vertices = gg.getVertices();
	int num_vertices = vertices.size();
	ArchetypeVertex[] v = new ArchetypeVertex[num_vertices];
	int i = 0;
	int j = 0;
	for (Iterator iter = vertices.iterator(); iter.hasNext(); )
	    v[i++] = (ArchetypeVertex)iter.next(); 

	int minclustsize;
	int maxclustsize;
	// if not looking for community around origin node
	// then assume clusters are of about equal size 50%-200%
	if (origin == null) {
	    minclustsize = (int) ((double)num_vertices/num_clusters*0.5);
	    maxclustsize = (int) ((double)num_vertices/num_clusters*2);
	} else {
	    // if looking for community around origin node, candidate
	    // cluster look for clusters as small as 3 vertices or as
	    // large as 1/2 graph
	    if ((communitysize < 3)||(communitysize >= g.numVertices())) {
		minclustsize = 3;
		maxclustsize = (int) ((double)num_vertices/2);
	    } else {
		minclustsize = (int) ((double)communitysize*0.5);
		maxclustsize = (int) ((double)communitysize*2);
	    }
	}
	if (minclustsize < 3) {minclustsize = 3;}
	if (maxclustsize >= num_vertices*0.75) {
	    maxclustsize = (int)(num_vertices*0.75);
	}

	System.out.println("minclustsize = "+minclustsize+
			   " maxclustsize "+maxclustsize);
	// need voltages assigned to vertices from super class so can
	// be used in a comparator
	final UserDatumNumberVertexValue lVV = super.vv;

	// add vertices to an array
	ArchetypeVertex voltage_ranks[] = new ArchetypeVertex[vertices.size()];

				

	LinkedList candidates = new LinkedList();
        
	int num_samples;
	if (origin != null) {
	    num_samples = 20;
	} else {
	    num_samples = num_candidates;
	}
	//			for the desired number of candidate
	//			clusters
        for (int nc = 0; nc < num_samples; nc++)
	    {
		ArchetypeVertex source = null;
		// if we are not looking for the community around a
		// particular node pick a random source
		if (origin == null)
		    source = v[(int)(Math.random() * (double)num_vertices)];
		// else choose particular 'origin' node to be the source
		else
		    source = origin;
		ArchetypeVertex target = null;
		// choose a random sink, making sure it's not the same
		// as the source
		do 
		    {
			target = v[(int)(Math.random() * 
					 (double)num_vertices)];
		    }
		while (source == target);
		// call method to iteratively assign voltages to each
		// non source/sink vertex
		calculateVoltagesL(gg,(Vertex)source, 
				   (Vertex)target, volt_rank_iterations, 
				   volt_rank_convergence);
            
	
		int numverticesadded = 0;
		for (Iterator iter = vertices.iterator(); iter.hasNext(); )
		    {
			ArchetypeVertex w = (ArchetypeVertex)iter.next();
			voltage_ranks[numverticesadded] = w;
			numverticesadded++;
		    }
		
		// sort vertex array by voltage
		Arrays.sort(voltage_ranks, new Comparator() 
		    {
			public int compare(Object arg0, Object arg1)
			{
			    ArchetypeVertex w1 = (ArchetypeVertex) arg0;
			    ArchetypeVertex w2 = (ArchetypeVertex) arg1;
			    double voltage1 = lVV.getNumber(w1).doubleValue();
			    double voltage2 = lVV.getNumber(w2).doubleValue();
			    if (voltage1 < voltage2)
				return -1;
			    else if (voltage1 > voltage2)
				return 1;
			    else
				return 0;
			} 
		    });

		ArchetypeVertex sortedVoltageArray[] = 
		    new ArchetypeVertex[voltage_ranks.length];
		sortedVoltageArray = voltage_ranks;	

		// figure out the min and max size for the candidate clusters
		double voltagedrop;

		try {
		    HashMap lowclust = new HashMap();
		    HashMap highclust = new HashMap();

		    // add vertices from the low end up to minclustersize
		    j = 0; 
		    for (j = 0; j < minclustsize; j++) {
			lowclust.put(sortedVoltageArray[j],null);
		    }	

		    // identify biggest voltage drop between min and
		    // max clustersize
		    double maxvoltagedrop = 0;
		    int cutpoint = minclustsize;
		    for (j = minclustsize; j < maxclustsize; j++) {
			ArchetypeVertex w1 = 
			    (ArchetypeVertex) sortedVoltageArray[j-1];
			ArchetypeVertex w2 = 
			    (ArchetypeVertex) sortedVoltageArray[j];
			double voltage1 = super.vv.getNumber(w1).doubleValue();
			double voltage2 = super.vv.getNumber(w2).doubleValue();
			if ((voltage2 - voltage1) > maxvoltagedrop) {
			    maxvoltagedrop = voltage2 - voltage1;
			    cutpoint = j-1;
			}
		    }

		    //add vertices up to biggest voltage drop
		    for (j = minclustsize; j <= cutpoint; j++) {
			ArchetypeVertex w1 = 
			    (ArchetypeVertex) sortedVoltageArray[j];
			lowclust.put(w1,null);
		    }
							
		    // add everything from the high end down to minclustersize
		    for (j = sortedVoltageArray.length-1; 
			 j >= sortedVoltageArray.length-minclustsize; j--) {
			highclust.put(sortedVoltageArray[j],null);
		    }	

		    //find biggest voltage drop from high end
		    maxvoltagedrop = 0;
		    cutpoint = sortedVoltageArray.length-maxclustsize;
		    for (j = sortedVoltageArray.length-minclustsize; 
			 ((j >= sortedVoltageArray.length - maxclustsize)&&
			  (j > 0)); j--) {
			ArchetypeVertex w1 = 
			    (ArchetypeVertex) sortedVoltageArray[j-1];
			ArchetypeVertex w2 = 
			    (ArchetypeVertex) sortedVoltageArray[j];
			double voltage1 = super.vv.getNumber(w1).doubleValue();
			double voltage2 = super.vv.getNumber(w2).doubleValue();
			if ((voltage2 - voltage1) > maxvoltagedrop) {
			    maxvoltagedrop = voltage2 - voltage1;
			    cutpoint = j;
			}
		    }

		    //add all vertices from high end down to cutpoint
		    for (j = sortedVoltageArray.length-minclustsize-1; 
			 j >= cutpoint; j--) {
			ArchetypeVertex w1 = 
			    (ArchetypeVertex) sortedVoltageArray[j];
			highclust.put(w1,null);
		    }

		    //add high and low cluster to set of candidate clusters
		    candidates.add(lowclust.keySet());
		    candidates.add(highclust.keySet());
		} 
		catch (NotEnoughClustersException e)
		    {
			// ignore this candidate, continue
		    }
	    }
        
        // repeat the following k-1 times: 
	// (k is the number of desired clusters)
        // * pick a vertex v as a cluster seed 
        //   (Wu/Huberman: most frequent vertex in candidates)
        // * calculate co-occurrence (in candidate clusters) 
        //   of this vertex with all others
        // * if a vertex occurs in more than 50% of the 
        //   same clusters as v, add it to v's cluster
        // * if cluster has more than 2 nodes remove, accept cluster 
	//   and remove those vertices from candidate clusters
        

	Object[] seed_candidates;
	Collection reserveseeds = new LinkedList();

	// if clustering entire graph, get seeds for final
	// clusters, ranked by how many different candidate
	// clusters they occur in
	if (origin == null) {
	    seed_candidates = getSeedCandidates(candidates);
	} else {
	    // else have only one seed - the origin node
	    // we're trying to find a community for
	    seed_candidates = new Object[1];
	    seed_candidates[0] = origin;
	}
        
	//try to find a final cluster for each seed
        for (j = 0; j < seed_candidates.length; j++)
	    {
		//stop if have run out of nodes to cluster
		if (remaining.isEmpty())
		    break;
                
		Object seed = seed_candidates[j]; 
		//if seed  has already been gobbled up by 
		//another cluster, loop again
		if (!remaining.contains(seed)) {
		    continue;
		} 
            
		//count the number of candidate clusters for the seed
		//maybe at some future point could just have this
		//returned by getSeedCandidates
		int numcandforseed = 0;
		for (Iterator iter = candidates.iterator(); iter.hasNext(); )
		    {
			Collection candidate = (Collection)iter.next();
			if (candidate.contains(seed)) {
			    numcandforseed++;
			}
		    }

		// get counts for all other vertices of how often
		// they co-occur with seed in candidate clusters
		Map occur_counts = getObjectCounts(candidates, seed);
		// if fewer than 2 vertices co-occur with this seed,
		// try another
		if (occur_counts.size() < 2) {
		    continue;
		} 
		// now that we have the counts, form a cluster
		try
		    {
			Collection new_cluster = new LinkedList();
			Set occur_countsset = occur_counts.keySet();
            		for (Iterator iter = occur_countsset.iterator(); 
			     iter.hasNext(); )
			    {
				Object okey = iter.next();
				double ovalue[] = 
				    (double[])occur_counts.get(okey);
				// if vertex co-occurs with seed in
				// more than 50% of the seed's
				// candidate clusters add vertex to
				// seed's cluster
				if (ovalue[0] >= 
				    Math.floor(numcandforseed/2)) {
				    new_cluster.add(okey);
				}
			    }	
               
			// if cluster contains more than two nodes
			if (new_cluster.size() >= minclustsize) { 
			    // ...remove the elements of new_cluster
			    // from each candidate...
			    for (Iterator iter = candidates.iterator(); 
				 iter.hasNext(); )
				{
				    Collection cluster = 
					(Collection)iter.next();
				    cluster.removeAll(new_cluster);
				}
			    // add seed's cluster to set of final clusters
			    clusters.add(new_cluster);
			    remaining.removeAll(new_cluster);
			} else {
			    reserveseeds.add(seed);
			}
			// if already found the max number of clusters, quit
			if (clusters.size() >= num_clusters*2) {
			    break;
			}
		    }
		catch (NotEnoughClustersException nece)
		    {
			// all remaining vertices are in the same cluster
			break;
		    }
	    }
        
	// now that we've found all the clusters of about the right
	// size see if we can find aditional, smaller clusters in the
	// remaining vertices for a total of up to 2 * the number of
	// requested clusters
	if ((remaining.size() >= 3)&&(clusters.size() < num_clusters*2)) {
	    for (Iterator iter2 = reserveseeds.iterator(); iter2.hasNext();)
		{
		    //stop if have run out of nodes to cluster
		    if (remaining.isEmpty())
			break;
									
		    ArchetypeVertex reserveseed = 
			(ArchetypeVertex)iter2.next();
		    //if seed  has already been gobbled up by 
		    //another cluster, loop again
		    if (!remaining.contains(reserveseed)) {
			continue;
		    } 
							
		    //count the number of candidate clusters for the seed
		    //maybe at some future point could just have this
		    //returned by getSeedCandidates
		    int numcandforseed = 0;
		    for (Iterator iter = candidates.iterator(); 
			 iter.hasNext(); )
			{
			    Collection candidate = (Collection)iter.next();
			    if (candidate.contains(reserveseed)) {
				numcandforseed++;
			    }
			}

		    // get counts for all other vertices of how often
		    // they co-occur with seed in candidate clusters
		    Map occur_counts = 
			getObjectCounts(candidates, reserveseed);
		    // if fewer than 2 vertices co-occur with this
		    // seed, try another
		    if (occur_counts.size() < 2) {
			continue;
		    } 
		    // now that we have the counts, form a cluster
		    try
			{
			    Collection new_cluster = new LinkedList();
			    Set occur_countsset = occur_counts.keySet();
			    for (Iterator iter = occur_countsset.iterator(); 
				 iter.hasNext(); )
				{
				    Object okey = iter.next();
				    double ovalue[] = 
					(double[])occur_counts.get(okey);
				    // if vertex co-occurs with seed
				    // in more than 50% of the seed's
				    // candidate clusters add vertex
				    // to seed's cluster
				    if (ovalue[0] >= 
					Math.floor(numcandforseed/2)) {
					new_cluster.add(okey);
				    }
				}	
								 
			    // if cluster contains more than two nodes
			    if (new_cluster.size() >= 3) { 
				// ...remove the elements of
				// new_cluster from each candidate...
				for (Iterator iter = candidates.iterator(); 
				     iter.hasNext(); )
				    {
					Collection cluster = 
					    (Collection)iter.next();
					cluster.removeAll(new_cluster);
				    }
				// add seed's cluster to set of final clusters
				clusters.add(new_cluster);
                		remaining.removeAll(new_cluster);
			    } 
			    // if already found the max number of
			    // clusters, quit
			    if (clusters.size() >= num_clusters*2) {
				break;
			    }
			}
		    catch (NotEnoughClustersException nece)
			{
			    // all remaining vertices are in the same cluster
			    break;
			}
        	}
	}

        // identify remaining vertices (if any) as a 'garbage' cluster
        if (!remaining.isEmpty())
            clusters.add(remaining);
        
        return clusters;
    }

    /**
     * Returns an array of cluster seeds, ranked in decreasing order
     * of number of appearances in the specified collection of candidate
     * clusters.
     * @param candidates
     */
    protected Object[] getSeedCandidates(Collection candidates)
    {
        final Map occur_counts = getObjectCounts(candidates, null);
        
	//get # of candidate clusters each vertex belongs to
        Object[] occurrences = occur_counts.keySet().toArray();
	//sort in decreasing order by number of candidate clusters containing
	//each vertex
        Arrays.sort(occurrences, new Comparator() 
            {
                public int compare(Object arg0, Object arg1)
                {
                    double[] count0 = (double[])occur_counts.get(arg0);
                    double[] count1 = (double[])occur_counts.get(arg1);
                    if (count0[0] < count1[0])
                        return 1;
                    else if (count0[0] > count1[0])
                        return -1;
                    else
                        return 0;
                } 
            });
        return occurrences;
    }
		
    //count number of candidate clusters each vertex is in
    //if a 'seed' vertex is given, then find the number of clusters
    //that both the seed and each individual vertex are in 
    protected Map getObjectCounts(Collection candidates, Object seed)
    {
        Map occur_counts = new HashMap();
        for (Iterator iter = candidates.iterator(); iter.hasNext(); )
	    {
		Collection candidate = (Collection)iter.next();
		if (seed == null || candidate.contains(seed))
		    {
			for (Iterator c_iter = candidate.iterator(); 
			     c_iter.hasNext(); )
			    {
				Object element = c_iter.next();
				double[] count = 
				    (double[])occur_counts.get(element);
				if (count == null)
				    {
					count = new double[1];
					count[0] = 1;
					occur_counts.put(element, count);
				    }
				else count[0]++;
			    }
		    }
	    }
        
        return occur_counts;
    }

    //simplified version to calculate voltages on a resistor network
    //with all resistors the same and a given source and sink at 1 and
    //0 volts important to intialize all vertices except for the
    //source and sink to middle value (0.5)
    public void calculateVoltagesL(ArchetypeGraph g,Vertex source, 
				   Vertex sink, int rank_iterations, 
				   double rank_threshold)
    {
        // set up initial voltages
        Indexer id = Indexer.getIndexer(g);
        Set vertices = g.getVertices();
        double[] volt_array = new double[vertices.size()];
        for (int i = 0; i < volt_array.length; i++)
	    {
		Vertex v = (Vertex)id.getVertex(i);
		if (v.equals(source))
		    {
			Number voltage = (Number) (new Double(1.0));
			volt_array[i] = voltage.doubleValue();
			vv.setNumber(v, voltage);
		    }
		else if (v.equals(sink)) {
		    Number voltage = (Number) (new Double(0));
		    volt_array[i] = voltage.doubleValue();
		    vv.setNumber(v, voltage);
		}
		else
		    {
			volt_array[i] = 0.5;
			vv.setNumber(v, new Double(0.5));
		    }
	    }
        
        // update voltages of each vertex to the (unweighted) average of its 
        // neighbors, until either (a) the number of iterations exceeds the
        // maximum number of iterations specified, or (b) the largest change of
        // any voltage is no greater than the specified convergence threshold. 
        int iteration = 0;
        double max_change = Double.POSITIVE_INFINITY;
        while (iteration++ < rank_iterations && max_change > rank_threshold)
	    {
		max_change = 0;
		for (Iterator iter = vertices.iterator(); iter.hasNext(); )
		    {
			Vertex v = (Vertex)iter.next();
			if (source.equals(v) || sink.equals(v))
			    continue;
			Set edges = v.getInEdges();
			double voltage_sum = 0;
			double weight_sum = 0;
			for (Iterator e_iter = edges.iterator(); e_iter.hasNext(); )
			    {
				Edge e = (Edge)e_iter.next();
				Vertex w = e.getOpposite(v);
				voltage_sum += volt_array[id.getIndex(w)];
				weight_sum += 1;
			    }
                
			double new_voltage;
			if (voltage_sum == 0 && weight_sum == 0)
			    new_voltage = 0;
			else
			    new_voltage = voltage_sum / weight_sum;
			max_change = Math.max(max_change, 
					      Math.abs(vv.getNumber(v).doubleValue() - new_voltage));
			vv.setNumber(v, new Double(new_voltage));
		    }
            
		// set up volt_array for next iteration
		for (int i = 0; i < volt_array.length; i++)
		    volt_array[i] = vv.getNumber(id.getVertex(i)).doubleValue();
	    }
    }
}
