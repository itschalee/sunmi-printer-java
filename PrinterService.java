package se.munker.yourPackageName;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterException;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.SunmiPrinterService;

import java.io.ByteArrayOutputStream;

public class PrinterService extends Service {

    private SunmiPrinterService sunmiPrinterService;

    @Override
    public void onCreate() {
        super.onCreate();
        connectPrinterService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("PrintIntent", "onStartCommand ran");
        if (intent != null) {
            String text = intent.getStringExtra("PRINT_TEXT");
            String base64Image = intent.getStringExtra("PRINT_IMAGE");

            if (text != null) {
                printText(text);
            }

            if (base64Image != null) {
                printImage(base64Image);
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void connectPrinterService() {
        try {
            InnerPrinterManager.getInstance().bindService(this, innerPrinterCallback);
        } catch (InnerPrinterException e) {
            e.printStackTrace();
        }
    }

    private InnerPrinterCallback innerPrinterCallback = new InnerPrinterCallback() {
        @Override
        protected void onConnected(SunmiPrinterService service) {
            sunmiPrinterService = service;
        }

        @Override
        protected void onDisconnected() {
            sunmiPrinterService = null;
        }
    };

    private void printText(String text) {
        if (sunmiPrinterService == null) return;
        try {
            sunmiPrinterService.printText(text + "\n\n\n", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printImage(String base64Image) {
        Log.d("PrintIntent", "printimage init");
        if (sunmiPrinterService == null) return;
        try {
            Log.d("PrintIntent", "try printimage");
            // Remove the data:image/png;base64, prefix if present
            String base64String = base64Image.replaceAll("data:image/png;base64,", "");

            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap originalBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (originalBitmap != null) {
                int targetWidth = 384; // Width to fit the printer's width

                // Scale the bitmap to fit the width
                Bitmap scaledBitmap = scaleBitmapToFitWidth(originalBitmap, targetWidth);
                Bitmap grayBitmap = toGrayscale(scaledBitmap);

                // Print image
                sunmiPrinterService.setAlignment(1, null); // Center alignment
                sunmiPrinterService.printBitmapCustom(grayBitmap, 2, null); // Grayscale printing
                sunmiPrinterService.lineWrap(3, null); // Print a few empty lines for separation
            } else {
                throw new IllegalArgumentException("Failed to decode bitmap from base64");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Bitmap scaleBitmapToFitWidth(Bitmap bitmap, int targetWidth) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        float scaleRatio = (float) targetWidth / (float) originalWidth;
        int scaledHeight = Math.round(originalHeight * scaleRatio);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, scaledHeight, true);
        return scaledBitmap;
    }



    private Bitmap toGrayscale(Bitmap bitmap) {
        int width, height;
        height = bitmap.getHeight();
        width = bitmap.getWidth();

        Bitmap grayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(grayBitmap);
        Paint paint = new Paint();

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0); // Set saturation to 0 for grayscale

        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorMatrixFilter);

        canvas.drawBitmap(bitmap, 0, 0, paint);

        return grayBitmap;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sunmiPrinterService != null) {
            try {
                InnerPrinterManager.getInstance().unBindService(this, innerPrinterCallback);
            } catch (InnerPrinterException e) {
                e.printStackTrace();
            }
        }
    }
}
