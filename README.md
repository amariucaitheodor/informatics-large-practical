## 2019 Informatics Large Practical

- [ilp-results](https://github.com/theodor1289/informatics-large-practical/tree/master/ilp-results) contains the .geojson files that show the flight path of the stateless and stateful drone on 12 different days of the year and .txt files that show movement and drone state (coins, power) at each step
- [powergrab](https://github.com/theodor1289/informatics-large-practical/tree/master/powergrab) contains the relevant source files which start the drone's movement
- [Report.pdf](https://github.com/theodor1289/informatics-large-practical/blob/master/report/Report.pdf) contains the class documentation and the detailed strategy implementation report
- [report-stateful.xlsx](https://github.com/theodor1289/informatics-large-practical/blob/master/evaluator/report-stateful.xlsx) contains the detailed coin collection stats of the stateful drone across over 700 maps
- [report-stateless.xlsx](https://github.com/theodor1289/informatics-large-practical/blob/master/evaluator/report-stateless.xlsx) contains the detailed coin collection stats of the stateless drone across over 700 maps 

#### To build the application:
cd powergrab
mvn package

#### To run the evaluator:
cd evaluator
python evaluator.py

#### To run the stateless drone e.g. on map 01/01/2019 starting from position 55.944425 (latitude) -3.188396 (longitude):
cd target
java -jar powergrab-0.0.1-SNAPSHOT.jar 01 01 2019 55.944425 -3.188396 5678 stateless

#### Demos:
- Rendering of a stateful drone's flight, seed 5679, map 12/08/2019
![alt text][stateful-img]

- Rendering of a stateless drone's flight, seed 5679, map 12/08/2019
![alt text][stateless-img]

[stateful-img]: https://github.com/theodor1289/informatics-large-practical/blob/master/report/screenshot2.png "Stateful flight"
[stateless-img]: https://github.com/theodor1289/informatics-large-practical/blob/master/report/screenshot1.png "Stateless flight"
