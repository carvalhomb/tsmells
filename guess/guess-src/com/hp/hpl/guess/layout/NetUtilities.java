/*$$
 * packages uchicago.src.*
 * Copyright (c) 1999, Trustees of the University of Chicago
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with
 * or without modification, are permitted provided that the following
 * conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *   Neither the name of the University of Chicago nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE TRUSTEES OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Nick Collier
 * nick@src.uchicago.edu
 *
 * packages cern.jet.random.*
 * Copyright (c) 1999 CERN - European Laboratory for Particle
 * Physics. Permission to use, copy, modify, distribute and sell this
 * software and its documentation for any purpose is hereby granted without
 * fee, provided that the above copyright notice appear in all copies
 * and that both that copyright notice and this permission notice appear in
 * supporting documentation. CERN makes no representations about the
 * suitability of this software for any purpose. It is provided "as is"
 * without expressed or implied warranty.
 *
 * Wolfgang Hoschek
 * wolfgang.hoschek@cern.ch
 * @author Hacked by Eytan Adar for Guess classes
 *$$*/
package com.hp.hpl.guess.layout;

import com.hp.hpl.guess.*;
import edu.uci.ics.jung.graph.Vertex;

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

import java.util.*;


/**
 * Performs various common operations on passed networks and returns them,
 * and/or returns a statistic on a passed network and returns the result.
 * Please note that these may be fairly "naive" algorithm implementations,
 * and no guarantees are made about the accuracy of the statistics.  The
 * intention is that these may be used for "on the fly" qualitative evaluation
 * of a model, but real network statistics should be done with more serious
 * software such as UCINET or Pajek.<p>
 *
 * ALL THE METHODS CAN BE CONSIDERED BETA AND SHOULD ONLY BE USED FOR
 * "ON THE FLY" CALCULATIONS. ACTUAL NETWORK STATISTICS SHOULD BE DONE
 * WITH DEDICATED NETWORK ANALYSIS SOFTWARE, SUCH AS PAJEK OR UCINET.
 *
 * @author Skye Bender-deMoll
 * @version $Revision: 1.1 $ $Date: 2005/10/05 20:19:39 $
 */
public class NetUtilities {

  /**
   * No-Argument constructor for convenience / aliasing.
   */
  public NetUtilities () {
  }

  /**
   * calculates density (ratio of arcs in network to maximum possible number
   * of arcs) of passed network.  Checks to make sure network is
   * non-multiplex. Includes self-loops.
   */
  public static double calcDensity (Set nodes) {
      double density = 0.0;
      int nNodes = nodes.size();
      int degreeSum = 0;
      //check to make sure that the network is not multiplex
      if (isMultiplexNet (nodes)) {
	  String error = "calcDensity expects a non-multiplex network. Please run collapseMultiplexNet() before calculating density.";
	  //RepastException exception = new RepastException (error);
	  //SimUtilities.showError (error, exception);
      } else {
	  //outdegree only!!
	  //COUNTS SELF LOOPS
	  Iterator it = nodes.iterator();
	  while(it.hasNext()) {
	      degreeSum += ((Node)it.next()).getOutEdges().size();
	  }
	  //density  = number of possible arcs / number present
	  density = (double) degreeSum / (double) (nNodes * nNodes);
      }
      return density;
  }

  /**
   * Calculates density of the network, but if collapseMulti is true, it first
   * collapses any multiplex ties.  If collapseMulti is false, it runs without
   * checking for multiplex ties.  Note: if the argument is false, and
   * multiplex ties exist, calcDensity will still return a value, but it will
   * not exactly correspond to the density.  But it will run much faster than
   * calcDensity(ArrayList nodes).  The possibility of self-loops is assumed
   * @param nodes the ArrayList of nodes to examine
   **/
  public static double calcDensity (Set nodes, boolean collapseMulti) {
    double density = 0.0;
    int nNodes = nodes.size ();
    int degreeSum = 0;
    //check to make sure that the network is not multiplex
    if (collapseMulti) {
      //NOT IMPLEMENTED UNTIL PROBLEM WITH MODIFYING ORIGINAL NET RESOLVED
      //nodes = collapseMultiplexNet(nodes);
    } else {
      //outdegree only!!
      //COUNTS SELF LOOPS
	Iterator it = nodes.iterator();
	while(it.hasNext()) {
	    degreeSum += ((Node)it.next()).getOutEdges().size();
	}
      //density  = number of possible arcs / number present
      density = (double) degreeSum / (double) (nNodes * nNodes);
    }
    return density;
  }

