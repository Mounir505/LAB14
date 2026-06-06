package com.example.securestoragelab;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securestoragelab.cache.CacheStore;
import com.example.securestoragelab.files.InternalTextStore;
import com.example.securestoragelab.files.StudentsJsonStore;
import com.example.securestoragelab.model.Student;
import com.example.securestoragelab.prefs.AppPrefs;
import com.example.securestoragelab.prefs.SecurePrefs;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SecureStorageJava";
    private final List<String> langs = Arrays.asList("fr", "en", "ar");

    private EditText etName;
    private EditText etToken;
    private Spinner spLang;
    private Switch swDark;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation des vues
        initViews();
        setupLangSpinner();
        setupListeners();

        // Charge les préférences à l'ouverture de l'application
        loadPrefsToUi();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etToken = findViewById(R.id.etToken);
        spLang = findViewById(R.id.spLang);
        swDark = findViewById(R.id.swDark);
        tvResult = findViewById(R.id.tvResult);
    }

    private void setupLangSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                langs
        );
        spLang.setAdapter(adapter);
    }

    private void setupListeners() {
        findViewById(R.id.btnSavePrefs).setOnClickListener(v -> savePrefs());
        findViewById(R.id.btnLoadPrefs).setOnClickListener(v -> loadPrefsToUi());
        findViewById(R.id.btnSaveJson).setOnClickListener(v -> saveJsonFile());
        findViewById(R.id.btnLoadJson).setOnClickListener(v -> loadJsonFile());
        findViewById(R.id.btnClear).setOnClickListener(v -> clearAll());
    }

    private void savePrefs() {
        String name = etName.getText().toString().trim();
        String lang = langs.get(Math.max(0, spLang.getSelectedItemPosition()));
        String theme = swDark.isChecked() ? "dark" : "light";

        // Sauvegarde des préférences classiques
        boolean ok = AppPrefs.save(this, name, lang, theme, false);

        // Sauvegarde du secret (Token)
        String token = etToken.getText().toString();
        if (!token.trim().isEmpty()) {
            try {
                SecurePrefs.saveToken(this, token);
            } catch (Exception e) {
                tvResult.setText("Erreur chiffrement token : " + e.getMessage());
                Log.e(TAG, "Erreur de chiffrement", e);
                return;
            }
        }

        // Règle de sécurité : ne JAMAIS logger le token en clair
        Log.d(TAG, "Prefs sauvegardées ok=" + ok + ", name=" + name + ", lang=" + lang + ", theme=" + theme);

        // Ecriture optionnelle dans le cache pour démontrer l'usage du cacheDir
        try {
            CacheStore.write(this, "last_ui.txt", "name=" + name + ", lang=" + lang + ", theme=" + theme);
        } catch (Exception ignored) {}

        tvResult.setText("✓ Sauvegarde des préférences terminée.\n(Le token est chiffré et sécurisé)");
        Toast.makeText(this, "Préférences sauvegardées", Toast.LENGTH_SHORT).show();
    }

    private void loadPrefsToUi() {
        AppPrefs.Triple triple = AppPrefs.load(this);

        etName.setText(triple.name);
        swDark.setChecked("dark".equals(triple.theme));

        int idx = langs.indexOf(triple.lang);
        spLang.setSelection(idx >= 0 ? idx : 0);

        int tokenLen = 0;
        try {
            String token = SecurePrefs.loadToken(this);
            tokenLen = (token == null) ? 0 : token.length();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du déchiffrement du token", e);
        }

        tvResult.setText(
                "Chargement terminé :\n" +
                        "Nom : " + triple.name + "\n" +
                        "Langue : " + triple.lang + "\n" +
                        "Thème : " + triple.theme + "\n" +
                        "Longueur du Token : " + tokenLen + " (Masqué)"
        );
    }

    private void saveJsonFile() {
        List<Student> students = Arrays.asList(
                new Student(1, "Amina", 20),
                new Student(2, "Omar", 21),
                new Student(3, "Sara", 19)
        );

        try {
            StudentsJsonStore.save(this, students);
            InternalTextStore.writeUtf8(this, "note.txt", "Dernière synchro JSON réussie.");
            tvResult.setText("✓ Fichier JSON sauvegardé (" + students.size() + " étudiants).");
        } catch (Exception e) {
            tvResult.setText("Erreur sauvegarde JSON : " + e.getMessage());
        }
    }

    private void loadJsonFile() {
        List<Student> students = StudentsJsonStore.load(this);
        String note;

        try {
            note = InternalTextStore.readUtf8(this, "note.txt");
        } catch (Exception e) {
            note = "(Fichier note.txt absent)";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Note interne : ").append(note).append("\n\n");
        sb.append("Étudiants chargés (").append(students.size()).append(") :\n");

        for (Student s : students) {
            sb.append(" • [").append(s.id).append("] ").append(s.name).append(" (").append(s.age).append(" ans)\n");
        }

        tvResult.setText(sb.toString());
    }

    private void clearAll() {
        // 1. Vider les préférences claires
        AppPrefs.clear(this);

        // 2. Vider les préférences chiffrées
        try {
            SecurePrefs.clear(this);
        } catch (Exception e) {
            Log.e(TAG, "Erreur suppression SecurePrefs", e);
        }

        // 3. Supprimer les fichiers JSON et texte internes
        StudentsJsonStore.delete(this);
        InternalTextStore.delete(this, "note.txt");

        // 4. Purger le dossier Cache
        int purged = CacheStore.purge(this);

        // 5. Réinitialiser l'interface UI
        etName.setText("");
        etToken.setText("");
        swDark.setChecked(false);
        spLang.setSelection(0);

        tvResult.setText("🗑 Nettoyage complet terminé.\nFichiers cache purgés : " + purged);
        Toast.makeText(this, "Toutes les données ont été effacées", Toast.LENGTH_SHORT).show();
    }
}