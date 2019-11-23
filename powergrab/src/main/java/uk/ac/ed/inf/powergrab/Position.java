package uk.ac.ed.inf.powergrab;

import java.util.HashMap;

class Position {
    final double latitude;
    final double longitude;

    // moveShift pre-calculates the shifts in latitude and longitude (double) represented by each of the 16 directions
    static final HashMap<Direction, double[]> moveShift = new HashMap<>();
    
    Position(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    Position nextPosition(Direction direction) {
        return new Position(this.latitude + moveShift.get(direction)[0], this.longitude + moveShift.get(direction)[1]);
    }
    
    boolean inPlayArea() {
        // compares the position with two points marking the upper left (Forrest Hill) and lower right (Buccleuch St bus stop) bounds of the play area
        return !(this.longitude<=-3.192473 || this.latitude>=55.946233 || this.longitude>=-3.184319 || this.latitude<=55.942617);
    }

    @Override
    public String toString() {
        return "Position{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
