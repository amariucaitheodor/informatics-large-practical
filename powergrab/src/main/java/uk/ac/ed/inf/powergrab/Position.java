package uk.ac.ed.inf.powergrab;

import java.util.HashMap;

class Position {
    final double latitude;
    final double longitude;

    // Every instance of this class shares the class variable (HashMap) below representing precomputed trigonometry calculations
    static final HashMap<Direction, Shift> moveShift = new HashMap<>();
    
    Position(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    Position nextPosition(Direction direction) {
        return new Position(this.latitude + moveShift.get(direction).getLatShift(), this.longitude + moveShift.get(direction).getLongShift());
    }
    
    boolean inPlayArea() {
        // compares the position with two points marking the upper left (Forrest Hill) and lower right (Buccleuch St bus stop) bounds of the play area
        return !(this.longitude<=-3.192473 || this.latitude>=55.946233 || this.longitude>=-3.184319 || this.latitude<=55.942617);
    }

    static class Shift {
        private double latShift;
        private double longShift;

        Shift(double latShift, double longShift) {
            this.latShift = latShift;
            this.longShift = longShift;
        }

        double getLongShift() {
            return longShift;
        }

        double getLatShift() {
            return latShift;
        }
    }

    @Override
    public String toString() {
        return "Position{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
