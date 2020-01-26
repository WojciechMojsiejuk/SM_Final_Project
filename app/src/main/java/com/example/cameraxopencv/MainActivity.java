package com.example.cameraxopencv;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cameraxopencv.db.Photo;
import com.example.cameraxopencv.db.PhotoViewModel;
import com.example.cameraxopencv.isic.ISICPhoto;
import com.example.cameraxopencv.isic.ISICPhotoService;
import com.example.cameraxopencv.isic.RetrofitInstance;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private PhotoViewModel photoViewModel;
    public static final int NEW_PHOTO_ACTIVITY_REQUEST_CODE = 1;

    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.INTERNET"};
    private int REQUEST_CODE_PERMISSIONS = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        if (allPermissionsGranted()) {
            init();

        } else {
            ActivityCompat.requestPermissions(this,REQUIRED_PERMISSIONS,REQUEST_CODE_PERMISSIONS);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                init();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    void init(){
        RecyclerView recyclerView = findViewById(R.id.recycler);
        final PhotoAdapter adapter = new PhotoAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        photoViewModel = ViewModelProviders.of(this).get(PhotoViewModel.class);
        photoViewModel.findAll().observe(this, new Observer<List<Photo>>() {
            @Override
            public void onChanged(List<Photo> photos) {
                adapter.setPhotos((photos));
            }
        });

        FloatingActionButton addPhotoButton = findViewById(R.id.add_button);
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivityForResult(intent, NEW_PHOTO_ACTIVITY_REQUEST_CODE);
            }
        });

        FloatingActionButton downloadPhotoButton = findViewById(R.id.download_button);
        downloadPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ISICPhotoService isicPhotoService = RetrofitInstance.getRetrofitInstance().create(ISICPhotoService.class);

                Call<List<ISICPhoto>> isicPhotoCall = isicPhotoService.getPhotos("5", "20");
                isicPhotoCall.enqueue(new Callback<List<ISICPhoto>>() {
                    @Override
                    public void onResponse(Call<List<ISICPhoto>> call, Response<List<ISICPhoto>> response) {
                        download(response.body());
                    }

                    @Override
                    public void onFailure(Call<List<ISICPhoto>> call, Throwable t) {
                        Snackbar.make(findViewById(R.id.main_view),"Something went wrong... Please try later!", Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    void download(List<ISICPhoto> isicPhotoList) {
        for(ISICPhoto photo:isicPhotoList){

            String url = "https://isic-archive.com/api/v1/image/"+photo.getId()+"/download";
            Log.d("Download", "Downloading from "+ "https://isic-archive.com/api/v1/image/"+photo.getId()+"/download");
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

            Picasso.with(getApplicationContext()).
                    load(url).
                    resize(1000, 1000).centerCrop().
                    into(picassoImageTarget(dir.getAbsolutePath(), filename));

            DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
            Date date;
            try{
                date = dateFormat.parse(photo.getCreationDate());
            }
            catch(ParseException pe)
            {
                date = new Date();
            }

            // To future me: implement thread to analyze downloaded image

            Random r = new Random();
            int a = r.nextInt(3);
            int b = r.nextInt(9);
            int c = r.nextInt(6)+1;
            int d = r.nextInt(6);

            Photo downloadPhoto = new Photo(filename,
                    date,
                    a,b,c,d);
            photoViewModel.insert(downloadPhoto);
            Log.d("Download", "Added with Picasso");
            Log.d("Download", "File: " + downloadPhoto.getFilepath());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == NEW_PHOTO_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK)
        {
            Bundle bundle = data.getExtras();

            String filename = bundle.getString(CameraActivity.EXTRA_PHOTO_FILENAME);
            if (filename == null)
            {
                Log.e("Camera", "No filename, couldn't create new photo instance");
                Toast.makeText(getApplicationContext(),
                        R.string.photo_not_saved,
                        Toast.LENGTH_LONG).show();
                finish();
            }
            Date creationDate = (Date)bundle.getSerializable(CameraActivity.EXTRA_PHOTO_TIME);
            String a_score_extra = bundle.getString(CameraActivity.EXTRA_PHOTO_A_SCORE);
            int a_score = 0;
            if (a_score_extra == null){
                Log.d("Camera", "No a score");
            }
            else{
                a_score = Integer.parseInt(a_score_extra);
            }

            String b_score_extra = bundle.getString(CameraActivity.EXTRA_PHOTO_B_SCORE);
            int b_score = 0;
            if (b_score_extra == null){
                Log.d("Camera", "No b score");
            }
            else{
                b_score = Integer.parseInt(b_score_extra);
            }

            String c_score_extra = bundle.getString(CameraActivity.EXTRA_PHOTO_C_SCORE);
            int c_score = 0;
            if (c_score_extra == null){
                Log.d("Camera", "No c score");
            }
            else{
                c_score = Integer.parseInt(c_score_extra);
            }

            String d_score_extra = bundle.getString(CameraActivity.EXTRA_PHOTO_D_SCORE);
            int d_score = 0;
            if (d_score_extra == null){
                Log.d("Camera", "No d score");
            }
            else{
                d_score = Integer.parseInt(b_score_extra);
            }

            Photo photo = new Photo(filename, creationDate, a_score, b_score, c_score, d_score);
            photoViewModel.insert(photo);
            Snackbar.make(findViewById(R.id.main_view), getString(R.string.photo_added), Snackbar.LENGTH_LONG).show();

        }
        else
        {
            Toast.makeText(getApplicationContext(),
                    R.string.photo_not_saved,
                    Toast.LENGTH_LONG).show();
        }
    }

    private Target picassoImageTarget(final String folderName, final String imageName) {
        Log.d("picassoImageTarget", imageName);
        return new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final File myImageFile = new File(
                                folderName, imageName); // Create image file
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(myImageFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                fos.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.i("image", "image saved to >>>" + myImageFile.getAbsolutePath());
                    }
                }).start();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
    }

    class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView creationDateTextView;
        ImageView moleImage;
        private Photo photo;

        public PhotoHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.photo_list_item,parent,false));
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            creationDateTextView = itemView.findViewById(R.id.creationDate);
            moleImage = itemView.findViewById(R.id.moleImage);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, ExaminationActivity.class);
            intent.putExtra(ExaminationActivity.EXTRA_EXAM_PHOTO_FILEPATH, photo.getFilepath());
            intent.putExtra(ExaminationActivity.EXTRA_EXAM_A_SCORE, Integer.toString(photo.getA()));
            intent.putExtra(ExaminationActivity.EXTRA_EXAM_B_SCORE, Integer.toString(photo.getB()));
            intent.putExtra(ExaminationActivity.EXTRA_EXAM_C_SCORE, Integer.toString(photo.getC()));
            intent.putExtra(ExaminationActivity.EXTRA_EXAM_D_SCORE, Integer.toString(photo.getD()));
            startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            photoViewModel.delete(photo);
            return true;
        }

        public void bind(Photo photo) {
            if(photo != null){
                if(photo.getFilepath()!=null)
                {
                    this.photo = photo;
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
                    creationDateTextView.setText(dateFormat.format(this.photo.getCreationDate()));
                    moleImage.setImageResource(R.drawable.ic_heart);
                }
            }
        }
    }


    class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>
    {
        private List<Photo> photos;

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PhotoHolder(getLayoutInflater(), parent);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            if(photos !=null)
            {
                Photo photo = photos.get(position);
                holder.bind(photo);
            }
            else
            {
                Log.d("MainActivity", "No photos");
            }

        }

        @Override
        public int getItemCount() {
            if (photos != null)
            {
                return photos.size();
            }
            else
            {
                return 0;
            }
        }

        void setPhotos(List<Photo> photos)
        {
            this.photos = photos;
            notifyDataSetChanged();
        }
    }

}