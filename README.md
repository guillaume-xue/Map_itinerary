# Projet de GLA

Version 2024

## Description

Ceci est l'archetype de projet de Génie Logiciel Avancé (GLA).

Il s'agit d'un projet Java. Ce dépôt définit un système de build et une application simple. Il est nécéssaire de consulter le fichier [CONTRIBUTING.md](CONTRIBUTING.md) pour utiliser ce dépôt.

## Lancement du programme

Ce projet utilise [maven](https://maven.apache.org/) de Apache pour la gestion de construction.

Afin de compiler et lancer les tests, éxecutez simplement

```
mvn verify
```

Dans sa version initiale, le programme fournit est un simple code qui se lance en terminal ou en application graphique.

Une fois le programme compilé, vous trouverez un jar executable dans le dossier target. Au nom de jar près (version changeante), vous pourrez l'exécuter avec:

```
java -jar target/project-2024.1.0.0-SNAPSHOT-jar-with-dependencies.jar
```

L'option de lancement `--info` causera l'affichage dans la console d'informations de l'application.

L'option de lancement `--gui` causera l'ouverture d'une fenêtre affichant le logo de l'Université de Paris.

L'option de lancement `--parse` permet d'analyser un fichier CSV ou une URL fournie en paramètre grâce au parser:  
`Usage: --parse source_file.csv` 
`Exemple: --parse output.csv`

L'option de lancement `--createfiles` permet de créer les 3 types de fichiers client pour créer les objets de notre graphe:  
`Exemple d'usage: --createfiles mapData.csv junctionsData.csv Schedule`  
où on pourra retrouver les fichiers horaires dans le répertoire Schedule, les données de cartes dans le fichier mapData.csv et les données de bifurcations dans junctionsData.csv

Pour le test du déterminisme sur la génération des fichiers :
utiliser la commande `mvn test -DrunDeterminismTest=true` car le test est 
volontairement skipped avec les autres commandes

Suggestion d'usage pour démonstration du gui :  
```
mvn clean package
mvn verify  
java -jar target/project-2024.1.0.0-SNAPSHOT-jar-with-dependencies.jar --createfiles mapData.csv junctionsData.csv Schedule/
java -jar target/project-2024.1.0.0-SNAPSHOT-jar-with-dependencies.jar --gui 
```

Pour simuler un trajet depuis Olympiades jusqu'à Saint Ouen, rentrer les coordonnées suivantes dans les champs From et To:  
From: 48.826944, 2.367034  
To: 48.905170, 2.322756  




