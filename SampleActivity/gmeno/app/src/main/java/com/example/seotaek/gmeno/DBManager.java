package com.example.seotaek.gmeno;

/**
 * Created by Seotaek on 2017-09-25.
 */
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DBManager extends SQLiteOpenHelper{
    private Context context;
    public DBManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }
    /** * Database가 존재하지 않을 때, 딱 한번 실행된다. * DB를 만드는 역할을 한다. * @param db */ @Override
    public void onCreate(SQLiteDatabase db) {
        // String 보다 StringBuffer가 Query 만들기 편하다.
        StringBuffer sb = new StringBuffer();
        sb.append(" CREATE TABLE TEST_TABLE ( ");
        sb.append(" _ID INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sb.append(" NAME TEXT, ");
        sb.append(" AGE INTEGER, ");
        sb.append(" PHONE TEXT ) ");
        // SQLite Database로 쿼리 실행
         db.execSQL(sb.toString());
        Toast.makeText(context, "Table 생성완료", Toast.LENGTH_SHORT).show();
    } /** * Application의 버전이 올라가서 * Table 구조가 변경되었을 때 실행된다. * @param db * @param oldVersion * @param newVersion */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Toast.makeText(context, "버전이 올라갔습니다.", Toast.LENGTH_SHORT).show();
    } /** * */
    public void testDB() { SQLiteDatabase db = getReadableDatabase();
    }
}




