package com.digitalwonders.ilhan.spherify;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;

/**
 * Created by ilhan on 21.02.2015.
 */
public class Spherify{
    private Mat image;
    private Mat spherifiedImage;

    public Spherify() {

    }


    private int imageSize = 640;
    private int maxSize = 1280;

    private Point getAngleDist(int i, int j) {
        Point returnVals = new Point();
        int halfSize = imageSize / 2;
        int x = i - halfSize;
        int y = j - halfSize;
        double angle = Math.atan2(y, x); ////////////////////////

        if (angle > 0)
            angle = angle * 180.0 / Math.PI;
        else
            angle = 360 + angle * 180.0 / Math.PI;

        if (angle == 360)
            angle = 0;


        double dist = Math.hypot(x, y);
        returnVals.x = angle;
        returnVals.y = dist;
        return returnVals;
    }

    private Point getUniformCoordinates(double angle, double dist) {


        Point uv = new Point();
        uv.y = (dist/(imageSize/2));
        uv.y = uv.y*uv.y;
        //uv.y = 1-(dist/(imageSize/2));
        //uv.y = uv.y*uv.y;
        //uv.y = 1- uv.y;
        uv.x = (angle/360);
        return uv;
    }

    public Mat rotateImage(Mat image, int angle) {
        int cols = image.cols();
        int rows = image.rows();
        Mat M = Imgproc.getRotationMatrix2D(new Point(cols / 2, rows / 2), angle, 1);
        //Mat dst = new Mat();
        Imgproc.warpAffine(image, image, M, new Size(cols, rows));
        return image;
    }
    public Bitmap spherifyIt(Bitmap bitmap) {

        if(image == null) {
            image = new Mat();
            image.create(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC3);
            Bitmap myBitmap32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Utils.bitmapToMat(myBitmap32, image);

            Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB,4);
        }

        Utils.bitmapToMat(bitmap, image);
        int width = image.cols();
        int height = image.rows();
        //#image = rotateImage(image, 270)
        if(height / 2 > imageSize)
            imageSize = height / 2;
        if(imageSize > maxSize)
            imageSize = maxSize;
        //#scaleFactorX = width / imageSize
        //#scaleFactorY =  height / imageSize
        //#print str(width) + " - " + str(height)
        int halfSize = imageSize / 2;
        Mat newImg = new Mat();
        newImg.create(imageSize, imageSize, CvType.CV_8UC4);
        int i=0, j=0;
        Point angleDist;
        Point uv;
        int x,y;
        for(j=0; j<imageSize; j++) {
            for(i=0; i<imageSize; i++) {
                angleDist = getAngleDist(i, j);
                uv = getUniformCoordinates(angleDist.x, angleDist.y);
                if(uv.x < 1 && uv.y < 1){
                    x = (int) (uv.x * width);
                    y = (int) (height - uv.y * height - 1);
                    newImg.put(j,i,image.get(y,x));
                }
            }
        }
        newImg = rotateImage(newImg, 270);

        spherifiedImage = newImg;
        return getSpherifiedBitmap();
    }

    public boolean loadImage(String filepath) {
        image = Highgui.imread(filepath);

        return !image.empty();

    }
    private Bitmap convertToBitmap(Mat img) {
        // convert to bitmap:
        /*if(img == null)
            return null;
        Mat tmp = new Mat();
        Imgproc.cvtColor(img, tmp, Imgproc.COLOR_BGR2RGB);

        Bitmap bm = Bitmap.createBitmap(tmp.cols(), tmp.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmp, bm);*/
        Bitmap bm = Bitmap.createBitmap(img.cols(), img.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bm);
        return bm;
    }
    public Bitmap getSpherifiedBitmap() {
        return convertToBitmap(spherifiedImage);
    }

    public void destroy(){
        if(image != null)
            image.release();
        if(spherifiedImage != null)
            spherifiedImage.release();
    }
}
