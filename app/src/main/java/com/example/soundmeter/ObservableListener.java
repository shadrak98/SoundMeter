package com.example.soundmeter;

import android.util.Log;

public class ObservableListener {

    private result.ChangeListener changeListener;
    private Double value;

    public void setOnChangeListener(result.ChangeListener listener){
        this.changeListener = listener;
    }

    public Double get(){
        return value;
    }

    public void set(Double value) {
        this.value = value;
        Log.d("ObservableListener", String.valueOf(changeListener));
        if(changeListener != null) {
            changeListener.onChange(value);
        }
    }

}
