package com.example.soundmeter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class result extends Fragment {

    private TextView filename;
    private TextView dBValue;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    private int recordID;
    private String fileName;
    private Double dBvalue;

    public result() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        filename = view.findViewById(R.id.filename);
        dBValue = view.findViewById(R.id.dBvalue);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        recordID = Integer.parseInt(String.valueOf(databaseReference.child("Last-Id")));

        fileName = String.valueOf(databaseReference.child("Data").child(String.valueOf(recordID)).child("fileName"));
        dBvalue = Double.valueOf(String.valueOf(databaseReference.child("Data").child(String.valueOf(recordID)).child("dBvalue")));

        filename.setText(fileName);
        dBValue.setText(String.valueOf(dBvalue));

    }
}
