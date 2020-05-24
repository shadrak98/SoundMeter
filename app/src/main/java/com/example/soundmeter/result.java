package com.example.soundmeter;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

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
    int i = 1;

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

        context = getContext();

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
//                progress.dismiss();
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 10000);

        Log.d("result1", String.valueOf(dBvalue));
        dBValue.setText("85");
//        progress.dismiss();
    }


    public void uploadFile(String fileName) {  //method calling from AudioListFragment
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("_audio", fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ConnectionManager connectionManager = new ConnectionManager();

        if(progress != null) { //progress is null
            progress.dismiss();
            dBValue.setText("22");
        }

        FragmentActivity activity = getActivity();
        if(activity != null && isAdded()) { // activity is null, not attached to fragment

            RequestQueue requestQueue = Volley.newRequestQueue(activity);
            String URL = connectionManager.ipaddress + "/uploadfile";

            ConnectionManager.sendData(jsonObject.toString(), requestQueue, URL, new ConnectionManager.volleyCallback() {
                @Override
                public void onSuccessResponse(String result) {
                    Log.d("VolleySuccess", result);
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Error", String.valueOf(error));
                }
            });
        } else {
            Log.d("Else","activity null");
//            dBValue.setText("22");   // dBValue is null (Textview)
        }

    }

}
