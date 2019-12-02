package uk.ac.ed.inf.powergrab;

import java.util.Random;

abstract class Drone {
    int movesLeft;
    float power;
    float coins;
    Position position;
    Random randomDirGen;

    float getPower() {
        return power;
    }

    float getCoins() {
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
