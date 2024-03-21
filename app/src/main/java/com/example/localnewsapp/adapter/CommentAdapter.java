package com.example.localnewsapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localnewsapp.R;
import com.example.localnewsapp.config.ApplicationConfig;
import com.example.localnewsapp.model.CommentList;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {

    private Context context;
    private List<CommentList> commentList;

    public CommentAdapter(Context context , List<CommentList> commentList){
        this.context = context;
        this.commentList = commentList;
    }

    public void setFilteredList(List<CommentList> filteredList) {
        this.commentList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment , parent , false);
        return new CommentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
        CommentList comment = commentList.get(position);
        if(comment.isAnonymous()) {
            holder.name.setText(ApplicationConfig.APP_ANONYMOUS_NAME);
        }
        else {
            holder.name.setText(comment.getName());
        }
        holder.comment.setText(comment.getComment());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class CommentHolder extends RecyclerView.ViewHolder{

        TextView name , comment;

        public CommentHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_tv);
            comment = itemView.findViewById(R.id.comment_tv);

        }
    }
}
