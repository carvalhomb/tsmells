package com.hp.hpl.guess.layout;

import java.util.*;
import com.hp.hpl.guess.*;
import java.awt.geom.*;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.graph.Vertex;

/** 
 * a port of http://www.schmuhl.org/graphopt/
 * @author Hacked by Eytan Adar for Guess
 */
public class Physics extends AbstractLayout {
    
    double node_mass = 30; // range 0-100
    double node_charge = 0.001; // range 0-100
    int spring_length = 0; // range 0-100
    double spring_constant = 1; // range 0-100
    double max_sa_movement = 5; // range 0-100
    int layers_to_hide = 0; // range 0-100

    int min_layer = Integer.MAX_VALUE;
    int max_layer = Integer.MIN_VALUE;

    static final double COULOMBS_CONSTANT = (double)8987500000.0;

    Hashtable pendingForces = new Hashtable();
    Hashtable layer = new Hashtable();
    Hashtable locations = new Hashtable();

    private List my_nodes = null;

    int steps = 500;
    boolean gather = false;
    Graph g = null;

    int iter = 0;

    public Physics(Graph g, boolean gather) {
	super(g);
	this.g = g;
	this.gather = gather;
	Set nodes = g.getNodes();
	ArrayList my_nodes = new ArrayList(nodes.size());
	my_nodes.addAll(nodes);
	Iterator it = my_nodes.iterator();
	while(it.hasNext()) {
	    Node n = (Node)it.next();
	    layer.put(n,new Integer(999999));
	    locations.put(n,new Coordinates(n.getX(),n.getY()));
	}
	assign_layers(my_nodes);
	seperateNodes(my_nodes);
	this.my_nodes = my_nodes;
      	System.out.println("working on: " + my_nodes.size() + " nodes");
	if (gather) {
	    System.out.println("layer range: " + min_layer + " " + max_layer);
	    for (int i = min_layer ; i <= max_layer ; i++) {
		gather_nodes_with_layer(my_nodes,i);
	    }
	}
    }
    
    public void advancePositions() {
	if (done)
	    return;

	if (iter >= steps) {
	    done = true;
	    return;
	} else {
	    advance();
	}
	iter++;
    }

    public void seperateNodes(List my_nodes) {
	Node this_node;
	Node other_node;
	String message;
	int clear;
	int counter = 0;
	int counter2;
	
	Random r = new Random();

	while (counter < my_nodes.size()) {
	    this_node = (Node)my_nodes.get(counter);
	    Coordinates this_loc = (Coordinates)locations.get(this_node);
	    counter2 = counter + 1;
	    clear = 1;
	    while ((counter2 < my_nodes.size()) && (clear == 1)) {
		// Only if this is not the same node...
		if (counter2 != counter) {
		    other_node = (Node)my_nodes.get(counter2);
		    Coordinates other_loc = (Coordinates)locations.get(other_node);
		    if ((this_loc.getX() == other_loc.getX()) &&
			(this_loc.getY() == other_loc.getY())) {
			
			// Move one of the nodes a small random amount
			//               this_node.getX() += 1;
			this_loc.setX(this_loc.getX() + 
				      (double)r.nextInt(20));
			this_loc.setY(this_loc.getY() + 
				      (double)r.nextInt(20));
			// drop out of the inner loop and reset the outer loop
			clear = 0;
			counter = 0;
		    }
		    else 
			counter2++;
		}
		else
		    counter2++;
	    }
	    if (clear == 1)
		counter++;
	}
    }


 
    public void advance() {
	Node this_node = null;
	Node other_node = null;

	//System.out.println(my_nodes);
	Iterator it = my_nodes.iterator();
	while(it.hasNext()) {
	    Node n = (Node)it.next();
	    Coordinates p2d = (Coordinates)pendingForces.get(n);
	    if (p2d == null) {
		pendingForces.put(n,new Coordinates(0.0,0.0));
	    } else {
		p2d.setX(0.0);
		p2d.setY(0.0);
	    }
	}

	
	// Set a flag to calculate (or not) the electrical forces that
	// the nodes apply on each other based on if both node types'
	// charges are zero.
	boolean apply_electric_charges = true;
	if (node_charge == 0.0)
	    apply_electric_charges = false;

	// Iterate through all nodes
	int number_of_nodes = my_nodes.size();
	double distance;
	for (int counter = 0; counter < number_of_nodes; counter++) {
	    this_node = (Node)my_nodes.get(counter);
	    // only determine forces on this node if it isn't in a
	    // layer we should hide
	    if (((Integer)layer.get(this_node)).intValue() > layers_to_hide) {
		if (apply_electric_charges) {
		    // Apply electrical force applied by all other nodes
		    for (int counter2 = counter + 1; 
			 counter2 < number_of_nodes; 
			 counter2++) {
			other_node = (Node)my_nodes.get(counter2);
			// only proceed if the other node isn't in a
			// layer we should hide
			if (((Integer)layer.get(other_node)).intValue() > layers_to_hide) {
			    distance = distance_between(this_node,other_node);
			    // let's protect ourselves from division
			    // by zero by ignoring two nodes that
			    // happen to be in the same place.  Since
			    // we separate all nodes before we work on
			    // any of them, this will only happen in
			    // extremely rare circumstances, and when
			    // it does, springs will probably pull
			    // them apart anyway.  also, if we are
			    // more than 50 away, the electric force
			    // will be negligable.  ***** may not
			    // always be desirable ****
			    if ((distance != 0.0) && (distance < 500.0)) {
				//	  if (distance != 0.0) { Apply
				//electrical force from node(counter2)
				//on node(counter)
				apply_electrical_force(other_node, this_node, distance);
			    }
			}
		    }
		}
		
		// Apply force from springs
		it = this_node.getNeighbors().iterator();
		while(it.hasNext()) {
		    other_node = (Node)it.next();
		    if (other_node.getName().compareTo(this_node.getName()) > 0) {
			continue;
		    } 
		    // only proceed if the other node isn't in a layer
		    // we should hide
		    if (((Integer)layer.get(other_node)).intValue() > layers_to_hide) {
			// Apply spring force on both nodes
			apply_spring_force(other_node, this_node);
		    }
		}
	    }
	}
	
	// Effect the movement of the nodes based on all pending forces
	move_nodes(my_nodes);
    }

