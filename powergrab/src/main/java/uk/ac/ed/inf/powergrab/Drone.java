package uk.ac.ed.inf.powergrab;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import com.mapbox.geojson.Feature;

class Drone {
    private int movesLeft;
    private double power;
    private double coins;
    private Position position;
    private Queue<Position> trace;
	
    double getPower() {
        return power;
    }

    double getCoins() {
        return coins;
    }
    
    Position getPosition() {
        return position;
    }
	
    Drone(double power, double coins, Position position){
		movesLeft = 250;
		trace = new LinkedList<>();
        trace.add(position);
		this.power = power;
		this.coins = coins;
		this.position = position;
    }

    void move(Direction dir)
    {
		position = position.nextPosition(dir);
		power -= Game.MOVEPOWERCOST;
		movesLeft--;

        if(trace.size()==5)
            trace.poll();
        trace.add(position);
    }
	
    boolean hasMoves()
    {
    	return movesLeft > 0;	
    }
        
    boolean hasPower()
    {
        return power >= 1.25;
    }

    boolean isStuck() {
        HashMap<String, Integer> occurrences = new HashMap<>();
        trace.forEach(pos -> occurrences.put(pos.toString(), occurrences.getOrDefault(pos.toString(), 0) + 1));
        for(int occurrence : occurrences.values())
            if(occurrence==3)
                return true;
        return false;
    }

	void charge(Set<Feature> chosenStations, GameMap gameMap) {
		for(Feature chosenStation : chosenStations) 
        if(!gameMap.getCollectedStations().contains(chosenStation)) {
        	this.coins += gameMap.getStationCoins(chosenStation);
        	this.power += gameMap.getStationPower(chosenStation);
        }
        gameMap.getCollectedStations().addAll(chosenStations);
	}
}
