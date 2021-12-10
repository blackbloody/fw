package com.darker.fwvideoplayer.models;

import android.os.Parcel;
import android.os.Parcelable;

public class MdlVideo implements Parcelable {

    private String title;
    private String duration;
    private String type;
    private String thumbnail;
    private String path;
    private String createdDate;

    public MdlVideo(String title, String duration, String type, String thumbnail, String path, String createdDate) {
        this.title = title;
        this.duration = duration;
        this.type = type;
        this.thumbnail = thumbnail;
        this.path = path;
        this.createdDate = createdDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    private MdlVideo(Parcel in) {
        title = in.readString();
        duration = in.readString();
        type = in.readString();
        thumbnail = in.readString();
        path = in.readString();
        createdDate = in.readString();
    }

    public static final Creator<MdlVideo> CREATOR = new Creator<MdlVideo>() {
        @Override
        public MdlVideo createFromParcel(Parcel in) {
            return new MdlVideo(in);
        }

        @Override
        public MdlVideo[] newArray(int size) {
            return new MdlVideo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(duration);
        dest.writeString(createdDate);
        dest.writeString(type);
        dest.writeString(thumbnail);
        dest.writeString(path);
    }

    @Override
    public String toString() {
        return "MdlVideo{" +
                "title='" + title + '\'' +
                ", duration='" + duration + '\'' +
                ", type='" + type + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
