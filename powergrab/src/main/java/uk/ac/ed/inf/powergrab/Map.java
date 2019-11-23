package uk.ac.ed.inf.powergrab;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class Map {
	private final List<Station> mapStations;
    private HashSet<Station> collectedStations;
    
	Map(List<Station> mapStations) {
	    collectedStations = new HashSet<>();
		this.mapStations = mapStations;
    }

    List<Station> getUncollectedStations() {
    	return mapStations.stream().filter(s -> !collectedStations.contains(s)).collect(Collectors.toList());
    }
    
    List<Station> getPositiveUncollectedStations() {
    	return getUncollectedStations().stream().filter(s -> s.getCoins()>=0).collect(Collectors.toList());
	}

	List<Station> getAllStations() {
		return mapStations;
	}
    
    Set<Station> getCollectedStations() {
		return collectedStations;
	}

    double distanceBetweenPoints(Position a, Position b) {
    	return Math.hypot(a.latitude-b.latitude, a.longitude-b.longitude);
    }
    
    boolean arePointsInRange(Position a, Position b) {
    	return distanceBetweenPoints(a, b) < Game.RANGEDIST;
    }

    double stationUtility(Station station) {
    	return Game.COINSWEIGHT * station.getCoins() +
    			Game.POWERWEIGHT * station.getPower();
    }
}
