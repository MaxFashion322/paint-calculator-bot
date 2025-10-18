package org.example.model;

public class Rectangle extends Hole {
    private final double height;
    private final double length;

    public Rectangle(double _height,double _length){
        height=_height;
        length=_length;
    }

    @Override
    public double getArea(){
        return height*length;
    }
}

