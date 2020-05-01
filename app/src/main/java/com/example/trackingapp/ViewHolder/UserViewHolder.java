package com.example.trackingapp.ViewHolder;


import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackingapp.Interface.iRecyclerItemClickListener;
import com.example.trackingapp.R;

public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView textUserEmail;
    iRecyclerItemClickListener irecyclerItemClickListener;

    public void setIrecyclerItemClickListener(iRecyclerItemClickListener irecyclerItemClickListener) {
        this.irecyclerItemClickListener = irecyclerItemClickListener;
    }

    public UserViewHolder(@NonNull View itemView) {
        super(itemView);
        textUserEmail=itemView.findViewById(R.id.user_email);
        itemView.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        irecyclerItemClickListener.onItemClickListener(v,getAdapterPosition());
    }
}
