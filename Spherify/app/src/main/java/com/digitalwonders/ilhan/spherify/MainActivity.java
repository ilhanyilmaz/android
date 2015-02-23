package com.digitalwonders.ilhan.spherify;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;


public class MainActivity extends ActionBarActivity {


    private static final int SELECT_PICTURE = 1;
    public final static String SPHERIFY_IMAGE_PATH = "com.digitalwonders.ilhan.spherify.IMAGE_PATH";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selectAnImage();
            }
        });

        // Get the intent that started this activity
        Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            if (extras.containsKey(Intent.EXTRA_STREAM)) {

                Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                Intent intent2 = new Intent(this, EditActivity.class);
                intent2.putExtra(SPHERIFY_IMAGE_PATH, uri.toString());

                startActivity(intent2);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void selectAnImage(){
        Intent pickIntent = new Intent();
        pickIntent.setType("image/*");
        pickIntent.setAction(Intent.ACTION_PICK);

        startActivityForResult(Intent.createChooser(pickIntent, "Select Picture"), SELECT_PICTURE);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == RESULT_OK){

            final Uri imageUri = data.getData();

            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra(SPHERIFY_IMAGE_PATH, imageUri.toString());

            startActivity(intent);

        }
    }
    protected void onResume(Bundle savedInstanceState){

    }
}
