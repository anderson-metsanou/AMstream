package com.example.amstream.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.amstream.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Écran 5 : Paramètres.
 * Utilise SharedPreferences pour stocker les préférences utilisateur (nom, thèmes, faction, et clé API).
 * Répond à la contrainte de widget du TP (CheckBox, Switch, RadioGroup/RadioButton).
 */
public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    private static final String PREFS_NAME = "com.example.amstream_preferences";

    private EditText editUsername;
    private SwitchCompat switchDark;
    private CheckBox checkboxEffects;
    private RadioGroup radioGroupFaction;
    private EditText editApiKey;
    private Button btnSave;

    private SharedPreferences sharedPreferences;

    /**
     * Justification onCreate : Charge le layout XML des préférences utilisateur,
     * initialise SharedPreferences et pré-charge les valeurs précédemment enregistrées.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Log.d(TAG, "onCreate : Chargement des paramètres.");

        // Liaison UI
        editUsername = findViewById(R.id.settings_edit_username);
        switchDark = findViewById(R.id.settings_switch_dark);
        checkboxEffects = findViewById(R.id.settings_checkbox_effects);
        radioGroupFaction = findViewById(R.id.settings_radio_group_faction);
        editApiKey = findViewById(R.id.settings_edit_api_key);
        btnSave = findViewById(R.id.settings_btn_save);

        // Charger SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        loadPreferences();

        // Enregistrer les paramètres
        btnSave.setOnClickListener(v -> savePreferences());

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_profile);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home || id == R.id.nav_collection) {
                    finish();
                    return true;
                } else if (id == R.id.nav_search) {
                    Intent intent = new Intent(this, SearchActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (id == R.id.nav_profile) {
                    return true;
                }
                return false;
            });
        }
    }

    /**
     * Justification onResume : L'écran devient interactif.
     * Simple log technique.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume : Écran paramètres actif.");
    }

    /**
     * Justification onPause : L'écran perd le focus.
     * Utile pour journaliser la fin d'édition des préférences.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause : Fermeture ou mise en pause des paramètres.");
    }

    /**
     * Justification onSaveInstanceState : Sauvegarde temporaire avant changement de configuration.
     * N'est pas strictement obligatoire pour les champs déjà sauvegardés dans SharedPreferences,
     * mais respecte scrupuleusement la contrainte académique du TP.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState : Sauvegarde de l'état temporaire.");
    }

    /**
     * Justification onRestoreInstanceState : Restauration de l'état temporaire après rotation.
     * Journalisation simple.
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState : Restauration de l'état temporaire.");
    }

    // Charger les préférences stockées dans les SharedPreferences (Exigence TP)
    private void loadPreferences() {
        String username = sharedPreferences.getString("pref_user_name", "Aventurier");
        boolean isDark = sharedPreferences.getBoolean("pref_dark_theme", true);
        boolean hasEffects = sharedPreferences.getBoolean("pref_magic_effects", true);
        String faction = sharedPreferences.getString("pref_favorite_faction", "Elfes");
        String apiKey = sharedPreferences.getString("pref_api_key", "");

        // Remplir les champs
        editUsername.setText(username);
        switchDark.setChecked(isDark);
        checkboxEffects.setChecked(hasEffects);
        editApiKey.setText(apiKey);

        // Sélectionner la faction
        if ("Nains".equalsIgnoreCase(faction)) {
            ((RadioButton) findViewById(R.id.settings_radio_dwarves)).setChecked(true);
        } else if ("Mordor".equalsIgnoreCase(faction)) {
            ((RadioButton) findViewById(R.id.settings_radio_mordor)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.settings_radio_elves)).setChecked(true);
        }
    }

    // Enregistrer les préférences dans SharedPreferences
    private void savePreferences() {
        String username = editUsername.getText().toString().trim();
        boolean isDark = switchDark.isChecked();
        boolean hasEffects = checkboxEffects.isChecked();
        String apiKey = editApiKey.getText().toString().trim();

        // Déterminer la faction choisie
        String faction = "Elfes";
        int selectedId = radioGroupFaction.getCheckedRadioButtonId();
        if (selectedId == R.id.settings_radio_dwarves) {
            faction = "Nains";
        } else if (selectedId == R.id.settings_radio_mordor) {
            faction = "Mordor";
        }

        // Sauvegarder via SharedPreferences Editor
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("pref_user_name", username.isEmpty() ? "Aventurier" : username);
        editor.putBoolean("pref_dark_theme", isDark);
        editor.putBoolean("pref_magic_effects", hasEffects);
        editor.putString("pref_favorite_faction", faction);
        editor.putString("pref_api_key", apiKey);
        editor.apply();

        Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show();
        finish(); // Retourner à l'écran Collection
    }
}
