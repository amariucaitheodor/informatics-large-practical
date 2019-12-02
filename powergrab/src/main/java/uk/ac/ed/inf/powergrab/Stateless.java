package uk.ac.ed.inf.powergrab;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

class Stateless extends Drone {

    Stateless(Position position, long seed) {
        movesLeft = 250;
        this.power = 250;
        this.coins = 0;
        this.position = position;
        this.randomDirGen = new Random(seed);
    }

    private Direction randomSafeDirection(Set<Direction> safeDirections) {
        Direction choice = Direction.randomDirection(randomDirGen);
        while (!getPosition().nextPosition(choice).inPlayArea() || !safeDirections.contains(choice)) {
            choice = Direction.randomDirection(randomDirGen);
        }
        return choice;
    }

    Direction chooseMoveDirection(Position currentPos, Map map) {
        Direction choice = null;
        double maxGain = Integer.MIN_VALUE;
        Station chosenStation = null;
        Set<Direction> safeDirections = new HashSet<>();

        for (Direction dir : Direction.values())
            if (currentPos.nextPosition(dir).inPlayArea()) {
                Position futurePosition = currentPos.nextPosition(dir);

                // calculate utility sum of nearest station to future position
                double directionUtilityGain = 0;
                Station nearestStation = null;
                double distanceToNearestStation = Integer.MAX_VALUE;

                for (Station station : map.getAllStations()) {
                    double distToStation = map.distanceBetweenPoints(futurePosition, station.getPosition());
                    if (map.arePointsInRange(futurePosition, station.getPosition()) &&
                            distToStation < distanceToNearestStation) {
                        nearestStation = station;
                        distanceToNearestStation = distToStation;
                        directionUtilityGain = map.stationUtility(station);
                    }
                }

                // any direction where we don't lose coins is a safe direction
                if (directionUtilityGain >= 0)
                    safeDirections.add(dir);

                // keep track of maximum coin profit
                if (directionUtilityGain > maxGain) {
                    maxGain = directionUtilityGain;
                    choice = dir;
                    chosenStation = nearestStation;
                }
            }

        // choice is between neutral directions and negative directions
        if (maxGain == 0)
            return randomSafeDirection(safeDirections);

        // either we pick the best choice among the worst when maxGain<0, or just the best choice overall when maxGain>0
        assert chosenStation != null;
        chargeFromStation(chosenStation, map);
        return choice;
    }
}
