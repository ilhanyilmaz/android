package com.digitalwonders.ilhan.spherify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


public class EditActivity extends Activity {

    private AppFunctions appFunctions;
    private Bitmap bitmap;
    private Uri bitmapUri;
    private ImageView imageView;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        init();
    }

    private void init(){

        appFunctions = new AppFunctions();
        Intent intent = getIntent();
        //String imagePath = intent.getStringExtra(MainActivity.SPHERIFY_IMAGE_PATH);

        bitmapUri = Uri.parse(intent.getStringExtra(MainActivity.SPHERIFY_IMAGE_PATH));
        imagePath = appFunctions.getRealPathFromURI(bitmapUri,getContentResolver());

        imageView = (ImageView) findViewById(R.id.imageView);
        Button spherifyButton = (Button) findViewById(R.id.buttonSpherify);
        spherifyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startSpherifyActivity();
            }
        });
        if(bitmap == null) {
            bitmap = appFunctions.loadImage(imagePath, getApplicationContext(), false);
            displayBitmap(bitmap);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
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
    protected void onResume() {
        super.onResume();
        if(imagePath != "") {
            bitmap = appFunctions.loadImage(imagePath, getApplicationContext(), false);
            displayBitmap(bitmap);
        }
        /*Toast toast;
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        String text = "resuming activity";
        toast = Toast.makeText(context, text, duration);
        toast.show();*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*Toast toast;
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        String text = "pausing activity";
        toast = Toast.makeText(context, text, duration);
        toast.show();*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        imageView.setImageDrawable(null);
        bitmap.recycle();
        //bitmap.recycle();
        /*Toast toast;
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        String text = "pausing activity";
        toast = Toast.makeText(context, text, duration);
        toast.show();*/
    }

    protected void displayBitmap(Bitmap bm) {
        // find the imageview and draw it!

        imageView.setImageBitmap(bm);
    }
    protected void startSpherifyActivity() {

        Intent intent = new Intent(this, SpherifyActivity.class);
        intent.putExtra(MainActivity.SPHERIFY_IMAGE_PATH, bitmapUri.toString());

        startActivity(intent);

    }


}
