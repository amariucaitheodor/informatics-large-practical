package uk.ac.ed.inf.powergrab;

import java.util.*;

import com.mapbox.geojson.Feature;

abstract class Drone {
    int movesLeft;
    double power;
    double coins;
    Position position;
    Queue<Position> trace;
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

    boolean isStuck() {
        HashMap<String, Integer> occurrences = new HashMap<>();
        trace.forEach(pos -> occurrences.put(pos.toString(), occurrences.getOrDefault(pos.toString(), 0) + 1));
        for(int occurrence : occurrences.values())
            if(occurrence==3)
                return true;
        return false;
    }

	void charge(Set<Feature> chosenStations, Map map) {
		for(Feature chosenStation : chosenStations) 
        if(!map.getCollectedStations().contains(chosenStation)) {
        	this.coins += map.getStationCoins(chosenStation);
        	this.power += map.getStationPower(chosenStation);
        }
        map.getCollectedStations().addAll(chosenStations);
	}

    Feature findRandomTarget(Map map) {
        int stationsNo = map.getAllStations().size();
        return map.getAllStations().get(dirGenerator.nextInt(stationsNo));
    }

    abstract Direction chooseMoveDirection(Position currentPos, Map map);

    double computePositionGain(Position position, Map map, List<Feature> nextStations) {
        double nextGain = 0;
        for (Feature station : map.getUncollectedStations())
            if (map.arePointsInRange(position, map.getStationPosition(station))) {
                nextGain += map.stationUtility(station);
                nextStations.add(station);
            }
        return nextGain;
    }
}
