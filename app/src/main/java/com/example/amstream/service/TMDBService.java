package com.example.amstream.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.amstream.BuildConfig;
import com.example.amstream.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service de communication avec l'API TMDB.
 * Remplit l'exigence du TP : exécution asynchrone via Executors (sans AsyncTask déprécié)
 * et traitement de l'absence de réseau.
 */
public class TMDBService {
    private static final String TAG = "TMDBService";
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    
    // Genre TMDB ID pour la thématique Fantasy / Merveilleux
    public static final int FANTASY_GENRE_ID = 14;

    private final ExecutorService executor;
    private final Handler mainHandler;

    public interface TMDBListCallback {
        void onSuccess(List<Movie> movies);
        void onError(String errorMessage);
    }

    public interface TMDBDetailCallback {
        void onSuccess(Movie movie);
        void onError(String errorMessage);
    }

    public TMDBService() {
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // Récupérer la clé API TMDB (SharedPreferences puis BuildConfig)
    private String getApiKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("com.example.amstream_preferences", Context.MODE_PRIVATE);
        String key = prefs.getString("pref_api_key", "");
        if (key.trim().isEmpty()) {
            key = BuildConfig.TMDB_API_KEY;
        }
        return key;
    }

    // Vérifier la connexion réseau de l'appareil (Exigence TP)
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    // Rechercher des films par mot-clé (Fantasy de préférence)
    public void searchMovies(Context context, String query, TMDBListCallback callback) {
        if (!isNetworkAvailable(context)) {
            callback.onError("NO_NETWORK");
            return;
        }

        String apiKey = getApiKey(context);
        if (apiKey.trim().isEmpty()) {
            callback.onError("MISSING_API_KEY");
            return;
        }

        executor.execute(() -> {
            try {
                // Encodage de la recherche pour URL
                String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
                String urlString = BASE_URL + "/search/movie?api_key=" + apiKey 
                        + "&query=" + encodedQuery + "&language=fr-FR&page=1";
                
                String response = makeHttpRequest(urlString);
                List<Movie> results = parseMoviesListJSON(response);
                
                mainHandler.post(() -> callback.onSuccess(results));
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de la recherche de films", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // Découvrir des films populaires de genre Fantasy par défaut (Thématique de l'app)
    public void discoverFantasyMovies(Context context, TMDBListCallback callback) {
        if (!isNetworkAvailable(context)) {
            callback.onError("NO_NETWORK");
            return;
        }

        String apiKey = getApiKey(context);
        if (apiKey.trim().isEmpty()) {
            callback.onError("MISSING_API_KEY");
            return;
        }

        executor.execute(() -> {
            try {
                String urlString = BASE_URL + "/discover/movie?api_key=" + apiKey 
                        + "&with_genres=" + FANTASY_GENRE_ID + "&language=fr-FR&sort_by=popularity.desc";
                
                String response = makeHttpRequest(urlString);
                List<Movie> results = parseMoviesListJSON(response);
                
                mainHandler.post(() -> callback.onSuccess(results));
            } catch (Exception e) {
                Log.e(TAG, "Erreur de découverte de films de fantasy", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // Récupérer les détails complets d'un film (Exigence Fiche Détail TableLayout)
    public void getMovieDetails(Context context, int tmdbId, TMDBDetailCallback callback) {
        if (!isNetworkAvailable(context)) {
            callback.onError("NO_NETWORK");
            return;
        }

        String apiKey = getApiKey(context);
        if (apiKey.trim().isEmpty()) {
            callback.onError("MISSING_API_KEY");
            return;
        }

        executor.execute(() -> {
            try {
                String urlString = BASE_URL + "/movie/" + tmdbId + "?api_key=" + apiKey + "&language=fr-FR";
                String response = makeHttpRequest(urlString);
                Movie movie = parseMovieDetailJSON(response);
                
                mainHandler.post(() -> callback.onSuccess(movie));
            } catch (Exception e) {
                Log.e(TAG, "Erreur de chargement des détails du film ID: " + tmdbId, e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // Effectuer une requête GET HTTP basique
    private String makeHttpRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Erreur HTTP: " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();
        return sb.toString();
    }

    // Parser la liste de films issue du JSON TMDB (JSONObject & JSONArray)
    private List<Movie> parseMoviesListJSON(String jsonResponse) throws JSONException {
        List<Movie> list = new ArrayList<>();
        JSONObject root = new JSONObject(jsonResponse);
        JSONArray results = root.getJSONArray("results");

        for (int i = 0; i < results.length(); i++) {
            JSONObject movieObj = results.getJSONObject(i);
            int id = movieObj.getInt("id");
            String title = movieObj.optString("title", "");
            String overview = movieObj.optString("overview", "");
            String posterRelativePath = movieObj.optString("poster_path", "");
            String posterUrl = posterRelativePath.equals("null") || posterRelativePath.isEmpty() 
                    ? "" 
                    : IMAGE_BASE_URL + posterRelativePath;
            String releaseDate = movieObj.optString("release_date", "");
            double rating = movieObj.optDouble("vote_average", 0.0);

            Movie movie = new Movie(id, title, overview, posterUrl, releaseDate, rating);
            list.add(movie);
        }
        return list;
    }

    // Parser les détails spécifiques d'un film pour la fiche détail
    private Movie parseMovieDetailJSON(String jsonResponse) throws JSONException {
        JSONObject movieObj = new JSONObject(jsonResponse);
        int id = movieObj.getInt("id");
        String title = movieObj.optString("title", "");
        String overview = movieObj.optString("overview", "");
        String posterRelativePath = movieObj.optString("poster_path", "");
        String posterUrl = posterRelativePath.equals("null") || posterRelativePath.isEmpty() 
                ? "" 
                : IMAGE_BASE_URL + posterRelativePath;
        String releaseDate = movieObj.optString("release_date", "");
        double rating = movieObj.optDouble("vote_average", 0.0);

        Movie movie = new Movie(id, title, overview, posterUrl, releaseDate, rating);

        // Champs de métadonnées additionnels requis par le TableLayout
        movie.setOriginalLanguage(movieObj.optString("original_language", "fr").toUpperCase());
        movie.setPopularity(movieObj.optDouble("popularity", 0.0));

        // Essayer d'extraire la durée s'il y a lieu (popularité utilisée à la place si non renseignée)
        return movie;
    }
}
