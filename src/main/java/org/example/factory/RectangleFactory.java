package org.example.factory;

import org.example.model.Hole;
import org.example.model.Rectangle;

public class RectangleFactory extends HoleFactory{
    private final double height;
    private final double length;

    public RectangleFactory(double _height,double _length) {
        height = _height;
        length = _length;
    }

    public Hole createHole(){
        return new Rectangle(height, length);
    }
}