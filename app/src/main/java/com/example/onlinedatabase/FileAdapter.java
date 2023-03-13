package com.example.onlinedatabase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinedatabase.room.Metadata;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    Context context;
    List<Metadata> list;

    public FileAdapter(Context context, List<Metadata> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_file,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileAdapter.ViewHolder holder, int position) {
        Metadata metadata = list.get(position);
        if (!list.get(position).getSync()){
           holder.sync_img.setVisibility(View.VISIBLE);
        }else {
            holder.sync_img.setVisibility(View.GONE);
        }

        holder.name.setText(metadata.getUrl());

    }

    @Override
    public int getItemCount() {

        return (list != null && list.size() !=0) ? list.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView sync_img, file;
        TextView name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sync_img = itemView.findViewById(R.id.sync_img);
            name = itemView.findViewById(R.id.name);
        }
    }
}
