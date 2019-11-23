package uk.ac.ed.inf.powergrab;

import java.util.*;

class Stateless extends Drone {

    private Stateless(Position position, long seed) {
        movesLeft = 250;
        this.power = 250;
        this.coins = 0;
        this.position = position;
        this.dirGenerator = new Random(seed);
    }

    static Drone createInstance(Position position, long seed, boolean submissionGeneration) {
        if (instance == null || submissionGeneration)
            instance = new Stateless(position, seed);
        return instance;
    }

    private Direction randomSafeDirection(Set<Direction> safeDirections) {
        Direction choice = Direction.randomDirection(dirGenerator);
        while (!getPosition().nextPosition(choice).inPlayArea() || !safeDirections.contains(choice)) {
            choice = Direction.randomDirection(dirGenerator);
        }
        return choice;
    }

    Direction chooseMoveDirection(Position currentPos, Map map) {
        Direction choice = null;
        double maxGain = Integer.MIN_VALUE;
        Set<Station> chosenStations = new HashSet<>();

        Set<Direction> safeDirections = new HashSet<>();

        for (Direction dir : Direction.values())
            if (currentPos.nextPosition(dir).inPlayArea()) {
                List<Station> nextStations = new ArrayList<>();
                double positionGain = computePositionGain(currentPos.nextPosition(dir), map, nextStations);

                // any direction where we gain coins is a safe direction
                if (positionGain >= 0)
                    safeDirections.add(dir);

                // keep track of maximum coin profit
                if (positionGain > maxGain) {
                    maxGain = positionGain;
                    choice = dir;
                    chosenStations = new HashSet<>(nextStations);
                }
            }

        if(safeDirections.isEmpty() || maxGain > 0) {
            charge(chosenStations, map);
            return choice;
        }

        // all directions are neutral
        return randomSafeDirection(safeDirections);
    }

    // calculate utility sum of all station within this position
    private double computePositionGain(Position position, Map map, List<Station> nextStations) {
        double nextGain = 0;
        for (Station station : map.getUncollectedStations())
            if (map.arePointsInRange(position, station.getPosition())) {
                nextGain += map.stationUtility(station);
                nextStations.add(station);
            }
        return nextGain;
    }
}
