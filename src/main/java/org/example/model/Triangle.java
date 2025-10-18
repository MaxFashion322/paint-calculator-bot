package org.example.model;

public class Triangle extends Hole {
    private final double base;
    private final double height;

    public Triangle(double _base,double _height){
        base = _base;
        height = _height;
    }

    @Override
    public double getArea(){
        return 0.5 * base * height;
    }
}

