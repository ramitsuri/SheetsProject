package com.ramitsuri.project.data;

import com.ramitsuri.project.MainApplication;
import com.ramitsuri.project.data.converter.BigDecimalConverter;
import com.ramitsuri.project.data.dao.LogDao;
import com.ramitsuri.project.data.dao.SheetDao;
import com.ramitsuri.project.entities.Log;
import com.ramitsuri.project.entities.SheetInfo;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Log.class, SheetInfo.class}, version = 1, exportSchema = true)
@TypeConverters({BigDecimalConverter.class})
public abstract class Database extends RoomDatabase {

    private static volatile Database INSTANCE;
    private static final String DB_NAME = "app_db";

    public static Database getInstance() {
        if (INSTANCE == null) {
            synchronized (Database.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(MainApplication.getInstance(),
                            Database.class, DB_NAME)
                            .addMigrations(DatabaseMigration.MIGRATION_1_2)
                            .addMigrations(DatabaseMigration.MIGRATION_2_3)
                            .addMigrations(DatabaseMigration.MIGRATION_3_4)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract LogDao logDao();

    public abstract SheetDao sheetDao();
}
