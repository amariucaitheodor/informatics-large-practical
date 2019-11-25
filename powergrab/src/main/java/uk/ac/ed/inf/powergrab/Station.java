package uk.ac.ed.inf.powergrab;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;

import java.util.List;

class Station {
    private double coins;
    private double power;
    private Position position;

    Station(Feature station) {
        this.coins = station.getNumberProperty("coins").doubleValue();
        this.power = station.getNumberProperty("power").doubleValue();

        Point stationPoint = (Point) station.geometry();
        assert stationPoint != null;
        List<Double> stationCoords = stationPoint.coordinates();
        this.position = new Position(stationCoords.get(1), stationCoords.get(0));
    }

    double getCoins() {
        return coins;
    }

    double getPower() {
        return power;
    }

    Position getPosition() {
        return position;
    }

    void deplete() {
        coins = 0;
        power = 0;
    }
}
