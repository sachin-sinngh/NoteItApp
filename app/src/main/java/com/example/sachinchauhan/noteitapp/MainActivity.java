package com.example.sachinchauhan.noteitapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MyRecyclerViewAdapter.CallBack{

    List<NoteData> noteDataList = new ArrayList<>();
    MyRecyclerViewAdapter mAdapter;
    public static final int EDIT_NOTE_ACTIVITY = 100;
    public static final int ADD_NOTE_ACTIVITY = 101;
    int index;
    DataBaseHandler dbHelper;
    RecyclerView MyRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Note It");

        dbHelper = new DataBaseHandler(this);
        new GetAllNotes().execute();


        mAdapter = new MyRecyclerViewAdapter(this,noteDataList, new CustomItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                NoteData noteData = noteDataList.get(position);
                Log.v("---LISTENER----TITLE", noteData.getTitle());
                Log.v("------LISTENER----ID",""+ noteData.getId());
                index = noteDataList.indexOf(noteData);
                Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                intent.putExtra("Edit", noteData);
                startActivityForResult(intent, EDIT_NOTE_ACTIVITY);
            }
        });
        MyRecyclerView = (RecyclerView) findViewById(R.id.myRecyclerView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        MyRecyclerView.setLayoutManager(mLayoutManager);
       // MyRecyclerView.setItemAnimator(new DefaultItemAnimator());
        MyRecyclerView.setAdapter(mAdapter);
        mAdapter.setUndoOn(true);
        setUpItemTouchHelper();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //setContentView(R.layout.);
        getMenuInflater().inflate(R.menu.list_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.add) {
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            intent.putExtra("requestCode",100);
            startActivityForResult(intent, ADD_NOTE_ACTIVITY);
        }
        if(id==R.id.info){
            Toast toast=Toast.makeText(this,"   Swipe Right to Delete\nUndo Lasts for 3 seconds only   ",Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP|Gravity.RIGHT,0,100);
            toast.show();
        }

        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == ADD_NOTE_ACTIVITY && resultCode == RESULT_OK) {

                NoteData new_Note_data = data.getParcelableExtra("Save");
                new AddNote().execute(new_Note_data);
            }
            if (requestCode == EDIT_NOTE_ACTIVITY && resultCode == RESULT_OK) {
                if(data.hasExtra("Save")) {
                    NoteData newNotedata = data.getParcelableExtra("Save");
                    Log.v("------TITLE----", newNotedata.getTitle());
                    Log.v("-------ID----", "" + newNotedata.getId());
                    new EditNote().execute(newNotedata);
                }
                else {
                    NoteData oldNotedata = data.getParcelableExtra("Delete");
                    new DeleteNote().execute(oldNotedata);
                    noteDataList.remove(index);
                    mAdapter.notifyDataSetChanged();
                   }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void CatchDeletedNote(NoteData noteData) {
        new DeleteNote().execute(noteData);
        mAdapter.notifyItemRangeChanged(0,mAdapter.getItemCount());
    }


    public class GetAllNotes extends AsyncTask<Void, Void, Boolean>{

        @Override
        protected Boolean doInBackground(Void... params) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String selectQuery = "SELECT * FROM " + dbHelper.TABLE_NOTES;
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    NoteData noteData = new NoteData();
                    noteData.setId(Integer.parseInt(cursor.getString(0)));
                    Log.v("------CURSOR-----",""+Integer.parseInt(cursor.getString(0)));
                    noteData.setTitle(cursor.getString(1));
                    noteData.setNote(cursor.getString(2));
                    noteDataList.add(noteData);
                } while (cursor.moveToNext());
            }

            cursor.close();
            Log.i("BACKGROUND-----------", "GET ALL NOTES");

            return noteDataList.isEmpty();
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            if (!bool)
                mAdapter.notifyDataSetChanged();
        }
    }

    public class AddNote extends AsyncTask<NoteData, Void, NoteData> {
        @Override
        protected NoteData doInBackground(NoteData... params) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            NoteData noteData = params[0];
            ContentValues values = new ContentValues();
            values.put(dbHelper.KEY_TITLE, noteData.getTitle());
            values.put(dbHelper.KEY_NOTE, noteData.getNote());

            long id=db.insert(dbHelper.TABLE_NOTES, null, values);
            if(id==-1)
            Log.i("IN ADDNOTE", "------ BACKGROUND-----ERROR");
            else
                noteData.setId((int)id);
            return noteData;
        }
        @Override
        protected void onPostExecute(NoteData noteData){
             noteDataList.add(noteData);
            mAdapter.notifyDataSetChanged();
            MyRecyclerView.scrollToPosition(noteDataList.size()-1);
        }
    }

    public class EditNote extends AsyncTask<NoteData,Void,NoteData>{

        @Override
        protected NoteData doInBackground(NoteData... params) {

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                NoteData noteData =params[0];

                ContentValues values = new ContentValues();
                values.put(dbHelper.KEY_TITLE, noteData.getTitle());
                values.put(dbHelper.KEY_NOTE, noteData.getNote());
                int id= noteData.getId();
            Log.v("---BACKGROUND---ID", String.valueOf(id));
            // updating row
                db.update(dbHelper.TABLE_NOTES,
                        values,
                        dbHelper.KEY_ID + " =?",
                        new String[]{String.valueOf(id)});
                return noteData;
        }
        @Override
        protected void onPostExecute(NoteData noteData){
                noteDataList.set(index, noteData);
                mAdapter.notifyDataSetChanged();
        }
    }

    public class DeleteNote extends AsyncTask<NoteData,Void,Void> {

        @Override
        protected Void doInBackground(NoteData... params) {
            NoteData noteData = params[0];
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int id = noteData.getId();
            Log.v("IN DELETE--------ID::", "" + id);
            db.delete(dbHelper.TABLE_NOTES, dbHelper.KEY_ID + " =?",
                    new String[]{String.valueOf(id)});
            return null;
        }

    }
    private void setUpItemTouchHelper() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            Drawable background;
            Drawable xMark;
            int xMarkMargin;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                xMark = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete_red_24px);
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int) MainActivity.this.getResources().getDimension(R.dimen.ic_clear_margin);
                initiated = true;
            }
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
           return false;
        }

       @Override
       public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

           int position = viewHolder.getAdapterPosition();
           MyRecyclerViewAdapter adapter = (MyRecyclerViewAdapter) recyclerView.getAdapter();
           if (adapter.isUndoOn() && adapter.isPendingRemoval(position)) {
               return 0;
           }
           return super.getSwipeDirs(recyclerView, viewHolder);
       }

       @Override
       public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
           int swipedPosition = viewHolder.getAdapterPosition();
           MyRecyclerViewAdapter adapter=(MyRecyclerViewAdapter) MyRecyclerView.getAdapter();
           boolean undoOn = adapter.isUndoOn();
           if (undoOn) {
               adapter.pendingRemoval(swipedPosition);

           }
       }

       @Override
       public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                               float dX, float dY, int actionState, boolean isCurrentlyActive) {

           View itemView = viewHolder.itemView;
           if (viewHolder.getAdapterPosition() == -1) {
                return;
           }
           if (!initiated) {
                init();
           }

           background.setBounds(itemView.getLeft() - (int)dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
           background.draw(c);

            int itemHeight = itemView.getBottom() - itemView.getTop();
            int intrinsicWidth = xMark.getIntrinsicWidth();
            int intrinsicHeight = xMark.getIntrinsicWidth();

            int xMarkLeft = itemView.getLeft() + xMarkMargin;
            int xMarkRight = itemView.getLeft() +xMarkMargin+intrinsicWidth;
            int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight)/2;
            int xMarkBottom = xMarkTop + intrinsicHeight;
            xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

            xMark.draw(c);

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
       }

        };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(MyRecyclerView);
    }

    private void setUpAnimationDecoratorHelper() {

        MyRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                initiated = true;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

                if (!initiated) {
                    init();
                }

                if (parent.getItemAnimator().isRunning()) {

                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one from the middle

                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    // this is fixed
                    int left = parent.getWidth();
                    int right =0;

                    // this we need to find out
                    int top = 0;
                    int bottom = 0;

                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(c);

                }
                super.onDraw(c, parent, state);
            }

        });
    }
}