  /**
   * Checks if there are any nodes i j for which there is more than one
   * tie i -> j (almost all network statistics assume that the network is
   * NOT multiplex)
   */
  public static boolean isMultiplexNet(Set nodes) {
    boolean multiplex = false;
    int nNodes = nodes.size();
    HashSet jNodes = new HashSet(nNodes);
    Iterator nodeIter = nodes.iterator();
    while (nodeIter.hasNext () && !multiplex) {
      jNodes.clear();
      Node context = (Node)nodeIter.next();
      Iterator edgeIter = context.getOutEdges().iterator();
      while (edgeIter.hasNext() && !multiplex)   //can break after 1st multiple tie is discovered
	  {
	      Node jNode = (Node)((Edge)edgeIter.next()).getOpposite(context);
	      if (jNodes.contains(jNode)) {
		  multiplex = true;
	      } else {
		  jNodes.add (jNode);
	      }
	  }
    }
    return multiplex;
  }

  /**
   * Returns a boolean indicating whether the network contains self-loops
   * (links from i -> i)
   *
   * @param nodes the ArrayList of nodes to examine for loops
   */
    public static boolean hasSelfLoops (Set nodes) {
	for (Iterator iter = nodes.iterator(); iter.hasNext(); ) {
	    Node iNode = (Node) iter.next ();
	    if (iNode.findEdge(iNode) != null) 
		return true;
	}
	return false;
    }

  /**
   * Removes redundant (same direction) ties between node pairs. Ignores weight
   * of ties, first encountered is kept, subsequent redundant ties are removed.
   * @param nodes the network from which to remove redundant ties
   **/
  /* MODIFIES PASSED NETWORK DISABLED UNTIL ISSUES RESOLVED
     public static ArrayList collapseMultiplexNet(ArrayList nodes)
     {
     ArrayList collapsed = new ArrayList();
     int nNodes = nodes.size();
     HashSet jNodes = new HashSet();
     Iterator nodeIter = nodes.iterator();
     while (nodeIter.hasNext())
     {
     jNodes.clear();
     //make clone so as not to alter original
     Node node = (Node)(nodeIter.next().clone());
     ArrayList edges = node.getOutEdges();
     for (int e = 0; e<edges.size(); e++)
     {
     Edge edge = (Edge)edges.get(e);
     if(jNodes.contains(edge.getTo()))
     {
     node.removeOutEdge(edge);
     ((Node)edge.getTo()).removeInEdge(edge);
     }
     else
     {
     jNodes.add(edge.getTo());
     }
     }
     collapsed.add(node);
     }
     return collapsed;
     }
  */

  /**
   * Returns the out degree (number of out edges) of the node.
   * @param iNode the node the degree will be returned for
   **/
  public static int getOutDegree (Node iNode) {
      return iNode.getOutEdges().size();
  }

  /**
   * Returns the in degree (number of in edges) of the node.
   * (Assumes inEdges have been set correctly)
   * @param iNode the node the degree will be returned for
   **/
  public static int getInDegree (Node iNode) {
      return iNode.getInEdges().size();
  }

  /**
   * Finds and returns the number of "parents" (nodes with links TO both
   * i and j)
   *
   * @param iNode one node of triad
   * @param jNode the second node of the triad
   */
  public static int getNumDirectTriads (Node iNode, Node jNode) {
    int parentCount = 0;
    //get all nodes with links to i
    HashSet iLinks = new HashSet ();
    Iterator en = iNode.getInEdges().iterator();
    while(en.hasNext()) {
	iLinks.add((Node)((Edge)en.next()).getOpposite(iNode));
    }
    //count how many also have links to j
    en = jNode.getInEdges().iterator();
    while(en.hasNext()) {
	if (iLinks.contains ((Node)((Edge)en.next()).getOpposite(jNode))) {
	    parentCount++;
	}
    }
    return parentCount;
  }

  /**
   * Returns an ArrayList of length equal to the number of components in the
   * graph, each entry of which is an ArrayList of the nodes in that component.
   * @param nodes the network in which components will be counted
   */
  public static Set getComponents(Set nodes) {
    ComponentFinder finder = new ComponentFinder ();
    return finder.findComponents(nodes);
  }

  // class is constructed to make possible the use of recursive
  // tree search methods within a static context of netUtilities
  private static class ComponentFinder {
      
      Set nodeList;
      int nNodes = 0;
      HashSet checked = new HashSet(nNodes);
      HashSet currentComps = new HashSet();
      HashSet currentComp = new HashSet();
      
      public Set findComponents (Set nodeList) {
	  nNodes = nodeList.size();
	  //Hashtable nodeNodeIndexer = new Hashtable (nNodes);
	  HashSet returnList = new HashSet();
	  //make it so we can get the passed nodes from the low level nodes
	  //for (int i = 0; i < nNodes; i++) {
	  //  Node iNode = (Node) nodeList.get (i);
	  //  nodeNodeIndexer.put (iNode.getNode (), iNode);
	  //}
	  
	  checked.clear();
	  Iterator it = nodeList.iterator();
	  while(it.hasNext()) {
	      Node iNode = (Node)it.next();
	      if (!checked.contains(iNode)) {
		  currentComp = new HashSet ();
		  currentComps.add(currentComp);
		  //puts iNode and all connected nodes into currentComponent
		  findConnectedNodes(iNode);
	      }
	  }
	  
	  int size = currentComps.size ();
	  it = currentComps.iterator();
	  while(it.hasNext()) {
	      HashSet set = (HashSet)it.next();
	      HashSet component = new HashSet(set.size());
	      //pull the items out of nodeList which correspond to the internal
	      //node objects found by the search
	      Iterator nodeIter = set.iterator();
	      while (nodeIter.hasNext ()) {
		  component.add (nodeIter.next());
	      }
	      
	      returnList.add (component);
	  }
	  
	  return returnList;
      }
      