    public void apply_electrical_force(Node other_node, Node this_node, 
				       double distance) {
	
	// determined using Coulomb's Law:
	//   force = k((q1*q2)/r^2)
	// where:
	//   force is in newtons
	//   k = Coulomb's constant
	//   q1 and q2 are the two electrical charges in coulombs, and
	//   r is the distance between the charges in meters
	
	double directed_force = COULOMBS_CONSTANT * 
	    ((node_charge * node_charge)/(distance * distance));

	Coordinates incForce =  
	    determine_electric_axal_forces(directed_force, distance, 
					   other_node, this_node);
	
	Coordinates oldForce = (Coordinates)pendingForces.get(this_node);
	oldForce.setX(oldForce.getX()+
		      incForce.getX());
	oldForce.setY(oldForce.getY()+
		      incForce.getY());
	
	oldForce = (Coordinates)pendingForces.get(other_node);
	oldForce.setX(oldForce.getX()-
		      incForce.getX());
	oldForce.setY(oldForce.getY()-
		      incForce.getY());
    }
    
    
    public void apply_spring_force(Node other_node, Node this_node) {
	// determined using Hooke's Law:
	//   force = -kx
	// where:
	//   k = spring constant
	//   x = displacement from ideal length in meters
	
	double distance = distance_between(other_node, this_node);
	// let's protect ourselves from division by zero by ignoring
	// two nodes that happen to be in the same place.  Since we
	// separate all nodes before we work on any of them, this will
	// only happen in extremely rare circumstances, and when it
	// does, electrical force will probably push one or both of
	// them one way or another anyway.
	if (distance == 0.0) 
	    return;
	
	double displacement = distance - spring_length;
	if (displacement < 0) 
	    displacement = -displacement;
	double directed_force = -1 * spring_constant * displacement;
	
	// remember, this is force directed away from the spring; a
	// negative number is back towards the spring (or, in our case,
	// back towards the other node)
	
	// get the force that should be applied to >this< node
	Coordinates incForce =  
	    determine_spring_axal_forces(directed_force, distance, 
					 spring_length,
					 other_node, this_node);
	
	Coordinates oldForce = (Coordinates)pendingForces.get(this_node);
	oldForce.setX(oldForce.getX()+
		      incForce.getX());
	oldForce.setY(oldForce.getY()+
		      incForce.getY());
	
	oldForce = (Coordinates)pendingForces.get(other_node);
	oldForce.setX(oldForce.getX()-
		      incForce.getX());
	oldForce.setY(oldForce.getY()-
		      incForce.getY());
    }
    
    
    public void move_nodes(List my_nodes) {
	// Since each iteration is isolated, time is constant at 1.
	// Therefore:
	//   Force effects acceleration.
	//   acceleration (d(velocity)/time) = velocity
	//   velocity (d(displacement)/time) = displacement
	//   displacement = acceleration
	
	// determined using Newton's second law:
	//   sum(F) = ma
	// therefore:
	//   acceleration = force / mass
	//   velocity     = force / mass
	//   displacement = force / mass
	
	Node this_node;

	for (int counter = 0; counter < my_nodes.size(); counter++) {
	    this_node = (Node)my_nodes.get(counter);
	    
	    Coordinates pending = (Coordinates)pendingForces.get(this_node);
	   
	    //System.out.println("force: " + pending.getX() + " " + pending.getY());
	    double x_movement = pending.getX() / node_mass;
	    if (x_movement > max_sa_movement)
		x_movement = max_sa_movement;
	    else if (x_movement < -max_sa_movement)
		x_movement = -max_sa_movement;
	    
	    double y_movement = pending.getY() / node_mass;
	    if (y_movement > max_sa_movement)
		y_movement = max_sa_movement;
	    else if (y_movement < -max_sa_movement)
		y_movement = -max_sa_movement;
	    
	    
	    //System.out.println("moving: " + this_node + " " + x_movement + " " + y_movement);
	    Coordinates loc = (Coordinates)locations.get(this_node);
	    loc.setX(loc.getX() + x_movement);
	    loc.setY(loc.getY() + y_movement);
	}
    }
    

