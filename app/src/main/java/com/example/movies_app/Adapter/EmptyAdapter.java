package com.example.movies_app.Adapter;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EmptyAdapter extends RecyclerView.Adapter<EmptyAdapter.EmptyViewHolder> {

    @NonNull
    @Override
    public EmptyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = new View(parent.getContext());
        return new EmptyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmptyViewHolder holder, int position) {
        // Không cần làm gì
    }

    @Override
    public int getItemCount() {
        return 0; // Không có item nào
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}