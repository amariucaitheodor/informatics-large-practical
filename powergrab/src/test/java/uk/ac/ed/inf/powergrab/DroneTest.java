package uk.ac.ed.inf.powergrab;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Unit test for PowerGrab App, Drone class.  [JUnit 4 version]
 */
public class DroneTest {
	
    @Before
    public void init() {
    	Game.precomputeMovementShift();
    }
	
    @Test
	public void testDroneConstructor() {
        assertNotNull(Stateless.createInstance(new Position(55.944425, -3.188396), 5678, false));
        assertNotNull(Stateful.createInstance(new Position(55.944425, -3.188396), 5678, false));
	}

    @Test
    public void testOnlyOneDroneInstance() {
        Drone statefulDrone = Stateful.createInstance(new Position(55.944425, -3.188396), 5678, false);
        Drone statefulDrone1 = Stateful.createInstance(new Position(55.944425, -3.188396), 5678, false);
        assertEquals(statefulDrone1, statefulDrone);
        assertEquals(statefulDrone1, Stateful.createInstance(new Position(55.944425, -3.188396), 5678, false));
    }
    
    @Test
	public void testRunOutOfPower() {
		Random dirGenerator = new Random();
    	// Initialize drone with 50 power because this is a rare event that a drone runs out of power
    	Drone testDrone = Stateful.createInstance(new Position(55.944425, -3.188396), 5678, false);
    	while(testDrone.hasMoves() && testDrone.hasPower())
        {
            Direction dir = Direction.randomDirection(dirGenerator);
            while(!testDrone.getPosition().nextPosition(dir).inPlayArea()) {
                dir = Direction.randomDirection(dirGenerator);
            }
            testDrone.move(dir);
        }
		assertFalse(testDrone.hasPower());
		assertTrue(testDrone.hasMoves());
	}
}
