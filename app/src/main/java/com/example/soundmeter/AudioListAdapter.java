package com.example.soundmeter;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.AudioViewHolder> {

    private File[] allFiles;
    private TimeAgo timeAgo;

    private boolean onClick = false;

    private onItemListClick onItemListClick;

    public AudioListAdapter(File[] allFiles, onItemListClick onItemListClick) {
        this.allFiles = allFiles;
        this.onItemListClick = onItemListClick;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item, parent, false);
        timeAgo = new TimeAgo();
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        holder.listTitle.setText(allFiles[position].getName());
        holder.listDate.setText(timeAgo.getTimeAgo(allFiles[position].lastModified()));
        if(onClick) {
            holder.uploadAudio.setImageResource(R.drawable.ic_check_box_black_32dp);
            Log.d("change","background");
        }

    }

    @Override
    public int getItemCount() {
        return allFiles.length;
    }

    public class AudioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView listImage;
        private TextView listTitle;
        private TextView listDate;
        private ImageButton uploadAudio;
        private ImageButton deleteAudio;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);

            listImage = itemView.findViewById(R.id.list_image_view);
            listTitle = itemView.findViewById(R.id.list_title);
            listDate = itemView.findViewById(R.id.list_date);
            uploadAudio = itemView.findViewById(R.id.uploadAudio);
            deleteAudio = itemView.findViewById(R.id.delete);

            itemView.setOnClickListener(this);
            uploadAudio.setOnClickListener(this);
            deleteAudio.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.uploadAudio:
                    onItemListClick.onUpload(allFiles[this.getLayoutPosition()]);
                    uploadAudio.setImageResource(R.drawable.ic_check_box_black_32dp);
                    onClick = true;
                    break;

                case R.id.delete:
                    onItemListClick.onDelete(allFiles[this.getLayoutPosition()]);
                    break;

                default:
                    onItemListClick.onClickListener(allFiles[getAdapterPosition()], getAdapterPosition());
            }

        }
    }

    public interface onItemListClick {
        void onClickListener(File file, int position);
        void onUpload(File file);
        void onDelete(File file);
    }
}
