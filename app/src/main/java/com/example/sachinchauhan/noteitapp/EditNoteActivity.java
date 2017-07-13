package com.example.sachinchauhan.noteitapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.Touch;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by SachinChauhan on 7/6/2017.
 */

public class EditNoteActivity extends AppCompatActivity{
    EditText title_edit;
    EditText note_edit;
    Intent launchingIntent;
    NoteData noteData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Note");

        title_edit = (EditText) findViewById(R.id.edit_title);
        note_edit = (EditText) findViewById(R.id.edit_note);

        launchingIntent = getIntent();
        if (launchingIntent.hasExtra("Edit")) {
            Bundle bundle = launchingIntent.getExtras();
            noteData = (NoteData) bundle.get("Edit");
            title_edit.setText(noteData.getTitle());
            note_edit.setText(noteData.getNote());
            Log.v("IN EDITACTIVITY-----",""+ noteData.getId());
        }
        else{
            noteData =new NoteData();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //setContentView(R.layout.);
        getMenuInflater().inflate(R.menu.edit_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
            if(id==R.id.save) {
                if (title_edit.getText().toString().isEmpty())
                    Toast.makeText(this, "   Title Can't Be Empty!!!   ", Toast.LENGTH_LONG).show();

                else {
                    noteData.setTitle(title_edit.getText().toString());
                    noteData.setNote(note_edit.getText().toString());

                    launchingIntent.putExtra("Save", noteData);
                    setResult(RESULT_OK, launchingIntent);
                    finish();
                }
            }
            if(id==R.id.delete_edit)
            {
                if(launchingIntent.hasExtra("requestCode"))
                    Toast.makeText(this,"  Press Back to Discard  ",Toast.LENGTH_SHORT).show();
                else {
                    launchingIntent.putExtra("Delete", noteData);
                    setResult(RESULT_OK, launchingIntent);
                    finish();
                }
            }
        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }
}
