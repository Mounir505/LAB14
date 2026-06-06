Lab 14 : Sauvegarde des données (SharedPreferences et Fichiers)
===============================================================

**Version :** 1.0

**SDK Minimum :** API 24 (Android 7.0 Nougat)

**Dépendance clé :** `androidx.security:security-crypto`

* * *

1\. Aperçu et Objectifs du Projet
---------------------------------

Ce projet est une application Android de démonstration fonctionnant 100% hors-ligne. Son objectif principal est d'illustrer les différentes méthodes de persistance locale disponibles sous Android, tout en imposant des standards de sécurité rigoureux. L'application ne se contente pas de sauvegarder des données ; elle les ségrègue selon leur niveau de sensibilité.

### Objectifs d'apprentissage atteints :

*   **Manipulation des SharedPreferences :** Différenciation entre l'écriture asynchrone (`apply()`) et synchrone (`commit()`).
*   **Chiffrement des secrets :** Utilisation de `EncryptedSharedPreferences` couplé à `MasterKey` (Keystore) pour protéger les tokens.
*   **Gestion des Fichiers Internes :** Écriture et lecture de fichiers textes purs (UTF-8) et sérialisation/désérialisation d'objets en JSON.
*   **Utilisation du Cache :** Stockage de données temporaires et création d'une routine de purge.
*   **Stockage Externe Spécifique :** Exportation de fichiers dans le répertoire externe propre à l'application sans nécessiter de permissions globales.

* * *

2\. Architecture Logique (Packages)
-----------------------------------

Le code source a été découpé de manière modulaire pour respecter le principe de responsabilité unique (Single Responsibility Principle). Voici la structure de l'application :

*   `com.example...model` : Contient les objets métiers (ex: la classe immuable `Student`).
*   `com.example...prefs` : Isole la logique de sauvegarde clé/valeur (données claires et données chiffrées).
*   `com.example...files` : Gère les entrées/sorties (I/O) sur le stockage interne de l'application (fichiers inaccessibles aux autres applications).
*   `com.example...cache` : Gère le dossier des fichiers temporaires (susceptibles d'être supprimés par le système si l'espace manque).
*   `com.example...external` : Gère l'écriture sur le volume externe sécurisé de l'application.
*   `com.example...ui` : Contient l'interface utilisateur (`MainActivity`) qui orchestre les interactions.

* * *

3\. Explication Détaillée des Composants (Mode Verbeux)
-------------------------------------------------------

### 3.1. Préférences Standard (AppPrefs)

Utilisées pour les données non sensibles (nom, langue, préférences d'affichage). Le mode `Context.MODE_PRIVATE` est strictement imposé pour empêcher la lecture par d'autres applications. La méthode `apply()` est privilégiée pour ne pas bloquer le thread principal (UI thread) lors de l'écriture sur le disque.

### 3.2. Préférences Sécurisées (SecurePrefs)

Le stockage d'un token d'API ou d'un mot de passe en clair dans les SharedPreferences est une faille de sécurité majeure (facilement lisible sur un téléphone rooté). Ce module utilise `MasterKey.Builder` avec l'algorithme _AES256\_GCM_. Les clés de préférences sont chiffrées en _AES256\_SIV_ et les valeurs en _AES256\_GCM_. Les données sont ainsi rendues illisibles dans le fichier XML généré.

### 3.3. Fichiers Internes (InternalTextStore & StudentsJsonStore)

Ce stockage est utilisé pour les données structurelles plus lourdes que des paires clé/valeur. Les flux `FileOutputStream` et `FileInputStream` manipulent les données. L'encodage est forcé en **UTF-8** pour garantir la compatibilité des caractères spéciaux. La bibliothèque `org.json` native d'Android est utilisée pour transformer une liste d'objets Java en une chaîne de caractères JSON, et inversement.

### 3.4. Le Dossier Cache (CacheStore)

Le `getCacheDir()` retourne un chemin vers un dossier spécifique. La particularité de ce dossier est que le système d'exploitation Android peut le vider de lui-même si le téléphone manque d'espace de stockage. Il est donc exclusivement utilisé pour des données qui peuvent être recalculées ou téléchargées à nouveau (ici, un résumé de la dernière interface).

* * *

4\. Checklist de Sécurité Validée
---------------------------------

Ce projet respecte les règles de l'art en matière de sécurité Android de base :

1.  **Zéro fuite dans le Logcat :** Aucune donnée sensible (comme le token) n'est envoyée dans les logs de console. Seule la longueur du token (ou un booléen indiquant sa présence) est affichée.
2.  **Chiffrement matériel :** Les clés maîtresses sont gérées par le Keystore d'Android, rendant l'extraction extrêmement difficile.
3.  **Masquage de l'interface :** L'attribut XML `android:inputType="textPassword"` empêche la lecture par-dessus l'épaule et désactive le dictionnaire du clavier.
4.  **Sanitisation des données :** Le bouton de nettoyage offre une méthode `clearAll()` qui détruit activement les données du disque plutôt que de simplement masquer l'interface.
5.  **Fichiers privés :** Aucun fichier n'est écrit sur la carte SD publique (MediaStore/SAF) sans raison, empêchant le vol de données par d'autres applications.

* * *

5\. Guide de Déploiement et de Test
-----------------------------------

Pour vérifier le bon fonctionnement technique de l'application, suivez cette procédure :

### Tests de l'Interface Utilisateur (UI)

*   Remplissez les champs, sauvegardez, puis fermez l'application ("Kill" de l'app).
*   Rouvrez l'application et chargez les données. Les champs doivent retrouver leur état d'origine.
*   Testez le bouton "Nettoyage Complet" et vérifiez que les données ne peuvent plus être chargées.

