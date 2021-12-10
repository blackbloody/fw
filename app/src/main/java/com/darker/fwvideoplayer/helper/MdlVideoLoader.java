package com.darker.fwvideoplayer.helper;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.darker.fwvideoplayer.constant.ESortStyle;
import com.darker.fwvideoplayer.models.MdlVideo;

import java.util.ArrayList;
import java.util.List;

public class MdlVideoLoader extends AsyncTaskLoader<List<MdlVideo>> {

    private static final String TAG = "MdlVideoLoader";
    private Context context;
    private List<MdlVideo> mCache;
    private VideoObserver videoObserver;

    private ESortStyle mainSortStyle;

    public MdlVideoLoader(@NonNull Context context, ESortStyle mainSortStyle) {
        super(context);
        this.context = context;
        this.mainSortStyle = mainSortStyle;
    }

    @Nullable
    @Override
    public List<MdlVideo> loadInBackground() {

        List<MdlVideo> toReturn = new ArrayList<>();

        String[] projections = { MediaStore.MediaColumns.DATA, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media._ID,
                MediaStore.Video.Thumbnails.DATA, MediaStore.Video.VideoColumns.DURATION, MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DATE_MODIFIED };
        String selection = MediaStore.Video.Media.MIME_TYPE + " =video/quicktime";

        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";
        switch (mainSortStyle) {
            case DATE_NEW:
                sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";
                break;
            case DATE_OLD:
                sortOrder = MediaStore.Video.Media.DATE_ADDED + " ASC";
                break;
            case SIZE_LARGE:
                sortOrder = MediaStore.Video.Media.SIZE + " DESC";
                break;
            case SIZE_SMALL:
                sortOrder = MediaStore.Video.Media.SIZE + " ASC";
                break;
            case DURATION_LONGEST:
                sortOrder = MediaStore.Video.Media.DURATION + " DESC";
                break;
            case DURATION_SHORTEST:
                sortOrder = MediaStore.Video.Media.DURATION + " ASC";
                break;
        }

        Cursor c = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projections, null, null, sortOrder);
        if (c != null && c.moveToFirst()){
            int id = c.getColumnIndex(MediaStore.Video.Media._ID);
            int title = c.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
            int duration = c.getColumnIndex(MediaStore.Video.VideoColumns.DURATION);
            int type = c.getColumnIndex(MediaStore.Video.Media.MIME_TYPE);
            int thumbnail = c.getColumnIndex(MediaStore.Video.Thumbnails.DATA);
            int path = c.getColumnIndex(MediaStore.MediaColumns.DATA);
            int createdDate = c.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED);

            do {
                MdlVideo mdl = new MdlVideo(c.getString(title), c.getString(duration),
                        c.getString(type), c.getString(thumbnail), c.getString(path), c.getString(createdDate));
                toReturn.add(mdl);
            }while (c.moveToNext());
        }

        return toReturn;
    }

    @Override
    public void deliverResult(@Nullable List<MdlVideo> data) {
        if (isReset())
            return;
        this.mCache = data;

        if (isStarted())
            super.deliverResult(data);
    }

    @Override
    protected void onStartLoading() {
        Handler h = new Handler(Looper.getMainLooper());
        if (mCache != null)
            deliverResult(mCache);
        if (videoObserver == null)
            videoObserver = new VideoObserver(h, this);
        if (takeContentChanged() || mCache == null)
            forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        if (videoObserver != null){
            context.getContentResolver().unregisterContentObserver(videoObserver);
            videoObserver = null;
        }
    }

    static class VideoObserver extends ContentObserver{

        private Loader loader;

        private VideoObserver(Handler handler, Loader loader) {
            super(handler);
            this.loader = loader;
        }

        @Override
        public void onChange(boolean selfChange) {
            loader.onContentChanged();
        }
    }
}
