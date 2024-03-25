package com.example.localnewsapp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.example.localnewsapp.adapter.CommentAdapter;
import com.example.localnewsapp.config.ApplicationConfig;
import com.example.localnewsapp.model.Comment;
import com.example.localnewsapp.model.CommentList;
import com.example.localnewsapp.utils.VolleySingleton;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private RequestQueue requestQueue;
    private int newsId, user_id;
    private Context context;
    private String mTitle;
    private ImageView imageView;
    private Toolbar toolbar;
    private TextView addComment;
    private EditText comment;
    private List<CommentList> commentArrayList;
    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        toolbar = findViewById(R.id.toolbar);

        recyclerView = findViewById(R.id.commentsRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestQueue = VolleySingleton.getmInstance(this).getRequestQueue();


        Bundle bundle = getIntent().getExtras();

        SharedPreferences preferences = getSharedPreferences(ApplicationConfig.APP_PREFERENCE_NAME, MODE_PRIVATE);
        user_id = preferences.getInt("user_id", 0);

        mTitle = bundle.getString("title");

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(mTitle);
        actionBar.setDisplayHomeAsUpEnabled(true);

        imageView = findViewById(R.id.poster_image);
        TextView name_tv = findViewById(R.id.name_tv);
        TextView description_tv = findViewById(R.id.description_tv);
        TextView posted_tv = findViewById(R.id.posted_tv);

        fab = findViewById(R.id.review_fab);
        context = this;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareContent();
            }
        });

        String image = bundle.getString("image");
        String description = bundle.getString("description");
        String name = bundle.getString("createdBy");
        String postedDate = bundle.getString("postedDate");
        newsId = bundle.getInt("id");

        Glide.with(this).load(image).into(imageView);
        name_tv.setText(name);
        description_tv.setText(description);

        //LocalDateTime dateTime = LocalDateTime.parse(postedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS"));
        // Parse the string to LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(postedDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // Truncate to seconds precision
        LocalDateTime truncatedDateTime = dateTime.withNano(0);
        String output = truncatedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        posted_tv.setText("Posted Date: "+ output);

        commentArrayList = new ArrayList<>();
        fetchComments();
        initAddComment();

    }

    private void shareContent() {
        String textToShare = ApplicationConfig.SHARE_NEWS_WRITE_UP.concat(mTitle);
        String mimeType = "text/plain";

        ShareCompat.IntentBuilder
                .from(this)
                .setType(mimeType)
                .setChooserTitle("Share this content via:")
                .setText(textToShare)
                .startChooser();
    }

    public void initAddComment() {
        final LinearLayout layoutAddComments = findViewById(R.id.layoutAddComments);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutAddComments);
        addComment = layoutAddComments.findViewById(R.id.textAddComment);
        addComment.setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        comment = findViewById(R.id.comment);
        Button submitCommentBtn = findViewById(R.id.submitComment);
        submitCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addComment.performClick();
                submitComment(view);
            }
        });
    }

    private void fetchComments() {


        String url = ApplicationConfig.BASE_URL.concat("News/"+newsId+"/getNewsComments");

        Log.d(TAG, "URL: "+ url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getString("code").equals("SUCCESS")) {
                                if(!response.getString("data").isEmpty()) {
                                    JSONArray data = response.getJSONArray("data");
                                    for (int i = 0; i < data.length(); i++) {
                                        try {
                                            JSONObject jsonObject = data.getJSONObject(i);
                                            String name = jsonObject.getString("commentedBy");
                                            String description = jsonObject.getString("comment");
                                            boolean anonymous = jsonObject.getBoolean("isAnonymous");

                                            CommentList item = new CommentList(name, description, anonymous);
                                            commentArrayList.add(item);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            commentAdapter = new CommentAdapter(DetailsActivity.this, commentArrayList);

                            recyclerView.setAdapter(commentAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(DetailsActivity.this, "Failed to fetch comments", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private void submitComment(View view) {
        ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setMessage("Submitting comment...");
        mDialog.show();

        SharedPreferences preferences = getSharedPreferences(ApplicationConfig.APP_PREFERENCE_NAME, MODE_PRIVATE);
        String full_name = preferences.getString("full_name", "Dummy User");

        //Getting settings preference
        SharedPreferences settingsPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean anonymousSet = settingsPreference.getBoolean("anonymous", false);

        Log.d(TAG, "Anonymous value"+ anonymousSet);

        Comment request = new Comment();
        request.setNewsId(newsId);
        request.setUserId(user_id);
        request.setComment(comment.getText().toString());
        request.setAnonymous(anonymousSet);

        //Converting request to json string
        ObjectMapper Obj = new ObjectMapper();
        String jsonStr = null;
        try {
            jsonStr = Obj.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"Request:"+ jsonStr);
        //Making API call
        String url = ApplicationConfig.BASE_URL.concat("News/"+newsId+"/addCommentToNews");
        JSONObject jsonBody = null;
        try {
            jsonBody = new JSONObject(jsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mDialog.hide();
                        try {
                            if (response.getString("code").equals("SUCCESS")) {
                                Toast.makeText(DetailsActivity.this, "Comment added successfully", Toast.LENGTH_SHORT).show();
                                CommentList commentList = new CommentList(full_name, comment.getText().toString(), anonymousSet);
                                commentArrayList.add(0, commentList);
                                commentAdapter.setFilteredList(commentArrayList);
                                //commentAdapter = new CommentAdapter(DetailsActivity.this, commentArrayList);
                                comment.setText("");
                                hideSoftKeyboard(DetailsActivity.this, view);
                            } else {
                                Toast.makeText(DetailsActivity.this, "Failed to add comment", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mDialog.hide();
                error.printStackTrace();
                Toast.makeText(DetailsActivity.this, "Failed to add comment", Toast.LENGTH_SHORT).show();
            }
        });

        this.requestQueue.add(jsonObjectRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // Handle the back arrow click
                onBackPressed(); // Or call finish() to navigate back
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }
}