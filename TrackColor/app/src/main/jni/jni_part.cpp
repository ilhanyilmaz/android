#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <stdio.h>

//#include "motionTrackerHist.h"

using namespace std;
using namespace cv;
 

int toGray(Mat img, Mat& gray);
int removeBackground(Mat img, Mat& foreground, double color[3], int offset);
int toHsv(Mat img, Mat& hsv);
int prepImg(Mat rgba, Mat& rgb, int width, int height);
int resizeImg(Mat src, Mat& dst, int width, int height);
int processDiffImg(Mat & diffImg);
int trackBiggestMovingObject(Mat & img, Mat & diffImg, Mat & trackImg);

#ifdef __cplusplus
extern "C" {
#endif




JNIEXPORT jint JNICALL Java_com_digitalwonders_ilhan_trackcolor_MainActivity_cTrackBiggestObj(JNIEnv*, jobject, jlong addrImg, jlong addrDiffImg, jlong addrTrackImg);
JNIEXPORT jint JNICALL Java_com_digitalwonders_ilhan_trackcolor_MainActivity_cTrackBiggestObj(JNIEnv*, jobject, jlong addrImg, jlong addrDiffImg, jlong addrTrackImg){

        Mat& mImg = *(Mat*)addrImg;
        Mat& mDiff = *(Mat*)addrDiffImg;
        Mat& mTrack = *(Mat*)addrTrackImg;

        int conv;
        jint retVal;
        conv = trackBiggestMovingObject(mImg, mDiff, mTrack);
        retVal = (jint)conv;
        return retVal;

}

JNIEXPORT jint JNICALL Java_com_digitalwonders_ilhan_trackcolor_MainActivity_cPrepImg(JNIEnv*, jobject, jlong addrRgba, jlong addrRgbSmall, jint width, jint height);
JNIEXPORT jint JNICALL Java_com_digitalwonders_ilhan_trackcolor_MainActivity_cPrepImg(JNIEnv*, jobject, jlong addrRgba, jlong addrRgbSmall, jint width, jint height){
        Mat& mRgba = *(Mat*)addrRgba;
        Mat& rgbSmall = *(Mat*)addrRgbSmall;

        int conv;
        jint retVal;


        //conv = toGray(mRgb, mGray);

        conv = prepImg(mRgba, rgbSmall, width, height);
        retVal = (jint)conv;

        return retVal;

}

JNIEXPORT jint JNICALL Java_com_digitalwonders_ilhan_trackcolor_MainActivity_cResizeImg(JNIEnv*, jobject, jlong addrSrc, jlong addrDst, jint width, jint height);
JNIEXPORT jint JNICALL Java_com_digitalwonders_ilhan_trackcolor_MainActivity_cResizeImg(JNIEnv*, jobject, jlong addrSrc, jlong addrDst, jint width, jint height){

        Mat& mSrc = *(Mat*)addrSrc;
        Mat& mDst = *(Mat*)addrDst;

        int conv;
        jint retVal;
        conv = resizeImg(mSrc, mDst, width, height);
        retVal = (jint)conv;
        return retVal;

}
JNIEXPORT jint JNICALL Java_com_digitalwonders_ilhan_trackcolor_MainActivity_cProcessDiffImg(JNIEnv*, jobject, jlong addrDiff);
JNIEXPORT jint JNICALL Java_com_digitalwonders_ilhan_trackcolor_MainActivity_cProcessDiffImg(JNIEnv*, jobject, jlong addrDiff){

        Mat& mDiff = *(Mat*)addrDiff;

        int conv;
        jint retVal;
        conv = processDiffImg(mDiff);
        retVal = (jint)conv;
        return retVal;
}

JNIEXPORT jint JNICALL Java_com_digitalwonders_ilhan_trackcolor_MainActivity_trackColor(JNIEnv*, jobject, jlong addrRgba, jlong addrGray, jlong jr, jlong jg, jlong jb, jint offset);
JNIEXPORT jint JNICALL Java_com_digitalwonders_ilhan_trackcolor_MainActivity_trackColor(JNIEnv*, jobject, jlong addrRgba, jlong addrGray, jlong jr, jlong jg, jlong jb, jint offset){
        Mat& mRgb = *(Mat*)addrRgba;
        Mat& mGray = *(Mat*)addrGray;

        int conv;
        jint retVal;


        //conv = toGray(mRgb, mGray);
        double* color = new double[3];
        color[0] = (double)jr;
        color[1] = (double)jg;
        color[2] = (double)jb;

        conv = removeBackground(mRgb, mGray, color, (int)offset);
        retVal = (jint)conv;

        return retVal;

}

JNIEXPORT jint JNICALL Java_com_digitalwonders_ilhan_trackcolor_MainActivity_convertNativeGray(JNIEnv*, jobject, jlong addrRgba, jlong addrGray);
JNIEXPORT jint JNICALL Java_com_digitalwonders_ilhan_trackcolor_MainActivity_convertNativeGray(JNIEnv*, jobject, jlong addrRgba, jlong addrGray) {
 
    Mat& mRgb = *(Mat*)addrRgba;
    Mat& mGray = *(Mat*)addrGray;
    
    int conv;
    jint retVal;    

    conv = toGray(mRgb, mGray);
    retVal = (jint)conv;
 
    return retVal;

}

JNIEXPORT jint JNICALL Java_com_digitalwonders_ilhan_trackcolor_MainActivity_rgb2hsv(JNIEnv*, jobject, jlong addrRgba, jlong addrHsv);
JNIEXPORT jint JNICALL Java_com_digitalwonders_ilhan_trackcolor_MainActivity_rgb2hsv(JNIEnv*, jobject, jlong addrRgba, jlong addrHsv) {

    Mat& mRgb = *(Mat*)addrRgba;
    Mat& mHsv = *(Mat*)addrHsv;

    int conv;
    jint retVal;

    conv = toHsv(mRgb, mHsv);
    retVal = (jint)conv;

    return retVal;

}
}

