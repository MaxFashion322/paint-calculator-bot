package org.example.core;

import org.example.model.Hole;
import org.example.model.Room;
import org.example.model.Wall;

import java.util.ArrayList;
import java.util.List;

public class UserSession {
    public String currentStep = "ASK_ROOM_QTY";
    public double roomQty;
    public double wallQty;
    public int currentRoomIndex;
    public int currentWallIndex;
    public List<Room> rooms = new ArrayList<>();
    public List<Wall> currentRoomWalls = new ArrayList<>();

    public double height;
    public double length;
    public double layers;
    public double consumption;
    public double holeQty;
    public int currentHoleIndex;
    public double tempParam1;
    public double tempParam2;
    public List<Hole> holes = new ArrayList<>();

    public Wall buildWall(){
        return new Wall(height,length,layers,consumption,holes);
    }
}
