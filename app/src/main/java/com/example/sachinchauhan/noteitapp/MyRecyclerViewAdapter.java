package com.example.sachinchauhan.noteitapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by SachinChauhan on 7/6/2017.
 */

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder>{
    List<NoteData> list= Collections.emptyList();
    CustomItemClickListener listener;
    List<NoteData> itemsPendingRemoval;
    boolean UndoOn;
    final int PENDING_REMOVAL_TIMEOUT=3000;
    int itemHeight;

    private android.os.Handler handler=new android.os.Handler();
    private HashMap<NoteData,Runnable> pendingRunnables=new HashMap<>();

    public interface CallBack{
        void CatchDeletedNote(NoteData noteData);
    }
    private CallBack callerActivity;

    public MyRecyclerViewAdapter(Activity activity, List<NoteData> list, CustomItemClickListener listener){
        this.list=list;
        this.listener=listener;
        this.itemsPendingRemoval=new ArrayList<>();
        callerActivity=(CallBack)activity;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView titleView,undoView,deletedView,note_preview;

        public MyViewHolder(View itemView) {
            super(itemView);
            titleView=(TextView)itemView.findViewById(R.id.title);
            note_preview=(TextView)itemView.findViewById(R.id.note_preview);
            undoView=(TextView)itemView.findViewById(R.id.undoView);
            deletedView=(TextView)itemView.findViewById(R.id.deletedTextview);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,parent,false);
        final MyViewHolder myViewHolder=new MyViewHolder(view);
      /*  view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(v,myViewHolder.getAdapterPosition());
            }
        });*/
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final NoteData noteData =list.get(position);

        Log.v("--IN OnBINDViewHolder--","ID--- "+noteData.getId());
        if(itemsPendingRemoval.contains(noteData)){
            holder.itemView.setBackgroundColor(Color.RED);
            holder.titleView.setVisibility(View.GONE);
            holder.note_preview.setVisibility(View.GONE);
            Log.v("ITEM HEIGHT",""+itemHeight);
            holder.deletedView.setHeight(noteData.getItemHeight());
            holder.undoView.setHeight(noteData.getItemHeight());
            holder.deletedView.setVisibility(View.VISIBLE);
            holder.undoView.setVisibility(View.VISIBLE);
            holder.undoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Runnable pendingRemovalRunnable = pendingRunnables.get(noteData);
                    pendingRunnables.remove(noteData);
                    if (pendingRemovalRunnable != null) handler.removeCallbacks(pendingRemovalRunnable);
                    itemsPendingRemoval.remove(noteData);
                    notifyItemChanged(list.indexOf(noteData));
                }
            });
        }else{
            holder.itemView.setBackgroundColor(ContextCompat.getColor((Context)callerActivity,R.color.app_color));
            holder.undoView.setVisibility(View.GONE);
            holder.deletedView.setVisibility(View.GONE);
            holder.titleView.setText(noteData.title);
            String notePreview[]=noteData.note.split(System.getProperty("line.separator"));
            holder.note_preview.setText(notePreview[0]);
            holder.titleView.setVisibility(View.VISIBLE);
            holder.note_preview.setVisibility(View.VISIBLE);
            holder.itemView.measure(0,0);
            itemHeight=holder.itemView.getMeasuredHeight();
            noteData.setItemHeight(itemHeight);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v,position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setUndoOn(boolean undoOn) {
        this.UndoOn = undoOn;
    }

    public boolean isUndoOn() {
        return UndoOn;
    }

    public void pendingRemoval(int position) {
        final NoteData noteData=list.get(position);
        Log.v("--IN pendingRemoval--","ID--- "+noteData.getId());
        if (!itemsPendingRemoval.contains(noteData)) {
            itemsPendingRemoval.add(noteData);

            notifyItemChanged(list.indexOf(noteData));

            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    remove(list.indexOf(noteData));
                }
            };
            handler.postDelayed(pendingRemovalRunnable,PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(noteData, pendingRemovalRunnable);
        }
    }

    public void remove(int position) {
        NoteData noteData=list.get(position);
        Log.v("--IN remove--","ID--- "+noteData.getId());
        if (itemsPendingRemoval.contains(noteData)) {
            itemsPendingRemoval.remove(noteData);
        }
        if (list.contains(noteData)) {
            list.remove(noteData);
            notifyItemRemoved(position);
            callerActivity.CatchDeletedNote(noteData);
        }
    }
    public boolean isPendingRemoval(int position) {
        NoteData noteData = list.get(position);
        Log.v("--IN isPendingRemoval--","ID--- "+noteData.getId());
        return itemsPendingRemoval.contains(noteData);
    }
}
