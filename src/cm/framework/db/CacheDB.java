package cm.framework.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import cm.framework.include.CacheData;

/**
 * 缓存数据库
 * 
 */
public class CacheDB {

	public static final String ID = "_id";
	public static final String URL = "url";
	public static final String REQUEST = "request";
	public static final String CREATEDATE = "createDate";
	public static final String RESPONSE = "response";
	public static final String LEN = "len";
	public static final String USERID = "userid";
	
	// 数据库名称
	private static String DB_NAME = "auto.db";
	// 数据库版本
	private static int DB_VERSION = 1;
	private SQLiteDatabase db;
	private SqliteHelper dbHelper;
	
	public CacheDB(Context context) {
		dbHelper = new SqliteHelper(context, DB_NAME, null, DB_VERSION);
		db = dbHelper.getWritableDatabase();
	}

	public void close() {
		db.close();
		dbHelper.close();
	}

	// 添加缓存
	public Long saveCache(CacheData cacheData) {
		ContentValues values = new ContentValues();
		values.put(URL, cacheData.url);
		values.put(REQUEST, cacheData.request);
		values.put(CREATEDATE, cacheData.createDate.toString());
		values.put(RESPONSE, cacheData.response);
		values.put(LEN, cacheData.len);
		values.put(USERID, cacheData.userid);
		
		Long uid = db.insert(SqliteHelper.TB_NAME, ID, values);
		Log.e("saveCache", uid + "");
		return uid;
	}
	
	// 删除缓存
	public int deleteCache(String url) {
		int id = db.delete(SqliteHelper.TB_NAME,
				URL + "=?", new String[]{url});
		Log.e("deleteCache", id + "");
		return id;
	}
	
	// 获取缓存
	public CacheData getCacheByUrl(String url, String userid) {
		CacheData cacheData = null;
		Cursor cursor;
		if(userid == null || userid.equals("")) {
			cursor = db.query(
					SqliteHelper.TB_NAME, 
					null, 
					URL + "='"+ url + "'", 
					null, 
					null,
					null, ID + " DESC");
		} else {
			cursor = db.query(
					SqliteHelper.TB_NAME, 
					null, 
					URL + "='"+ url +"' and " + USERID + "='" + userid + "'", 
					null, 
					null,
					null, ID + " DESC");
		}
		
		cursor.moveToFirst();
		if(!cursor.isAfterLast() && (cursor.getString(1) != null)) {
			cacheData = new CacheData();
			cacheData.id = cursor.getInt(0);
			cacheData.url = cursor.getString(1);
			cacheData.request = cursor.getString(2);
			cacheData.createDate = cursor.getString(3);
			cacheData.response = cursor.getString(4);
			cacheData.len = cursor.getInt(5);
			
		}
		cursor.close();
		return cacheData;
	}
	
	class SqliteHelper extends SQLiteOpenHelper {
		// 表名
		public static final String TB_NAME = "cache";

		public SqliteHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		// 创建表
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TB_NAME + "(" 
					+ ID + " integer primary key," 
					+ URL + " varchar," 
					+ REQUEST + " varchar," 
					+ CREATEDATE + " varchar," 
					+ RESPONSE + " blob," 
					+ LEN + " integer, " 
					+ USERID + " varchar )");
			Log.e("Database", "onCreate");
		}

		// 更新表
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TB_NAME);
			onCreate(db);
			Log.e("Database", "onUpgrade");
		}

		// 更新列
		public void updateColumn(SQLiteDatabase db, String oldColumn,
				String newColumn, String typeColumn) {
			try {
				db.execSQL("ALTER TABLE " + TB_NAME + " CHANGE " + oldColumn
						+ " " + newColumn + " " + typeColumn);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
