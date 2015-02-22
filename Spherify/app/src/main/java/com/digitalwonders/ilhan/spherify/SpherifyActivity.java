package com.digitalwonders.ilhan.spherify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Random;


public class SpherifyActivity extends Activity {

    private Spherify spherify;
    private Bitmap bitmap;
    private AppFunctions appFunctions;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spherify);
        init();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spherify, menu);
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

    @Override
    protected void onStop() {
        super.onStop();
        spherify.destroy();
        bitmap.recycle();

    }

    private void init(){

        appFunctions = new AppFunctions();

        Button saveButton = (Button) findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveIt();
            }
        });
        imageView = (ImageView) findViewById(R.id.imageView);
        Intent intent = getIntent();
        Uri imageUri = Uri.parse(intent.getStringExtra(MainActivity.SPHERIFY_IMAGE_PATH));
        String imagePath = appFunctions.getRealPathFromURI(imageUri, getContentResolver());


        spherify = new Spherify();


        bitmap = appFunctions.loadImage(imagePath, getApplicationContext(), true);


        spherifyIt();
    }
    protected void saveIt() {
        Toast toast;
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        String text = "saving it! Please wait...";
        toast = Toast.makeText(context, text, duration);
        toast.show();

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/spherified");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void spherifyIt() {
        Toast toast;
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        String text = "Spherifying it! Please wait...";
        toast = Toast.makeText(context, text, duration);
        toast.show();

        new SpherifyTast().execute(bitmap);

    }

    protected void displayBitmap(Bitmap bm) {
        // find the imageview and draw it!
        bitmap.recycle();
        bitmap = bm;
        imageView.setImageBitmap(bm);
    }

    private class SpherifyTast extends AsyncTask<Bitmap, Void, Bitmap> {
        /** The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute() */
        protected Bitmap doInBackground(Bitmap... bitmaps) {
            return spherify.spherifyIt(bitmaps[0]);
        }

        /** The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground() */
        protected void onPostExecute(Bitmap result) {
            displayBitmap(result);
        }
    }


}
