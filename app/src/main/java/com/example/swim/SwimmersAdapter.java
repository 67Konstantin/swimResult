package com.example.swim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SwimmersAdapter extends RecyclerView.Adapter<SwimmersAdapter.ViewHolder> {

    private List<MainActivity.Swimmer> swimmersList;

    public SwimmersAdapter(List<MainActivity.Swimmer> swimmersList) {
        this.swimmersList = swimmersList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_swimmer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainActivity.Swimmer swimmer = swimmersList.get(position);
        holder.textViewNumber.setText(String.valueOf(position + 1)); // Нумерация начинается с 1
        holder.textViewName.setText(swimmer.getSurname() + " " + swimmer.getName());
        holder.textViewTime.setText(swimmer.getTime() + " сек");
        holder.textViewBirthYear.setText("Год рождения: " + swimmer.getBirthYear());
    }

    @Override
    public int getItemCount() {
        return swimmersList.size();
    }
    public void setSwimmersList(List<MainActivity.Swimmer> swimmersList) {
        this.swimmersList = swimmersList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNumber;
        TextView textViewName;
        TextView textViewTime;
        TextView textViewBirthYear;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNumber = itemView.findViewById(R.id.textViewNumber);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewBirthYear = itemView.findViewById(R.id.textViewBirthYear);
        }
    }
}
