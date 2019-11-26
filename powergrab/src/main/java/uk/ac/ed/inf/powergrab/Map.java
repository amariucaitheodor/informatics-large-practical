package uk.ac.ed.inf.powergrab;

import com.mapbox.geojson.Feature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class Map {
    private final List<Station> mapStations;
    private HashSet<Station> collectedStations;

    Map(List<Feature> mapStationsAsFeatures) {
        collectedStations = new HashSet<>();

        // extract stations from GeoJson Features
        this.mapStations = new ArrayList<>();
        mapStationsAsFeatures.forEach(featureStation -> this.mapStations.add(new Station(featureStation)));
    }

    List<Station> getPositiveUncollectedStations() {
        List<Station> uncollectedStations =
                mapStations.stream().filter(s -> !collectedStations.contains(s)).collect(Collectors.toList());
        return uncollectedStations.stream().filter(s -> s.getCoins() >= 0).collect(Collectors.toList());
    }

    List<Station> getAllStations() {
        return mapStations;
    }

    Set<Station> getCollectedStations() {
        return collectedStations;
    }

    double distanceBetweenPoints(Position a, Position b) {
        return Math.hypot(a.latitude - b.latitude, a.longitude - b.longitude);
    }

    boolean arePointsInRange(Position a, Position b) {
        return distanceBetweenPoints(a, b) < Game.RANGEDIST;
    }

    double stationUtility(Station station) {
        return Game.COINSWEIGHT * station.getCoins() +
                Game.POWERWEIGHT * station.getPower();
    }
}
