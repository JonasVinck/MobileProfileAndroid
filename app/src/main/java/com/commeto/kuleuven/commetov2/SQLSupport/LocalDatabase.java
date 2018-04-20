package com.commeto.kuleuven.commetov2.SQLSupport;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by Jonas on 2/03/2018.
 */

@Database(entities = LocalRoute.class, version = 6, exportSchema = false)
public abstract class LocalDatabase extends RoomDatabase{

    private static LocalDatabase instance;

    public abstract  LocalRouteDAO localRouteDAO();

    private static final Migration migration1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE localroute ADD COLUMN offroad INTEGER NOT NULL DEFAULT \'0\';");
        }
    };
    private static final Migration migration2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE localroute ADD COLUMN calibration INTEGER NOT NULL DEFAULT \'19\';");
        }
    };
    private static final Migration migration3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS backup (\n" +
                    " id INTEGER PRIMARY KEY,\n" +
                    " offroad INTEGER NOT NULL\n" +
                    ");");
        }
    };
    private static final Migration migration4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL("DROP TABLE IF EXISTS backup;");

            database.execSQL("CREATE TABLE IF NOT EXISTS backup (\n" +
                    "localId INTEGER NOT NULL PRIMARY KEY," +
                    "id INTEGER NOT NULL," +
                    "sent INTEGER NOT NULL," +
                    "username TEXT," +
                    "ridename TEXT," +
                    "speed REAL NOT NULL," +
                    "distance REAL NOT NULL," +
                    "time INTEGER NOT NULL," +
                    "duration INTEGER NOT NULL," +
                    "calibration INTEGER NOT NULL," +
                    "type TEXT DEFAULT 'void'," +
                    "updated INTEGER NOT NULL DEFAULT 0," +
                    "lastUpdated INTEGER NOT NULL DEFAULT 0" +
                    ");");
            database.execSQL("INSERT INTO backup (localId, id, sent, username, ridename, speed, distance, time, duration, calibration) " +
                            "SELECT localId, id, sent, username, ridename, speed, distance, time, duration, calibration FROM localroute");

            database.execSQL("DROP TABLE IF EXISTS localroute");

            database.execSQL("aLTER TABLE backup RENAME TO localroute");
        }
    };
    private static final Migration getMigration5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE localroute ADD COLUMN description TEXT DEFAULT \'\';");
        }
    };

    public static LocalDatabase getInstance(Context context){

        if(instance == null){
            instance = Room.databaseBuilder(context, LocalDatabase.class, "localDB")
                    .allowMainThreadQueries()
                    .addMigrations(migration1_2, migration2_3, migration3_4, migration4_5, getMigration5_6)
                    .build();
        }
        return instance;
    }

    public static void destroyIntance(){
        instance = null;
    }
}
