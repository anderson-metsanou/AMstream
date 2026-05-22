# Mini-Rapport — AMstream

**Cours :** INF 392 — Programmation Mobile (Édition 2025/2026)
**Enseignant :** Prof. Bomgni Alain — Université de Dschang
**Étudiant :** Anderson Metsanou
**Date de rendu :** Mai 2026

---

## 1. Justification de la thématique

La thématique retenue pour AMstream est celle des **films et séries de Fantasy / Merveilleux** (Le Seigneur des Anneaux, Harry Potter, Game of Thrones, The Witcher, Chroniques de Narnia). Ce choix se justifie par plusieurs raisons :

- **Richesse visuelle exploitable :** Le genre fantasy offre un univers graphique fort (forêts elfiques, runes, palettes de couleurs sombres et dorées) qui permet de construire une identité visuelle immédiatement reconnaissable et distincte d'une application de streaming générique.
- **Catalogue TMDB abondant :** Le genre Fantasy (ID 14 dans l'API TMDB) dispose d'un catalogue riche de centaines de titres populaires, ce qui garantit des résultats de recherche pertinents et variés pour les démonstrations.
- **Cohérence de bout en bout :** L'ensemble de l'application — du Splash Screen jusqu'aux paramètres — est habillé selon cette thématique : palette de couleurs mystique (violet profond `#2E1A47`, or elfique `#D4AF37`, fond ardoise `#0F0C1B`), police d'écriture Cinzel pour les titres, vocabulaire thématique dans les messages utilisateur (« Portail magique », « Aventurier », factions Elfes/Nains/Mordor).

---

## 2. Description des écrans

L'application comporte **5 écrans** répartis dans 5 activités distinctes, reliées par des `Intent` explicites.

### Écran 1 — Splash Screen (`SplashActivity`)

Écran d'accueil thématique affiché au lancement de l'application. Il présente le logo AMstream centré sur un fond mystique sombre, accompagné d'une barre de progression animée simulant un chargement. Au bout de 3 secondes, l'application redirige automatiquement vers l'écran principal (Ma Collection). En cas de mise en arrière-plan durant cette phase, la redirection planifiée est annulée dans `onPause()` pour éviter toute transition involontaire.

### Écran 2 — Ma Collection (`CollectionActivity`)

Écran principal de l'application. Il affiche la collection personnelle de l'utilisateur sous forme d'une liste verticale de `CardView` dans un `RecyclerView` (gestionnaire de disposition linéaire). Chaque carte présente l'affiche du film (chargée via Glide), le titre, la note personnelle (étoiles) et le statut de visionnage (pastille colorée). Un `FloatingActionButton` en forme de « squircle » permet d'accéder à l'écran de recherche TMDB. Le menu contextuel (accessible via les trois points de chaque carte) propose trois actions : modifier le statut, modifier la note et supprimer le film (avec confirmation via `AlertDialog`). La toolbar supérieure contient un menu d'options donnant accès aux paramètres.

### Écran 3 — Recherche TMDB (`SearchActivity`)

Écran de recherche connecté à l'API TMDB. Une `SearchView` intégrée dans la `Toolbar` permet de saisir un mot-clé. Les résultats sont affichés dans un `RecyclerView` configuré en grille de 2 colonnes (`GridLayoutManager`). Par défaut, à l'ouverture de l'écran, l'application charge automatiquement les films populaires du genre Fantasy via l'endpoint `/discover/movie?with_genres=14`. Un système de filtres par pastilles (`ChipGroup`) est disponible pour affiner les résultats. Un clic sur une affiche ouvre la fiche détaillée correspondante (Écran 4).

### Écran 4 — Fiche Détail (`DetailActivity`)

Écran de consultation et d'édition d'un film. La partie supérieure affiche l'affiche au format portrait accompagnée du titre en police Cinzel dorée. Un `TableLayout` vitré transparent présente les métadonnées TMDB récupérées de manière asynchrone : langue originale, popularité, durée et genre. Le synopsis est affiché sous une barre d'accentuation violette. La partie inférieure constitue le formulaire de suivi personnel : un `Spinner` pour le statut de visionnage (« À voir », « En cours », « Vu »), une `RatingBar` sur 5 étoiles pour la note personnelle, un `EditText` pour rédiger un avis textuel, et un `Button` déclenchant un `DatePickerDialog` pour sélectionner la date de visionnage. Le bouton « Ajouter à ma collection » ou « Mettre à jour » enregistre ces données dans le fichier interne local.

### Écran 5 — Paramètres (`SettingsActivity`)

Écran de configuration utilisateur exploitant les `SharedPreferences`. Il contient un `EditText` pour le nom d'utilisateur, un `SwitchCompat` pour activer le thème sombre, une `CheckBox` pour les effets visuels, un `RadioGroup` de trois `RadioButton` pour choisir une faction préférée (Elfes, Nains, Mordor) et un champ dédié à la clé API TMDB. Toutes les valeurs sont pré-chargées au lancement et sauvegardées via `SharedPreferences.Editor.apply()` lors de la validation.

---

## 3. Explications techniques concept par concept

### 3.1. Navigation multi-activité et transmission de données

L'application est structurée autour de 5 activités distinctes. La navigation entre elles s'effectue par des `Intent` explicites. La transmission du film sélectionné entre les écrans Collection/Recherche et Détail est assurée par la sérialisation de l'objet `Movie` (qui implémente `Serializable`) via `intent.putExtra("selected_movie", movie)`. Le retour à l'écran précédent s'effectue par appel à `finish()`, ce qui détruit l'activité fille et restaure l'activité parente dans la pile.

### 3.2. Appels réseau asynchrones (TMDB)

Les requêtes HTTP vers l'API TMDB sont déléguées à un `ExecutorService` (exécuteur de thread unique) dans la classe utilitaire `TMDBService`. Ce choix remplace `AsyncTask`, déprécié depuis l'API 30. Le flux est le suivant : (1) vérification de la connectivité réseau via `ConnectivityManager` et `NetworkCapabilities` ; (2) connexion HTTP GET via `HttpURLConnection` avec un timeout de 8 secondes ; (3) lecture du flux de réponse et parsing manuel des objets `JSONObject` et `JSONArray` de la bibliothèque standard `org.json` ; (4) retour des résultats sur le thread principal via un `Handler(Looper.getMainLooper())` pour mise à jour du `RecyclerView`.

### 3.3. Persistance hybride des données

Deux mécanismes de persistance coexistent conformément aux exigences du TP :

- **`SharedPreferences`** : Stocke les paramètres utilisateur (nom, thème, faction, effets visuels) et la clé API TMDB. Lecture et écriture via `getSharedPreferences()` et `SharedPreferences.Editor`.
- **Fichiers internes (Internal Storage)** : La collection personnelle de films est sérialisée en tableau JSON (`JSONArray`) et écrite dans le fichier `collection.json` du stockage privé de l'application via `Context.openFileOutput()`. La lecture s'effectue via `Context.openFileInput()`. Le singleton `CollectionManager` centralise toutes les opérations CRUD (lecture, ajout/mise à jour, suppression) et écrase le fichier intégralement à chaque modification pour garantir la cohérence transactionnelle.

### 3.4. Cycle de vie Android

Chaque activité implémente et documente explicitement les méthodes du cycle de vie : `onCreate()`, `onResume()`, `onPause()`, `onSaveInstanceState()` et `onRestoreInstanceState()`. Par exemple, dans `DetailActivity`, les saisies du formulaire (position du Spinner, valeur de la RatingBar, texte de l'avis, date sélectionnée) sont sauvegardées dans le `Bundle` lors de `onSaveInstanceState()` et restaurées dans `onRestoreInstanceState()`, ce qui empêche la perte de données en cas de rotation de l'écran. Dans `CollectionActivity`, la position de défilement du `RecyclerView` est sauvegardée et restaurée de la même manière.

### 3.5. Gestion de l'absence de réseau

Avant chaque appel API, la méthode `isNetworkAvailable()` de `TMDBService` interroge le `ConnectivityManager` du système pour vérifier la présence d'une connexion active (Wi-Fi, cellulaire ou Ethernet). En l'absence de réseau, l'appel est immédiatement annulé et un message d'erreur thématique est affiché à l'utilisateur via un `Toast`. La collection locale reste intégralement accessible en lecture car elle ne dépend d'aucun accès réseau.

### 3.6. Sécurité de la clé API

La clé API TMDB n'est jamais codée en dur dans le code source. Deux mécanismes complémentaires sont proposés : (1) injection à la compilation depuis le fichier `local.properties` via la directive `buildConfigField` de Gradle, générant la constante `BuildConfig.TMDB_API_KEY` ; (2) saisie directe par l'utilisateur dans l'écran Paramètres, stockée dans les `SharedPreferences`. La classe `TMDBService` consulte en priorité les `SharedPreferences`, puis se rabat sur `BuildConfig` si le champ est vide.

---

## 4. Difficultés rencontrées et solutions apportées

### 4.1. Remplacement d'AsyncTask par Executors

`AsyncTask` étant officiellement déprécié depuis Android 11 (API 30), il a fallu implémenter un mécanisme asynchrone alternatif. La solution adoptée combine un `ExecutorService` (pour l'exécution en arrière-plan) et un `Handler` attaché au `Looper` principal (pour le retour sur le thread UI). Cette approche est plus explicite, plus fiable et plus maintenable que l'ancien `AsyncTask`.

### 4.2. Persistance de la collection sans SQLite/Room

Le sujet du TP impose l'utilisation exclusive de fichiers internes pour le stockage de la collection. L'absence de base de données relationnelle oblige à réécrire l'intégralité du fichier JSON à chaque opération de modification. Pour limiter les risques de corruption, la classe `CollectionManager` est implémentée en tant que Singleton, centralisant toutes les opérations d'écriture à travers un point d'accès unique et garantissant la cohérence des données en mémoire et sur le disque.

### 4.3. Gestion des erreurs réseau multiples

L'appel à l'API TMDB peut échouer pour plusieurs raisons distinctes : absence de réseau, clé API manquante ou invalide, timeout de connexion, erreur de parsing JSON. Chaque cas est identifié et traité séparément dans la méthode `handleNetworkError()` de `SearchActivity`, ce qui permet d'afficher un message d'erreur ciblé et compréhensible à l'utilisateur au lieu d'un message générique.

### 4.4. Maintien de la cohérence de la navigation

La navigation entre les 5 activités peut engendrer des désynchronisations visuelles, notamment au niveau de la barre de navigation basse. La solution consiste à forcer la sélection de l'onglet actif dans la méthode `onResume()` de chaque activité principale, garantissant que l'indicateur visuel correspond toujours à l'écran réellement affiché après un retour depuis une activité fille.

### 4.5. Chargement d'images et performance

Le chargement des affiches de films depuis les serveurs TMDB représente un volume important de données réseau et de mémoire. L'intégration de la bibliothèque Glide résout ce problème grâce à son système de double cache (mémoire RAM et disque), son annulation automatique des requêtes liées au cycle de vie de l'activité, et sa gestion native des placeholders et des images d'erreur.

---

*Fin du mini-rapport — AMstream — INF 392 — Université de Dschang*
