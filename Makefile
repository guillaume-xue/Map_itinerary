all:
	mvn clean package
	mvn verify

soft:
	mvn verify

run:
	java -jar target/project-1.2.0.jar --gui