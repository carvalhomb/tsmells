package com.hp.hpl.guess.util;

import java.util.HashSet;

public class AnnoHashSet extends HashSet {

    public String annotation = null;

    public AnnoHashSet() {
    }

    public AnnoHashSet(String annotation) {
	this.annotation = annotation;
    }
}
