package com.digitalwonders.ilhan.trackcolor;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.*;
import org.opencv.core.*;


public class MotionTracker {
	protected Size kernelOpen;
	protected Size kernelClose;
	protected int blurValue;
	protected int thresholdLower;
	protected int thresholdHigher;
	protected List<MatOfPoint> contours;
	protected Mat frame;
	protected Mat diffImg;
	protected Mat trackImg;
	protected VideoCapture cap;
	protected boolean showPositions = false;
	protected boolean showDiffImg = false;
	protected boolean showTracker = false;
	protected int minObjectArea;
	protected List<Rect> rectangles;
	protected MatOfPoint biggestContour;


	public MotionTracker() {
	    //def __init__(self, capture, showTrackerImage = False, calibrationFile = None, blur = 2):
		diffImg = new Mat();
		trackImg = new Mat();
		contours = new ArrayList<MatOfPoint>();
		rectangles = new ArrayList<Rect>();
		kernelOpen = new Size(3, 3);
		kernelClose = new Size(12, 5);
		blurValue = 2;
		thresholdHigher= 40;
		thresholdLower = 17;
		minObjectArea = 50;

		//self.showTracker = showTrackerImage
		/*if not calibrationFile == None :
            self.estimator = le.LocationEstimator(calibrationFile)
        if self.showDiffImage:
            self.createDiffImageWindow()
        self.rectangles = None*/
	}	
	public void processDiffFrame() {
		
		Mat tDiffImg = diffImg;
        Mat element;
        
        if(diffImg.empty())
            return;    
            
        Imgproc.threshold(tDiffImg, tDiffImg, 1, 255, Imgproc.THRESH_BINARY);
        element = Imgproc.getStructuringElement( Imgproc.MORPH_ELLIPSE, kernelOpen);
        
        Imgproc.erode(tDiffImg, tDiffImg, element);
        Imgproc.dilate(tDiffImg, tDiffImg, element);
        
        element = Imgproc.getStructuringElement( Imgproc.MORPH_RECT, kernelClose);
        Imgproc.morphologyEx( tDiffImg, diffImg, Imgproc.MORPH_CLOSE, element );
	}
	
	public void findMovingObjects() {

        double biggestContourArea = 0;
        double contourArea;
        biggestContour = null;
        MatOfPoint contour;
        Mat canny_output = new Mat();
        contours.clear();
        contours = new ArrayList<MatOfPoint>();
        /// Detect edges using canny
        Imgproc.Canny( diffImg, canny_output, 100, 200, 3, true );
        /// Find contours
        Mat hierarcy = new Mat();
        Imgproc.findContours( canny_output, contours, hierarcy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        canny_output.release();
        hierarcy.release();

        
        for( int i = 0; i< contours.size(); i++ )
        {
            contour = contours.get(i);
            contourArea = Imgproc.contourArea(contour);
            if(contourArea<minObjectArea)
                continue;

            if(contourArea>biggestContourArea) {
                biggestContourArea = contourArea;
                biggestContour = contour;
            }
            
            Rect rect = Imgproc.boundingRect(contour);
            /*rect = findNonZeroBoundary(diffImg,rect);         Remove shadow
            if(rect.width == 0 || rect.height == 0)
                continue;*/
            rectangles.add(rect);

        }

    }

    /*Rect findNonZeroBoundary(Mat img, Rect rect) {
        
    	int x = rect.x;
        int y = rect.y;
        int w = rect.width;
        int h = rect.height;
        boolean lFound = false;
        boolean rFound = false;
        boolean uFound = false;
        boolean bFound = false;
        int l,r,u,b; // left right up bottom
        l=x;
        r=x+w-1;
        u=y;
        b=y+h-1;
        
        //printf("%i-%i / %i-%i\n",x,y,w,h);
            
        for(int i=0; (!lFound || !rFound) && i<w; i++) {
            if(!lFound)
                l=x+i;
            if(!rFound)
                r=x+w-i-1;
            //printf("x: %i-%i\n",l,r);
            
            if(Core.countNonZero(img.submat(new Rect(l,y,1,h)))>0)
                lFound = true;
            if(Core.countNonZero(img.submat(new Rect(r,y,1,h)))>0)
                rFound = true;
        }
        
        
        for(int i=0; (!uFound || !bFound)&& i<h; i++) {
            if(!uFound)
                u=y+i;
            if(!bFound)
                b=y+h-i-1;
            //printf("y: %i-%i\n",u,b);
            if(Core.countNonZero(img.submat(new Rect(x,u,w,1)))>0)
                uFound = true;
            if(Core.countNonZero(img.submat(new Rect(x,b,w,1)))>0)
                bFound = true;
        }
        
        if(!lFound || !rFound || !uFound || !bFound)
            System.out.println("just shadow\n");
        else if(l==r || b==u)
            System.out.println("just a line\n");
        else
            return new Rect(l,u,r-l,b-u);
        
        return new Rect(0,0,0,0);
    }*/
    
    public void drawRectangles() {
        trackImg = frame.clone();
        for(int i=0; i<rectangles.size();i++) {
            Core.rectangle(trackImg, new Point(rectangles.get(i).x,rectangles.get(i).y), new Point(rectangles.get(i).x+rectangles.get(i).width,rectangles.get(i).y+rectangles.get(i).height),new Scalar(255));
        }
    }
    public Point getBiggestObjectPos() {
        if(biggestContour == null)
            return null;
        Rect biggestObjectRect = Imgproc.boundingRect(biggestContour);
        return new Point(biggestObjectRect.x + biggestObjectRect.width/2,biggestObjectRect.y + biggestObjectRect.height/2);
    }

    public void drawBiggestObject() {
    	if(biggestContour == null)
            return;
    	trackImg = frame.clone();
    	Rect biggestObjectRect = Imgproc.boundingRect(biggestContour);
    	Core.rectangle(trackImg, new Point(biggestObjectRect.x,biggestObjectRect.y), new Point(biggestObjectRect.x+biggestObjectRect.width,biggestObjectRect.y+biggestObjectRect.height),new Scalar(255));
    }
    
    public Point findObject2dBottom(int pos) {
        Rect obj = rectangles.get(pos);
        return new Point(obj.x+obj.width/2,obj.y+obj.height);
    }
    
    public Point findObject2dCenter(int pos) {
        Rect obj = rectangles.get(pos);
        return new Point(obj.x+obj.width/2,obj.y+obj.height/2);
    }
    public Mat getDiffImg() {
    	return diffImg;
    }
    public Mat getTrackImg() {
    	return trackImg;
    }
    public Mat getFrame() {
    	return frame;
    }
    public void setMinObjectArea(int areaSize) {
    	minObjectArea = areaSize;
    }
    public void update() {}
    
}

