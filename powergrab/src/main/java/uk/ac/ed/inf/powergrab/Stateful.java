package uk.ac.ed.inf.powergrab;

import java.util.*;

class Stateful extends Drone {
    // 'trace' variable keeps track of the drone's last 5 positions
    private Queue<Position> trace;
    // 'target' variable keeps track of the drone's desired station to be reached next
    private Station target;

    Stateful(Position position, long seed, Map map) {
        movesLeft = 250;
        trace = new LinkedList<>();
        trace.add(position);
        this.power = 250;
        this.coins = 0;
        this.position = position;
        this.randomDirGen = new Random(seed);
        this.target = closestPositiveUncollectedStation(position, map);
    }

    @Override
    void move(Direction dir) {
        super.move(dir);
        if (trace.size() == 5)
            trace.poll();
        trace.add(position);
    }

    private Station randomStation(Map map) {
        int stationsNo = map.getAllStations().size();
        return map.getAllStations().get(randomDirGen.nextInt(stationsNo));
    }

    // if drone has been at the same position 3 times in its last 5 moves, then it is stuck
    private boolean isStuck() {
        HashMap<String, Integer> occurrences = new HashMap<>();
        trace.forEach(pos ->
                occurrences.put(pos.toString(), occurrences.getOrDefault(pos.toString(), 0) + 1));

        for (int occurrence : occurrences.values())
            if (occurrence == 3)
                // 3 occurrences of the same position have been found in the last 5 moves
                return true;

        return false;
    }

    private Station closestPositiveUncollectedStation(Position currentPos, Map map) {
        return Collections.min(map.getPositiveUncollectedStations(),
                Comparator.comparingDouble(a -> map.distanceBetweenPoints(a.getPosition(), currentPos)));
    }

    private Position assessTargetPosition(Position currentPos, Map map) {
        if (map.getPositiveUncollectedStations().isEmpty())  //with no positive uncollected stations left, game is
            // over and target does not matter
            return map.getAllStations().get(0).getPosition();

        if (isStuck()) // drone is stuck so choose a random station as new target
            target = randomStation(map);

        if (map.getCollectedStations().contains(target)) // target has just been collected, select a new one
            return closestPositiveUncollectedStation(currentPos, map).getPosition();
        else // keep target
            return target.getPosition();
    }

    private Direction safeDirectionClosestToTarget(EnumMap<Direction, Double> safeDirectionsStateful) {
        double distance = Integer.MAX_VALUE;
        Direction closestDirection = null;
        // go through all safe directions and choose the closest one based on distance to target (stored in EnumMap)
        for (java.util.Map.Entry<Direction, Double> safeDir : safeDirectionsStateful.entrySet())
            if (safeDir.getValue() < distance) {
                distance = safeDir.getValue();
                closestDirection = safeDir.getKey();
            }
        return closestDirection;
    }

    Direction chooseMoveDirection(Position currentPos, Map map) {
        Direction choice = null;
        double maxGain = Integer.MIN_VALUE;
        Station chosenStation = null;
        EnumMap<Direction, Double> safeDirectionsStateful = new EnumMap<>(Direction.class);
        Position targetPos = assessTargetPosition(currentPos, map);

        for (Direction dir : Direction.values())
            if (currentPos.nextPosition(dir).inPlayArea()) {
                Position nextPosition = currentPos.nextPosition(dir);

                // calculate utility sum of nearest station to future position
                double directionUtilityGain = 0;
                Station nearestStation = null;
                double distanceToNearestStation = Integer.MAX_VALUE;

                for (Station station : map.getAllStations()) {
                    double distToStation = map.distanceBetweenPoints(nextPosition, station.getPosition());
                    if (map.arePointsInRange(nextPosition, station.getPosition()) &&
                            distToStation < distanceToNearestStation) {
                        nearestStation = station;
                        distanceToNearestStation = distToStation;
                        directionUtilityGain = map.stationUtility(station);
                    }
                }

                // any direction where we gain coins is a safe direction
                if (directionUtilityGain >= 0)
                    safeDirectionsStateful.put(dir, map.distanceBetweenPoints(nextPosition, targetPos));

                // keep track of maximum coin profit
                if (directionUtilityGain > maxGain) {
                    maxGain = directionUtilityGain;
                    choice = dir;
                    chosenStation = nearestStation;
                }
            }

        // all directions are neutral
        if (maxGain == 0)
            return safeDirectionClosestToTarget(safeDirectionsStateful);

        assert chosenStation != null;
        chargeFromStation(chosenStation, map);
        return choice;
    }
}
