package uk.ac.ed.inf.powergrab;

import com.mapbox.geojson.Feature;

import java.util.*;

class Stateless extends Drone {

    // Singleton design pattern to ensure only one drone is used
    private static Drone instance = null;

    static Drone createInstance(Position position, long seed, boolean submissionGeneration) {
        if (instance == null || submissionGeneration)
            instance = new Stateless(250, 0, position, seed);
        return instance;
    }

    private Stateless(double power, double coins, Position position, long seed){
        movesLeft = 250;
        this.power = power;
        this.coins = coins;
        this.position = position;
        this.dirGenerator = new Random(seed);
    }

    private Direction safeDirection(Set<Direction> safeDirections) {
        Direction choice = Direction.randomDirection(dirGenerator);
        while(!getPosition().nextPosition(choice).inPlayArea() || !safeDirections.contains(choice)) {
            choice = Direction.randomDirection(dirGenerator);
        }
        return choice;
    }

    Direction chooseMoveDirection(Position currentPos, Map map) {
        Direction choice = null;
        double maxGain = Integer.MIN_VALUE;
        Set<Feature> chosenStations = new HashSet<>();

        Set<Direction> safeDirections = new HashSet<>();

        for (Direction dir : Direction.values())
            if (currentPos.nextPosition(dir).inPlayArea()) {
                Position nextPosition = currentPos.nextPosition(dir);
                double nextGain = 0;
                List<Feature> nextStations = new ArrayList<>();

                for (Feature station : map.getUncollectedStations())
                    if (map.arePointsInRange(nextPosition, map.getStationPosition(station))) {
                        nextGain += map.stationUtility(station);
                        nextStations.add(station);
                    }

                if (nextGain >= 0)
                    safeDirections.add(dir);

                if (nextGain > maxGain) {
                    maxGain = nextGain;
                    choice = dir;
                    chosenStations = new HashSet<>(nextStations);
                }
            }

        charge(chosenStations, map);

        if(maxGain!=0)
            return choice;
        else
            return safeDirection(safeDirections);
    }
}
