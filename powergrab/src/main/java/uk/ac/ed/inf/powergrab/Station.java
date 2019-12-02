package uk.ac.ed.inf.powergrab;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;

import java.util.List;

class Station {
    private float coins;
    private float power;
    private final Position position;

    Station(Feature station) {
        this.coins = station.getNumberProperty("coins").floatValue();
        this.power = station.getNumberProperty("power").floatValue();

        Point stationPoint = (Point) station.geometry();
        assert stationPoint != null;
        List<Double> stationCoords = stationPoint.coordinates();
        this.position = new Position(stationCoords.get(1), stationCoords.get(0));
    }

    float getCoins() {
        return coins;
    }

    float getPower() {
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
