package com.hp.hpl.guess.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class VerticalTextIcon implements Icon, SwingConstants{ 
    private Font font = UIManager.getFont("Label.font"); 
    private FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font); 
 
    private String text; 
    private int width, height; 
    private boolean clockwize; 
 
    public VerticalTextIcon(String text, boolean clockwize){ 
        this.text = text; 
        width = SwingUtilities.computeStringWidth(fm, text) + 2; 
        height = fm.getHeight() + 2; 
        this.clockwize = clockwize; 
    } 
 
    public void paintIcon(Component c, Graphics g, int x, int y){ 
        Graphics2D g2 = (Graphics2D)g; 
        Font oldFont = g.getFont(); 
        Color oldColor = g.getColor(); 
        AffineTransform oldTransform = g2.getTransform(); 
 
        g.setFont(font); 
        g.setColor(Color.black); 
        if(clockwize){ 
            g2.translate(x+getIconWidth(), y); 
            g2.rotate(Math.PI/2); 
        }else{ 
            g2.translate(x, y+getIconHeight()); 
            g2.rotate(-Math.PI/2); 
        } 
        g.drawString(text, 0, fm.getLeading()+fm.getAscent()); 
 
        g.setFont(oldFont); 
        g.setColor(oldColor); 
        g2.setTransform(oldTransform); 
    } 
 
    public int getIconWidth(){ 
        return height; 
    } 
 
    public int getIconHeight(){ 
        return width; 
    } 
}
