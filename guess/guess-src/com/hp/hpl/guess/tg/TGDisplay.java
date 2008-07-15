/*
 * TouchGraph LLC. Apache-Style Software License
 *
 *
 * Copyright (c) 2001-2002 Alexander Shapiro. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        TouchGraph LLC (http://www.touchgraph.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "TouchGraph" or "TouchGraph LLC" must not be used to endorse
 *    or promote products derived from this software without prior written
 *    permission.  For written permission, please contact
 *    alex@touchgraph.com
 *
 * 5. Products derived from this software may not be called "TouchGraph",
 *    nor may "TouchGraph" appear in their name, without prior written
 *    permission of alex@touchgraph.com.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL TOUCHGRAPH OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 */

package com.hp.hpl.guess.tg;

import com.touchgraph.graphlayout.*;
import com.touchgraph.graphlayout.interaction.*;
import com.touchgraph.graphlayout.graphelements.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Enumeration;
import java.net.URL;
import java.io.InputStreamReader;

import com.hp.hpl.guess.ui.FrameListener;
import com.hp.hpl.guess.freehep.*;

public class TGDisplay extends GLPanel implements FrameListener {
    
    /** Default constructor.
     */
    public TGDisplay() {
	super();
    }

    public void randomGraph() throws TGException {
    }    

    public void center() {
	//pan.getHVScroll().slowScrollToCenter(n1);
    }

    public void center(Object o) {
	center();
    }

    public void repaint() {
	super.repaint();
    }

    public void setFrozen(boolean state){}

    public void exportGIF(String filename) {
	HEPWriter.export(filename,getTGPanel(),HEPWriter.GIF);
    }

    public void exportJPG(String filename){
	HEPWriter.export(filename,getTGPanel(),HEPWriter.JPG);
    }

    public void exportPDF(String filename){
	HEPWriter.export(filename,getTGPanel(),HEPWriter.PDF);
    }

    public void exportPS(String filename){
	HEPWriter.export(filename,getTGPanel(),HEPWriter.PS);
    }

    public void exportEPS(String filename){
	HEPWriter.export(filename,getTGPanel(),HEPWriter.EPS);
    }

    public void exportSVG(String filename){
	HEPWriter.export(filename,getTGPanel(),HEPWriter.SVG);
    }

    public void exportSWF(String filename){
	HEPWriter.export(filename,getTGPanel(),HEPWriter.SWF);
    }

    public void exportJAVA(String filename){
	HEPWriter.export(filename,getTGPanel(),HEPWriter.JAVA);
    }

    public void exportCGM(String filename){
	HEPWriter.export(filename,getTGPanel(),HEPWriter.CGM);
    }

    public void exportEMF(String filename){
	HEPWriter.export(filename,getTGPanel(),HEPWriter.EMF);
    }

    public void exportPNG(String filename){
	HEPWriter.export(filename,getTGPanel(),HEPWriter.PNG);
    }

    public Color getDisplayBackground() {
	return(null);
    }

    public void setDisplayBackground(Color c) {
    }

    public void setBackgroundImage(String filename) {
    }

    public void setBackgroundImage(String filename, double x, double y) {
    }

    public void removeBackgroundImage() {
    }

    public BufferedImage getFullImage() {
	return(null);
    }
} // end com.touchgraph.graphlayout.TGDisplay
