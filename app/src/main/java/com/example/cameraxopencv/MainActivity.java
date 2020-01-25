package com.example.cameraxopencv;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cameraxopencv.db.DBPhoto;
import com.example.cameraxopencv.db.PhotoViewModel;
import com.example.cameraxopencv.isic.ISICPhoto;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import static com.example.cameraxopencv.isic.RetrofitInstance.ISIC_API_URL;

public class MainActivity extends AppCompatActivity {
    private PhotoViewModel photoViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        RecyclerView recyclerView = findViewById(R.id.recycler);
        final PhotoAdapter adapter = new PhotoAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        photoViewModel = ViewModelProviders.of(this).get(PhotoViewModel.class);
        photoViewModel.findAll().observe(this, new Observer<List<DBPhoto>>() {
            @Override
            public void onChanged(@Nullable final List<DBPhoto> photos) {
                adapter.setPhotos((photos));
            }
        });
    }



    class DBPhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView creationDateTextView;
        ImageView moleImage;
        private DBPhoto photo;

        public DBPhotoHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.photo_list_item,parent,false));
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            creationDateTextView = itemView.findViewById(R.id.creationDate);
            moleImage = itemView.findViewById(R.id.moleImage);
        }

        @Override
        public void onClick(View v) {
//            editBook = book;
//            Intent intent = new Intent(MainActivity.this, EditBookActivity.class);
//            intent.putExtra(EditBookActivity.EXTRA_EDIT_BOOK_TITLE, book.getTitle());
//            intent.putExtra(EditBookActivity.EXTRA_EDIT_BOOK_AUTHOR, book.getAuthor());
//            startActivityForResult(intent, EDIT_BOOK_ACTIVITY_REQUEST_CODE);
        }

        @Override
        public boolean onLongClick(View v) {
//            bookViewModel.delete(book);
            return true;
        }

        public void bind(DBPhoto photo) {
            if(photo != null){
                if(photo.getFilepath()!=null)
                {
                    //DB DBPhoto
                    DBPhoto photoFromDB = (DBPhoto)photo;
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
                    creationDateTextView.setText(dateFormat.format(photoFromDB.getCreationDate()));
                    moleImage.setImageResource(R.drawable.ic_heart);
                }

//                else{
//                    //ISIC DBPhoto
//                    ISICPhoto photoFromApi = (ISICPhoto) photo;
//                    Picasso.with(itemView.getContext())
//                            .load(ISIC_API_URL + photoFromApi.getId() + "/thumbnail")
//                            .placeholder(R.drawable.ic_heart).into(moleImage);
//                }
            }
        }
    }


    class ISICPhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView creationDateTextView;
        ImageView moleImage;
        private DBPhoto photo;

        public ISICPhotoHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.photo_list_item,parent,false));
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            creationDateTextView = itemView.findViewById(R.id.creationDate);
            moleImage = itemView.findViewById(R.id.moleImage);
        }

        @Override
        public void onClick(View v) {
//            editBook = book;
//            Intent intent = new Intent(MainActivity.this, EditBookActivity.class);
//            intent.putExtra(EditBookActivity.EXTRA_EDIT_BOOK_TITLE, book.getTitle());
//            intent.putExtra(EditBookActivity.EXTRA_EDIT_BOOK_AUTHOR, book.getAuthor());
//            startActivityForResult(intent, EDIT_BOOK_ACTIVITY_REQUEST_CODE);
        }

        @Override
        public boolean onLongClick(View v) {
//            bookViewModel.delete(book);
            return true;
        }

        public void bind(ISICPhoto photo) {
            if(photo != null){
                    //ISIC DBPhoto
                    ISICPhoto photoFromApi = (ISICPhoto) photo;
                    Picasso.with(itemView.getContext())
                            .load(ISIC_API_URL + photoFromApi.getId() + "/thumbnail")
                            .placeholder(R.drawable.ic_heart).into(moleImage);

                }
            }
        }
    }

    class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    {
        private List<Photo> photos;
        private static final int DB_PHOTO= 0;
        private static final int ISIC_PHOTO= 1;

        @Override
        public int getItemViewType(int position) {
            Photo photo = photos.get(position);
            if(photo.getFilepath()!=null)
            {
                return DB_PHOTO;
            }
            else
            {
                return ISIC_PHOTO;
            }
        }

        @NonNull
        @Override
        public  RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            if(viewType == DB_PHOTO)
            {
                layoutInflater.inflate(R.layout.photo_list_item, parent, false);
                return new MainActivity.DBPhotoHolder(, parent);
            }
            else
            {
                layoutInflater.inflate(R.layout.photo_list_item, parent, false);
                return new MainActivity.ISICPhotoHolder(view);
            }


        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            if(photos !=null)
            {
                Photo photo = photos.get(position);
                if (viewHolder instanceof MainActivity.DBPhotoHolder) {
                    ((MainActivity.DBPhotoHolder) viewHolder).bind((DBPhoto)photos);
                } else {
                    ((MainActivity.ISICPhotoHolder) viewHolder).bind((ISICPhoto) photos);
                }
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