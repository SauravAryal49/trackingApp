package com.example.trackingapp.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackingapp.R;

public class FriendRequestViewHolder extends RecyclerView.ViewHolder {
    public TextView txt_user_Email;
    public ImageView btn_accept,btn_reject;

    public FriendRequestViewHolder(@NonNull View itemView) {
        super(itemView);
        txt_user_Email=itemView.findViewById(R.id.user_email);
        btn_accept=itemView.findViewById(R.id.btn_accept);
        btn_reject=itemView.findViewById(R.id.btn_reject);


    }
}
