package com.digitalwonders.ilhan.trackcolor;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
//import org.opencv.imgproc.Imgproc;

public class MotionTrackerHSV extends MotionTracker {

	private List<double[]> colors;
	private int offset;
	//public MotionTrackerHist(VideoCapture _cap, List<String> posHistFile=List<string>(), vector<string> negHistFile=vector<string>(), bool _showTracker=false, bool _showDiffImg=false):MotionTracker(_cap, _showTracker, _showDiffImg) {
	public MotionTrackerHSV() {
		super();
		colors = new ArrayList<double[]>();
		offset = 15;
    }
    
    void update(Mat _frame) {
        /*Mat diffImageHSV;
        Mat diffImageRGB = new Mat();*/
    	
        frame = _frame;
        //Imgproc.blur(frame, frame, new Size(blurValue, blurValue));
        diffImg.release();
        diffImg = removeBackground(frame);
        //Imgproc.cvtColor(diffImageHSV, diffImg, Imgproc.COLOR_HSV2BGR);
        //Imgproc.cvtColor(diffImageRGB, diffImg, Imgproc.COLOR_BayerGR2GRAY);
    }
    void setColor(int hue, int sat, int val) {
    	if(colors.size()== 0)
    		return;
    	colors.get(0)[0] = hue;
    	colors.get(0)[1] = sat;
    	colors.get(0)[2] = val;
    }
    void setOffset(int _offset) {
    	offset = _offset;
    }
    Mat removeBackground(Mat image) {
    	Mat foreground = new Mat();
    	if(colors.size()==0)
    		return image;
    	
    	
    	Scalar lowerb = new Scalar(colors.get(0));
    	Scalar upperb = new Scalar(colors.get(0));
    	
    	lowerb.val[0] = lowerb.val[0] - offset;
    	if(lowerb.val[0]<0)
    		lowerb.val[0] = 0;
    	upperb.val[0] = upperb.val[0] + offset;
    	
    	lowerb.val[1] = lowerb.val[1] - offset;
    	if(lowerb.val[1]<0)
    		lowerb.val[1] = 0;
    	upperb.val[1] = upperb.val[1] + offset;
    	if(upperb.val[1]>255)
    		upperb.val[1] = 255;
    	
    	lowerb.val[2] = lowerb.val[2] - offset;
    	if(lowerb.val[2]<0)
    		lowerb.val[2] = 0;
    	upperb.val[2] = upperb.val[2] + offset;
    	if(upperb.val[2]>255)
    		upperb.val[2] = 255;
    	
    	
    	/*lowerb.val[0] = 30;
    	upperb.val[0] = 180;
    	lowerb.val[1] = 30;
    	upperb.val[1] = 255;
    	lowerb.val[2] = 0;
    	upperb.val[2] = 255;*/
    	Core.inRange(image, lowerb, upperb, foreground);
    	return foreground;
    	/*List<Mat> images = new ArrayList<Mat>();
        int discValue = 10;
        int thresholdValue = 1;
        Mat hsvt = new Mat();
        Imgproc.cvtColor(image, hsvt, Imgproc.COLOR_BGR2HSV);
        images.add(hsvt);
        Mat backproj = new Mat();
        Mat disc;
        Mat thresh = new Mat();
        List<Mat> mats = new ArrayList<Mat>();
        Mat mask = new Mat();
        Mat foreground = new Mat();
        
        MatOfFloat ranges = new MatOfFloat(0,180,0,256);
        MatOfInt channels = new MatOfInt(0,1);	
        
        
        Imgproc.calcBackProject( images, channels, posHistogram, backproj, ranges, 1);
        
        disc = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new Size(discValue,discValue));
        Imgproc.filter2D(backproj, backproj, -1,disc);
        
        Imgproc.threshold(backproj, thresh, thresholdValue, 255, Imgproc.THRESH_BINARY);
        mats.add(thresh);
        mats.add(thresh);
        mats.add(thresh);
        
        Core.merge(mats, mask);
        Core.bitwise_and(image,mask,foreground);
        
        
        return foreground;*/
    }

	public void addColor(double[] ds) {
		
    	colors.add(ds);
	}
    
}
