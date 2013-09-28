package cm.framework.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyAutoCompleteDb {

	public static final String ID = "_id";
	// public static final String URL = "url";
	// public static final String REQUEST = "request";
	// public static final String CREATEDATE = "createDate";
	// public static final String RESPONSE = "response";
	// public static final String LEN = "len";
	public static final String MAILBOX = "mailbox";

	private static String DB_NAME = "AutoComplete.db";
	// 版本
	private static int DB_VERSION = 1;
	private SQLiteDatabase db;
	private MySQLiteOpenHelper dbHelper;

	public MyAutoCompleteDb(Context context) {
		dbHelper = new MySQLiteOpenHelper(context, DB_NAME, null, DB_VERSION);
		db = dbHelper.getWritableDatabase();
	}

	public void Close() {
		db.close();
		dbHelper.close();
	}

	public Long addMailboxDb(String string) {
		ContentValues values = new ContentValues();
		values.put(MAILBOX, string);
		Long uid = db.insert(MySQLiteOpenHelper.TB_NAME, ID, values);
		Log.e("saveCache", uid + "");
		return uid;
	}

	public List<String> getMailbox() {
		List<String> mailList = new ArrayList<String>();
		String string = null;
		Cursor cursor = db.query(MySQLiteOpenHelper.TB_NAME, null, null, null,
				null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			string = cursor.getString(1);
			mailList.add(string);
			System.out.println(string);
			cursor.moveToNext();
		}
		cursor.close();

		return mailList;
	}

	public void clearDb() {
		db.delete(MySQLiteOpenHelper.TB_NAME, null, null);
	}

	class MySQLiteOpenHelper extends SQLiteOpenHelper {
	
		public static final String TB_NAME = "autocomplete";

		public MySQLiteOpenHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);

		}
 
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TB_NAME + "(" + ID
					+ " integer primary key,"
					// + URL + " varchar," + REQUEST
					// + " varchar,"
					// + CREATEDATE + " varchar,"
					// + RESPONSE + " blob,"
					// + LEN + " integer "
					+ MAILBOX + " varchar" + ")"
					);
			db.execSQL("INSERT INTO " + TB_NAME + " ("+ MAILBOX +")" + "VALUES"
					+ " ('@sohu.com')"); 
			db.execSQL("INSERT INTO " + TB_NAME + " (" + MAILBOX +")" + "VALUES"
					+ " ('@chinaren.com')");
			db.execSQL("INSERT INTO " + TB_NAME + " (" + MAILBOX +")" + "VALUES"
					+ " ('@sogou.com')");
			db.execSQL("INSERT INTO " + TB_NAME + " (" + MAILBOX +")" + "VALUES"
					+ " ('@vip.sohu.com')");
			db.execSQL("INSERT INTO " + TB_NAME + " (" + MAILBOX +")" + "VALUES"
					+ " ('@17173.com')");
			db.execSQL("INSERT INTO " + TB_NAME + " (" + MAILBOX +")" + "VALUES"
					+ " ('@focus.cn')");
			db.execSQL("INSERT INTO " + TB_NAME + " (" + MAILBOX +")" + "VALUES"
					+ " ('@game.sohu.com')");
			Log.e("Database", "onCreate");

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TB_NAME);
			onCreate(db);
			Log.e("Database", "onUpgrade");
		}

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
