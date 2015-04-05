package com.hope.filedownloader.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper{

	private static final String DB_NAME = "download_record.db";
	private static final int VERSION = 1;
	private static final String COMMA = ",";
	public DBOpenHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE IF NOT EXISTS fileRecord (" +
				"id INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
				"url TEXT" + COMMA + 
				"threadId INTEGER" + COMMA +
				"length INTEGER)";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS filedownlog");
		onCreate(db);
	}

}
