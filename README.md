# PeerToPeerProject

## Description

Ce projet est une preuve de concept (PoC) développée en Java pour démontrer la parallélisation, la compression de données, et la gestion des déconnexions dans un téléchargeur peer-to-peer. L'objectif est de permettre le partage de fichiers entre machines sans serveur centralisé, tout en simulant des conditions de réseau réalistes comme des connexions lentes.

## Fonctionnalités

- **Téléchargement parallèle** : Les fichiers sont divisés en segments, téléchargés simultanément pour améliorer les performances.
- **Compression des données** : Réduit la taille des fichiers transférés pour optimiser les échanges.
- **Gestion des déconnexions** : Si un peer se déconnecte pendant un téléchargement, le système redistribue les segments manquants aux autres peers actifs.

## Structure du projet

- `Client/` : Code source pour les machines clients qui effectuent les téléchargements.
- `Downloader/` : Composants responsables de la gestion des téléchargements et des segments.
- `Diary/` : Serveur centralisé jouant le rôle de coordinateur et gestionnaire des peers.
- `downloads/` : Répertoire pour stocker les fichiers téléchargés.
- `Rapport_Intergiciel.pdf` : Rapport détaillant le fonctionnement et les objectifs du projet.

## Installation

1. **Cloner le dépôt** :

   ```bash
   git clone https://github.com/AngelLagr/PeerToPeerProject.git
   ```

2. **Configurer l'environnement Java** :
   - Assurez-vous d'avoir une version récente du JDK installée.
   - Configurez correctement votre IDE ou environnement de compilation.

3. **Compiler les sources** :

   ```bash
   javac Diary/DiaryServer.java Client/Client.java
   ```

## Utilisation

### Lancer le serveur (Diary)

Le serveur Diary coordonne les interactions entre les clients. Lancez-le sur une machine avec la commande suivante :

```bash
java Diary.DiaryServer
```

### Lancer les clients

Sur les machines clientes, exécutez la commande suivante pour démarrer le client peer-to-peer :

```bash
java Client.Client <identifiant> <IP_du_Diary> <delai>
```

- **`identifiant`** : Un identifiant unique pour chaque client.
- **`IP_du_Diary`** : L'adresse IP ou le nom d'hôte du serveur Diary.
- **`delai`** : Temps en millisecondes pour simuler une connexion lente. Ce délai est ajouté à chaque envoi de paquet.

### Exemple d'exécution

1. Lancez le serveur Diary :

   ```bash
   java Diary.DiaryServer
   ```

2. Lancez deux clients sur des machines différentes :

   ```bash
   java Client.Client client1 192.168.1.10 100
   java Client.Client client2 192.168.1.10 200
   ```

Dans cet exemple :
- `client1` communique avec un délai de 100ms par paquet.
- `client2` communique avec un délai de 200ms par paquet.

## Points techniques clés

1. **Parallélisation** : Chaque fichier est segmenté en parties indépendantes. Les segments sont téléchargés en parallèle depuis différents peers, optimisant ainsi les performances.
2. **Compression des données** : Avant l'envoi, les segments sont compressés pour minimiser la bande passante utilisée.
3. **Gestion des déconnexions** : Si un peer se déconnecte avant d'avoir fini de partager ses segments, les autres clients reprennent automatiquement les téléchargements manquants.

## Licence

Ce projet est sous licence MIT. Consultez le fichier `LICENSE` pour plus d'informations.
