package com.hope.filedownloader.core;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


/**
 * ҵ��bean
 * 
 */
public class FileService {
	private DBOpenHelper openHelper;

	public FileService(Context context) {
		openHelper = new DBOpenHelper(context);
	}

	/**
	 * ��ȡÿ���߳��Ѿ����ص��ļ�����
	 * 
	 * @param path
	 * @return
	 */
	public Map<Integer, Long> getData(String path) {
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = db
				.rawQuery(
						"select threadid, downlength from filedownlog where downpath=?",
						new String[] { path });
		Map<Integer, Long> data = new HashMap<Integer, Long>();
		while (cursor.moveToNext()) {
			data.put(cursor.getInt(0), cursor.getLong(1));
		}
		cursor.close();
		db.close();
		return data;
	}

	/**
	 * ����ÿ���߳��Ѿ����ص��ļ�����
	 * 
	 * @param path
	 * @param map
	 */
	public void insertData(String path, Map<Integer, Long> map) {// int threadid,
																// int position
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			for (Map.Entry<Integer, Long> entry : map.entrySet()) {
				db.execSQL(
						"insert into filedownlog(downpath, threadid, downlength) values(?,?,?)",
						new Object[] { path, entry.getKey(), entry.getValue() });
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		db.close();
	}

	/**
	 * ʵʱ����ÿ���߳��Ѿ����ص��ļ�����
	 * 
	 * @param path
	 * @param map
	 */
	public void updateData(String path, int threadId, long pos) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.execSQL(
				"update filedownlog set downlength=? where downpath=? and threadid=?",
				new Object[] { pos, path, threadId });
		db.close();
	}

	/**
	 * ���ļ�������ɺ�ɾ����Ӧ�����ؼ�¼
	 * 
	 * @param path
	 */
	public void deleteData(String path) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.execSQL("delete from filedownlog where downpath=?",
				new Object[] { path });
		db.close();
	}

}
