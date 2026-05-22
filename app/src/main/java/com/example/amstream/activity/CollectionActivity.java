package com.example.amstream.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amstream.R;
import com.example.amstream.adapter.CollectionAdapter;
import com.example.amstream.manager.CollectionManager;
import com.example.amstream.model.Movie;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Écran 2 : Ma Collection.
 * Affiche la collection locale de films, gère le FAB de recherche, le menu principal et le menu contextuel de modification.
 */
public class CollectionActivity extends AppCompatActivity {
    private static final String TAG = "CollectionActivity";
    private static final String KEY_SCROLL_POSITION = "scroll_position";

    private RecyclerView recyclerView;
    private TextView textEmpty;
    private TextView textCount;
    private CollectionAdapter adapter;
    private CollectionManager collectionManager;
    private int savedScrollPosition = 0;

    /**
     * Justification onCreate : Initialise la vue, associe la toolbar personnalisée,
     * configure le RecyclerView et attache le FloatingActionButton pour ouvrir l'écran de recherche.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        Log.d(TAG, "onCreate : Chargement de l'écran Collection.");

        // Configurer la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_collection);
        toolbar.setTitle(""); // Hide default title immediately
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> {
            // Ouvrir les paramètres ou un menu
            startActivity(new Intent(this, SettingsActivity.class));
        });

        // Initialiser les widgets
        recyclerView = findViewById(R.id.recycler_collection);
        textEmpty = findViewById(R.id.text_empty_collection);
        textCount = findViewById(R.id.text_collection_count);
        FloatingActionButton fabSearch = findViewById(R.id.fab_search);

        // Configuration manager
        collectionManager = CollectionManager.getInstance(this);

        // Configuration RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Navigation vers la recherche TMDB
        fabSearch.setOnClickListener(v -> {
            Intent intent = new Intent(CollectionActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Configurer la navigation basse
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_collection);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Déjà sur l'accueil (Collection est l'accueil dans cette app)
                return true;
            } else if (id == R.id.nav_collection) {
                return true;
            } else if (id == R.id.nav_search) {
                Intent intent = new Intent(this, SearchActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // Enregistrer la position si restaurée
        if (savedInstanceState != null) {
            savedScrollPosition = savedInstanceState.getInt(KEY_SCROLL_POSITION, 0);
        }
    }

    /**
     * Justification onResume : Rechargement systématique de la collection locale.
     * C'est essentiel si l'utilisateur revient de DetailActivity ou de SettingsActivity
     * après y avoir modifié ses données de collection.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume : Actualisation de la liste de films.");
        refreshList();
        
        // Restaurer la position du scroll
        if (savedScrollPosition > 0 && recyclerView.getLayoutManager() != null) {
            recyclerView.getLayoutManager().scrollToPosition(savedScrollPosition);
        }
    }

    /**
     * Justification onPause : Sauvegarde temporaire avant mise en arrière-plan.
     * Permet d'enregistrer la position courante de défilement de la liste.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause : L'écran Collection perd le focus.");
        if (recyclerView.getLayoutManager() != null) {
            LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
            savedScrollPosition = lm.findFirstVisibleItemPosition();
        }
    }

    /**
     * Justification onSaveInstanceState : Sauvegarde persistante de l'état UI en cas de changement de configuration (ex: rotation d'écran).
     * On y stocke la position courante de défilement du RecyclerView.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (recyclerView.getLayoutManager() != null) {
            LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
            outState.putInt(KEY_SCROLL_POSITION, lm.findFirstVisibleItemPosition());
        }
        Log.d(TAG, "onSaveInstanceState : Sauvegarde de la position du RecyclerView.");
    }

    /**
     * Justification onRestoreInstanceState : Restauration de l'état UI suite à une rotation d'écran.
     * Récupère l'index du film visible en haut de liste.
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        savedScrollPosition = savedInstanceState.getInt(KEY_SCROLL_POSITION, 0);
        Log.d(TAG, "onRestoreInstanceState : Restauration de la position du RecyclerView.");
    }

    // Créer le menu d'options dans la Toolbar (Paramètres)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.collection_menu, menu);
        return true;
    }

    // Gérer la sélection du menu d'options
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(CollectionActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Charger/Rafraîchir les films du RecyclerView
    private void refreshList() {
        List<Movie> list = collectionManager.getMovies();
        if (list.isEmpty()) {
            textEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            textCount.setText("Aucun film enregistré");
        } else {
            textEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            textCount.setText(list.size() + " films et séries enregistrés");
            
            // Instancier l'adaptateur
            adapter = new CollectionAdapter(this, list, new CollectionAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Movie movie) {
                    // Clic normal -> Ouvrir la fiche détail
                    Intent intent = new Intent(CollectionActivity.this, DetailActivity.class);
                    intent.putExtra("selected_movie", movie);
                    startActivity(intent);
                }

                @Override
                public void onMenuClick(View view, Movie movie) {
                    // Clic sur les 3 points -> Ouvrir un menu contextuel personnalisé
                    showActionDialog(movie);
                }
            });
            recyclerView.setAdapter(adapter);
        }
    }

    // Affiche la boîte de dialogue d'action pour modifier ou supprimer un film
    private void showActionDialog(Movie movie) {
        String[] options = {
                getString(R.string.menu_edit_status),
                getString(R.string.menu_edit_rating),
                getString(R.string.menu_delete)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(movie.getTitle());
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showEditStatusDialog(movie);
                } else if (which == 1) {
                    showEditRatingDialog(movie);
                } else if (which == 2) {
                    showDeleteConfirmDialog(movie);
                }
            }
        });
        builder.show();
    }

    // Dialogue pour modifier le statut du film
    private void showEditStatusDialog(Movie movie) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.menu_edit_status);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_status, null);
        builder.setView(dialogView);

        Spinner spinner = dialogView.findViewById(R.id.dialog_spinner_status);
        
        // Sélectionner la valeur actuelle
        String currentStatus = movie.getPersonalStatus();
        String[] statuses = getResources().getStringArray(R.array.status_array);
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(currentStatus)) {
                spinner.setSelection(i);
                break;
            }
        }

        builder.setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newStatus = spinner.getSelectedItem().toString();
                movie.setPersonalStatus(newStatus);
                collectionManager.saveMovie(CollectionActivity.this, movie);
                refreshList();
                Toast.makeText(CollectionActivity.this, R.string.toast_updated, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();
    }

    // Dialogue pour modifier la note personnelle
    private void showEditRatingDialog(Movie movie) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.menu_edit_rating);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_rating, null);
        builder.setView(dialogView);

        RatingBar ratingBar = dialogView.findViewById(R.id.dialog_rating_bar);
        ratingBar.setRating(movie.getPersonalRating());

        builder.setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                float newRating = ratingBar.getRating();
                movie.setPersonalRating(newRating);
                collectionManager.saveMovie(CollectionActivity.this, movie);
                refreshList();
                Toast.makeText(CollectionActivity.this, R.string.toast_updated, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();
    }

    // Dialogue de confirmation de suppression (Exigence Feedback)
    private void showDeleteConfirmDialog(Movie movie) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_delete_title);
        builder.setMessage(R.string.dialog_delete_message);
        builder.setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                collectionManager.deleteMovie(CollectionActivity.this, movie.getTmdbId());
                refreshList();
                Toast.makeText(CollectionActivity.this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();
    }
}