    public double distance_between(Node node1, Node node2) {
	// distance = |sqrt((x1 - x2)^2 + (y1 - y2)^2)|
	
	Coordinates node_one = (Coordinates)locations.get(node1);
	Coordinates node_two = (Coordinates)locations.get(node2);
	
	double x_difference = node_one.getX() - node_two.getX();
	double y_difference = node_one.getY() - node_two.getY();
	
	double distance =Math.sqrt((x_difference * x_difference) + 
				   (y_difference * y_difference));
	if (distance < 0)
	    distance = -distance;
	
	return distance;
    }

    public Coordinates determine_electric_axal_forces(double directed_force, 
						  double distance,
						  Node otherN, 
						  Node thisN) {
	// We know what the directed force is.  We now need to translate it
	// into the appropriate x and y componenets.
	// First, assume: 
	//                 other_node
	//                    /|
	//  directed_force  /  |
	//                /    | y
	//              /______|
	//    this_node     x         
	//
	// other_node.x > this_node.x
	// other_node.y > this_node.y
	// the force will be on this_node away from other_node
	
	// the proportion (distance/y_distance) is equal to the
	// proportion (directed_force/y_force), as the two triangles
	// are similar.  therefore, the magnitude of y_force =
	// (directed_force*y_distance)/distance the sign of y_force is
	// negative, away from other_node

	double x;
	double y;

	Coordinates other_node = (Coordinates)locations.get(otherN);
	Coordinates this_node = (Coordinates)locations.get(thisN);

	double y_distance = other_node.getY() - this_node.getY();
	if (y_distance < 0)
	    y_distance = -y_distance;
	y = -1 * ((directed_force * y_distance) / distance);

	// the x component works in exactly the same way.
	double x_distance = other_node.getX() - this_node.getX();
	if (x_distance < 0)
	    x_distance = -x_distance;
	x = -1 * ((directed_force * x_distance) / distance);
	
	
	// Now we need to reverse the polarity of our answers based on
	// the falsness of our assumptions.
	if (other_node.getX() < this_node.getX())
	    x = x * -1;
	if (other_node.getY() < this_node.getY())
	    y = y * -1;
	
	return(new Coordinates(x,y));
    }


    public Coordinates determine_spring_axal_forces(double directed_force, 
						double distance, 
						int   spring_length,
						Node other_node,
						Node this_node) {
	// if the spring is just the right size, the forces will be 0,
	// so we can skip the computation.
	//
	// if the spring is too long, our forces will be identical to those
	// computed by determine_electrical_axal_forces() (this_node will
	// be pulled toward other_node).
	//
	// if the spring is too short, our forces will be the opposite of
	// those computed by determine_electrical_axal_forces() (this_node
	// will be pushed away from other_node)
	//
	// finally, since both nodes are movable, only one-half of the
	// total force should be applied to each node, so half the
	// forces for our answer.
	
	double x = 0.0;
	double y = 0.0;
	if (distance == spring_length) {
	    x = 0.0;
	    y = 0.0;
	}
	else {
	    Coordinates newDisp = 
		determine_electric_axal_forces(directed_force, distance, 
					       other_node, this_node);
	    x = newDisp.getX();
	    y = newDisp.getY();
	    if (distance < spring_length) {
		x = -1 * x;
		y = -1 * y;
	    }
	    x = 0.5 * x;
	    y = 0.5 * y;
	}
	
	return(new Coordinates(x,y));
    }

