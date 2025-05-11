# Projet de Génie Logiciel Avancé

Version 2025

Planificateur d'itinéraire urbain.  

## Description

Logiciel de planification d'itinéraire. Permet de lire un réseau de transport selon un format prédéfini et de calculer le trajet le plus court d'un point A (adresse ou coordonnées) à un point B (adresse ou coordonnées), en temps ou distance.

## Lancement du programme

### Dependances & pré-requis

* Java JDK 17 minimum.
* Une connexion internet pour afficher la carte et faire les recherches par adresses. En l'absence de connexion, il est necessaire de fournir des coordonnées GPS valides situées en Ile de France pour les addresses de départ et d'arrivée. 

### Installation

Pour installer le projet deux choix possibles:  

* Cloner le dossier:
```
git clone https://moule.informatique.univ-paris-diderot.fr/delarmin/gla-groupe-3-lundi.git
```

Ou:  

* Télécharger la dernière version du code source depuis les [releases](https://moule.informatique.univ-paris-diderot.fr/delarmin/gla-groupe-3-lundi/-/releases)

### Compilation

Ce projet utilise [maven](https://maven.apache.org/) de Apache pour la gestion de construction.

Une fois dans la racine du projet:  

* Pour compiler:
```
make all
```
Ou:  
```
mvn clean verify
```

### Utilisation

* Suggestion d'usage pour démarrage rapide (nécessite d'avoir compilé):

```
make run
```
Permet de lancer la configuration (création des fichiers de données) et l'interface graphique en utilisant les fichiers csv données par l'API RATP du réseau Ile de France.

* Pour une utilisation plus en détails, le Jar créé après la compilation peut être utilisé avec plusieurs options de lancements:

Pour utiliser le jar:  
```
java -jar target/project-2025.1.1.0-PROTOTYPE.jar [options...]
```

:warning: Attention à utiliser la dernière version en date du Jar, le plus simple étant de prendre celle utilisée dans le Makefile :
target/project-2025.1.1.0-PROTOTYPE.jar

* Options de lancement
    * `--info` -> affiche dans la console la version et les auteurs de l'application.
    * `--help` -> affiche dans la console le manuel d'utilisation des options de lancement.
    * `--create-files` -> créer les 2 fichiers (.csv) de données nécessaires à la lecture du réseau et le dossier contenant les fichiers d'horaires (.csv)
    * `--parse` -> parse lles 2 fichiers (.csv) de données nécessaires à la lecture du réseau et le dossier contenant les fichiers d'horaires (.csv) et crée le modèle du programme. (Fonction de DEBUG)
    * `--gui` -> lance l'interface graphique du programme en gérant automatiquement les options précédentes. 

### Tests

Deux tests optionnels peuvent être lancés indépendamment du reste:

* Un test de déterminisme (assez long à éxécuter) pour la génération des fichiers:
```
mvn test -D runDeterminismTest=true
```

* Un test plutôt destiné à des fins de debug et d'optimisation de l'algorithme:
```
mvn test -D runEfficiencyTest=true
```
Génère un log avec une suite de résultats de recherche de trajets ainsi que leur temps d'éxécution.

## Aide

Pour des explications plus détaillées de l'utilisation du programme, se référer au [wiki](https://moule.informatique.univ-paris-diderot.fr/delarmin/gla-groupe-3-lundi/-/wikis/home). 

## Auteurs

[contacts](contacts.md)


## Historique des versions

* 1.1.0
    * Ajout de la gestion des horaires
    * Optimisation des résultats
    * Amélioration de l'UI

* 1.0.0
    * Prototype

