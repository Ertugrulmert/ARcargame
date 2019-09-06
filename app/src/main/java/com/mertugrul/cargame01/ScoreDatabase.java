package com.mertugrul.cargame01;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {HighScore.class}, version = 2)
public abstract class ScoreDatabase extends RoomDatabase {

    public abstract HighScoreDao highScoreDao();
}