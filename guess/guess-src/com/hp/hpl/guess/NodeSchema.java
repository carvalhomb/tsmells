package com.hp.hpl.guess;

import java.util.*;
import java.sql.*;
import com.hp.hpl.guess.storage.*;

public class NodeSchema extends Schema
{

    Graph myG = null;

    public NodeSchema(Graph graph)
    {
	StorageFactory.getSL().fillSchema(this,graph);
	myG = graph;
    }

    public void createIntegerField(String fieldName,int def) {
	myG.addNodeField(fieldName,Types.INTEGER,new Integer(def));
    }

    public void createDoubleField(String fieldName,double def) {
	myG.addNodeField(fieldName,Types.DOUBLE,new Double(def));
    }

    public void createBooleanField(String fieldName,boolean def) {
	myG.addNodeField(fieldName,Types.BOOLEAN,new Boolean(def));
    }
    
    public void createStringField(String fieldName,String def) {
	myG.addNodeField(fieldName,Types.VARCHAR,def);
    }

}