int resizeImg(Mat src, Mat& dst, int width, int height)
{
    resize(src, dst, Size(width, height));

    if (dst.rows == height && dst.cols == width)
    {
        return (1);
    }
    return(0);

}
int prepImg(Mat rgba, Mat& rgbSmall, int width, int height)
{
    Mat rgb;
    cvtColor(rgba, rgb, CV_RGBA2RGB); // Assuming RGBA input
    resize(rgb, rgbSmall, Size(width, height));

    //bgrBig.release();

    if (rgbSmall.rows == height && rgbSmall.cols == width)
    {
        return (height);
    }
    return(0);

}
int toHsv(Mat img, Mat& hsv)
{

    cvtColor(img, hsv, CV_BGR2HSV); // Assuming RGBA input

    if (hsv.rows == img.rows && hsv.cols == img.cols)
    {
        return (1);
    }
    return(0);
}

int toGray(Mat img, Mat& gray)
{
    cvtColor(img, gray, CV_RGBA2GRAY); // Assuming RGBA input
    
    if (gray.rows == img.rows && gray.cols == img.cols)
    {
        return (1);
    }
    return(0);
}


int removeBackground(Mat img, Mat& foreground, double color[3], int offset) {


    	Scalar lowerb = Scalar(color[0], color[1], color[2], 0);
    	Scalar upperb = Scalar(color[0], color[1], color[2], 255);

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

    	/*lowerb.val[2] = 0;
    	upperb.val[2] = 255;*/

    	inRange(img, lowerb, upperb, foreground);
    	return (1);
}
int processDiffImg(Mat & diffImg)
{

		Size kernelOpen(2,2);
        Size kernelClose(5,5);
        Mat element = getStructuringElement( MORPH_ELLIPSE, kernelOpen);

        erode(diffImg, diffImg, element);
        dilate(diffImg, diffImg, element);

        element = getStructuringElement( MORPH_RECT, kernelClose);
        morphologyEx( diffImg, diffImg, MORPH_CLOSE, element );

        //threshold(diffImg, diffImg, 1, 255, CV_THRESH_BINARY_INV);

}

int trackBiggestMovingObject(Mat & img, Mat & diffImg, Mat & trackImg)
{

    Mat canny_output;
    vector<vector<Point> > contours;
    //vector<Rect> rectangles;
    double biggestContourArea = 0;
    double cContourArea = 0;
    Rect biggestContourRect(0,0,0,0);
    /// Detect edges using canny
    Canny( diffImg, trackImg, 100, 200, 3 );
    /// Find contours
    //findContours( trackImg, contours, RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);
    findContours( diffImg, contours, RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);

    //vector<Rect>::iterator it;
    //it = rectangles.begin();
    for( int i = 0; i< contours.size(); i++ )
    {
        cContourArea = contourArea(contours[i]);
        if(cContourArea<30)
            continue;
        //drawContours( frame, contours, i, 255 );

        if(cContourArea > biggestContourArea) {
            biggestContourArea = cContourArea;
            biggestContourRect = boundingRect(contours[i]);
            continue;
        }
    }
    rectangle(img, Point(biggestContourRect.x,biggestContourRect.y), Point(biggestContourRect.x+biggestContourRect.width,biggestContourRect.y+biggestContourRect.height),Scalar(255,0,0));
    if(biggestContourArea>0)
        return 1;
    else
        return 0;
}
