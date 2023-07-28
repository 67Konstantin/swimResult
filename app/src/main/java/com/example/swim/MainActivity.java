package com.example.swim;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Swimmer> swimmersList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SwimmersAdapter adapter;
    private EditText editTextSearchYear;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREFS_KEY = "swimmers_list_key";
    EditText editTextSurname, editTextName, editTextTime, editTextBirthYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE);

        editTextSurname = findViewById(R.id.editTextSurname);
        editTextName = findViewById(R.id.editTextName);
        editTextTime = findViewById(R.id.editTextTime);
        editTextBirthYear = findViewById(R.id.editTextBirthYear);
        recyclerView = findViewById(R.id.recyclerView);
        editTextSearchYear = findViewById(R.id.editTextSearchYear);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SwimmersAdapter(swimmersList);
        recyclerView.setAdapter(adapter);
        restoreSwimmersList();
    }

    private void restoreSwimmersList() {
        String jsonList = sharedPreferences.getString("swimmers_list", "");
        if (!jsonList.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Swimmer>>() {
            }.getType();
            swimmersList = gson.fromJson(jsonList, type);
            adapter.setSwimmersList(swimmersList);
        }
    }

    public void onClearButtonClick(View view) {
        showAlertDialog("");
    }


    private void saveSwimmersList() {
        Gson gson = new Gson();
        String jsonList = gson.toJson(swimmersList);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("swimmers_list", jsonList);
        editor.apply();
    }

    public void onSearchButtonClick(View view) {
        String searchYearString = editTextSearchYear.getText().toString();
        if (searchYearString.isEmpty()) {
            // Если поле поиска пустое, показываем всех участников
            adapter.setSwimmersList(swimmersList);
        } else {
            // Иначе, ищем участников по заданному году рождения

            List<Swimmer> searchResults = new ArrayList<>();
            for (Swimmer swimmer : swimmersList) {
                if (swimmer.getBirthYear().equals(searchYearString)) {
                    searchResults.add(swimmer);
                }
            }
            adapter.setSwimmersList(searchResults);
        }
    }
    private void showAlertDialog(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Вы уверены что хотите очистить список?")
                .setMessage(text)
                .setCancelable(false)
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        swimmersList.clear();
                        adapter.notifyDataSetChanged();
                        saveSwimmersList();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean isValidate() {
        if (editTextSurname.getText().toString().isEmpty()) {
            editTextSurname.setError("Введите");
            return false;
        } else if (editTextName.getText().toString().isEmpty()) {
            editTextName.setError("Введите");
            return false;
        } else if (editTextBirthYear.getText().toString().isEmpty()) {
            editTextBirthYear.setError("Введите");
            return false;
        } else if (editTextTime.getText().toString().isEmpty()) {
            editTextTime.setError("Введите");
            return false;
        } else return true;
    }

    public void onAddButtonClick(View view) {

        if (isValidate()) {
            String surname = editTextSurname.getText().toString();
            String name = editTextName.getText().toString();
            String timeString = editTextTime.getText().toString();
            double time;
            String[] qwerty = timeString.split("\\.");
            if (qwerty.length>2) {
                // Время в формате мм:сс.СС
                String[] timeParts = timeString.split("\\.");
                int minutes = Integer.parseInt(timeParts[0]);
                String abc = timeParts[1]+"."+timeParts[2];
                double seconds = Double.parseDouble(abc);
                time = minutes * 60 + seconds;
            } else {
                // Время в формате сс.СС
                time = Double.parseDouble(timeString);

            }
            String birthYear = editTextBirthYear.getText().toString();

            Swimmer swimmer = new Swimmer(surname, name, time, birthYear);
            swimmersList.add(swimmer);

            // Сортировка списка по возрастанию времени заплыва
            Collections.sort(swimmersList, new Comparator<Swimmer>() {
                @Override
                public int compare(Swimmer swimmer1, Swimmer swimmer2) {
                    return Double.compare(swimmer1.getTime(), swimmer2.getTime());
                }
            });

            adapter.notifyDataSetChanged();
            saveSwimmersList();
            editTextSurname.setText("");
            editTextName.setText("");
            editTextTime.setText("");
            editTextBirthYear.setText("");
        }
    }

    class Swimmer {
        private String surname;
        private String name;
        private double time;
        private String birthYear;

        public Swimmer(String surname, String name, double time, String birthYear) {
            this.surname = surname;
            this.name = name;
            this.time = time;
            this.birthYear = birthYear;
        }

        public String getSurname() {
            return surname;
        }

        public String getName() {
            return name;
        }

        public double getTime() {
            return time;
        }

        public String getBirthYear() {
            return birthYear;
        }
    }
}
