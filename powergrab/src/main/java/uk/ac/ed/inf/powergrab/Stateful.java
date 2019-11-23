package uk.ac.ed.inf.powergrab;

import java.util.*;

class Stateful extends Drone {
    private Queue<Position> trace;

    static Drone createInstance(Position position, long seed, boolean submissionGeneration) {
        if (instance == null || submissionGeneration)
            instance = new Stateful(position, seed);
        return instance;
    }

    private Stateful(Position position, long seed){
        movesLeft = 250;
        trace = new LinkedList<>();
        trace.add(position);
        this.power = 250;
        this.coins = 0;
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

    private Station target;

    private Station chooseRandomTarget(Map map) {
        int stationsNo = map.getAllStations().size();
        return map.getAllStations().get(dirGenerator.nextInt(stationsNo));
    }

    private boolean isStuck() {
        HashMap<String, Integer> occurrences = new HashMap<>();
        trace.forEach(pos -> occurrences.put(pos.toString(), occurrences.getOrDefault(pos.toString(), 0) + 1));
        for(int occurrence : occurrences.values())
            if(occurrence==3)
                return true;
        return false;
    }

    private Station closestStation(Position currentPos, Map map) {
        // with no positive uncollected stations left, game is over and target does not matter
        // note: negative stations will never be collected
        if(map.getPositiveUncollectedStations().isEmpty())
            return chooseRandomTarget(map);

        return Collections.min(map.getPositiveUncollectedStations(), Comparator.comparingDouble(a -> map.distanceBetweenPoints(a.getPosition(), currentPos)));
    }

    private Position chooseTargetReturnPosition(Position currentPos, Map map) {
        if(isStuck())
            target = chooseRandomTarget(map);
        if(target==null)
            target = closestStation(currentPos, map);
        Position targetPos;
        if(map.getCollectedStations().contains(target))
            targetPos = closestStation(currentPos, map).getPosition();
        else
            targetPos = target.getPosition();
        return targetPos;
    }

    private Direction closestSafeDirection(EnumMap<Direction, Double> safeDirectionsStateful) {
        double distance = Integer.MAX_VALUE;
        Direction closestDirection = null;
        for(java.util.Map.Entry<Direction, Double> safeDir : safeDirectionsStateful.entrySet())
            if(safeDir.getValue()<distance) {
                distance = safeDir.getValue();
                closestDirection = safeDir.getKey();
            }
        return closestDirection;
    }

    Direction chooseMoveDirection(Position currentPos, Map map) {
        Direction choice = null;
        double maxGain = Integer.MIN_VALUE;
        Set<Station> chosenStations = new HashSet<>();

        EnumMap<Direction, Double> safeDirectionsStateful = new EnumMap<>(Direction.class);
        Position targetPos = chooseTargetReturnPosition(currentPos, map);

        for (Direction dir : Direction.values())
            if (currentPos.nextPosition(dir).inPlayArea()) {
                Position nextPosition = currentPos.nextPosition(dir);
                List<Station> nextStations = new ArrayList<>();
                double positionGain = computePositionGain(nextPosition, map, nextStations);

                if (positionGain >= 0)
                    safeDirectionsStateful.put(dir, map.distanceBetweenPoints(nextPosition, targetPos));

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
