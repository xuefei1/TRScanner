package trlabs.trscanner.users.journals;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


// database for operating on Journal
public class JournalDBHelper extends SQLiteOpenHelper {

        public static final String DATABASE_NAME="journal.db";
        public static final int VERSION = 1;

        //database name and version
        public JournalDBHelper(Context context) {
            super(context, DATABASE_NAME, null, VERSION);
            // TODO Auto-generated constructor stub
        }

        //create Database: id (key), title, content, create
        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            db.execSQL(
                "create table journal(_id integer primary key autoincrement," +
                "title varchar(20)," +
                "content varchar(256)," +
                "created varchar(255))"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub

        }



}

