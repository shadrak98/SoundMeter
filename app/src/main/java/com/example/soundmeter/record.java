package com.example.soundmeter;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class record {

    private String timeStamp;
    private Double dBvalue;
    private String fileName;

    public record(String timestamp, Double value, String filename) {
        this.timeStamp = timestamp;
        this.dBvalue = value;
        this.fileName = filename;
    }

}