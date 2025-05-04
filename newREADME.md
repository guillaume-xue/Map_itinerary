# Projet de Génie Logiciel Avancé

Version 2025

Planificateur d'itinéraire urbain.  

## Description

Logiciel de planification d'itinéraire fait en Java. Permet de lire un réseau de transport selon un format prédéfini et de calculer le trajet le plus court d'un point A ( adresse ou coordonnées ) à un point B, en temps comme en distance.

## Lancement du programme

### Dependances & pré-requis

* Java JDK 17. 
* Une connexion internet pour afficher la carte et faire les recherches par adresses.  

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
mvn verify
```

### Lancement du programme et utilisatation

* Suggestion d'usage pour démarrage rapide:

```
make run
```
Permet de lancer la configuration (création des fichiers de données) et lancer l'interface graphique.  

Pour une utilisation plus en détails le Jar créé après la compilation possède plusieurs options de lancements:

```
java -jar target/project-2025.1.1.0-PROTOTYPE.jar [options...]
```
:warning: Attention à utiliser la dernière version en date du jar, le plus simple étant de prendre celle utilisée dans le Makefile.

* Options de lancement
    * `--info` -> affiche dans la console les informations de l'application.
    * `--help` -> affiche dans la console le manuel d'utilisation des options de lancement.
    * `--create-files`
    * `--parse`
    * `--gui`

## Aide

Pour des explications plus détaillées de l'utilisation du programme, se référer au [wiki](https://moule.informatique.univ-paris-diderot.fr/delarmin/gla-groupe-3-lundi/-/wikis/home). 

## Auteurs

/contacts.md


## Historique des versions

* 1.1.0
    * Various bug fixes and optimizations
    * See [commit change]() or See [release history]()
* 1.0.0
    * Prototype

