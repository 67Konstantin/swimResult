package com.example.swim;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private List<Swimmer> swimmersList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SwimmersAdapter adapter;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREFS_KEY = "swimmers_list_key";
    public static boolean isEditMode = false;
    EditText editTextSurname, editTextName, editTextTime, editTextBirthYear, editTextDistance;
    Spinner editTextGender;
    String filterGender = "";
    String filterDistance = "";
    String filterBirthYear = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE);

        editTextDistance = findViewById(R.id.editTextDistance);
        editTextGender = findViewById(R.id.editTextGender);
        editTextSurname = findViewById(R.id.editTextSurname);
        editTextName = findViewById(R.id.editTextName);
        editTextTime = findViewById(R.id.editTextTime);
        editTextBirthYear = findViewById(R.id.editTextBirthYear);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SwimmersAdapter(swimmersList);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    if (isEditMode) {
                        View childView = rv.findChildViewUnder(e.getX(), e.getY());
                        if (childView != null) {
                            int position = rv.getChildAdapterPosition(childView);
                            editSwimmerDialog(position);
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });
        restoreSwimmersList();
    }

    // Метод для редактирования участника в списке
    @SuppressLint("MissingInflatedId")
    void editSwimmerDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.edit_swimmer_dialog, null);
        builder.setView(dialogView);
        builder.setTitle("Редактировать участника");
        Swimmer swimmerToEdit = swimmersList.get(position);

        EditText editTextSurname = dialogView.findViewById(R.id.editTextEditSurname);
        EditText editTextName = dialogView.findViewById(R.id.editTextEditName);
        EditText editTextTime = dialogView.findViewById(R.id.editTextEditTime);
        EditText editTextBirthYear = dialogView.findViewById(R.id.editTextEditBirthYear);
        Spinner editTextGender = dialogView.findViewById(R.id.editTextEditGender);
        EditText editTextDistance = dialogView.findViewById(R.id.editTextEditDistance);
        int index;
        if(Objects.equals(swimmerToEdit.getGender(), "м")||Objects.equals(swimmerToEdit.getGender(), "М")) {index = 1;}
        else {index = 2;}
        // Заполнение полей данными выбранного участника
        editTextSurname.setText(swimmerToEdit.getSurname());
        editTextName.setText(swimmerToEdit.getName());
        editTextTime.setText(String.valueOf(swimmerToEdit.getTime()));
        editTextBirthYear.setText(String.valueOf(swimmerToEdit.getBirthYear()));

        editTextGender.setSelection(1);
        Log.d("123321", editTextGender.getSelectedItem().toString());
        editTextDistance.setText(String.valueOf(swimmerToEdit.getDistance()));

        builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Обработка сохранения изменений
                String surname = editTextSurname.getText().toString();
                String name = editTextName.getText().toString();
                double time = Double.parseDouble(editTextTime.getText().toString());
                String birthYear = editTextBirthYear.getText().toString();
                String distance = editTextDistance.getText().toString();
                String gender = editTextGender.getSelectedItem().toString();
                //Меняю класс на новый
                swimmerToEdit.setSurname(surname);
                swimmerToEdit.setName(name);
                swimmerToEdit.setTime(time);
                swimmerToEdit.setBirthYear(birthYear);
                swimmerToEdit.setDistance(distance);
                swimmerToEdit.setGender(gender);

                Collections.sort(swimmersList, new Comparator<Swimmer>() {
                    @Override
                    public int compare(Swimmer swimmer1, Swimmer swimmer2) {
                        return Double.compare(swimmer1.getTime(), swimmer2.getTime());
                    }
                });

                adapter.notifyDataSetChanged();
                saveSwimmersList();
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Ничего не делаем при нажатии "Отмена"
            }
        });
        builder.setNeutralButton("Удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Удаление участника из списка
                swimmersList.remove(position);
                adapter.notifyDataSetChanged();
                saveSwimmersList();
            }
        });

        builder.show();
        //Toast.makeText(this, swimmerToEdit.getGender(), Toast.LENGTH_SHORT).show();
    }

    // Метод для активации/деактивации режима редактирования
    public void toggleEditMode(View view) {
        isEditMode = !isEditMode;
        if (isEditMode) {
            // Включение режима редактирования
            view.setBackgroundColor(getResources().getColor(R.color.light_blue_80BFFF));

        } else {
            // Отключение режима редактирования
            view.setBackgroundColor(getResources().getColor(R.color.blue_0077ff));

        }
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

                        //Скидываем  параметры и показываем пустой список
                        filterBirthYear = "";
                        filterDistance = "";
                        filterGender = "";
                        List<Swimmer> filteredList = new ArrayList<>();
                        for (Swimmer swimmer : swimmersList) {
                            if (filterBirthYear.isEmpty() || String.valueOf(swimmer.getBirthYear()).equals(filterBirthYear)) {
                                if (filterDistance.isEmpty() || swimmer.getDistance().equalsIgnoreCase(filterDistance)) {
                                    if (filterGender.isEmpty() || swimmer.getGender().equalsIgnoreCase(filterGender)) {
                                        filteredList.add(swimmer);
                                    }
                                }
                            }
                        }
                        // Обновляем список в RecyclerView с учетом фильтров
                        adapter.setSwimmersList(filteredList);

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
        } else if (editTextDistance.getText().toString().isEmpty()) {
            editTextDistance.setError("Введите");
            return false;
        } else return true;
    }

    public void onAddButtonClick(View view) {

        if (isValidate()) {
            String surname = editTextSurname.getText().toString();
            String name = editTextName.getText().toString();
            String timeString = editTextTime.getText().toString();
            String gender = editTextGender.getSelectedItem().toString();
            String distance = editTextDistance.getText().toString();
            double time;
            String[] qwerty = timeString.split("\\.");
            if (qwerty.length > 2) {
                // Время в формате мм:сс.СС
                String[] timeParts = timeString.split("\\.");
                int minutes = Integer.parseInt(timeParts[0]);
                String abc = timeParts[1] + "." + timeParts[2];
                double seconds = Double.parseDouble(abc);
                time = minutes * 60 + seconds;
            } else {
                // Время в формате сс.СС
                time = Double.parseDouble(timeString);

            }
            String birthYear = editTextBirthYear.getText().toString();

            Swimmer swimmer1 = new Swimmer(surname, name, time, birthYear, gender, distance);
            swimmersList.add(swimmer1);

            // Сортировка списка по возрастанию времени заплыва
            Collections.sort(swimmersList, new Comparator<Swimmer>() {
                @Override
                public int compare(Swimmer swimmer1, Swimmer swimmer2) {
                    return Double.compare(swimmer1.getTime(), swimmer2.getTime());
                }
            });
            // Применяем фильтры на обновленный список участников


            List<Swimmer> filteredList = new ArrayList<>();
            for (Swimmer swimmer : swimmersList) {
                if (filterBirthYear.isEmpty() || String.valueOf(swimmer.getBirthYear()).equals(filterBirthYear)) {
                    if (filterDistance.isEmpty() || swimmer.getDistance().equalsIgnoreCase(filterDistance)) {
                        if (filterGender.isEmpty() || swimmer.getGender().equalsIgnoreCase(filterGender)) {
                            filteredList.add(swimmer);
                        }
                    }
                }
            }
            // Обновляем список в RecyclerView с учетом фильтров
            adapter.setSwimmersList(filteredList);


            adapter.notifyDataSetChanged();
            saveSwimmersList();
            editTextSurname.setText("");
            editTextName.setText("");
            editTextTime.setText("");
            editTextBirthYear.setText("");
        }

    }

    public void onFilterButtonClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.filter_dialog, null);
        builder.setView(dialogView);
        builder.setTitle("Фильтры");
        builder.setPositiveButton("Применить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Обработка фильтрации
                filterBirthYear = ((EditText) dialogView.findViewById(R.id.editTextFilterBirthYear)).getText().toString();
                filterDistance = ((EditText) dialogView.findViewById(R.id.editTextFilterDistance)).getText().toString();
                filterGender = ((Spinner) dialogView.findViewById(R.id.editTextFilterGender)).getSelectedItem().toString();

                List<Swimmer> filteredList = new ArrayList<>();
                for (Swimmer swimmer : swimmersList) {
                    if (filterBirthYear.isEmpty() || String.valueOf(swimmer.getBirthYear()).equals(filterBirthYear)) {
                        if (filterDistance.isEmpty() || swimmer.getDistance().equalsIgnoreCase(filterDistance)) {
                            if (filterGender.isEmpty() || swimmer.getGender().equalsIgnoreCase(filterGender)) {
                                filteredList.add(swimmer);
                            }
                        }
                    }
                }

                adapter.setSwimmersList(filteredList);
            }
        });
        builder.setNegativeButton("Сбросить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Сброс фильтров и показ всех участников
                adapter.setSwimmersList(swimmersList);
            }
        });
        builder.show();
    }


    class Swimmer {
        private String surname;
        private String name;
        private double time;
        private String birthYear;

        private String gender;   // Пол (м или ж)
        private String distance; // Дистанция


        public Swimmer(String surname, String name, double time, String birthYear, String gender, String distance) {
            this.surname = surname;
            this.name = name;
            this.time = time;
            this.birthYear = birthYear;
            this.gender = gender;
            this.distance = distance;
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

        public String getGender() {
            return gender;
        }

        public String getDistance() {
            return distance;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }

        public void setSurname(String surname) {
            this.surname = surname;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setTime(double time) {
            this.time = time;
        }

        public void setBirthYear(String birthYear) {
            this.birthYear = birthYear;
        }
    }
}
