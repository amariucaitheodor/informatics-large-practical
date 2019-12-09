# 2019 Informatics Large Practical
## University of Edinburgh

### Build app:
cd powergrab
mvn package

### Run Evaluator:
cd evaluator
python evaluator.py

### Run build:
cd target
java -jar powergrab-0.0.1-SNAPSHOT.jar 01 01 2019 55.944425 -3.188396 5678 stateless
