package com.example.lr2_oop.property;

public class TransformProperty {
    private double x;//позиция по X
    private double y;//позиция по Y
    private double width;//ширина
    private double height;//высота
    private double rotate;//угол поворота в градусах
    //test
    private double scaleX;//масштаб по X
    private double scaleY;//масштаб по Y
    //test

    //трансформации
    public void Translate(double dx, double dy)//перемещение
    {//пока что просто добавлять будем перемещение
        this.x += dx;
        this.y += dy;
    }
    public void Resize(double dw, double dh)//изменение размера
    {//пока что только прибавлять прирост
        this.width += dw;
        this.height += dh;
    }
    public void Rotate(double angle){//поворот, добавлением угла
        this.rotate += angle;
    }
    public void SetPosition(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
