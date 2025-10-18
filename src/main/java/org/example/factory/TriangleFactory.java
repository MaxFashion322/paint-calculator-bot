package org.example.factory;

import org.example.model.Hole;
import org.example.model.Triangle;

public class TriangleFactory extends HoleFactory{
    private final double base;
    private final double height;

    public TriangleFactory(double _base, double _height) {
        base = _base;
        height = _height;
    }

    public Hole createHole(){
        return new Triangle(base, height);
    }
}