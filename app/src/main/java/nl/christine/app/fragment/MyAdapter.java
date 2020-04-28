/*
 * Copyright (c) 2020, Zaphod Consulting BV, Christine Karman
 * This project is free software: you can redistribute it and/or modify it under the terms of
 * the Apache License, Version 2.0. You can find a copy of the license at
 * http://www.apache.org/licenses/LICENSE-2.0.
 */

package nl.christine.app.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import nl.christine.app.R;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    List<String> messages = new ArrayList<>();

    public void addMessage(String message) {
        messages.add(message);
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.messages_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private class ViewHolder extends BaseViewHolder {

        TextView messageView;

        public ViewHolder(View view) {
            super(view);
            messageView = view.findViewById(R.id.message);
        }

        @Override
        public void onBind(int position) {
            super.onBind(position);
            messageView.setText(messages.get(position));
        }

        @Override
        protected void clear() {
            messageView.setText("");
        }
    }
}
