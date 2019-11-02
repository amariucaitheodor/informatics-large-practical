package uk.ac.ed.inf.powergrab;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

class Map {
	private final List<Feature> mapStations;
    private HashSet<Feature> collectedStations;
    private List<Point> path;
    
	Map(Position initialDronePos, List<Feature> mapStations) {
	    collectedStations = new HashSet<>();
		path = new ArrayList<>();
	    addPositionToPath(initialDronePos);
	    this.mapStations = mapStations;
    }

    List<Feature> getUncollectedStations() {
    	return mapStations.stream().filter(s -> !collectedStations.contains(s)).collect(Collectors.toList());
    }
    
    List<Feature> getPositiveUncollectedStations() {
    	return getUncollectedStations().stream().filter(s -> getStationCoins(s)>=0).collect(Collectors.toList());
	}

	List<Feature> getAllStations() {
		return mapStations;
	}
    
    Set<Feature> getCollectedStations() {
		return collectedStations;
	}

	void addPositionToPath(Position pos) {
		this.path.add(Point.fromLngLat(pos.longitude, pos.latitude));
	}

	Position getStationPosition(Feature station) {
		Point stationPoint = (Point) station.geometry();
		assert stationPoint != null;
		List<java.lang.Double> stationCoord = stationPoint.coordinates();
		return new Position(stationCoord.get(1), stationCoord.get(0));
	}
	
    double getStationCoins(Feature station) {
    	return station.getNumberProperty("coins").doubleValue();
    }
    
    double getStationPower(Feature station) {
    	return station.getNumberProperty("power").doubleValue();
    }
    
    double proximityBetweenPoints(Position a, Position b) {
    	return 1.0 / distanceBetweenPoints(a, b);
    }
    
    private double distanceBetweenPoints(Position a, Position b) {
    	return Math.hypot(a.latitude-b.latitude, a.longitude-b.longitude);
    }
    
    boolean arePointsInRange(Position a, Position b) {
    	return distanceBetweenPoints(a, b) < Game.RANGEDIST;
    }

	FeatureCollection createOutputMap() {
		List<Feature> finalFeatures = mapStations;
    	finalFeatures.add(Feature.fromGeometry(LineString.fromLngLats(path)));
		return FeatureCollection.fromFeatures(finalFeatures);
	}
    
    double stationUtility(Feature station) {
    	return Game.COINSWEIGHT * getStationCoins(station) +
    			Game.POWERWEIGHT * getStationPower(station);
    }
}
