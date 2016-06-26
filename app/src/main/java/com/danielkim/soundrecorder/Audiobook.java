package com.danielkim.soundrecorder;

import android.os.Parcel;
import android.os.Parcelable;

public class Audiobook implements Parcelable {
    private int id; //id in database
    private String name; // alias of an audiobook
    private String filePath; //file path
    private long position; // last played position
    private long duration; // audiobook duration
    private long lastOpened; // date/time when audiobook was last played

    public Audiobook() {
    }

    public Audiobook(Parcel in) {
        id = in.readInt();
        name = in.readString();
        filePath = in.readString();
        position = in.readLong();
        duration = in.readLong();
        lastOpened = in.readLong();
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public long getLastOpened() {
        return lastOpened;
    }

    public void setLastOpened(long lastOpened) {
        this.lastOpened = lastOpened;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public static final Creator<Audiobook> CREATOR = new Creator<Audiobook>() {
        public Audiobook createFromParcel(Parcel in) {
            return new Audiobook(in);
        }

        public Audiobook[] newArray(int size) {
            return new Audiobook[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(filePath);
        dest.writeLong(position);
        dest.writeLong(duration);
        dest.writeLong(lastOpened);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}