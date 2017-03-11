package com.momobites.www.inventory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileDescriptor;
import java.io.IOException;

import static com.momobites.www.inventory.Data.InventoryProvider.LOG_TAG;

/**
 * Created by prashant on 3/9/2017.
 */

public class Utility {private Utility() {


}
    public static Bitmap getBitmapFromUri(ImageView imageView, Context context, Uri uri) {

        if (uri == null) {
            return null;
        }

        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {

            parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, opts);
            int photoW = opts.outWidth;
            int photoH = opts.outHeight;

            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            opts.inJustDecodeBounds = false;
            opts.inSampleSize = scaleFactor;
            opts.inPurgeable = true;
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, opts);

            if (image.getWidth() > image.getHeight()) {
                Matrix mat = new Matrix();
                int degree = 90;
                mat.postRotate(degree);
                Bitmap imageRotate = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), mat, true);
                return imageRotate;
            } else {
                return image;
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error closing ParcelFile Descriptor");
            }
        }
    }

}

