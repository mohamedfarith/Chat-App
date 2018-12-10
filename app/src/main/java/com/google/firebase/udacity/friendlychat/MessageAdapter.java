package com.google.firebase.udacity.friendlychat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyMessageViewHolder> {
    Context context;
    ArrayList<FriendlyMessage> messagesList;
    public static final String TAG = MessageAdapter.class.getSimpleName();
    MessageAdapter(Context context, ArrayList<FriendlyMessage> messagesList){
     this.context = context;
     this.messagesList = messagesList;
    }
    @Override
    public MyMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message,parent,false);
        return new MyMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyMessageViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: is called "+messagesList.get(position).getText());
            holder.message.setText(messagesList.get(position).getText());
            holder.name.setText(messagesList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }
    class MyMessageViewHolder extends RecyclerView.ViewHolder{
        TextView message;
        TextView name;

        public MyMessageViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.messageTextView);
            name = itemView.findViewById(R.id.nameTextView);
        }
    }
}
