package com.example.soundmeter;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class result extends Fragment {

    private TextView filename;
    public TextView dBValue = null;

    private int recordID;
    private String fileName, recordFile;
    private Double dBvalue;

    private String ipaddress = "192.168.43.142";
    private String port = "5000";

    private RecordFragment recordFragment;

    private Uri audioFileUri;
    StorageReference storageReference;
    private Double value;
    ProgressDialog progress;
    Context context;

    public result() {
        // Required empty public constructor
    }

//    public result(Context mContext){
//        this.context = mContext;
//    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        filename = (TextView) view.findViewById(R.id.filename);
        dBValue = (TextView) view.findViewById(R.id.dBvalue);

        recordFragment = new RecordFragment();
        recordFile = recordFragment.recordFile;

        filename.setText("Shadrak");
        dBValue.setText("58");

        progress = new ProgressDialog(getContext());
        progress.setTitle("Loading");
        progress.setMessage("Wait till loading...");
        progress.setCancelable(false);
        progress.show();

        Runnable progressRunnable = new Runnable() {

            @Override
            public void run() {
                progress.cancel();
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 30000);

        Log.d("result1", String.valueOf(dBvalue));

    }

    public void onResponse(Double value){
        Log.d("resultclass", String.valueOf(value));
        dBValue.setText(String.valueOf(value));
    }

    public void uploadFile(File audioFile) {

        // Sending to Firebase
        audioFileUri = Uri.fromFile(audioFile);

        storageReference = FirebaseStorage.getInstance().getReference("data");
        storageReference.putFile(audioFileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        audioFileUri = uri;
                        Log.d("uripath", String.valueOf(audioFileUri));
//                        Toast.makeText(getActivity(this), "uploaded", Toast.LENGTH_SHORT).show();
                        connectserver(String.valueOf(audioFileUri));
                    }
                });
            }
        });
    }

    private void connectserver(String URI) {
        // Sending to Flask Server
        String postURL = "http://"+ipaddress+":"+port+"/uploadfile";

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("_audio", recordFile,
                        RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), URI))
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
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                String lol = response.body().string();
                value = Double.valueOf(lol);
                Log.d("Flask Server",lol);

                Log.d("Response", String.valueOf(value));
            }
        });
    }

}
