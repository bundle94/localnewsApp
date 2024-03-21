package com.example.localnewsapp.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.localnewsapp.R;
import com.example.localnewsapp.adapter.NewsAdapter;
import com.example.localnewsapp.config.ApplicationConfig;
import com.example.localnewsapp.model.CreatedBy;
import com.example.localnewsapp.model.News;
import com.example.localnewsapp.utils.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private RequestQueue requestQueue;
    private List<News> newsList;
    private Context context;
    private SearchView searchView;
    private NewsAdapter newsAdapter;
    SwipeRefreshLayout swipeRefreshLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        getActivity().setTitle("Home");
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.refreshLayout);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        searchView = (SearchView) view.findViewById(R.id.searchView);
        searchView.clearFocus();
        context = view.getContext();
        return view;
    }

    @Override
    public void onViewCreated(View view,
                              Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        // Refresh  the layout
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                        //Clear the moviesList for the new record that will be fetched
                        newsList = new ArrayList<>();
                        fetchNews();

                        //Set this to false to avoid multiple refresh
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );

        requestQueue = VolleySingleton.getmInstance(context).getRequestQueue();

        newsList = new ArrayList<>();
        fetchNews();
    }

    //Search filter implementation
    private void filterList(String text){
        if(!text.isEmpty()) {
            List<News> filteredNewsList = new ArrayList<>();
            for (News news : newsList) {
                if (news.getTitle().toLowerCase().contains(text.toLowerCase())) {
                    filteredNewsList.add(news);
                }
            }

            if (filteredNewsList.isEmpty()) {
                Toast.makeText(context, "No data found", Toast.LENGTH_SHORT).show();
            } else {
                newsAdapter.setFilteredList(filteredNewsList);
            }
        }
    }

    //Fetch the movies
    private void fetchNews() {

        ProgressDialog mDialog = new ProgressDialog(context);
        mDialog.setMessage("Fetching movies...");
        mDialog.show();

        String url = ApplicationConfig.BASE_URL.concat("News");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mDialog.dismiss();
                        try {
                            if (response.getString("code").equals("SUCCESS")) {
                                JSONArray news = response.getJSONArray("data");
                                for (int i = 0 ; i < news.length() ; i ++){
                                    JSONObject jsonObject = news.getJSONObject(i);
                                    String title = jsonObject.getString("title");
                                    String description = jsonObject.getString("description");
                                    String image = jsonObject.getString("image");
                                    String status = jsonObject.getString("status");
                                    int id = jsonObject.getInt("id");
                                    JSONObject createdBy = jsonObject.getJSONObject("createdBy");
                                    int userId = createdBy.getInt("id");
                                    String fullName = createdBy.getString("fullName");
                                    String email = createdBy.getString("email");
                                    String createdAt = createdBy.getString("createdAt");
                                    CreatedBy createdByUser = new CreatedBy(userId, fullName, email, createdAt);
                                    News newsObj = new News(id, title, description, status, image, createdByUser);
                                    newsList.add(newsObj);

                                    newsAdapter = new NewsAdapter(context , newsList);

                                    recyclerView.setAdapter(newsAdapter);
                                }
                            } else {
                                Toast.makeText(context, "An error occurred while fetching news", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //for (int i = 0 ; i < response.length() ; i ++){
                            /*try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String title = jsonObject.getString("title");
                                String overview = jsonObject.getString("description");
                                String poster = jsonObject.getString("poster");
                                Double rating = jsonObject.getDouble("rating");
                                int id = jsonObject.getInt("id");
                                String genre = jsonObject.getString("genre");
                                String releaseDate = jsonObject.getString("release_date");
                                String casts = jsonObject.getString("casts");

                                Movie movie = new Movie(title , poster , overview , rating, id, genre, releaseDate, casts);
                                movieList.add(movie);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }*/


                            newsAdapter = new NewsAdapter(context , newsList);

                            recyclerView.setAdapter(newsAdapter);
                        //}
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mDialog.dismiss();
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        //Put the request in volley queue
        requestQueue.add(jsonObjectRequest);
    }
}