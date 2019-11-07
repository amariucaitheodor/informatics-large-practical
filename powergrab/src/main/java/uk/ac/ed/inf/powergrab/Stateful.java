package uk.ac.ed.inf.powergrab;

import com.mapbox.geojson.Feature;

import java.util.*;

class Stateful extends Drone {

    static Drone createInstance(Position position, long seed, boolean submissionGeneration) {
        if (instance == null || submissionGeneration)
            instance = new Stateful(250, 0, position, seed);
        return instance;
    }

    private Stateful(double power, double coins, Position position, long seed){
        movesLeft = 250;
        trace = new LinkedList<>();
        trace.add(position);
        this.power = power;
        this.coins = coins;
        this.position = position;
        this.dirGenerator = new Random(seed);
    }

    @Override
    void move(Direction dir) {
        super.move(dir);
        if(trace.size()==5)
            trace.poll();
        trace.add(position);
    }

    private Feature target;

    private Feature closestStation(Position currentPos, Map map) {
        if(map.getPositiveUncollectedStations().isEmpty())
            return findRandomTarget(map); // with no positive uncollected stations left, game is over and target does not matter

        return Collections.max(map.getPositiveUncollectedStations(), Comparator.comparingDouble(a -> map.proximityBetweenPoints(map.getStationPosition(a), currentPos)));
    }

    private Position getTargetPosition(Position currentPos, Map map) {
        if(isStuck())
            target = findRandomTarget(map);
        if(target==null)
            target = closestStation(currentPos, map);
        Position targetPos;
        if(map.getCollectedStations().contains(target))
            targetPos = map.getStationPosition(closestStation(currentPos, map));
        else
            targetPos = map.getStationPosition(target);
        return targetPos;
    }

    private Direction closestSafeDirection(EnumMap<Direction, Double> safeDirectionsStateful) {
        double proximity = Integer.MIN_VALUE;
        Direction closestDirection = null;
        for(java.util.Map.Entry<Direction, Double> safeDir : safeDirectionsStateful.entrySet())
            if(safeDir.getValue()>proximity) {
                proximity = safeDir.getValue();
                closestDirection = safeDir.getKey();
            }
        return closestDirection;
    }

    Direction chooseMoveDirection(Position currentPos, Map map) {
        Direction choice = null;
        double maxGain = Integer.MIN_VALUE;
        Set<Feature> chosenStations = new HashSet<>();

        EnumMap<Direction, Double> safeDirectionsStateful = new EnumMap<>(Direction.class);
        Position targetPos = getTargetPosition(currentPos, map);

        for (Direction dir : Direction.values())
            if (currentPos.nextPosition(dir).inPlayArea()) {
                Position nextPosition = currentPos.nextPosition(dir);
                List<Feature> nextStations = new ArrayList<>();
                double positionGain = computePositionGain(nextPosition, map, nextStations);

                if (positionGain >= 0)
                    safeDirectionsStateful.put(dir, map.proximityBetweenPoints(nextPosition, targetPos));

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
            return closestSafeDirection(safeDirectionsStateful);
    }

}
