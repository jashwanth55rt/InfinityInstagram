package com.infinity.app.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helpers to load + compress images before upload.
 * Compressing on-device keeps Firebase Storage bills small and uploads fast.
 */
public final class ImageUtils {
    private ImageUtils() {}

    /**
     * Decodes the picked image and returns a JPEG byte[] suitable for upload.
     *
     * @param maxDim     longest side after resize (e.g. 1080 for posts, 512 for avatars)
     * @param qualityPct JPEG quality, 0-100 (e.g. 80)
     */
    public static byte[] compress(Context ctx, Uri uri, int maxDim, int qualityPct) throws IOException {
        // First, decode bounds only, so we can compute an inSampleSize for memory-efficient decoding.
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream s = ctx.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(s, null, bounds);
        }
        int inSample = 1;
        int largest = Math.max(bounds.outWidth, bounds.outHeight);
        while (largest / inSample > maxDim * 2) inSample *= 2;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = inSample;
        Bitmap bmp;
        try (InputStream s = ctx.getContentResolver().openInputStream(uri)) {
            bmp = BitmapFactory.decodeStream(s, null, opts);
        }
        if (bmp == null) throw new IOException("Could not decode image");

        // Scale to the requested longest-side.
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        float scale = (float) maxDim / Math.max(w, h);
        if (scale < 1f) {
            bmp = Bitmap.createScaledBitmap(bmp, Math.round(w * scale), Math.round(h * scale), true);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, qualityPct, out);
        return out.toByteArray();
    }
}
