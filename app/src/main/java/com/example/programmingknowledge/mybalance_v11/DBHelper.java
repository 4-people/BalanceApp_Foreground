package com.example.programmingknowledge.mybalance_v11;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.w3c.dom.Text;

import java.util.Date;


public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    public DBHelper(Context context){
        super(context, "balanceappdb", null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db){
        //목표밸런스
        String goalbalanceSQL = "CREATE TABLE tb_goalbalance "+
                "(id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "sleep REAL,"+"work REAL,"+"study REAL,"+"exercise REAL,"+"leisure REAL,"+"other REAL,"+"week TEXT);";
        db.execSQL(goalbalanceSQL);

        //일별 테이블
        String daySQL = "CREATE TABLE tb_dailybalance "+
                "(id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "date TEXT,"+ "week TEXT,"+ "sleep REAL,"+"work REAL,"+"study REAL,"+"exercise REAL,"+"leisure REAL,"+"other REAL);";
        db.execSQL(daySQL);

        //현재 측정 테이블
        String todaySQL = "CREATE TABLE tb_timeline "+
                "(id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "date TEXT,"+"place TEXT,"+"category TEXT,"+"starttime TEXT,"+"endtime TEXT);";
        db.execSQL(todaySQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        if(newVersion == DATABASE_VERSION){
            db.execSQL("drop table if exists tb_goalbalance");

            db.execSQL("drop table if exists tb_dailybalance");

            db.execSQL("drop table if exists tb_todaycount");

            onCreate(db);
        }
    }

}
