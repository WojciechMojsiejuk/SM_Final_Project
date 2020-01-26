package com.example.cameraxopencv.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Photo.class}, version = 4, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class PhotoDatabase extends RoomDatabase {
    public abstract PhotoDao photoDao();

    private static volatile PhotoDatabase INSTANCE;
    public static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static PhotoDatabase getDatabase(final Context context)
    {
        if (INSTANCE == null)
        {
            synchronized (PhotoDatabase.class)
            {
                if (INSTANCE == null)
                {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PhotoDatabase.class,
                            "photo_db").fallbackToDestructiveMigration().addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback()
    {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            databaseWriteExecutor.execute(()-> {
                PhotoDao dao = INSTANCE.photoDao();
                dao.deleteAll();
//                java.sql.Date sqlDate = new java.sql.Date(new Date().getTime());
                Photo photo = new Photo("Filepath", new Date(), 0, 0, 0, 0);
                dao.insert(photo);
            });
        }
    };

}

