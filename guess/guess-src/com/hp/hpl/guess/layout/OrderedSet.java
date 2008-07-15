// $Id: OrderedSet.java,v 1.1 2005/10/05 20:19:39 eytanadar Exp $
package com.hp.hpl.guess.layout;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/** 
 * OrderedSet is needed so that we can produce consistent layouts. We
 * need a <em>consistent</em> order (not a particular order) in neighbor,
 * predecessor, and successor sets as well as the sets of edges,
 * outgoingEdges, and IncomingEdges. Notice that no particular
 * operator has been applied to this set. The point is simply that
 * iterators for other types of sets may return elements in a
 * <em>different</em> order every time.  We need to use a Set because we
 * don't want duplicates.  
 *
 * @author Hacked by Eytan Adar for Guess
 */
public class OrderedSet extends ArrayList
    implements java.io.Serializable
{

  public final static OrderedSet nullValue = new OrderedSet();

  public boolean add(Object object) {
    if (this.contains(object)) {
      return false;
    } else {
      return super.add(object);
    }
  }

  public boolean addAll(Collection set) {
    if (set == null) {
      return false;
    }
    else {
      boolean retVal = true;
      Iterator setIter = set.iterator();
      while (setIter.hasNext()) {
        if (add(setIter.next())) {
        }
        else {
          retVal = false;
        }
      }
      return retVal;
    }
  }
}