    public void assign_layers(List nodes) {
	for (int i = 1; assign_layer(nodes,i) > 0; i++);
    }

    int assign_layer(List my_nodes, int layer_number) {
	// implement this algorithm:
	// counter = 0
	// for each node 
	//   if this node is not in a layer lower than the current
	//     if this node has no nodes it points to
	//        or
	//     if every node that this node points to is in a lower layer than the 
	//              current layer
	//       put it in the current layer
	//       increment counter
	//
	//   counter2 = 1
	//   while the counter is 0
	//     and
	//   while every node is not in a layer
	//     for each node
	//       if this node is not in a layer lower than the current
	//         if this node points to only counter2 other nodes that are not 
	//                 in a lower layer than the current
	//           put it in the current layer
	//           increment counter
	//     increment counter2
	//
	// return counter
	
	int nodes_assigned_to_this_layer = 0;
	for (int x = 0; x < my_nodes.size(); x++) {
	    Node my_node = (Node)my_nodes.get(x);
	    if (((Integer)layer.get(my_node)).intValue() >= layer_number) {
		if (my_node.getOutEdges().size() == 0 ||
		    all_connections_in_lower_layer(my_node.getNeighbors(),
						   layer_number)) {
		    layer.put(my_node,new Integer(layer_number));
		    min_layer = Math.min(min_layer,layer_number);
		    max_layer = Math.max(max_layer,layer_number);
		    nodes_assigned_to_this_layer++;
		}
	    }
	}
	
	int connecting_nodes_threshold = 1;
	while (nodes_assigned_to_this_layer == 0 && 
	       !every_node_is_in_a_layer(my_nodes)) {
	    for (int x = 0; x < my_nodes.size(); x++) {
		Node my_node = (Node)my_nodes.get(x);
		if (((Integer)layer.get(my_node)).intValue() >= layer_number) {
		    if (nodes_not_in_lower_layer(my_node.getNeighbors(),
						 layer_number) == 
			connecting_nodes_threshold) {
			layer.put(my_node,new Integer(layer_number));
			min_layer = Math.min(min_layer,layer_number);
			max_layer = Math.max(max_layer,layer_number);
			nodes_assigned_to_this_layer++;
		    }
		}
	    }
	    connecting_nodes_threshold++;
	}
	return nodes_assigned_to_this_layer;
    }
    
    public boolean all_connections_in_lower_layer(Set s, int layer_number) {
	Iterator it = s.iterator();
	while(it.hasNext()) {
	    Node n = (Node)it.next();
	    if (((Integer)layer.get(n)).intValue() > layer_number)
		return false;
	}
	return true;
    }

    public boolean every_node_is_in_a_layer(List my_nodes) {
	for (int i = 0 ; i < my_nodes.size() ; i++) {
	    Node my_node = (Node)my_nodes.get(i);
	    if (((Integer)layer.get(my_node)).intValue() == 999999)
		return false;
	}
	return true;
    }

    public int nodes_not_in_lower_layer(Set these_nodes, 
					 int layer_number) {
	int returnValue = 0;
	Iterator it = these_nodes.iterator();
	while(it.hasNext()) {
	    Node n = (Node)it.next();
	    if (((Integer)layer.get(n)).intValue() >= layer_number)
		returnValue++;
	}
	return returnValue;
    }
    
    public void gather_nodes_with_layer(List my_nodes, int which_layer) {
	Random r = new Random();
	for (int x = 0; x < my_nodes.size(); x++) {
	    Node my_node = (Node)my_nodes.get(x);
	    if (((Integer)layer.get(my_node)).intValue() > which_layer + 1) {
		Iterator it = my_node.getNeighbors().iterator();
		while (it.hasNext()) {
		    Node temp = (Node)it.next();
		    if (((Integer)layer.get(temp)).intValue() == 
			which_layer + 1) {
			Coordinates loc = (Coordinates)locations.get(temp);
			locations.put(temp, 
				      new Coordinates(my_node.getX() - 
						      0.5 + r.nextDouble(),
						      my_node.getY() - 
						      0.5 + r.nextDouble()));
		    }
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
	return(true);
    }    
}
