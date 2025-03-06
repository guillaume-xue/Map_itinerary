.PHONY: all verify run

all: verify run

verify:
	mvn verify

gui:
	cd target && java -cp "project-2024.1.0.0-SNAPSHOT.jar:dependency/*" fr.u_paris.gla.project.App --gui
