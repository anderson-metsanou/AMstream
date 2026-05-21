# Références et Justifications Techniques — AMstream

Ce document recense les sources d'information, les tutoriels et la documentation officielle utilisés pour le développement de l'application Android **AMstream**, ainsi que les justifications d'architecture et de choix de bibliothèques imposées ou suggérées dans le cadre du TP.

---

## 1. Documentation Officielle Android (Google)

*   **Cycle de vie des activités (Activity Lifecycle)**
    *   *Lien* : [https://developer.android.com/guide/components/activities/activity-lifecycle](https://developer.android.com/guide/components/activities/activity-lifecycle)
    *   *Usage dans le projet* : Implémentation systématique et documentée des méthodes `onCreate()`, `onStart()`, `onResume()`, `onPause()`, `onStop()`, `onDestroy()`, `onSaveInstanceState()` et `onRestoreInstanceState()` sur l'ensemble des 5 activités de l'application pour garantir la robustesse face à la rotation d'écran.
*   **RecyclerView et CardView**
    *   *Lien* : [https://developer.android.com/develop/ui/views/layout/recyclerview](https://developer.android.com/develop/ui/views/layout/recyclerview)
    *   *Usage dans le projet* : Configuration du gestionnaire de disposition linéaire (`LinearLayoutManager`) pour la collection et du gestionnaire de grille (`GridLayoutManager` à 2 colonnes) pour les résultats de recherche TMDB.
*   **Stockage de données locales (Fichiers Internes & SharedPreferences)**
    *   *Lien* : [https://developer.android.com/training/data-storage](https://developer.android.com/training/data-storage)
    *   *Usage dans le projet* : Persistance de la collection via `Context.openFileOutput` et `Context.openFileInput` en format JSON (`collection.json`) et persistance des paramètres utilisateur et clé API TMDB via `SharedPreferences`.
*   **Concurrence et Tâches en arrière-plan (Executors)**
    *   *Lien* : [https://developer.android.com/guide/background/asynchronous/java-executor](https://developer.android.com/guide/background/asynchronous/java-executor)
    *   *Usage dans le projet* : Utilisation de `Executors.newSingleThreadExecutor()` pour déporter les appels HTTP de l'API TMDB hors du thread d'interface utilisateur (UI Thread), et utilisation de `Handler(Looper.getMainLooper())` pour renvoyer les réponses JSON parsées sur le thread principal.

---

## 2. Bibliothèques Externes

*   **Glide (Chargement et Cache d'Images)**
    *   *Lien* : [https://github.com/bumptech/glide](https://github.com/bumptech/glide)
    *   *Justification technique* :
        *   Glide intègre automatiquement une double mise en cache (mémoire RAM et disque) très performante, évitant des requêtes répétitives sur le réseau.
        *   Glide s'associe idéalement au cycle de vie de l'activité ou du fragment Android appelant, annulant automatiquement les requêtes en cours de téléchargement si l'activité est détruite.
        *   Permet l'intégration de transformations visuelles natives directement lors de la mise en cache (ex : effet de coins arrondis `RoundedCorners` de 12dp pour correspondre au style Fantasy Premium de nos CardViews).

---

## 3. APIs et Données Externes

*   **The Movie Database (TMDB) API v3**
    *   *Lien* : [https://developer.themoviedb.org/docs](https://developer.themoviedb.org/docs)
    *   *Usage dans le projet* :
        *   Endpoint Recherche : `/search/movie` pour trouver des films à l'aide de mots-clés.
        *   Endpoint Découverte : `/discover/movie?with_genres=14` pour pré-charger des films de genre *Fantasy* par défaut au lancement de la recherche.
        *   Endpoint Détails : `/movie/{movie_id}` pour récupérer les caractéristiques complémentaires nécessaires à la Fiche Détail (langue originale, popularité).

---

## 4. Instructions de configuration de la clé API TMDB pour le correcteur

Pour que l'application puisse communiquer avec l'API TMDB lors des tests de correction, deux méthodes sécurisées ont été mises en œuvre (sans clé codée en dur) :

### Option A : Via le fichier `local.properties` (Recommandé pour la compilation)
1. Ouvrez le fichier `local.properties` situé à la racine du projet Android Studio.
2. Ajoutez la ligne suivante en remplaçant `VOTRE_CLE_API` par votre clé API TMDB v3 :
   ```properties
   TMDB_API_KEY=VOTRE_CLE_API
   ```
3. Compilez et lancez le projet. Gradle lira automatiquement la clé et la générera dans la classe d'exécution `BuildConfig.TMDB_API_KEY`.

### Option B : Via l'interface utilisateur de l'application (Recommandé lors de l'exécution sur émulateur)
1. Lancez l'application sur votre terminal ou émulateur.
2. Depuis l'écran de la Collection, appuyez sur l'option **Paramètres** (accessible via le menu des trois points verticaux en haut à droite).
3. Saisissez votre clé API TMDB v3 dans le champ dédié **Configuration API**.
4. Appuyez sur **Enregistrer les paramètres**. La clé sera immédiatement sauvegardée dans les `SharedPreferences` et sera opérationnelle pour toutes les recherches futures.
