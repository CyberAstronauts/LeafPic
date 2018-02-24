package org.horaapps.leafpic.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.ExifInterface;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.IOException;

/**
 * Created by dnld on 3/25/17.
 */

public class BitmapUtils {
    public static Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    public static Bitmap getCroppedBitmap(Bitmap srcBmp){
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()){
            dstBmp = Bitmap.createBitmap(srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2, 0,
                    srcBmp.getHeight(), srcBmp.getHeight()
            );
        } else {
            dstBmp = Bitmap.createBitmap(srcBmp, 0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(), srcBmp.getWidth()
            );
        }
        return dstBmp;
    }

    public static int getOrientation(String imgPath){
        ExifInterface exif;
        try {
            exif = new ExifInterface( imgPath );
            int orientation = exif.getAttributeInt( ExifInterface.TAG_ORIENTATION, 1 );
            int rotation = 0;
            switch ( orientation ) {
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = SubsamplingScaleImageView.ORIENTATION_180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = SubsamplingScaleImageView.ORIENTATION_90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = SubsamplingScaleImageView.ORIENTATION_270;
                    break;
                default:
                    rotation = SubsamplingScaleImageView.ORIENTATION_0;
                    break;
            }
            return rotation;
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return 0;
    }
}
