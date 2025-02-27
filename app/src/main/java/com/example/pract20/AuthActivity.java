package com.example.pract20;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
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

public class AuthActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button loginButton, registerButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);

        auth=FirebaseAuth.getInstance();

        emailField=findViewById(R.id.emailField);
        passwordField=findViewById(R.id.passwordField);
        loginButton=findViewById(R.id.loginButton);
        registerButton=findViewById(R.id.registerButton);

        loginButton.setOnClickListener(v->loginUser());
        registerButton.setOnClickListener(v->registUser());
    }

    private void registUser() {
        String email=emailField.getText().toString();
        String password=passwordField.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() <5){
            Toast.makeText(this, "Пароль не менее 5", Toast.LENGTH_SHORT).show();
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Неверный формат почты", Toast.LENGTH_SHORT).show();
        }
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this,task->{
            if(task.isSuccessful()){
                Toast.makeText(AuthActivity.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                saveUserToFirestore();
            }else{
                if(task.getException() !=null){
                    String errorMessage=task.getException().getMessage();
                    Toast.makeText(AuthActivity.this, "Ошибка" + errorMessage   , Toast.LENGTH_SHORT).show();
                    Log.e("AuthError", "Ошибка регистрации",task.getException());

                }
            }
        });
    }

    private void saveUserToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("email", emailField);
        user.put("password", passwordField);
        user.put("role", "user");
        db.collection("users").document(auth.getCurrentUser().getUid()).
                set(user).addOnSuccessListener(v -> {
                    Toast.makeText(AuthActivity.this, "Данные пользователя успешно сохранены", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AuthActivity.this, MainActivity.class));
                    finish();
                }).addOnFailureListener(e -> {
                    Toast.makeText(AuthActivity.this, "Данные пользователя не сохранены" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loginUser() {
        String email=emailField.getText().toString();
        String password=passwordField.getText().toString();

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task->{
            if (task.isSuccessful()) {
                checkUserRole();
            }else{
                Toast.makeText(AuthActivity.this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserRole() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null){
            FirebaseFirestore db =FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                String role=documentSnapshot.getString("role");
                if (role!=null) {
                    switch (role) {
                        case "admin":
                            //startActivity(new Intent(AuthActivity.this, AdminActivity.class));
                            break;
                        case "employee":
                            //startActivity(new Intent(AuthActivity.this, EmpActivity.class));
                            break;
                        case "user":
                            startActivity(new Intent(AuthActivity.this, UserActivity.class));
                            break;
                    }
                    finish();
                }
            });
        }
    }
}