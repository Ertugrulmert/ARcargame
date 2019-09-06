package com.mertugrul.cargame01;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.graphics.Paint;


import java.util.List;


@Dao
public interface HighScoreDao{
    @Delete
    public void delete(HighScore highScore);

    @Insert
    void insert(HighScore highScore);

    @Query("SELECT * FROM HighScore")
    LiveData<List<HighScore>> getHighScore();
}