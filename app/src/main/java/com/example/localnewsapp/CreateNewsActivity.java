package com.example.localnewsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.localnewsapp.config.ApplicationConfig;
import com.example.localnewsapp.utils.VolleyMultipartRequest;
import com.example.localnewsapp.utils.VolleySingleton;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateNewsActivity extends AppCompatActivity {

    private LinearLayout layoutMiscellaneous;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int CAMERA_REQUEST = 1888;
    private static final int REQUEST_PERMISSIONS = 100;
    private static final int PICK_IMAGE_REQUEST =1 ;
    private String filePath;
    private Bitmap bitmap;
    private EditText news_title;
    private EditText news_subtitle;
    private EditText news_content;
    private ImageView saveButton;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_news);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create News");
        actionBar.setDisplayHomeAsUpEnabled(true);

        layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        saveButton = findViewById(R.id.imageSave);
        news_title = findViewById(R.id.inputNewsTitle);
        news_subtitle = findViewById(R.id.inputNewsSubtitle);
        news_content = findViewById(R.id.inputNewsText);
        requestQueue = VolleySingleton.getmInstance(this).getRequestQueue();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSubmit();
            }
        });
        initMiscellaneous();
    }

    private void initMiscellaneous() {
        //final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if(hasNecessaryPermissions()) {
                selectImage();
            } else {
                Toast.makeText(this, "Not enough permission for this operation", Toast.LENGTH_SHORT).show();
            }
        });

        layoutMiscellaneous.findViewById(R.id.layoutCaptureImage).setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if(hasNecessaryPermissions()) {
                openCamera();
            } else {
                Toast.makeText(this, "Not enough permission for this operation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void openCamera() {

        /*Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);*/
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            requestCameraPermission();
        } else {
            // Permission is granted
            startCameraIntent();
        }
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(CreateNewsActivity.this,
                new String[]{android.Manifest.permission.CAMERA},
                REQUEST_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                startCameraIntent();
            } else {
                // Permission denied
                Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("ACTION_FROM_CAMERA", "I am here from camera");

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri picUri = data.getData();
            filePath = getPath(picUri);
            if (filePath != null) {
                try {

                    //textView.setText("File Selected");
                    Log.d("filePath", String.valueOf(filePath));
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), picUri);
                    //imageSelected.setImageBitmap(bitmap);
                    Toast.makeText(this, "Image Selected", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                Toast.makeText(this,"No image selected", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            bitmap = (Bitmap) data.getExtras().get("data");
            filePath = "Not file path, captured image with camera";
            Toast.makeText(this, "Image captured and Selected", Toast.LENGTH_SHORT).show();
        }

    }
    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        if(document_id != null) {
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        }
        else {
            Toast.makeText(this, "This Image is not supported", Toast.LENGTH_SHORT).show();
            return null;
        }
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }


    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }


    public void doSubmit() {

        if (news_title.getText().toString().isEmpty() || news_content.getText().toString().isEmpty() || filePath == null) {
            Toast.makeText(this, "All credentials are required", Toast.LENGTH_SHORT).show();
        }
        else {
            //Initializing progress  indicator
            ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Posting news...");
            mDialog.show();

            String url = ApplicationConfig.BASE_URL.concat("News");
            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    mDialog.hide();
                    String resultResponse = new String(response.data);
                    // parse success output
                    if (response.statusCode == 200) {
                        Toast.makeText(CreateNewsActivity.this, "News posted successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CreateNewsActivity.this, MainActivity.class));
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mDialog.hide();
                    error.printStackTrace();
                    Toast.makeText(CreateNewsActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    SharedPreferences preferences = getSharedPreferences(ApplicationConfig.APP_PREFERENCE_NAME, MODE_PRIVATE);
                    int user_id = preferences.getInt("user_id", 0);

                    params.put("Title", news_title.getText().toString());
                    params.put("Status", String.valueOf(1));
                    params.put("Description", news_content.getText().toString());
                    params.put("CreatedByUserId", String.valueOf(user_id));

                    return params;
                }

                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    long imagename = System.currentTimeMillis();
                    params.put("Image", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                    return params;
                }
            };

            multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            this.requestQueue.add(multipartRequest);
        }
    }

    private boolean hasNecessaryPermissions() {
        boolean permission = false;
        if ((ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(CreateNewsActivity.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(CreateNewsActivity.this,
                    android.Manifest.permission.CAMERA))) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        REQUEST_PERMISSIONS);
            }
        } else {
            Log.e("Else", "Else");
            permission = true;
        }

        return permission;
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
}