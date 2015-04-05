package com.hope.filedownloader.core;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class FileService {

	private DBOpenHelper mHelper;
	
	public FileService(Context context) {
		mHelper = new DBOpenHelper(context);
	}
	
	public void insertData(String url, Map<Integer, Long> data) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			for (Map.Entry<Integer, Long> entry : data.entrySet()) {
				db.execSQL("insert into fileRecord(url, threadId, length), value(?,?,?)",
						new Object[]{url, entry.getKey(), entry.getValue()});
			}
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
			db.close();
		}
	}
	
	public Map<Integer, Long> getData(String url){
		Map<Integer, Long> data = new HashMap<Integer, Long>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("select threadId, length from fileRecord where url = ?", new String[]{url});
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			data.put(cursor.getInt(cursor.getColumnIndex("threadId")),
					cursor.getLong(cursor.getColumnIndex("length")));
		}
		cursor.close();
		db.close();
		return data;
	}
	
	public void updateData(String url, int threadId, long length) {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("update fileRecord set length = ? where url = ? and threadId = ?",
				new Object[]{length, url, threadId});
		db.close();
	}
	
	public void deleteData(String url) {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("delete from fileRecord where url = ?", new Object[]{url});
		db.close();
	}
	
	private SQLiteDatabase getWritableDatabase() {
		return mHelper.getWritableDatabase();
	}
	
	private SQLiteDatabase getReadableDatabase() {
		return mHelper.getReadableDatabase();
	}
}
