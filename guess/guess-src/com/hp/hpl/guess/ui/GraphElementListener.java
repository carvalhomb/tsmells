package com.hp.hpl.guess.ui;

public interface GraphElementListener {

    public Object get(String field);
    
    public void set(String field, Object value);

    public void highlight(boolean state);
}
