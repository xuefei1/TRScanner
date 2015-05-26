package trlabs.trscanner.users.journals;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by intern2 on 05/03/2015.
 */
public class JournalDB {
    private JournalDBHelper dbHelper;
    private SQLiteDatabase sqLiteDatabase;

    public JournalDB(Context context) {
        dbHelper = new JournalDBHelper(context);
    }


    public boolean isExistingKey(String key) {
        sqLiteDatabase = dbHelper.getWritableDatabase();
        Cursor c = sqLiteDatabase.rawQuery("SELECT created FROM journal where created = '"+ key +"'", null);
        if(c != null){
            if(c.getCount()!=0){
                return true;
            }
        }
        return false;
    }

    /**
     * add journal
     */
    public void save(Journal journal) {
        String sql = "insert  into journal(title,content,created) values(?,?,?)";
        sqLiteDatabase = dbHelper.getWritableDatabase();
        sqLiteDatabase.execSQL(
                sql,
                new String[]{journal.getTitle(), journal.getContent(),
                        journal.getDatetime()});
        ContentValues values = new ContentValues();
        values.put("title", journal.getTitle());
        values.put("content", journal.getContent());
        values.put("created", journal.getDatetime());
        sqLiteDatabase.insert("journal", null, values);

    }

    public long save_with_id(Journal journal) {
        String sql = "insert  into journal(title,content,created) values(?,?,?)";
        sqLiteDatabase = dbHelper.getWritableDatabase();
        sqLiteDatabase.execSQL(
                sql,
                new String[]{journal.getTitle(), journal.getContent(),
                        journal.getDatetime()});
        ContentValues values = new ContentValues();
        values.put("title", journal.getTitle());
        values.put("content", journal.getContent());
        values.put("created", journal.getDatetime());
        return sqLiteDatabase.insert("journal", null, values);
    }

    /**
     * update journal
     *
     */
    public void update(Journal journal) {
        sqLiteDatabase = dbHelper.getWritableDatabase();
        sqLiteDatabase.execSQL(
                "update journal set title=?,content=?,created=? where _id=?",
                new Object[]{journal.getTitle(), journal.getContent(),
                        journal.getDatetime(), journal.getId()});
    }


    /**
     * 返回一个游标，也可以使用注释部分，返回一个List集合（使用SimpleAdapter），在这里使用游标，主要是因为
     * AdapterContextMenuInfo中的ID是和数据库中一致的
     * @return  cursor
     */
    public ArrayList<Pair<String, String>> getAllJournals() {
        ArrayList<Pair<String, String>> journalList = new ArrayList<Pair<String, String>>(count());
        sqLiteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from journal ", null);

        while(cursor.moveToNext()){
            String content = cursor.getString(cursor.getColumnIndex("content"));
            String created = cursor.getString(cursor.getColumnIndex("created"));
            journalList.add(new Pair<String,String>(content,created));
        }

        return journalList;
    }

    /**
     * get total number of journals
     * http://stackoverflow.com/questions/3094257/android-sqlite-cursor-out-of-bounds-exception-on-select-count-from-table
     */
    public int count() {
        int count = 0;
        sqLiteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select count(*) from journal ",
                null);
        cursor.moveToFirst();
        count = cursor.getInt(0);
        return count;

    }

    public Journal getJournalById(int id) {
        sqLiteDatabase = dbHelper.getWritableDatabase();
        Journal journal = null;
        Cursor cursor = sqLiteDatabase.rawQuery(
                "select * from journal where _id= ?", new String[] { id + "" });
        if (cursor.moveToFirst()) {

            String title = cursor.getString(cursor.getColumnIndex("title"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            String created = cursor.getString(cursor.getColumnIndex("created"));
            journal = new Journal(title, content, created);
        }

        return journal;
    }

    public Journal getJournalByDate(String date) {
        sqLiteDatabase = dbHelper.getWritableDatabase();
        Journal journal = null;
        Cursor cursor = sqLiteDatabase.rawQuery(
                "select * from journal where created = "+"'"+date+"'",null);
        if (cursor.moveToFirst()) {

            String title = cursor.getString(cursor.getColumnIndex("title"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            String created = cursor.getString(cursor.getColumnIndex("created"));

            journal = new Journal(title, content, created);
        }
        return journal;
    }

    public void deleteByDate(String date) {
        sqLiteDatabase = dbHelper.getWritableDatabase();
        sqLiteDatabase.execSQL("delete from journal where created =?", new Object[] {date });
    }

    /**
     * delete journal by ID
     *
     * @param id
     * journal id
     */
    public void delete(Integer id) {
        sqLiteDatabase = dbHelper.getWritableDatabase();
        sqLiteDatabase.execSQL("delete from journal where _id=?",
                new Object[] { id });
    }

    public void updateColumn(String date, String title, String content) {
        sqLiteDatabase = dbHelper.getWritableDatabase();
        sqLiteDatabase.execSQL("UPDATE journal SET title='"+title+"', content = '"+content+"' WHERE created = '"+date+"'");
    }

}