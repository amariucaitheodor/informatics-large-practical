package uk.ac.ed.inf.powergrab;

import com.mapbox.geojson.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App 
{
    public static void main( String[] args ) throws IOException
    {
    	String year = args[2];
    	String month = args[1];
    	String day = args[0];
        String mapStationsURL = "http://homepages.inf.ed.ac.uk/stg/powergrab/" + year + "/" + month + "/" + day + "/powergrabmap.geojson";
        Position initialDronePos = new Position(Double.parseDouble(args[3]), Double.parseDouble(args[4]));
        long seed = Long.parseLong(args[5]);
        String droneType = args[6];

        String mapSource = readFromURL(mapStationsURL);
        FeatureCollection mapStations = FeatureCollection.fromJson(mapSource);
        
    	new Game(seed, mapStations, initialDronePos, droneType, year, month, day).play();

		//generateResultFiles(false);
    }

    private static String readFromURL(String givenURL) throws IOException {
        try(BufferedReader in = new BufferedReader(new InputStreamReader(new URL(givenURL).openStream()))) {
            StringBuilder sb = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
            return sb.toString();
        }
    }
    
    private static double mapPerfectScore(FeatureCollection mapStations) {
    	double s=0;
		assert mapStations.features() != null;
		for(Feature station : mapStations.features()) {
    		double stationCoins = station.getNumberProperty("coins").doubleValue();
    		if(stationCoins>=0)
    			s+=stationCoins;
    	}
    	return s;
    }

    private static void generateResultFiles(boolean forSubmission) throws IOException {
		Position initialDronePos = new Position(55.944425, -3.188396);
    	double statelessErrorSum = 0;
    	double statefulErrorSum = 0;
    	int mapsTested = 0;

    	int yearEnd = forSubmission? 2019 : 2020;
    	for(int year = 2019; year<= yearEnd; year++) {
			String yearStr = String.valueOf(year);
			for(int month=1; month<=12; month++) {
				String monthStr = month<10? "0" + month : String.valueOf(month);

				int dayStart = forSubmission? month : 1;
				int dayEnd = forSubmission? month : 28; //TODO: deal with longer months at http://homepages.inf.ed.ac.uk/stg/powergrab/
				for(int day = dayStart; day<=dayEnd; day++) {
					String dayStr = day<10? "0" + day : String.valueOf(day);
					String mapStationsURL = "http://homepages.inf.ed.ac.uk/stg/powergrab/" + yearStr + "/" + monthStr + "/" + dayStr + "/powergrabmap.geojson";
					String mapSource = readFromURL(mapStationsURL);
					double perfectScore = mapPerfectScore(FeatureCollection.fromJson(mapSource));

					Game stateless = new Game(5678, FeatureCollection.fromJson(mapSource), initialDronePos, "stateless", yearStr, monthStr, dayStr);
					stateless.play();
					double statelessError = Math.abs(stateless.getGameScore() - perfectScore);
					statelessErrorSum += statelessError;

					Game stateful = new Game(5678, FeatureCollection.fromJson(mapSource), initialDronePos, "stateful", yearStr, monthStr, dayStr);
					stateful.play();
					double statefulError = Math.abs(stateful.getGameScore() - perfectScore);
					statefulErrorSum += statefulError;
					if(statefulError > 400)
						System.out.println("Major stateful error (" + statefulError + ") on " + yearStr + "/" + monthStr + "/" + dayStr);

					Path path = Paths.get(String.format("./%s-%s-%s-%s.txt", "stateful", dayStr, monthStr, yearStr));
					long lineCount = Files.lines(path).count();
					if(lineCount<250)
						System.out.println("Stateful drone ran out of power before making 250 moves.");

					path = Paths.get(String.format("./%s-%s-%s-%s.txt", "stateless", dayStr, monthStr, yearStr));
					lineCount = Files.lines(path).count();
					if(lineCount<250)
						System.out.println("Stateless drone ran out of power before making 250 moves.");

					mapsTested++;
				}
			}
		}
    	System.out.println("Maps tested: " + mapsTested);
		System.out.println(String.format("Mean error of stateless from perfect score on %s maps: ", forSubmission? "submission" : "all") + (statelessErrorSum/mapsTested));
		System.out.println(String.format("Mean error of stateful from perfect score on %s maps: ", forSubmission? "submission" : "all") + (statefulErrorSum/mapsTested));
	}
}
