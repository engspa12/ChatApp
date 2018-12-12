package com.example.dbm.chatapp;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
    ImageView photoImageView;
    TextView messageTextView;
    TextView authorTextView;
    TextView dateTextView;

    public ChatMessageViewHolder(View itemView) {
        super(itemView);

        photoImageView = (ImageView) itemView.findViewById(R.id.photoImageView);
        messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
        authorTextView = (TextView) itemView.findViewById(R.id.nameTextView);
        dateTextView = (TextView) itemView.findViewById(R.id.dateTextView);

    }
}
