package com.hx.hongbao.table;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by BigStar on 2017/1/29.
 * 
 */

@Entity
public class UserNote {
    @Id
    private String name;
    private int score;
    @Generated(hash = 22909922)
    public UserNote(String name, int score) {
        this.name = name;
        this.score = score;
    }
    @Generated(hash = 1746527500)
    public UserNote() {
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getScore() {
        return this.score;
    }
    public void setScore(int score) {
        this.score = score;
    }
}
