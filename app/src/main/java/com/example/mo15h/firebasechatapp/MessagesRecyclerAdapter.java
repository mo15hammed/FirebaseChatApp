package com.example.mo15h.firebasechatapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessagesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "MessagesRecyclerAdapter";
    private FirebaseAuth mAuth;
    private List<Message> messagesList;
    private User chatUser;
    private Context mContext;

    public MessagesRecyclerAdapter(Context mContext, List<Message> messagesList, User user) {
        mAuth = FirebaseAuth.getInstance();
        this.messagesList = messagesList;
        this.chatUser = user;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View my_view = LayoutInflater.from(mContext).inflate(R.layout.my_message_item_layout, viewGroup, false);
        View his_view = LayoutInflater.from(mContext).inflate(R.layout.his_message_item_layout, viewGroup, false);

        if (mAuth != null) {
            if (messagesList.get(i).getFrom().equals(mAuth.getUid())) {
                return new MyMessagesViewHolder(my_view);
            } else {
                return new HisMessagesViewHolder(his_view);
            }
        }
        return new HisMessagesViewHolder(his_view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        Message msg = messagesList.get(i);
        Log.d(TAG, "onBindViewHolder: Message = " + msg);


        if (mAuth != null) {
            if (msg.getFrom().equals(mAuth.getUid())) {

                final MyMessagesViewHolder holder = (MyMessagesViewHolder) viewHolder;
                if (msg.getType().equals("text")) {

                    holder.mMessage.setVisibility(View.VISIBLE);
                    holder.mMessageImage.setVisibility(View.GONE);
                    holder.mMessage.setText(msg.getMessage());
                    holder.mMessageImage.setImageResource(0);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.mTime.getLayoutParams();
                    params.removeRule(RelativeLayout.BELOW);
                    params.addRule(RelativeLayout.BELOW, R.id.txt_my_message);

                    holder.mMessage.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {

                            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("message", holder.mMessage.getText());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(mContext, "Copied !", Toast.LENGTH_SHORT).show();

                            return true;
                        }
                    });

                } else if (msg.getType().equals("image")) {

                    holder.mMessage.setVisibility(View.INVISIBLE);
                    holder.mMessageImage.setVisibility(View.VISIBLE);
                    holder.mMessage.setText("");

                    Picasso.get()
                            .load(msg.getMessage())
                            .placeholder(R.drawable.default_avatar)
                            .error(R.drawable.default_avatar)
                            .into(holder.mMessageImage);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.mTime.getLayoutParams();
                    params.removeRule(RelativeLayout.BELOW);
                    params.addRule(RelativeLayout.BELOW, R.id.img_my_message);

                }
                holder.mTime.setText(GetTimeAgo.getTime(msg.getTimestamp()));


            } else {
                final HisMessagesViewHolder holder = (HisMessagesViewHolder) viewHolder;
                if (msg.getType().equals("text")) {

                    holder.mMessage.setVisibility(View.VISIBLE);
                    holder.mMessageImage.setVisibility(View.GONE);
                    holder.mMessage.setText(msg.getMessage());
                    holder.mMessageImage.setImageResource(0);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.mTime.getLayoutParams();
                    params.removeRule(RelativeLayout.BELOW);
                    params.addRule(RelativeLayout.BELOW, R.id.txt_his_message);

                    holder.mMessage.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {

                            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("message", holder.mMessage.getText());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(mContext, "Copied !", Toast.LENGTH_SHORT).show();

                            return true;
                        }
                    });

                } else if (msg.getType().equals("image")) {

                    holder.mMessage.setVisibility(View.INVISIBLE);
                    holder.mMessageImage.setVisibility(View.VISIBLE);
                    holder.mMessage.setText("");

                    Picasso.get()
                            .load(msg.getMessage())
                            .placeholder(R.drawable.default_avatar)
                            .error(R.drawable.default_avatar)
                            .into(holder.mMessageImage);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.mTime.getLayoutParams();
                    params.removeRule(RelativeLayout.BELOW);
                    params.addRule(RelativeLayout.BELOW, R.id.img_his_message);

                }
                Picasso.get()
                        .load(chatUser.getThumbImage())
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(holder.mProfileImage);
                holder.mTime.setText(GetTimeAgo.getTime(msg.getTimestamp()));

            }
        }
    }


    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class MyMessagesViewHolder extends RecyclerView.ViewHolder {

        private ImageView mMessageImage;
        private TextView mMessage, mTime;

        public MyMessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            mMessage = itemView.findViewById(R.id.txt_my_message);
            mMessageImage = itemView.findViewById(R.id.img_my_message);
            mTime = itemView.findViewById(R.id.txt_my_time);

            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mTime.getVisibility() == View.GONE)
                        mTime.setVisibility(View.VISIBLE);
                    else
                        mTime.setVisibility(View.GONE);
                }
            };

            itemView.setOnClickListener(clickListener);
            mMessage.setOnClickListener(clickListener);

        }
    }

    public class HisMessagesViewHolder extends RecyclerView.ViewHolder {

        private ImageView mProfileImage, mMessageImage;
        private TextView mMessage, mTime;

        public HisMessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            mProfileImage = itemView.findViewById(R.id.his_profile_image);
            mMessage = itemView.findViewById(R.id.txt_his_message);
            mTime = itemView.findViewById(R.id.txt_his_time);
            mMessageImage = itemView.findViewById(R.id.img_his_message);

            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mTime.getVisibility() == View.GONE)
                        mTime.setVisibility(View.VISIBLE);
                    else
                        mTime.setVisibility(View.GONE);
                }
            };

            itemView.setOnClickListener(clickListener);
            mMessage.setOnClickListener(clickListener);


        }
    }
}
