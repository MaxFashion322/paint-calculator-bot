package org.example.model;
import java.util.ArrayList;

public class Room {
    public ArrayList<Wall> walls = new ArrayList<>();

    public Room(ArrayList<Wall> _walls) {
        walls=_walls;
    }

    public Room(){}

    public double getTotalPaint(){
        double total=0;
        for(Wall w:walls){
            total+=w.getTotalConsumption();
        }
        return total;
    }
}
