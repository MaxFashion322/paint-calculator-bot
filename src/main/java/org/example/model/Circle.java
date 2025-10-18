package org.example.model;

public class Circle extends Hole {
    private final double radius;

    public Circle(double _radius){
        radius=_radius;
    }

    @Override
    public double getArea(){
        return Math.PI * radius * radius;
    }
}

