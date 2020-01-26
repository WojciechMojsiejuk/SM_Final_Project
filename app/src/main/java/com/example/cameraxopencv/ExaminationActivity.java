package com.example.cameraxopencv;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class ExaminationActivity extends AppCompatActivity {

    Interpreter tflite;

    public static final String EXTRA_EXAM_PHOTO_FILEPATH = "EXTRA_EXAM_PHOTO_FILEPATH";
    public static final String EXTRA_EXAM_A_SCORE = "EXTRA_EXAM_A_SCORE";
    public static final String EXTRA_EXAM_B_SCORE = "EXTRA_EXAM_B_SCORE";
    public static final String EXTRA_EXAM_C_SCORE = "EXTRA_EXAM_C_SCORE";
    public static final String EXTRA_EXAM_D_SCORE = "EXTRA_EXAM_D_SCORE";

    private ImageView examinationPhoto;
    private CardView resultsView;
    private TextView aScore;
    private TextView bScore;
    private TextView cScore;
    private TextView dScore;
    private TextView TDS;
    private TextView NN;
    private TextView diagnosis;

    private String filepath;
    private int a_score;
    private int b_score;
    private int c_score;
    private int d_score;
    private double tds;
    private String finalDiagnosis;
    private boolean showResults = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.examination_detail);

        examinationPhoto = findViewById(R.id.examinationPhoto);
        resultsView = findViewById(R.id.resultsView);
        aScore = findViewById(R.id.a_score);
        bScore = findViewById(R.id.b_score);
        cScore = findViewById(R.id.c_score);
        dScore = findViewById(R.id.d_score);
        TDS = findViewById(R.id.tds_score);
        NN = findViewById(R.id.nn_score);
        diagnosis = findViewById(R.id.diagnosis);

        //load model file
//
//        try{
//            tflite = new Interpreter(loadModelFile());
//        }
//        catch (Exception ex)
//        {
//            ex.printStackTrace();
//        }


        if(getIntent().hasExtra(EXTRA_EXAM_PHOTO_FILEPATH)
                && getIntent().hasExtra(EXTRA_EXAM_A_SCORE)
                && getIntent().hasExtra(EXTRA_EXAM_B_SCORE)
                && getIntent().hasExtra(EXTRA_EXAM_C_SCORE)
                && getIntent().hasExtra(EXTRA_EXAM_D_SCORE)
        )
        {
            this.filepath = getIntent().getStringExtra(EXTRA_EXAM_PHOTO_FILEPATH);
            try{
                this.a_score = Integer.parseInt(getIntent().getStringExtra(EXTRA_EXAM_A_SCORE));
            }
            catch (NumberFormatException nfe){
                this.a_score=0;
            }
            try{
                this.b_score = Integer.parseInt(getIntent().getStringExtra(EXTRA_EXAM_B_SCORE));
            }
            catch (NumberFormatException nfe)
            {
                this.b_score=0;
            }
            try{
                this.c_score = Integer.parseInt(getIntent().getStringExtra(EXTRA_EXAM_C_SCORE));
            }
            catch(NumberFormatException nfw)
            {
                this.c_score=0;
            }
            try{
                this.d_score = Integer.parseInt(getIntent().getStringExtra(EXTRA_EXAM_D_SCORE));
            }
            catch (NumberFormatException nfe)
            {
                this.d_score=0;
            }



            if (filepath != null)
            {
//                Picasso.with(this).
//                        load(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/"+this.filepath)
//                        .into(examinationPhoto);
                String folderName = "Melanoma";
                File dir = new File (Environment.getExternalStorageDirectory(), folderName);
                Log.d("Examination bitmap", "Loading bitmap from " + dir+"/"+this.filepath);
                Bitmap bitmap = BitmapFactory.decodeFile( dir+"/"+this.filepath);
                if(bitmap!=null){
                    examinationPhoto.setImageBitmap(bitmap);
                }
                else
                {
                    examinationPhoto.setImageResource(R.drawable.ic_heart);
                }
//
//                new Callback() {
//                    @Override
//                    public void onSuccess() {
//                        Log.d("Examination bitmap", "Image loaded successfully");
//                    }
//
//                    @Override
//                    public void onError() {
//                        Log.e("Examination bitmap", "Error while loading an image, used replacement instead");
//                        examinationPhoto.setImageResource(R.drawable.ic_heart);
//                    }
//                });

            }

            aScore.setText(Integer.toString(a_score));
            bScore.setText(Integer.toString(b_score));
            cScore.setText(Integer.toString(c_score));
            dScore.setText(Integer.toString(d_score));

        }

        final Button button = findViewById(R.id.results);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                showResults=!showResults;
                if(showResults)
                {
                    examinationPhoto.setVisibility(View.GONE);
                    resultsView.setVisibility(View.VISIBLE);
                }
                else
                {
                    examinationPhoto.setVisibility(View.VISIBLE);
                    resultsView.setVisibility(View.GONE);
                }
            }
        });

        final Button analyzer = findViewById(R.id.analyze);
        analyzer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tds = 1.3*a_score + 0.1 * b_score + 0.5 * c_score + 0.5 * d_score;
                TDS.setText(Double.toString(tds));

                if(tds < 4.75){
                    diagnosis.setText(R.string.benign);
                }
                else if (tds>5.45)
                {
                    diagnosis.setText(R.string.malignant);
                }
                else{
                    diagnosis.setText(R.string.suspicious);
                }


                //TFLITE
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//
//                            tflite.run(input,output);
//                        } catch (final Exception e) {
//                            //if they aren't found, throw an error!
//                            throw new RuntimeException("Error initializing classifiers!", e);
//                        }
//                    }
//                }).start();


            }
        });

    }

    //TFLITE
//    private MappedByteBuffer loadModelFile() throws IOException{
//        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("model.tflite");
//        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
//        FileChannel fileChannel = inputStream.getChannel();
//        long startOffset = fileDescriptor.getStartOffset();
//        long declardeLength = fileDescriptor.getDeclaredLength();
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declardeLength);
//
//    }

}
