# TODO:
move methods, vars, enums to right classes: dronetype to drone, maybe choosemovedir too
write report

### Build app:
cd powergrab
mvn package

### Run build:
cd target
java -jar powergrab-0.0.1-SNAPSHOT.jar 01 01 2019 55.944425 -3.188396 5678 stateless

### TO TEST CODE QUALITY:
Start SonarQube instance
cd powergrab
mvn clean verify sonar:sonar

## isDroneStuck in clasa Drone + ALTE METODE IN CLASE SEPARATE, Game e prea mare
=======
STARTING FROM MAIN REPO DIRECTORY:
cd powergrab
mvn package
cd target
java -jar powergrab-0.0.1-SNAPSHOT.jar 01 01 2019 55.944425 -3.188396 5678 stateless


TO TEST CODE QUALITY:
cd powergrab
mvn clean verify sonar:sonar
