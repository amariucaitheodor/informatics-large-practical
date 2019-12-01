package uk.ac.ed.inf.powergrab;

import java.util.EnumMap;

class Position {
    // stores pre-calculated shifts in latitude and longitude (double) represented by each of the 16 directions, this
    // is done in the Game class at program initialization
    static final EnumMap<Direction, double[]> moveShift = new EnumMap<>(Direction.class);
    final double latitude;
    final double longitude;

    Position(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    Position nextPosition(Direction direction) {
        return new Position(this.latitude + moveShift.get(direction)[0],
                this.longitude + moveShift.get(direction)[1]);
    }

    // compares the position with two points marking the upper left (Forrest Hill) and lower right (Buccleuch St bus
    // stop) bounds of the play area
    boolean inPlayArea() {
        return !(this.longitude <= -3.192473 ||
                this.latitude >= 55.946233 ||
                this.longitude >= -3.184319 ||
                this.latitude <= 55.942617);
    }

    // this method is overridden to provide the string representation of the class with identical hash codes for
    // identical latitudes and longitudes
    @Override
    public String toString() {
        return "Position{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
