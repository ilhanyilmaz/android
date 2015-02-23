package com.digitalwonders.ilhan.trackcolor;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

//import android.support.v7.app.ActionBarActivity;


public class MainActivity extends Activity implements CvCameraViewListener2, SensorEventListener {

    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;
    private TextView             tvXAxis;
    private TextView             tvYAxis;
    private TextView             tvZAxis;
    private TextView             tv4;
    private TextView             tv5;
    private TextView             tv6;
    private SensorManager       mSensorManager;
    private Sensor mRotationVector;
    private float[]             rotationVector;

    private LinearLayout cameraDisplayLayout;
    private Mat                    mRgba;
    private MotionTrackerHSV	mtb;
    private int lastX = 0;
    private int lastY = 0;
    private float screenImageRatio;
    private int seekBarHue =0;
    private int seekBarSat =0;
    private int seekBarVal =0;
    private int seekBarOffset =15;
    private SeekBar seekBar;
    private SeekBar seekBar2;
    private SeekBar seekBar3;
    private SeekBar seekBar4;
    private boolean newRoiEvent = false;
    private int maxWidth = 960;
    private int maxHeight = 720;
    private int displayWidth = 1080;
    private int displayHeight = 810;
    private int processWidth = 480;
    private int processHeight = 360;

    private static final int NORMAL = 0;
    private static final int HSV = 1;
    private static final int DIFF = 2;
    private static final int TRACKING = 3;
    private int DISPLAY_MODE = 0;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        if (mIsJavaCamera)
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        else
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_native_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setMaxFrameSize(maxWidth, maxHeight);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        rotationVector = new float[3];

