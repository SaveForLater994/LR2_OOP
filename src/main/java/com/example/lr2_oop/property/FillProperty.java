package com.example.lr2_oop.property;

import javafx.scene.paint.Color;

public class FillProperty {
    private Color color;
    private boolean isTransparent;

    public Color getColor(){
        return color;
    }
    public boolean isTransp(){
        return isTransparent;
    }
    public void setColor(Color color){
        this.color = color;
    }
    public void setTransparency(boolean isTrans){
        isTransparent = isTrans;
    }

}
