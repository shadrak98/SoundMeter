package com.example.soundmeter;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AsyncTaskClass extends AsyncTask<File, Void, Double> {

    ProgressDialog progress;
    result result;
    StorageReference storageReference;
    Uri audioFileUri;
    String postURL, recordFile;
    RecordFragment recordFragment;
    Double dbvalue;

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
//        result  = new result();
//        progress = new ProgressDialog(result.requireContext());
//        progress.setTitle("Loading");
//        progress.setMessage("Wait while loading...");
//        progress.setCancelable(false);
//        progress.show();
    }

    @Override
    protected Double doInBackground(File... files) {
        // Sending to Firebase
        audioFileUri = Uri.fromFile(files[0]);

        storageReference = FirebaseStorage.getInstance().getReference("Audio");
        storageReference.putFile(audioFileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        audioFileUri = uri;
                        Log.d("uripath", String.valueOf(audioFileUri));
//                        connectserver(String.valueOf(audioFileUri));
                    }
                });
            }
        });

        recordFragment = new RecordFragment();
        recordFile = recordFragment.recordFile;

        String postURL = "http://192.168.43.142:5000/uploadfile";

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_audio", recordFile,
                        RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), String.valueOf(audioFileUri)))
                .build();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postURL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                Log.d("Flask Server","Failed to connect to server");
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.d("Flask Server","Failed to connect to server");
//                    }
//                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                String lol = response.body().string();
                dbvalue = Double.valueOf(lol);
                Log.d("Flask Server",lol);
//                Log.d("Response", String.valueOf(dbvalue));

                Log.d("value", String.valueOf(dbvalue));
            }
        });

        return dbvalue;
    }

    @Override
    protected void onPostExecute(Double value){
        super.onPostExecute(value);
        result = new result();
//        result.dBValue.setText(String.valueOf(value));
        Log.d("Asynctask", String.valueOf(value));
//        result.onResponse(value);
    }
}

