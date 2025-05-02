all:
	mvn clean package
	mvn verify

soft:
	mvn verify

run:
	java -jar target/project-2025.1.1.0-PROTOTYPE.jar --gui