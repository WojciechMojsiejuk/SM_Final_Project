package com.example.cameraxopencv;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class CameraActivity  extends AppCompatActivity implements View.OnClickListener {
    private int REQUEST_CODE_PERMISSIONS = 101;
    public static final String EXTRA_PHOTO_FILENAME = "EXTRA_PHOTO_FILENAME";
    public static final String EXTRA_PHOTO_TIME = "EXTRA_PHOTO_TIME";
    public static final String EXTRA_PHOTO_A_SCORE = "EXTRA_PHOTO_A_SCORE";
    public static final String EXTRA_PHOTO_B_SCORE = "EXTRA_PHOTO_B_SCORE";
    public static final String EXTRA_PHOTO_C_SCORE = "EXTRA_PHOTO_C_SCORE";
    public static final String EXTRA_PHOTO_D_SCORE = "EXTRA_PHOTO_D_SCORE";

    private int a_score;
    private int b_score;
    private int c_score;
    private int d_score;

    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.INTERNET"};
    TextureView textureView;
    ImageView ivBitmap;
    LinearLayout llBottom;

    boolean tumorDetectionFilter = false;
    boolean hairRemovalFilter = false;
    boolean skinColorChange = false;
    boolean detectWhite = false;
    boolean detectRed = false;
    boolean detectLightBrown = false;
    boolean detectDarkBrown = false;
    boolean detectBlueGray = false;
    boolean detectBlack = false;
    boolean detectAssymetry = false;


    ImageCapture imageCapture;
    ImageAnalysis imageAnalysis;
    Preview preview;

    FloatingActionButton btnCapture, btnOk, btnCancel;

    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_activity);

        Random r = new Random();
        this.a_score = r.nextInt(3);
        this.b_score = r.nextInt(9);
        this.c_score = r.nextInt(6)+1;
        this.d_score = r.nextInt(6);


        btnCapture = findViewById(R.id.btnCapture);
        btnOk = findViewById(R.id.btnAccept);
        btnCancel = findViewById(R.id.btnReject);

        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        llBottom = findViewById(R.id.llBottom);
        textureView = findViewById(R.id.textureView);
        ivBitmap = findViewById(R.id.ivBitmap);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,REQUIRED_PERMISSIONS,REQUEST_CODE_PERMISSIONS);
        }
    }

    private void startCamera() {

        CameraX.unbindAll();
        preview = setPreview();
        imageCapture = setImageCapture();
        imageAnalysis = setImageAnalysis();

        //bind to lifecycle:
        CameraX.bindToLifecycle(this, preview, imageCapture, imageAnalysis);
    }


    private Preview setPreview() {

        Rational aspectRatio = new Rational(textureView.getWidth(), textureView.getHeight());
        Size screen = new Size(textureView.getWidth(), textureView.getHeight()); //size of the screen


        PreviewConfig pConfig = new PreviewConfig.Builder().setTargetAspectRatio(aspectRatio).setTargetResolution(screen).build();
        Preview preview = new Preview(pConfig);

        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    @Override
                    public void onUpdated(Preview.PreviewOutput output) {
                        ViewGroup parent = (ViewGroup) textureView.getParent();
                        parent.removeView(textureView);
                        parent.addView(textureView, 0);

                        textureView.setSurfaceTexture(output.getSurfaceTexture());
                        updateTransform();
                    }
                });

        return preview;
    }


    private ImageCapture setImageCapture() {
        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
        final ImageCapture imgCapture = new ImageCapture(imageCaptureConfig);


        btnCapture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                imgCapture.takePicture(new ImageCapture.OnImageCapturedListener() {
                    @Override
                    public void onCaptureSuccess(ImageProxy image, int rotationDegrees) {
                        Bitmap bitmap = textureView.getBitmap();
                        showAcceptedRejectedButton(true);
                        ivBitmap.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(ImageCapture.UseCaseError useCaseError, String message, @Nullable Throwable cause) {
                        super.onError(useCaseError, message, cause);
                    }
                });


                /*File file = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "" + System.currentTimeMillis() + "_JDCameraX.jpg");
                imgCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        Bitmap bitmap = textureView.getBitmap();
                        showAcceptedRejectedButton(true);
                        ivBitmap.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {

                    }
                });*/
            }
        });

        return imgCapture;
    }


    private ImageAnalysis setImageAnalysis() {

        // Setup image analysis pipeline that computes average pixel luminance
        HandlerThread analyzerThread = new HandlerThread("OpenCVAnalysis");
        analyzerThread.start();


        ImageAnalysisConfig imageAnalysisConfig = new ImageAnalysisConfig.Builder()
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .setCallbackHandler(new Handler(analyzerThread.getLooper()))
                .setImageQueueDepth(1).build();

        ImageAnalysis imageAnalysis = new ImageAnalysis(imageAnalysisConfig);

        imageAnalysis.setAnalyzer(
                new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(ImageProxy image, int rotationDegrees) {
                        //Analyzing live camera feed begins.

                        final Bitmap bitmap = textureView.getBitmap();



                        if(bitmap==null)
                            return;

                        Mat mat = new Mat();
                        Utils.bitmapToMat(bitmap, mat);

                        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);

                        //Implement filters here

                        if(tumorDetectionFilter)
                        {
                            // Region of interest

                            Mat roi = new Mat();
                            Imgproc.cvtColor(mat, roi, Imgproc.COLOR_RGB2GRAY );

                            //Kernel declaration

                            Mat kernel = new Mat(5, 5, CvType.CV_8U, Scalar.all(1));
                            Imgproc.morphologyEx(roi, roi, Imgproc.MORPH_CLOSE, kernel);
                            Imgproc.threshold(roi, roi, 127, 255, Imgproc.THRESH_BINARY);

                            //Tumor mask
                            Mat tm_mask = new Mat();
                            Core.bitwise_not(mat, tm_mask, roi);
                            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                            Mat hierarchy = new Mat();
                            Scalar color = new Scalar(0, 255, 255);

                            Imgproc.cvtColor(tm_mask, tm_mask, Imgproc.COLOR_RGB2GRAY);
                            Imgproc.findContours(tm_mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                            Imgproc.drawContours(mat, contours, -1, color, 3);

                        }
                        else if(detectWhite)
                        {
                            Core.inRange(mat, new Scalar(197, 188, 217), new Scalar(255, 255, 255), mat);
                        }
                        else if(detectRed)
                        {
                            Core.inRange(mat, new Scalar(118, 21, 17), new Scalar(255, 0, 0), mat);
                        }
                        else if(detectLightBrown)
                        {
                            Core.inRange(mat, new Scalar(163, 82, 16), new Scalar(179, 81, 2), mat);
                        }
                        else if(detectDarkBrown)
                        {
                            Core.inRange(mat, new Scalar(87, 26, 0), new Scalar(135, 44, 5), mat);
                        }
                        else if(detectBlueGray)
                        {
                            Core.inRange(mat, new Scalar(97, 97, 97), new Scalar(113, 108, 139), mat);
                        }
                        else if(detectBlack)
                        {
                            Core.inRange(mat, new Scalar(0, 0, 0), new Scalar(44, 31, 30), mat);
                        }

                        Utils.matToBitmap(mat, bitmap);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ivBitmap.setImageBitmap(bitmap);
                            }
                        });

                    }
                });


        return imageAnalysis;

    }

    private void showAcceptedRejectedButton(boolean acceptedRejected) {
        if (acceptedRejected) {
            CameraX.unbind(preview, imageAnalysis);
            llBottom.setVisibility(View.VISIBLE);
            btnCapture.hide();
            textureView.setVisibility(View.GONE);
        } else {
            btnCapture.show();
            llBottom.setVisibility(View.GONE);
            textureView.setVisibility(View.VISIBLE);
            textureView.post(new Runnable() {
                @Override
                public void run() {
                    startCamera();
                }
            });
        }
    }


    private void updateTransform() {
        Matrix mx = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int) textureView.getRotation();

        switch (rotation) {
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float) rotationDgr, cX, cY);
        textureView.setTransform(mx);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {

        for (String permission: REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.resetFilter:
                resetMenuOptions();
                startCamera();
                return true;

            case R.id.detectWhite:
                resetMenuOptions();
                detectWhite = true;
                startCamera();
                return true;

            case R.id.detectRed:
                resetMenuOptions();
                detectRed = true;
                startCamera();
                return true;

            case R.id.detectLightBrown:
                resetMenuOptions();
                detectLightBrown = true;
                startCamera();
                return true;

            case R.id.detectDarkBrown:
                resetMenuOptions();
                detectDarkBrown = true;
                startCamera();
                return true;

            case R.id.detectBlueGray:
                resetMenuOptions();
                detectBlueGray = true;
                startCamera();
                return true;

            case R.id.detectBlack:
                resetMenuOptions();
                detectBlack = true;
                startCamera();
                return true;

            case R.id.tumor_detection:
                resetMenuOptions();
                tumorDetectionFilter = true;
                startCamera();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    void resetMenuOptions()
    {
        tumorDetectionFilter = false;
        hairRemovalFilter = false;
        skinColorChange = false;
        detectWhite = false;
        detectRed = false;
        detectLightBrown = false;
        detectDarkBrown = false;
        detectBlueGray = false;
        detectBlack = false;
        detectAssymetry = false;
    }

    @Override
    public void onClick(View v) {
        Intent replyIntent = new Intent();
        switch (v.getId()) {
            case R.id.btnReject:
                showAcceptedRejectedButton(false);
                setResult(RESULT_CANCELED, replyIntent);
                break;

            case R.id.btnAccept:
                String filename = "" + System.currentTimeMillis() + "_JDCameraX.jpeg";
                String folderName = "Melanoma";
                File dir = new File (Environment.getExternalStorageDirectory(), folderName);
                if (!dir.exists())
                {
                    boolean success = dir.mkdirs();
                    if(success)
                    {
                        Log.d("Melanoma folder", "folder created successfully");
                    }
                    else
                    {
                        Log.e("Melanoma folder", "folder creation failed");
                    }
                }
                File file = new File(
                        dir.getAbsolutePath(), filename);
                imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        showAcceptedRejectedButton(false);
                        replyIntent.putExtra(EXTRA_PHOTO_FILENAME, filename);
                        replyIntent.putExtra(EXTRA_PHOTO_TIME, new Date());
                        replyIntent.putExtra(EXTRA_PHOTO_A_SCORE, Integer.toString(a_score));
                        replyIntent.putExtra(EXTRA_PHOTO_B_SCORE, Integer.toString(b_score));
                        replyIntent.putExtra(EXTRA_PHOTO_C_SCORE, Integer.toString(c_score));
                        replyIntent.putExtra(EXTRA_PHOTO_D_SCORE, Integer.toString(d_score));

                        setResult(RESULT_OK, replyIntent);
                        finish();
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                        Toast.makeText(getApplicationContext(), R.string.photo_error, Toast.LENGTH_LONG).show();
                    }
                });
                break;
        }
    }
}
