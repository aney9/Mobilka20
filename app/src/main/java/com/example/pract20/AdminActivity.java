package com.example.pract20;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private EditText emailField, passwordField, roleField;
    private Button addUserButton, deleteUserButton, updateUserButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        roleField = findViewById(R.id.roleField);
        addUserButton = findViewById(R.id.addUserButton);
        deleteUserButton = findViewById(R.id.deleteUserButton);
        updateUserButton = findViewById(R.id.updateUserButton);

        addUserButton.setOnClickListener(v -> addUser());
        deleteUserButton.setOnClickListener(v -> deleteUser());
        updateUserButton.setOnClickListener(v -> updateUser());
    }

    private void addUser() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String role = roleField.getText().toString();

        if (email.isEmpty() || password.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Создаем пользователя в Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Если пользователь успешно создан, сохраняем его данные в Firestore
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    saveUserToFirestore(user.getUid(), email, role, password);
                }
            } else {
                Toast.makeText(AdminActivity.this, "Ошибка при создании пользователя: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserToFirestore(String userId, String email, String role, String password) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("role", role);
        user.put("password", password); // Сохраняем пароль (не рекомендуется)

        // Используем UID пользователя как идентификатор документа
        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(v -> Toast.makeText(AdminActivity.this, "Пользователь добавлен", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(AdminActivity.this, "Ошибка при добавлении пользователя: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteUser() {
        String email = emailField.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(this, "Введите email пользователя", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ищем пользователя по email, чтобы получить его UID
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Получаем UID пользователя из документа
                        String userId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        // Удаляем пользователя из Firestore по UID
                        db.collection("users").document(userId)
                                .delete()
                                .addOnSuccessListener(v -> Toast.makeText(AdminActivity.this, "Пользователь удален", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(AdminActivity.this, "Ошибка при удалении пользователя: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(AdminActivity.this, "Пользователь с таким email не найден", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(AdminActivity.this, "Ошибка при поиске пользователя: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateUser() {
        String email = emailField.getText().toString();
        String role = roleField.getText().toString();
        String password = passwordField.getText().toString();

        if (email.isEmpty() || role.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ищем пользователя по email, чтобы получить его UID
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Получаем UID пользователя из документа
                        String userId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        // Обновляем данные пользователя в Firestore по UID
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("role", role);
                        updates.put("password", password); // Обновляем пароль (не рекомендуется)

                        db.collection("users").document(userId)
                                .update(updates)
                                .addOnSuccessListener(v -> Toast.makeText(AdminActivity.this, "Пользователь обновлен", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(AdminActivity.this, "Ошибка при обновлении пользователя: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(AdminActivity.this, "Пользователь с таким email не найден", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(AdminActivity.this, "Ошибка при поиске пользователя: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}