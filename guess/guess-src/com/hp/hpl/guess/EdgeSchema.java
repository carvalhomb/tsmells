package com.hp.hpl.guess;

import java.util.*;
import java.sql.*;
import com.hp.hpl.guess.storage.*;

public class EdgeSchema extends Schema {

    Graph myG = null;

    public EdgeSchema(Graph graph)
    {
	StorageFactory.getSL().fillSchema(this,graph);
	myG = graph;
    }

    public void createIntegerField(String fieldName,int def) {
	myG.addEdgeField(fieldName,Types.INTEGER,new Integer(def));
    }

    public void createDoubleField(String fieldName,double def) {
	myG.addEdgeField(fieldName,Types.DOUBLE,new Double(def));
    }

    public void createBooleanField(String fieldName,boolean def) {
	myG.addEdgeField(fieldName,Types.BOOLEAN,new Boolean(def));
    }
    
    public void createStringField(String fieldName,String def) {
	myG.addEdgeField(fieldName,Types.VARCHAR,def);
    }
}
