package com.example.amstream.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Classe représentant un film ou une série.
 * Regroupe les données immuables de l'API TMDB et les données modifiables de l'utilisateur. Implémente Serializable pour être transmis via Intent.
 */
public class Movie implements Serializable {
    private static final long serialVersionUID = 1L;

    // Données provenant de TMDB
    private int tmdbId;
    private String title;
    private String overview;
    private String posterPath;
    private String releaseDate;
    private double tmdbRating;
    private String originalLanguage;
    private double popularity;
    private int duration;
    private String genre;

    // Données personnelles de l'utilisateur
    private float personalRating;   // Sur 5 étoiles
    private String personalStatus; // "À voir", "En cours", "Vu"
    private String personalReview; // Avis rédigé
    private String watchDate;      // Date de visionnage (format localisé)

    // Constructeur par défaut
    public Movie() {
        this.personalRating = 0.0f;
        this.personalStatus = "À voir";
        this.personalReview = "";
        this.watchDate = "";
        this.duration = 0;
        this.genre = "";
    }

    // Constructeur complet
    public Movie(int tmdbId, String title, String overview, String posterPath, String releaseDate, double tmdbRating) {
        this();
        this.tmdbId = tmdbId;
        this.title = title;
        this.overview = overview;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
        this.tmdbRating = tmdbRating;
    }

    // Sérialisation en JSONObject pour le stockage interne
    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("tmdbId", tmdbId);
        obj.put("title", title);
        obj.put("overview", overview);
        obj.put("posterPath", posterPath);
        obj.put("releaseDate", releaseDate);
        obj.put("tmdbRating", tmdbRating);
        obj.put("originalLanguage", originalLanguage);
        obj.put("popularity", popularity);
        obj.put("duration", duration);
        obj.put("genre", genre);
        obj.put("personalRating", (double) personalRating);
        obj.put("personalStatus", personalStatus);
        obj.put("personalReview", personalReview);
        obj.put("watchDate", watchDate);
        return obj;
    }

    // Désérialisation depuis un JSONObject
    public static Movie fromJSONObject(JSONObject obj) throws JSONException {
        Movie movie = new Movie();
        movie.setTmdbId(obj.getInt("tmdbId"));
        movie.setTitle(obj.optString("title", ""));
        movie.setOverview(obj.optString("overview", ""));
        movie.setPosterPath(obj.optString("posterPath", ""));
        movie.setReleaseDate(obj.optString("releaseDate", ""));
        movie.setTmdbRating(obj.optDouble("tmdbRating", 0.0));
        movie.setOriginalLanguage(obj.optString("originalLanguage", ""));
        movie.setPopularity(obj.optDouble("popularity", 0.0));
        movie.setDuration(obj.optInt("duration", 0));
        movie.setGenre(obj.optString("genre", ""));
        movie.setPersonalRating((float) obj.optDouble("personalRating", 0.0));
        movie.setPersonalStatus(obj.optString("personalStatus", "À voir"));
        movie.setPersonalReview(obj.optString("personalReview", ""));
        movie.setWatchDate(obj.optString("watchDate", ""));
        return movie;
    }

    // Getters and Setters
    public int getTmdbId() { return tmdbId; }
    public void setTmdbId(int tmdbId) { this.tmdbId = tmdbId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public double getTmdbRating() { return tmdbRating; }
    public void setTmdbRating(double tmdbRating) { this.tmdbRating = tmdbRating; }

    public String getOriginalLanguage() { return originalLanguage; }
    public void setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; }

    public double getPopularity() { return popularity; }
    public void setPopularity(double popularity) { this.popularity = popularity; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public float getPersonalRating() { return personalRating; }
    public void setPersonalRating(float personalRating) { this.personalRating = personalRating; }

    public String getPersonalStatus() { return personalStatus; }
    public void setPersonalStatus(String personalStatus) { this.personalStatus = personalStatus; }

    public String getPersonalReview() { return personalReview; }
    public void setPersonalReview(String personalReview) { this.personalReview = personalReview; }

    public String getWatchDate() { return watchDate; }
    public void setWatchDate(String watchDate) { this.watchDate = watchDate; }
}
