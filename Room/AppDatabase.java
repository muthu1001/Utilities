package com.thanthi.dtnext.dtnextapplication.database;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import android.content.Context;
import androidx.annotation.NonNull;

import com.thanthi.dtnext.dtnextapplication.model.*;
import com.thanthi.dtnext.dtnextapplication.utils.Constant;

@Database(
        entities = {
                MenuApi.class,
                Article.class,
                Favourites.class,
                Hub.class,
                Settings.class,
                NotificationHub.class},
        version = 5, exportSchema = false)

public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase db;

    public abstract DbDao dbDao();

    public static AppDatabase getDatabase(final Context context){
        if (db == null){
            synchronized (AppDatabase.class){
                if (db == null){
                    db = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, Constant.DATABASE_NAME)
                            .allowMainThreadQueries()
                            .addMigrations(
                                    MIGRATION_1_2,
                                    MIGRATION_2_3,
                                    MIGRATION_3_4,
                                    MIGRATION_4_5)
                            .build();
                }
            }
        }
        return db;
    }

    //Migration
    //latest database version for internal testing is 4

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tableMenu ADD COLUMN mobileCacheTime INTEGER NOT NULL DEFAULT 1");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE tableSupport (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL DEFAULT 0, categoryType TEXT, description TEXT)");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tableArticle ADD COLUMN newTitleOfAccess TEXT");
            database.execSQL("ALTER TABLE tableFavourite ADD COLUMN newTitleOfAccess TEXT");
            database.execSQL("ALTER TABLE tableHub ADD COLUMN newTitleOfAccess TEXT");
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tableMenu ADD COLUMN sectionName TEXT");
            database.execSQL("ALTER TABLE tableMenu ADD COLUMN url TEXT");
            database.execSQL("ALTER TABLE tableMenu ADD COLUMN parentSectionName TEXT");
        }
    };

}
