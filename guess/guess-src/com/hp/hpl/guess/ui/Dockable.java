package com.hp.hpl.guess.ui;

public interface Dockable {

    public int getDirectionPreference();

    public void opening(boolean state);

    public void attaching(boolean state);

    public String getTitle();

    public GuessJFrame getWindow();

    public void setWindow(GuessJFrame gjf);

}
