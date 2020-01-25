package com.example.cameraxopencv;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cameraxopencv.isic.ISICPhoto;
import com.example.cameraxopencv.isic.ISICPhotoContainer;
import com.example.cameraxopencv.isic.ISICPhotoService;
import com.example.cameraxopencv.isic.RetrofitInstance;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.cameraxopencv.isic.RetrofitInstance.ISIC_API_URL;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        fetchData();
    }


    private void fetchData() {
        ISICPhotoService isicPhotoService = RetrofitInstance.getRetrofitInstance().create(ISICPhotoService.class);

        Call<ISICPhotoContainer> isicPhotoContainerCall = isicPhotoService.getPhotos("5");
        isicPhotoContainerCall.enqueue(new Callback<ISICPhotoContainer>() {
            @Override
            public void onResponse(Call<ISICPhotoContainer> call, Response<ISICPhotoContainer> response) {
                setupPhotoListView(response.body().getIsicPhotoList());
            }

            @Override
            public void onFailure(Call<ISICPhotoContainer> call, Throwable t) {
                Snackbar.make(findViewById(R.id.main_view),"Something went wrong... Please try later!", Snackbar.LENGTH_LONG).show();
            }
        });

    }

    private void setupPhotoListView(List<ISICPhoto> isicPhotoList) {
        RecyclerView recyclerView = findViewById(R.id.recycler);
        final ISICAdapter adapter = new ISICAdapter();
        adapter.setPhotos(isicPhotoList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    public class ISICAdapter extends RecyclerView.Adapter<ISICPhotoViewHolder> {
        private List<ISICPhoto> photos;

        @Override
        public ISICPhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ISICPhotoViewHolder isicPhotoViewHolder = new ISICPhotoViewHolder(getLayoutInflater(), parent);
            return isicPhotoViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ISICPhotoViewHolder holder, int position) {
            if (photos != null) {
                ISICPhoto isicPhoto = photos.get(position);
                holder.bind(isicPhoto);
            } else {
                Log.d("MainActivity", "No photos");
            }
        }
        void setPhotos(List<ISICPhoto> isicPhotos) {
            this.photos = isicPhotos;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

    }

    public class ISICPhotoViewHolder extends RecyclerView.ViewHolder {
        TextView creationDateTextView;
        ImageView moleImage;

        public ISICPhotoViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.photo_list_item, parent, false));
            creationDateTextView = itemView.findViewById(R.id.creationDate);
            moleImage = itemView.findViewById(R.id.moleImage);

        }

        public void bind(ISICPhoto isicPhoto) {
            if(isicPhoto != null && isicPhoto.getId() != null && isicPhoto.getCreationDate() != null){
                creationDateTextView.setText(isicPhoto.getCreationDate());
                if(isicPhoto.getId() != null){
                    Picasso.with(itemView.getContext())
                            .load(ISIC_API_URL + isicPhoto.getId() + "/thumbnail")
                            .placeholder(R.drawable.ic_heart).into(moleImage);
                }
                else
                {
                    moleImage.setImageResource(R.drawable.ic_heart);
                }
            }
        }
    }
}