package com.hp.hpl.guess.ui;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * @pyobj v
 */
public interface FrameListener {
    
    /**
     * @pyexport
     */
    public void center();
    
    /**
     * @pyexport
     */
    public void center(Object o);

    public void repaint();

    /**
     * @pyexport
     */
    public void setFrozen(boolean state);
   
    /**
     * @pyexport
     */
    public void exportGIF(String filename);
    
    /**
     * @pyexport
     */
    public void exportJPG(String filename);
    
    /**
     * @pyexport
     */
    public void exportPDF(String filename);
    
    /**
     * @pyexport
     */
    public void exportPS(String filename);
    
    /**
     * @pyexport
     */
    public void exportEPS(String filename);

    /**
     * @pyexport
     */    
    public void exportSVG(String filename);

    /**
     * @pyexport
     */
    public void exportSWF(String filename);
    
    /**
     * @pyexport
     */
    public void exportJAVA(String filename);

    /**
     * @pyexport
     */    
    public void exportCGM(String filename);

    /**
     * @pyexport
     */    
    public void exportEMF(String filename);

    /**
     * @pyexport
     */
    public void exportPNG(String filename);

    /**
     * @pyexport
     */
    public Color getDisplayBackground();

    /**
     * @pyexport
     */
    public void setDisplayBackground(Color c);

    /**
     * @pyexport
     */
    public void setBackgroundImage(String filename);

    /**
     * @pyexport
     */
    public void setBackgroundImage(String filename, double x, double y);

    /**
     * @pyexport
     */
    public void removeBackgroundImage();

    public BufferedImage getFullImage();
}
