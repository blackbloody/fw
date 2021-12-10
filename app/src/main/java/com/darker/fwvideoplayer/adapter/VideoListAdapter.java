package com.darker.fwvideoplayer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.darker.fwvideoplayer.R;
import com.darker.fwvideoplayer.models.MdlVideo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoListViewHolder> {

    private static final String TAG = "VideoListAdapter";
    private Context context;
    private List<MdlVideo> listItem;
    private OnSelected selected;

    public VideoListAdapter(Context context, List<MdlVideo> listItem) {
        this.context = context;
        this.listItem = listItem;
        selected = (OnSelected)context;
    }

    public interface OnSelected{
        void OnClickItem(MdlVideo video, List<MdlVideo> listItem, int position);
    }

    @NonNull
    @Override
    public VideoListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VideoListViewHolder vh, int position) {
        MdlVideo item = listItem.get(position);

        vh.tvName.setText(item.getTitle());

        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("MMM dd");
        long d = Long.parseLong(item.getCreatedDate());
        Date date = new Date(d);
        String dateS = df.format(date);

        vh.tvDate.setText(dateS);
        String path =  item.getPath().replace("/storage/emulated/0/","").replace(item.getTitle(),"");
        vh.tvFolder.setText(path.substring(0, path.length() - 1));

        long dur = Long.parseLong(item.getDuration());
        @SuppressLint("DefaultLocale") String titlDur = String.format("%02d.%02d.%02d",
                TimeUnit.MILLISECONDS.toHours(dur),
                TimeUnit.MILLISECONDS.toMinutes(dur) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(dur)),
                TimeUnit.MILLISECONDS.toSeconds(dur) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(dur)));
        if (titlDur.contains("00")){
            String checkHourZero = titlDur.substring(0, 2);
            if (checkHourZero.contains("00")){
                titlDur =  titlDur.substring(3);
            }
        }
        vh.tvDuration.setText(titlDur.replace(".", ":"));
    }

    @Override
    public int getItemCount() {
        return listItem == null ? 0 : listItem.size();
    }

    public class VideoListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        CardView lMain;
        TextView tvName,tvDate, tvFolder, tvDuration;

        public VideoListViewHolder(View view)
        {
            super(view);

            lMain = view.findViewById(R.id.item_main);
            tvName = view.findViewById(R.id.tv_name);
            tvDate = view.findViewById(R.id.tv_date);
            tvFolder = view.findViewById(R.id.tv_folder);
            tvDuration = view.findViewById(R.id.tv_duration);

            lMain.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final MdlVideo video = listItem.get(getAdapterPosition());
            selected.OnClickItem(video, listItem, getAdapterPosition());
        }
    }
}
