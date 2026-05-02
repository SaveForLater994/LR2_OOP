package com.example.lr2_oop.model;

import com.example.lr2_oop.property.FillProperty;
import com.example.lr2_oop.property.StrokeProperty;
import com.example.lr2_oop.property.TransformProperty;
import javafx.geometry.Rectangle2D;

public abstract class Shape {
    protected Layer onLayer;
    protected FillProperty fill;
    protected StrokeProperty stroke;//ахахахахах дрочить
    protected TransformProperty transform;
    protected String id;
    protected boolean isSelected;//выделена ли

    public abstract boolean contains(double x, double y);//попадание курсора
    public abstract Rectangle2D getBounds();//габариты
    public abstract void move(double dx, double dy);//перемещение
    public abstract void resize(double factor);//масштабирование

    public void SetPosition(double x, double y){
        transform.SetPosition(x,y);
    }
    public void SetRotation(double angle){
        transform.Rotate(angle);
    }
}
