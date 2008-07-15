package com.hp.hpl.guess;

import java.util.*;
import com.hp.hpl.guess.ui.GraphElementListener;

public interface GraphElement extends Comparable {
    public void __setattr__(String name, Object value);
    
    public Object __getattr__(String name);
    
    public void show();
    
    public void hide();
    
    //Returns a hashtable of field names and values.
    public Map values();

    public GraphElementListener getRep();
}
