package com.example.amstream.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amstream.R;
import com.example.amstream.adapter.SearchAdapter;
import com.example.amstream.model.Movie;
import com.example.amstream.service.TMDBService;

import java.util.ArrayList;
import java.util.List;

/**
 * Écran 3 : Recherche TMDB.
 * Offre un SearchView fonctionnel et affiche les résultats de recherche (ou des suggestions par défaut)
 * dans un RecyclerView configuré en grille de 2 colonnes.
 */
public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    private static final String KEY_QUERY_STRING = "search_query";

    private RecyclerView recyclerView;
    private TextView textLoading;
    private TextView textEmpty;
    private SearchAdapter adapter;
    
    private TMDBService tmdbService;
    private String currentQuery = "";

    /**
     * Justification onCreate : Initialise la vue, configure la toolbar de retour,
     * instancie le service TMDB asynchrone, puis configure la grille à 2 colonnes du RecyclerView.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Log.d(TAG, "onCreate : Chargement de l'écran Recherche.");

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Widgets
        recyclerView = findViewById(R.id.recycler_search);
        textLoading = findViewById(R.id.text_search_loading);
        textEmpty = findViewById(R.id.text_search_empty);

        // Service réseau TMDB
        tmdbService = new TMDBService();

        // RecyclerView en Grille de 2 colonnes (Exigence Conception visuelle)
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Configurer la recherche dans la Toolbar
        setupSearchView(toolbar);

        // Restaurer la requête de recherche si présente
        if (savedInstanceState != null) {
            currentQuery = savedInstanceState.getString(KEY_QUERY_STRING, "");
        }

        // Par défaut, charger les films populaires du genre Fantasy pour ne pas laisser l'écran vide
        if (currentQuery.trim().isEmpty()) {
            discoverFantasyDefaults();
        } else {
            performSearch(currentQuery);
        }
    }

    /**
     * Justification onResume : L'écran devient interactif.
     * Simple log technique.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume : L'écran de recherche est au premier plan.");
    }

    /**
     * Justification onPause : L'écran perd le focus.
     * Utile pour journaliser la fin d'interaction utilisateur.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause : L'écran de recherche perd le focus.");
    }

    /**
     * Justification onSaveInstanceState : Sauvegarde de la chaîne de recherche en cours.
     * Permet d'éviter de perdre les résultats affichés en cas de rotation d'écran.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.outState.onSaveInstanceState(outState);
        outState.putString(KEY_QUERY_STRING, currentQuery);
        Log.d(TAG, "onSaveInstanceState : Sauvegarde de la requête : " + currentQuery);
    }

    /**
     * Justification onRestoreInstanceState : Restauration de la chaîne après rotation d'écran.
     * Relance la recherche pour reconstruire le cache visuel.
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentQuery = savedInstanceState.getString(KEY_QUERY_STRING, "");
        Log.d(TAG, "onRestoreInstanceState : Restauration de la requête : " + currentQuery);
        if (!currentQuery.trim().isEmpty()) {
            performSearch(currentQuery);
        }
    }

    // Gestion du clic de retour de la toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Configurer le widget SearchView dans la Toolbar
    private void setupSearchView(Toolbar toolbar) {
        // Ajouter dynamiquement une SearchView à la toolbar
        SearchView searchView = new SearchView(this);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setIconifiedByDefault(false); // Toujours visible
        
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.MATCH_PARENT, 
                Toolbar.LayoutParams.WRAP_CONTENT
        );
        searchView.setLayoutParams(layoutParams);
        toolbar.addView(searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                performSearch(query);
                searchView.clearFocus(); // Ferme le clavier
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                return false;
            }
        });
    }

    // Appeler l'API de découverte de films de Fantasy (Thématique de l'app)
    private void discoverFantasyDefaults() {
        textLoading.setVisibility(View.VISIBLE);
        textEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        tmdbService.discoverFantasyMovies(this, new TMDBService.TMDBListCallback() {
            @Override
            public void onSuccess(List<Movie> movies) {
                textLoading.setVisibility(View.GONE);
                displayResults(movies);
            }

            @Override
            public void onError(String errorMessage) {
                textLoading.setVisibility(View.GONE);
                handleNetworkError(errorMessage);
            }
        });
    }

    // Lancer la recherche sur l'API TMDB
    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            discoverFantasyDefaults();
            return;
        }

        textLoading.setVisibility(View.VISIBLE);
        textEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        tmdbService.searchMovies(this, query, new TMDBService.TMDBListCallback() {
            @Override
            public void onSuccess(List<Movie> movies) {
                textLoading.setVisibility(View.GONE);
                displayResults(movies);
            }

            @Override
            public void onError(String errorMessage) {
                textLoading.setVisibility(View.GONE);
                handleNetworkError(errorMessage);
            }
        });
    }

    // Afficher les résultats dans la grille
    private void displayResults(List<Movie> movies) {
        if (movies.isEmpty()) {
            textEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            adapter = new SearchAdapter(this, movies, new SearchAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Movie movie) {
                    // Au clic, on navigue vers l'écran détail
                    Intent intent = new Intent(SearchActivity.this, DetailActivity.class);
                    intent.putExtra("selected_movie", movie);
                    startActivity(intent);
                }
            });
            recyclerView.setAdapter(adapter);
        }
    }

    // Gère les différents types d'erreurs (Réseau manquant, clé manquante...)
    private void handleNetworkError(String error) {
        if ("NO_NETWORK".equals(error)) {
            Toast.makeText(this, R.string.toast_no_network, Toast.LENGTH_LONG).show();
            textEmpty.setText(R.string.toast_no_network);
        } else if ("MISSING_API_KEY".equals(error)) {
            Toast.makeText(this, R.string.toast_api_key_missing, Toast.LENGTH_LONG).show();
            textEmpty.setText(R.string.toast_api_key_missing);
        } else {
            Toast.makeText(this, getString(R.string.toast_error) + " : " + error, Toast.LENGTH_SHORT).show();
            textEmpty.setText(R.string.toast_error);
        }
        textEmpty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }
}
