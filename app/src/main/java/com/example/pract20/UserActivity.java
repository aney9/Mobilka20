package com.example.pract20;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserActivity extends AppCompatActivity {

    private ListView servicesListView;
    private Button bookServicesButton;
    private FirebaseFirestore db;
    private List<String> servicesList;
    private List<String> ID;
    private ArrayAdapter<String> adapter;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);

        db = FirebaseFirestore.getInstance();
        servicesListView = findViewById(R.id.servicesListView);
        bookServicesButton = findViewById(R.id.bookServiceButton);
        servicesList = new ArrayList<>();
        ID = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, servicesList);
        servicesListView.setAdapter(adapter);

        servicesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        calendar = Calendar.getInstance();

        // Загружаем услуги
        loadServices();

        bookServicesButton.setOnClickListener(v -> {
            int selectedPosition = servicesListView.getCheckedItemPosition();
            if (selectedPosition != ListView.INVALID_POSITION) {
                String servicesID = ID.get(selectedPosition);
                showDateTimePickerDialog(servicesID);
            } else {
                Toast.makeText(this, "Выберите услугу", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDateTimePickerDialog(String servicesID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите дату и время");
        View view = getLayoutInflater().inflate(R.layout.dialog_date_time_picker, null);
        builder.setView(view);
        TextView dateField = view.findViewById(R.id.dateField);
        TextView timeField = view.findViewById(R.id.timeField);

        dateField.setOnClickListener(v -> showDatePickerDialog(dateField));
        timeField.setOnClickListener(v -> showTimePickerDialog(timeField));

        builder.setPositiveButton("Записаться", ((dialog, which) -> {
            String date = dateField.getText().toString();
            String time = timeField.getText().toString();
            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Выберите дату и время", Toast.LENGTH_SHORT).show();
                return;
            }
            bookServices(servicesID, date, time);
        }));
        builder.setNegativeButton("Отмена", ((dialog, which) -> dialog.dismiss()));
        builder.create().show();
    }

    private void bookServices(String servicesID, String date, String time) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String clientName = user.getEmail();
            db.collection("services").document(servicesID).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String serviceName = documentSnapshot.getString("servicesName"); // Исправлено
                            Map<String, Object> appointment = new HashMap<>();
                            appointment.put("clientID", user.getUid());
                            appointment.put("clientName", clientName);
                            appointment.put("serviceName", serviceName);
                            appointment.put("date", date);
                            appointment.put("time", time);

                            db.collection("appointment").add(appointment)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(UserActivity.this, "Запись создана", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(UserActivity.this, "Ошибка записи", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    });
        }
    }

    private void showTimePickerDialog(TextView timeField) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    timeField.setText(hourOfDay + ":" + (minute < 10 ? "0" + minute : minute));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void showDatePickerDialog(TextView dateField) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    dateField.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }


    private void loadServices() {
        db.collection("services").get().addOnSuccessListener(queryDocumentSnapshots -> {
            servicesList.clear();
            ID.clear();
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                String servicesName = documentSnapshot.getString("servicesName");
                String serviceID = documentSnapshot.getId();
                if (servicesName != null) {
                    servicesList.add(servicesName);
                    ID.add(serviceID);
                }
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Ошибка загрузки услуг", Toast.LENGTH_SHORT).show();
        });
    }
}