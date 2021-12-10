package com.darker.fwvideoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.darker.fwvideoplayer.R;
import com.darker.fwvideoplayer.models.MdlVideo;

import java.text.MessageFormat;
import java.util.List;

public class VideoGridAdapter extends RecyclerView.Adapter<VideoGridAdapter.VideoGridViewHolder> {

    private Context context;
    private List<MdlVideo> listItem;
    private OnSelected selected;

    public VideoGridAdapter(Context context, List<MdlVideo> listItem) {
        this.context = context;
        this.listItem = listItem;
        selected = (OnSelected)context;
    }

    public interface OnSelected{
        void OnClickItem(MdlVideo video, List<MdlVideo> listItem, int position);
    }

    @NonNull
    @Override
    public VideoGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoGridAdapter.VideoGridViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VideoGridViewHolder vh, int position) {
        MdlVideo item = listItem.get(position);
        vh.tvName.setText(item.getTitle());
        Glide.with(context).load("file://" + item.getThumbnail()).skipMemoryCache(false).into(vh.ivThumb);
    }

    @Override
    public int getItemCount() {
        return listItem != null ? listItem.size() : 0;
    }

    public class VideoGridViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        CardView lMain;
        ImageView ivThumb;
        TextView tvName;

        public VideoGridViewHolder(View view){
            super(view);

            lMain = view.findViewById(R.id.item_main);
            ivThumb = view.findViewById(R.id.iv_thumb);
            tvName = view.findViewById(R.id.tv_name);

            lMain.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final MdlVideo video = listItem.get(getAdapterPosition());
            selected.OnClickItem(video, listItem, getAdapterPosition());
        }
    }
}