### Inspection du Système de Fichiers (Device File Explorer)

Dans Android Studio, ouvrez le _Device File Explorer_ et naviguez vers `/data/data/com.example.securestoragejava/` :

*   **Dossier /shared\_prefs/ :** Vérifiez la présence de `app_prefs.xml` (lisible) et `secure_prefs.xml` (illisible/chiffré).
*   **Dossier /files/ :** Ouvrez `students.json` pour valider le formatage du tableau d'étudiants.
*   **Dossier /cache/ :** Validez la présence des fichiers temporaires avant leur purge.

* * *
* * *

6. Démonstration Vidéo
----------------------

Une démonstration complète de l'application a été réalisée afin de valider l'ensemble des fonctionnalités de persistance locale et de sécurité présentées dans ce laboratoire.

### Scénario de démonstration

La vidéo couvre les étapes suivantes :

1. **Sauvegarde des préférences utilisateur**
   - Saisie du nom et de la langue.
   - Enregistrement dans les SharedPreferences.
   - Fermeture puis réouverture de l'application.
   - Chargement et restauration automatique des données.

2. **Stockage sécurisé d'un token**
   - Saisie d'un token sensible.
   - Enregistrement dans les EncryptedSharedPreferences.
   - Vérification du chargement sécurisé après redémarrage de l'application.

3. **Gestion des fichiers internes**
   - Création d'une liste d'étudiants.
   - Sauvegarde au format JSON.
   - Lecture et affichage des données enregistrées.

4. **Utilisation du cache**
   - Génération d'un fichier temporaire.
   - Vérification de sa présence.
   - Suppression du cache via l'interface utilisateur.

5. **Exportation vers le stockage externe spécifique**
   - Création d'un fichier d'export.
   - Vérification de son emplacement dans le répertoire externe de l'application.

6. **Nettoyage complet des données**
   - Suppression des préférences standard.
   - Suppression des préférences chiffrées.
   - Suppression des fichiers internes et du cache.
   - Vérification de l'absence de données après rechargement.

### Résultat attendu

À l'issue de la démonstration, l'utilisateur peut constater que :

- Les données non sensibles sont correctement persistées via SharedPreferences.
- Les informations sensibles sont protégées grâce au chiffrement AES fourni par EncryptedSharedPreferences.
- Les fichiers JSON sont correctement sauvegardés et restaurés.
- Le cache peut être créé et purgé sans impact sur les données permanentes.
- Les exports externes sont générés dans l'espace privé de l'application.
- La fonctionnalité de nettoyage détruit effectivement toutes les données enregistrées.

### Lien de la vidéo



https://github.com/user-attachments/assets/b64b1e20-c739-4817-a812-f0d68810f489


* * *
