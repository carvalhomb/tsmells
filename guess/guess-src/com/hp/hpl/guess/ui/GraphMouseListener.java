package com.hp.hpl.guess.ui;

import java.util.EventListener;
import com.hp.hpl.guess.Node;
import com.hp.hpl.guess.Edge;

public interface GraphMouseListener extends EventListener {

    public void mouseEnterNode(Node n);

    public void mouseEnterEdge(Edge e);

    public void mouseLeaveNode(Node n);

    public void mouseLeaveEdge(Edge e);
}