        initUI();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onDestroy()
    {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemSwitchCamera = menu.add("Toggle Native/Java camera");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        String toastMessage = new String();
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mItemSwitchCamera) {
            mOpenCvCameraView.setVisibility(SurfaceView.GONE);
            mIsJavaCamera = !mIsJavaCamera;

            if (mIsJavaCamera) {
                mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
                toastMessage = "Java Camera";
            } else {
                mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_native_surface_view);
                toastMessage = "Native Camera";
            }

            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
            mOpenCvCameraView.enableView();
            Toast toast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
            toast.show();
        }

        return true;
    }

    public void onCameraViewStarted(int width, int height) {
        mtb = new MotionTrackerHSV();
        double[] color = new double[3];
        color[0] = seekBarHue;
        color[1] = seekBarSat;
        color[2] = seekBarVal;
        mtb.addColor(color);

        //Display display = getWindowManager().getDefaultDisplay();
        //Point size = new Point();
        //display.getSize(size);


        screenImageRatio = (float) (processHeight/ ((float)displayHeight));
    }

    public void onCameraViewStopped() {
    }

    private boolean startProcessing = false;
    private Point objectCenter;
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        Mat mHsv = new Mat();
        Mat mRgbaSmall = new Mat();
        Mat mRgbSmall = new Mat();
        Mat mDisplay;

        if(!startProcessing)
            return inputFrame.rgba();
        try {
            //mRgba.release();
            mRgba = inputFrame.rgba().clone();
            Imgproc.resize(mRgba, mRgbaSmall, new Size(processWidth,processHeight));
            Imgproc.cvtColor(mRgbaSmall, mRgbSmall, Imgproc.COLOR_BGRA2BGR);
            mRgbaSmall.release();
            Imgproc.cvtColor(mRgbSmall, mHsv, Imgproc.COLOR_BGR2HSV);

            if(newRoiEvent) {
                newRoiEvent = false;
                //mtb.addColor(mHsv.get(lastX, lastY));
                double hsvValues[] = mHsv.get(lastY, lastX);
                seekBarHue = (int) hsvValues[0];
                seekBarSat = (int) hsvValues[1];
                seekBarVal = (int) hsvValues[2];
                seekBar.setProgress(seekBarHue);
                seekBar2.setProgress(seekBarSat);
                seekBar3.setProgress(seekBarVal);
            }
            mRgbSmall.release();

            mtb.setColor(seekBarHue, seekBarSat, seekBarVal);
            mtb.setOffset(seekBarOffset);

            //mtb.update(mRgbSmall);
            mtb.update(mHsv);
            //
            mtb.processDiffFrame();
            mtb.findMovingObjects();
            mtb.drawBiggestObject();
            objectCenter = mtb.getBiggestObjectPos();


            //mtb.drawRectangles();

            switch (DISPLAY_MODE) {
                case HSV:
                    mDisplay = mHsv;
                    break;
                case DIFF:
                    mDisplay =  mtb.getDiffImg();
                    break;
                case TRACKING:
                    mDisplay = mtb.getTrackImg();
                    break;
                default:
                    mDisplay = inputFrame.rgba();
            }

            Imgproc.resize(mDisplay, mRgba, new Size(maxWidth,maxHeight));

            mHsv.release();
            mtb.getTrackImg().release();
            mtb.getDiffImg().release();
            inputFrame.rgba().release();
            return mRgba;
        }
        catch(Exception e) {
            System.out.printf(e.toString());
            if(!mRgba.empty())
                mRgba.release();
            if(!mRgbaSmall.empty())
                mRgbaSmall.release();
            if(!mRgbSmall.empty())
                mRgbSmall.release();
            if(!mHsv.empty())
                mHsv.release();
            if(!mtb.getTrackImg().empty())
                mtb.getTrackImg().release();
            if(!mtb.getDiffImg().empty())
                mtb.getDiffImg().release();
            return inputFrame.rgba();
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        rotationVector[0] = event.values[0];
        rotationVector[1] = event.values[1];
        rotationVector[2] = event.values[2];

        tvXAxis.setText(Float.toString(rotationVector[0]));
        tvYAxis.setText(Float.toString(rotationVector[1]));
        tvZAxis.setText(Float.toString(rotationVector[2]));

        if(objectCenter != null) {
            tv5.setText(Double.toString(objectCenter.x));
            tv6.setText(Double.toString(objectCenter.y));
        }

    }

    public boolean onTouchEvent(MotionEvent e) {


        lastX = (int) ((e.getX() - cameraDisplayLayout.getLeft()) * screenImageRatio );
        lastY = (int) (e.getY() * screenImageRatio);

        if(lastX >= processWidth || lastY >= processHeight || lastX < 0 || lastY < 0)
            return true;

        tv4.setText(Integer.toString(cameraDisplayLayout.getLeft()));
        //tv5.setText(Integer.toString(lastX));
        //tv6.setText(Integer.toString(lastY));

        startProcessing = true;

        switch (e.getAction()) {
            case MotionEvent.ACTION_UP:
                newRoiEvent = true;
                /*float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                  dx = dx * -1 ;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                  dy = dy * -1 ;
                }

                mRenderer.setAngle(
                        mRenderer.getAngle() +
                        ((dx + dy) * TOUCH_SCALE_FACTOR);  // = 180.0f / 320
                requestRender();*/
        }

        //mPreviousX = x;
        //mPreviousY = y;
        return true;
    }
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioButton:
                if (checked)
                    DISPLAY_MODE = NORMAL;
                    break;
            case R.id.radioButton2:
                if (checked)
                    DISPLAY_MODE = DIFF;
                    break;
            case R.id.radioButton3:
                if (checked)
                    DISPLAY_MODE = TRACKING;
                break;
        }
    }


    private void initUI() {

        cameraDisplayLayout = (LinearLayout) findViewById(R.id.cameraDisplayLayout);
        tvXAxis = (TextView) findViewById(R.id.tvXAxis);
        tvYAxis = (TextView) findViewById(R.id.tvYAxis);
        tvZAxis = (TextView) findViewById(R.id.tvZAxis);
        tv4 = (TextView) findViewById(R.id.tv4);
        tv5 = (TextView) findViewById(R.id.tv5);
        tv6 = (TextView) findViewById(R.id.tv6);
        seekBar = (SeekBar) findViewById(R.id.seekBarHue);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                seekBarHue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }
        });
        seekBar2 = (SeekBar) findViewById(R.id.seekBarSat);
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                seekBarSat = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }
        });
        seekBar3 = (SeekBar) findViewById(R.id.seekBarVal);
        seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                seekBarVal = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }
        });
        seekBar4 = (SeekBar) findViewById(R.id.seekBarOffset);
        seekBar4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                seekBarOffset = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }
        });
    }
    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
    }*/
}
