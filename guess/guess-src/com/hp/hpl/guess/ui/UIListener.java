package com.hp.hpl.guess.ui;

import java.util.EventListener;
import com.hp.hpl.guess.Node;
import com.hp.hpl.guess.Edge;

public interface UIListener extends GraphMouseListener {

    public void shiftClickNode(Node n);

    public void shiftClickEdge(Edge e);

    public void clickNode(Node n);

    public void clickEdge(Edge e);
}
