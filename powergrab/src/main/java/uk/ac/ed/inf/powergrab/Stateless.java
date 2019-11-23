package uk.ac.ed.inf.powergrab;

import java.util.*;

class Stateless extends Drone {

    static Drone createInstance(Position position, long seed, boolean submissionGeneration) {
        if (instance == null || submissionGeneration)
            instance = new Stateless(position, seed);
        return instance;
    }

    private Stateless(Position position, long seed){
        movesLeft = 250;
        this.power = 250;
        this.coins = 0;
        this.position = position;
        this.dirGenerator = new Random(seed);
    }

    private Direction randomSafeDirection(Set<Direction> safeDirections) {
        Direction choice = Direction.randomDirection(dirGenerator);
        while(!getPosition().nextPosition(choice).inPlayArea() || !safeDirections.contains(choice)) {
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

                if (positionGain >= 0)
                    safeDirections.add(dir);

                if (positionGain > maxGain) {
                    maxGain = positionGain;
                    choice = dir;
                    chosenStations = new HashSet<>(nextStations);
                }
            }

        charge(chosenStations, map);

        if(maxGain!=0)
            return choice;
        else
            return randomSafeDirection(safeDirections);
    }
}
