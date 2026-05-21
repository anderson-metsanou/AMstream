package com.example.amstream.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.amstream.R;
import com.example.amstream.manager.CollectionManager;
import com.example.amstream.model.Movie;
import com.example.amstream.service.TMDBService;

import java.util.Calendar;

/**
 * Écran 4 : Fiche Détail d'un film.
 * Affiche l'affiche grand format, les détails de l'API (synopsis, métadonnées TableLayout)
 * et le formulaire de suivi (Spinner, RatingBar, EditText, DatePickerDialog).
 */
public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "DetailActivity";
    private static final String KEY_STATE_STATUS = "state_status";
    private static final String KEY_STATE_RATING = "state_rating";
    private static final String KEY_STATE_REVIEW = "state_review";
    private static final String KEY_STATE_DATE = "state_date";

    private ImageView imgPoster;
    private TextView txtTitle, txtTmdbRating, txtReleaseDate, txtLanguage, txtPopularity, txtSynopsis;
    private Spinner spinnerStatus;
    private RatingBar ratingBar;
    private EditText editReview;
    private Button btnDate, btnAdd;

    private Movie movie;
    private CollectionManager collectionManager;
    private TMDBService tmdbService;
    private String selectedDate = "";

    /**
     * Justification onCreate : Récupère le film transmis par Intent, configure les vues,
     * initialise le gestionnaire de collection et le service TMDB, et attache le DatePickerDialog pour le calendrier.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Log.d(TAG, "onCreate : Chargement des détails.");

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Liaison UI
        imgPoster = findViewById(R.id.detail_poster);
        txtTitle = findViewById(R.id.detail_movie_title);
        txtTmdbRating = findViewById(R.id.detail_tmdb_rating);
        txtReleaseDate = findViewById(R.id.detail_release_date);
        txtLanguage = findViewById(R.id.detail_language);
        txtPopularity = findViewById(R.id.detail_popularity);
        txtSynopsis = findViewById(R.id.detail_synopsis);
        spinnerStatus = findViewById(R.id.detail_spinner_status);
        ratingBar = findViewById(R.id.detail_rating_bar);
        editReview = findViewById(R.id.detail_review_edit);
        btnDate = findViewById(R.id.detail_btn_date);
        btnAdd = findViewById(R.id.detail_btn_add);

        // Managers
        collectionManager = CollectionManager.getInstance(this);
        tmdbService = new TMDBService();

        // Récupérer le film depuis l'Intent
        movie = (Movie) getIntent().getSerializableExtra("selected_movie");

        if (movie != null) {
            populateBasicInfo();

            // Vérifier s'il est déjà enregistré dans la collection locale
            Movie savedMovie = collectionManager.getMovieById(movie.getTmdbId());
            if (savedMovie != null) {
                movie = savedMovie; // Utiliser les données personnelles locales
                populatePersonalInfo();
                btnAdd.setText(R.string.detail_update_collection);
            }

            // Récupérer les métadonnées manquantes en asynchrone (langue, popularité...) via TMDB
            fetchExtendedDetails();
        }

        // Listener DatePickerDialog (Exigence Widget DatePicker / DatePickerDialog)
        btnDate.setOnClickListener(v -> showDatePicker());

        // Listener Bouton Enregistrer / Ajouter
        btnAdd.setOnClickListener(v -> saveToCollection());
    }

    /**
     * Justification onResume : L'écran devient interactif.
     * Simple log technique.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume : Détails du film au premier plan.");
    }

    /**
     * Justification onPause : L'activité est mise en pause.
     * Utile pour journaliser la fin d'édition du formulaire.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause : Formulaire de détails en pause.");
    }

    /**
     * Justification onSaveInstanceState : Sauvegarde de l'état d'édition utilisateur (statut, note, texte de l'avis et date sélectionnée).
     * Empêche la perte des saisies en cas de rotation soudaine de l'appareil.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_STATE_STATUS, spinnerStatus.getSelectedItemPosition());
        outState.putFloat(KEY_STATE_RATING, ratingBar.getRating());
        outState.putString(KEY_STATE_REVIEW, editReview.getText().toString());
        outState.putString(KEY_STATE_DATE, selectedDate);
        Log.d(TAG, "onSaveInstanceState : Sauvegarde de la saisie utilisateur.");
    }

    /**
     * Justification onRestoreInstanceState : Restauration des saisies utilisateur après rotation.
     * Remet les valeurs dans le Spinner, la RatingBar, l'EditText et le bouton de date.
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        spinnerStatus.setSelection(savedInstanceState.getInt(KEY_STATE_STATUS, 0));
        ratingBar.setRating(savedInstanceState.getFloat(KEY_STATE_RATING, 0.0f));
        editReview.setText(savedInstanceState.getString(KEY_STATE_REVIEW, ""));
        selectedDate = savedInstanceState.getString(KEY_STATE_DATE, "");
        if (!selectedDate.isEmpty()) {
            btnDate.setText(selectedDate);
        }
        Log.d(TAG, "onRestoreInstanceState : Restauration de la saisie utilisateur.");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Charger les informations basiques transmises par l'intent
    private void populateBasicInfo() {
        txtTitle.setText(movie.getTitle());
        txtTmdbRating.setText(String.format("%.1f/10", movie.getTmdbRating()));
        txtReleaseDate.setText(movie.getReleaseDate());
        txtSynopsis.setText(movie.getOverview());

        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            Glide.with(this)
                    .load(movie.getPosterPath())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(imgPoster);
        }
    }

    // Remplir le formulaire personnel si le film est dans la collection
    private void populatePersonalInfo() {
        // Spinner
        String[] statuses = getResources().getStringArray(R.array.status_array);
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(movie.getPersonalStatus())) {
                spinnerStatus.setSelection(i);
                break;
            }
        }

        // Note & Avis
        ratingBar.setRating(movie.getPersonalRating());
        editReview.setText(movie.getPersonalReview());

        // Date de visionnage
        selectedDate = movie.getWatchDate();
        if (selectedDate != null && !selectedDate.trim().isEmpty()) {
            btnDate.setText(selectedDate);
        }
    }

    // Charger les métadonnées asynchrones complémentaires depuis TMDB
    private void fetchExtendedDetails() {
        tmdbService.getMovieDetails(this, movie.getTmdbId(), new TMDBService.TMDBDetailCallback() {
            @Override
            public void onSuccess(Movie updatedMovie) {
                // Remplir le TableLayout (Exigence structurelle)
                txtLanguage.setText(updatedMovie.getOriginalLanguage());
                txtPopularity.setText(String.format("%.1f", updatedMovie.getPopularity()));
                
                // Mettre à jour les données manquantes du modèle
                movie.setOriginalLanguage(updatedMovie.getOriginalLanguage());
                movie.setPopularity(updatedMovie.getPopularity());
            }

            @Override
            public void onError(String errorMessage) {
                // Pas d'affichage bloquant si indisponible, on met des valeurs par défaut
                txtLanguage.setText("N/A");
                txtPopularity.setText("N/A");
                Log.w(TAG, "Impossible de charger les métadonnées TMDB supplémentaires : " + errorMessage);
            }
        });
    }

    // Afficher le DatePickerDialog
    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Formatter la date (JJ/MM/AAAA)
                    selectedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    btnDate.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    // Enregistrer les modifications locales
    private void saveToCollection() {
        // Enregistrer la saisie dans l'objet
        movie.setPersonalStatus(spinnerStatus.getSelectedItem().toString());
        movie.setPersonalRating(ratingBar.getRating());
        movie.setPersonalReview(editReview.getText().toString().trim());
        movie.setWatchDate(selectedDate);

        // Appel de sauvegarde persistante via openFileOutput
        boolean isSaved = collectionManager.saveMovie(this, movie);
        if (isSaved) {
            Toast.makeText(this, R.string.toast_added, Toast.LENGTH_SHORT).show();
            finish(); // Retourner à l'activité appelante
        } else {
            Toast.makeText(this, R.string.toast_error, Toast.LENGTH_SHORT).show();
        }
    }
}
