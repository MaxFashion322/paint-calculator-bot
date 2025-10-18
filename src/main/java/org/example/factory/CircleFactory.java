package org.example.factory;

import org.example.model.Circle;
import org.example.model.Hole;

public class CircleFactory extends HoleFactory{
    private final double radius;

    public CircleFactory(double radius) {
        this.radius = radius;
    }

    @Override
    public Hole createHole() {
        return new Circle(radius);
    }
}