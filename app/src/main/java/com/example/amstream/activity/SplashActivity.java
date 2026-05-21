package com.example.amstream.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.amstream.R;

/**
 * Écran 1 : Splash Screen.
 * Affiche l'identité visuelle de la thématique Fantasy et redirige automatiquement vers l'écran collection.
 */
public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int DELAY_MS = 3000;
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable redirectRunnable = new Runnable() {
        @Override
        public void run() {
            navigateToCollection();
        }
    };

    /**
     * Justification onCreate : Initialise l'activité, charge la vue XML thématique de démarrage,
     * et planifie la redirection automatique après 3 secondes (3000ms).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Log.d(TAG, "onCreate : Initialisation de l'écran Splash.");

        // Planifie la redirection automatique vers l'écran principal
        handler.postDelayed(redirectRunnable, DELAY_MS);
    }

    /**
     * Justification onResume : Appelée lorsque l'écran redevient actif.
     * Si l'utilisateur revient sur le Splash, on s'assure que la redirection est toujours planifiée.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume : L'écran Splash est actif.");
    }

    /**
     * Justification onPause : Appelée quand l'activité perd le focus.
     * Pour éviter toute fuite de mémoire ou transition indésirable en arrière-plan,
     * on annule le handler de redirection si l'utilisateur quitte l'application durant les 3 secondes.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause : Annulation de la planification de redirection.");
        handler.removeCallbacks(redirectRunnable);
    }

    /**
     * Justification onSaveInstanceState : Permet de sauvegarder l'état de l'UI en cas de destruction.
     * Pour le Splash, aucune donnée utilisateur complexe n'est modifiée, on consigne juste l'appel.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState : Sauvegarde de l'état du Splash.");
    }

    /**
     * Justification onRestoreInstanceState : Permet de restaurer l'état après destruction.
     * Simple journalisation car l'état du Splash est statique.
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState : Restauration de l'état du Splash.");
    }

    private void navigateToCollection() {
        Intent intent = new Intent(SplashActivity.this, CollectionActivity.class);
        startActivity(intent);
        finish(); // Empêche de revenir sur le Splash en appuyant sur Retour
    }
}
