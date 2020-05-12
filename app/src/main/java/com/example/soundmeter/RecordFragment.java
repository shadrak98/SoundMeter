package com.example.soundmeter;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
public class RecordFragment extends Fragment implements View.OnClickListener {

    private NavController navController;
    private ImageButton listBtn;
    private ImageButton recordBtn;

    private boolean isRecording = false;
    private String recordPermissions = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;

    private MediaRecorder mediaRecorder;
    private String recordFile;

    private Chronometer timer;

    private TextView fileName;

    private String ipaddress = "192.168.43.142";
    private String port = "5000";
    private String recordPath;

    private File recordAudioFile;

    private Uri audioFileUri;

    StorageReference storageReference;

    public RecordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        listBtn = view.findViewById(R.id.record_list_btn);
        recordBtn = view.findViewById(R.id.record_btn);
        timer = view.findViewById(R.id.record_timer);
        fileName = view.findViewById(R.id.record_filename);

        listBtn.setOnClickListener(this);
        recordBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.record_list_btn:
                if(isRecording) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    alertDialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            navController.navigate(R.id.action_recordFragment2_to_audioListFragment2);
                            isRecording = false;
                        }
                    });
                    alertDialog.setNegativeButton("CANCEL", null);
                    alertDialog.setTitle("Audio Still Recording");
                    alertDialog.setMessage("Are you sure, you want to stop recording?");
                    alertDialog.create().show();
                } else {
                    navController.navigate(R.id.action_recordFragment2_to_audioListFragment2);
                }
                break;

            case R.id.record_btn:
                if(isRecording) {
                    //Stop Recording
                    stopRecording();
                    recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_off_black_90dp, null));
                    isRecording = false;
                } else {
                    //Start Recording
                    if(checkPermissions()) {
                        startRecording();
                        recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_none_black_90dp, null));
                        isRecording = true;
                    }
                }
                break;
        }
    }

    private void startRecording() {
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();

        recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.ENGLISH);
        Date now = new Date();
        recordFile = "Recording_"+ format.format(now) +".3gp";

        fileName.setText("Recording, FileName : " + recordFile);

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath + "/" + recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setMaxDuration(30000);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();
    }

    private void stopRecording() {
        timer.stop();

        fileName.setText("Recording Stopped, File Saved : " + recordFile);

        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;

        recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();
    }

    public void uploadFile(File audioFile) {

        // Sending to Firebase
        audioFileUri = Uri.fromFile(audioFile);

        storageReference = FirebaseStorage.getInstance().getReference().child("random");
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

                Log.d("Flask Server",response.body().string());

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                        TextView responseText = findViewById(R.id.responseText);
//                        try {
////                            responseText.setText(response.body().string());
//                            Log.d("Flask Server",response.body().string());
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
            }
        });
    }

    private boolean checkPermissions() {
        if(ActivityCompat.checkSelfPermission(getContext(), recordPermissions) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{recordPermissions}, PERMISSION_CODE);
            return false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isRecording) {
            stopRecording();
        }
    }
}
