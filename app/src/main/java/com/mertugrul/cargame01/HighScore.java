package com.mertugrul.cargame01;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class HighScore{
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int highScore;

    public HighScore(@NonNull int highScore, int id){
        this.highScore=highScore;
        this.id = id;
    }

    public void setHighScore(int highScore){
        this.highScore = highScore;
    }
    public int getHighScore(){
        return highScore;
    }

    public int getId(){
        return id;
    }
}
