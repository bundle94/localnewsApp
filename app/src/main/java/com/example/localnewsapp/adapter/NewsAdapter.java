package com.example.localnewsapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.localnewsapp.DetailsActivity;
import com.example.localnewsapp.R;
import com.example.localnewsapp.model.News;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsHolder>{
    private Context context;
    private List<News> newsList;

    public NewsAdapter(Context context , List<News> news){
        this.context = context;
        newsList = news;
    }

    public void setFilteredList(List<News> filteredList) {
        this.newsList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item , parent , false);
        return new NewsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsHolder holder, int position) {

        News news = newsList.get(position);
        holder.rating.setText(news.getCreatedBy().getFullName());
        holder.title.setText(news.getTitle());
        holder.overview.setText(news.getDescription());
        Glide.with(context).load(news.getImage()).into(holder.imageView);

        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context , DetailsActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString("title" , news.getTitle());
                bundle.putString("description" , news.getDescription());
                bundle.putString("image" , news.getImage());
                bundle.putString("createdBy" , news.getCreatedBy().getFullName());
                bundle.putInt("id", news.getId());
                bundle.putString("postedDate", news.getCreatedBy().getCreatedAt());

                intent.putExtras(bundle);

                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public class NewsHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView title , overview , rating;
        ConstraintLayout constraintLayout;

        public NewsHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageview);
            title = itemView.findViewById(R.id.title_tv);
            overview = itemView.findViewById(R.id.overview_tv);
            rating = itemView.findViewById(R.id.rating);
            constraintLayout = itemView.findViewById(R.id.main_layout);
        }
    }
}
