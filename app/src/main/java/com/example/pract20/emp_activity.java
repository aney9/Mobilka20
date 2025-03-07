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

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class emp_activity extends AppCompatActivity {

    private EditText serviceNameField, serviceCategoryField;
    private Button addServiceButton, deleteServiceButton, updateServiceButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp);

        db = FirebaseFirestore.getInstance();

        serviceNameField = findViewById(R.id.serviceNameField);
        serviceCategoryField = findViewById(R.id.serviceCategoryField);
        addServiceButton = findViewById(R.id.addServiceButton);
        deleteServiceButton = findViewById(R.id.deleteServiceButton);
        updateServiceButton = findViewById(R.id.updateServiceButton);

        addServiceButton.setOnClickListener(v -> addService());
        deleteServiceButton.setOnClickListener(v -> deleteService());
        updateServiceButton.setOnClickListener(v -> updateService());
    }
    private void addService() {
        String serviceName = serviceNameField.getText().toString();
        String serviceCategory = serviceCategoryField.getText().toString();

        if (serviceName.isEmpty() || serviceCategory.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> service = new HashMap<>();
        service.put("serviceName", serviceName);
        service.put("serviceCategory", serviceCategory);

        db.collection("services").document(serviceName).set(service)
                .addOnSuccessListener(v -> Toast.makeText(emp_activity.this, "Услуга добавлена", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(emp_activity.this, "Ошибка при добавлении услуги", Toast.LENGTH_SHORT).show());
    }

    private void deleteService() {
        String serviceName = serviceNameField.getText().toString();

        if (serviceName.isEmpty()) {
            Toast.makeText(this, "Введите название услуги", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("services").document(serviceName).delete()
                .addOnSuccessListener(v -> Toast.makeText(emp_activity.this, "Услуга удалена", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(emp_activity.this, "Ошибка при удалении услуги", Toast.LENGTH_SHORT).show());
    }

    private void updateService() {
        String serviceName = serviceNameField.getText().toString();
        String serviceCategory = serviceCategoryField.getText().toString();

        if (serviceName.isEmpty() || serviceCategory.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("serviceCategory", serviceCategory);

        db.collection("services").document(serviceName).update(updates)
                .addOnSuccessListener(v -> Toast.makeText(emp_activity.this, "Услуга обновлена", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(emp_activity.this, "Ошибка при обновлении услуги", Toast.LENGTH_SHORT).show());
    }
}