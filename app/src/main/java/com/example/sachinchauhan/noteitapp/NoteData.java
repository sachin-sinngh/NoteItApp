package com.example.sachinchauhan.noteitapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by SachinChauhan on 7/6/2017.
 */

public class NoteData implements Parcelable{
    String title,note;
    int id;

    int itemHeight;

    public NoteData(){}

    public NoteData(int Id, String Title, String Note){
        this.id=Id;
        this.title=Title;
        this.note=Note;
    }

    public NoteData(String Title, String Note){
        this.title=Title;
        this.note=Note;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    protected NoteData(Parcel in) {
        title = in.readString();
        note = in.readString();
        id=in.readInt();
    }

    public static final Creator<NoteData> CREATOR = new Creator<NoteData>() {
        @Override
        public NoteData createFromParcel(Parcel in) {
            return new NoteData(in);
        }

        @Override
        public NoteData[] newArray(int size) {
            return new NoteData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(note);
        dest.writeInt(id);
    }

    public int getItemHeight() {
        return itemHeight;
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
    }
}
