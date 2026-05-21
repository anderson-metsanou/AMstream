package com.example.amstream.manager;

import android.content.Context;
import android.util.Log;

import com.example.amstream.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton gérant la persistance de la collection de films dans les fichiers internes de l'appareil.
 * Utilise la méthode recommandée par le TP : écriture d'un fichier JSON local avec openFileOutput.
 */
public class CollectionManager {
    private static final String TAG = "CollectionManager";
    private static final String FILE_NAME = "collection.json";
    private static CollectionManager instance;
    private final List<Movie> movies;

    private CollectionManager(Context context) {
        this.movies = new ArrayList<>();
        loadCollection(context);
    }

    public static synchronized CollectionManager getInstance(Context context) {
        if (instance == null) {
            instance = new CollectionManager(context.getApplicationContext());
        }
        return instance;
    }

    // Récupérer la liste en mémoire
    public List<Movie> getMovies() {
        return new ArrayList<>(movies);
    }

    // Trouver un film par son ID TMDB
    public Movie getMovieById(int tmdbId) {
        for (Movie m : movies) {
            if (m.getTmdbId() == tmdbId) {
                return m;
            }
        }
        return null;
    }

    // Vérifier si un film existe dans la collection
    public boolean exists(int tmdbId) {
        return getMovieById(tmdbId) != null;
    }

    // Ajouter ou mettre à jour un film
    public boolean saveMovie(Context context, Movie movie) {
        Movie existing = getMovieById(movie.getTmdbId());
        if (existing != null) {
            // Mise à jour des valeurs personnelles
            existing.setPersonalRating(movie.getPersonalRating());
            existing.setPersonalStatus(movie.getPersonalStatus());
            existing.setPersonalReview(movie.getPersonalReview());
            existing.setWatchDate(movie.getWatchDate());
        } else {
            // Nouvel ajout
            movies.add(movie);
        }
        return persistCollection(context);
    }

    // Supprimer un film
    public boolean deleteMovie(Context context, int tmdbId) {
        Movie existing = getMovieById(tmdbId);
        if (existing != null) {
            movies.remove(existing);
            return persistCollection(context);
        }
        return false;
    }

    // Charger la collection depuis le fichier interne
    private void loadCollection(Context context) {
        movies.clear();
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            JSONArray array = new JSONArray(sb.toString());
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                movies.add(Movie.fromJSONObject(obj));
            }
            Log.d(TAG, "Collection chargée avec succès : " + movies.size() + " films.");
        } catch (IOException e) {
            Log.w(TAG, "Fichier collection.json inexistant ou inaccessible, initialisation d'une collection vide.");
        } catch (JSONException e) {
            Log.e(TAG, "Erreur lors du parsing JSON du fichier de collection.", e);
        }
    }

    // Écrire la collection en mémoire dans le stockage interne
    private boolean persistCollection(Context context) {
        try {
            JSONArray array = new JSONArray();
            for (Movie m : movies) {
                array.put(m.toJSONObject());
            }
            String jsonString = array.toString();

            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes(StandardCharsets.UTF_8));
            fos.close();
            Log.d(TAG, "Collection sauvegardée dans le stockage interne.");
            return true;
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Erreur lors de l'écriture de la collection dans le stockage interne.", e);
            return false;
        }
    }
}