      // recursively calls itself to find all nodes connected to iNode
      private void findConnectedNodes(Node iNode) {
	  checked.add(iNode);
	  currentComp.add(iNode);
	  Iterator edgeEnum = iNode.getOutEdges().iterator();
	  while(edgeEnum.hasNext()) {
	      Edge edge = (Edge) edgeEnum.next();
	      Node nextNode = (Node)edge.getOpposite((Node)iNode);
	      if (!checked.contains(nextNode))
		  findConnectedNodes(nextNode);
	      else if (!currentComp.contains(nextNode)) {
		  HashSet set = getComponentFor(nextNode);
		  set.addAll(currentComp);
		  currentComps.remove(currentComp);
		  currentComp = set;
	      }
	  }
      }
      
      private HashSet getComponentFor (Node node) {
	  int size = currentComps.size();
	  Iterator it = currentComps.iterator();
	  while(it.hasNext()) {
	      HashSet set = (HashSet)it.next();
	      if (set.contains (node)) {
		  return set;
	      }
	  }
	  
	  // should never get here
	  return null;
      }
  }

  public static DenseDoubleMatrix2D getAllShortPathMatrix (Set nodes) {
    //CHECK FOR MULTIPLEX!!
    //SYMMETRY?
    int nNodes = nodes.size ();
    DenseDoubleMatrix2D distMatrix = new DenseDoubleMatrix2D (nNodes, nNodes);
    distMatrix.assign (Double.POSITIVE_INFINITY);
    //ArrayList nodeIndexer = new ArrayList(nNodes);

    // index of nodes to there index in the
    HashMap nodeIndexer = new HashMap ();
    DoubleArrayList priorityList = new DoubleArrayList ();
    ArrayList nodeQueue = new ArrayList ();
    HashSet checkedNodes = new HashSet ();

    Iterator it = nodes.iterator();
    int w = 0;
    
    Node[] nds = new Node[nNodes];

    while(it.hasNext()) {
	Node work = (Node)it.next();
	nodeIndexer.put(work, new Integer(w));
	nds[w] = work;
	w++;
    }

    for (int i = 0; i < nNodes; i++) {
      checkedNodes.clear ();
      priorityList.clear ();
      nodeQueue.clear ();
      //find paths to all nodes connected to i
      Node iNode = (Node)nds[i];
      distMatrix.setQuick (i, i, 0.0);
      checkedNodes.add (iNode);
      priorityList.add (0.0);
      nodeQueue.add (iNode);
      while (nodeQueue.size () > 0) {
        //find node with smallest priority value
        double fringeNodePrior = Double.POSITIVE_INFINITY;
        int fringeNodeIndex = Integer.MAX_VALUE;
        for (int n = 0; n < priorityList.size (); n++) {
          if (priorityList.getQuick (n) < fringeNodePrior) {
            fringeNodeIndex = n;
            fringeNodePrior = priorityList.getQuick (fringeNodeIndex);
          }
        }
        Node fringeNode = (Node) nodeQueue.get (fringeNodeIndex);
        double fringeNodeDist = priorityList.getQuick (fringeNodeIndex);
        nodeQueue.remove (fringeNodeIndex);
        priorityList.remove (fringeNodeIndex);
        checkedNodes.add (fringeNode);

        //put distance in matrix
        int index = ((Integer) nodeIndexer.get (fringeNode)).intValue ();
        distMatrix.setQuick (i, index, fringeNodeDist);
        distMatrix.setQuick (index, i, fringeNodeDist);
        //loop over its edges, adding nodes to queue
	Iterator edgeEnum = fringeNode.getOutEdges().iterator();
        while(edgeEnum.hasNext()) {
          Edge edge = (Edge)edgeEnum.next();
          Node workNode = (Node)edge.getOpposite(fringeNode);
          if (!checkedNodes.contains(workNode)) {
            //calc workNode's distance from iNode
	      double eweight = 
		  ((Double)edge.__getattr__("weight")).doubleValue();
	      double workNodeDist = fringeNodeDist + eweight;
	      int prevDistIndex = nodeQueue.indexOf(workNode);
	      if (prevDistIndex >= 0) {
              //check if it has a lower distance
              if (priorityList.getQuick (prevDistIndex) > workNodeDist) {
                //repace it with new value
                priorityList.set (prevDistIndex, workNodeDist);
              }
            } else {
              //add the worknode to the queue with priority
              priorityList.add (workNodeDist);
              nodeQueue.add (workNode);
            }
          }
        }
      }
    }

    return distMatrix;
  }

}
