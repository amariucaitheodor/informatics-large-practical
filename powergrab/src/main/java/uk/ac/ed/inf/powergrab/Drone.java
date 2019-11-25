package uk.ac.ed.inf.powergrab;

import java.util.Random;

abstract class Drone {
    // Singleton design pattern to ensure only one drone is used
    static Drone instance = null;
    int movesLeft;
    double power;
    double coins;
    Position position;
    Random dirGenerator;

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

    boolean hasMovesLeft() {
        return movesLeft > 0;
    }

    boolean hasPower() {
        return power >= 1.25;
    }

    void chargeFromStation(Station chosenStation, Map map) {
        if (map.getCollectedStations().contains(chosenStation))
            return;

        this.coins += chosenStation.getCoins();
        this.power += chosenStation.getPower();
        chosenStation.deplete();
        map.getCollectedStations().add(chosenStation);
    }

    abstract Direction chooseMoveDirection(Position currentPos, Map map);
}
