package uk.ac.ed.inf.powergrab;

import java.util.*;

abstract class Drone {
    int movesLeft;
    double power;
    double coins;
    Position position;
    Random dirGenerator;

    // Singleton design pattern to ensure only one drone is used
    static Drone instance = null;

    double getPower() {
        return power;
    }

    double getCoins() {
        return coins;
    }
    
    Position getPosition() {
        return position;
    }

    void move(Direction dir) {
		position = position.nextPosition(dir);
		power -= Game.MOVEPOWERCOST;
		movesLeft--;
    }
	
    boolean hasMoves()
    {
    	return movesLeft > 0;	
    }
        
    boolean hasPower()
    {
        return power >= 1.25;
    }

	void charge(Set<Station> chosenStations, Map map) {
		for(Station chosenStation : chosenStations)
        if(!map.getCollectedStations().contains(chosenStation)) {
        	this.coins += chosenStation.getCoins();
        	this.power += chosenStation.getPower();
        }
        map.getCollectedStations().addAll(chosenStations);
	}

    abstract Direction chooseMoveDirection(Position currentPos, Map map);

    double computePositionGain(Position position, Map map, List<Station> nextStations) {
        double nextGain = 0;
        for (Station station : map.getUncollectedStations())
            if (map.arePointsInRange(position, station.getPosition())) {
                nextGain += map.stationUtility(station);
                nextStations.add(station);
            }
        return nextGain;
    }
}
