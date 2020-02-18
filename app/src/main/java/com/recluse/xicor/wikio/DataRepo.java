package com.recluse.xicor.wikio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by ROJIT on 6/21/2018.
 */

public class DataRepo extends SQLiteOpenHelper {
    public DataRepo(Context context) {
        super(context, "WikioRepo", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE wikis(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "topic_name TEXT," +
                "link TEXT," +
                "summary TEXT," +
                "html TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP wikis;");
        onCreate(db);
    }

    public void insertWiki(String topic_name,String summary,String link,String html){
        ContentValues cv = new ContentValues();
        cv.put("topic_name",topic_name);
        cv.put("summary",summary);
        cv.put("link",link);
        cv.put("html",html);
        this.getWritableDatabase().insert("wikis",null,cv);
    }

    public LinkedHashMap<Integer,HashMap<String,String>> getWikis(){
        Cursor c = this.getReadableDatabase().rawQuery("SELECT * FROM wikis ORDER BY id DESC;",new String[]{});
        LinkedHashMap<Integer,HashMap<String,String>> data = new LinkedHashMap<>();
        while(c.moveToNext()){
            HashMap<String,String> sub_data = new HashMap<>();
            sub_data.put("name",c.getString(1));
            sub_data.put("link",c.getString(2));
            sub_data.put("summary",c.getString(3));
            sub_data.put("html",c.getString(4));
            data.put(c.getInt(0),sub_data);
        }
        return data;
    }

    public boolean isLinkThere(String link){
        Cursor c = this.getReadableDatabase().rawQuery("SELECT * FROM wikis WHERE link='"+link+"';",new String[]{});
        int count = 0;
        while(c.moveToNext()){
            count++;
        }
        if (count > 0){
            return true;
        }
        return false;
    }
}
