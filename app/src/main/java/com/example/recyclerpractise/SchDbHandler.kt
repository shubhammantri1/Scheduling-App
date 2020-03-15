package com.example.recyclerpractise

import android.R.string
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast


class SchDbHandler(var context: Context): SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        val createScheduleTable: String = "CREATE TABLE $TABLE_NAME (" +
        "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
        "$COL_TIME VARCHAR(256)," +
        "$COL_AMP VARCHAR(256)," +
        "$COL_TEXT VARCHAR(256)," +
        "$COL_NOTIFYME BOOL);"
        db?.execSQL(createScheduleTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun insertData(user: User){
        val db = this.writableDatabase
        var cv = ContentValues()
        cv.put(COL_TIME, user.time)
        cv.put(COL_AMP, user.amp)
        cv.put(COL_TEXT, user.text)
        cv.put(COL_NOTIFYME,user.notifyMe)
        var result = db.insert(TABLE_NAME, null, cv)
        if(result == (-1).toLong()){
            Toast.makeText(context, "FAILED", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(context, "SUCCESSFULLY CREATED", Toast.LENGTH_SHORT).show()
        }
    }

    fun readData():MutableList<User>{
        var list: MutableList<User> = ArrayList()
        val db = this.readableDatabase
        val query = " Select * from $TABLE_NAME "
        val cursor: Cursor = db.rawQuery(query, null)
        if(cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                var user = User()
                user.id = cursor.getString(cursor.getColumnIndex(COL_ID)).toInt()
                user.time = cursor.getString(cursor.getColumnIndex(COL_TIME))
                user.amp = cursor.getString(cursor.getColumnIndex(COL_AMP))
                user.text = cursor.getString(cursor.getColumnIndex(COL_TEXT))
                user.notifyMe = cursor.getString(cursor.getColumnIndex(COL_NOTIFYME)).toBoolean()
                list.add(user)
                cursor.moveToNext();
            }
        }
        cursor.close()
        db.close()
        return list
    }

    fun deleteData(userId: Long){
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COL_ID=?", arrayOf(userId.toString()))
        db.close()
    }

    fun updateData(user: User){
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_TIME, user.time)
        cv.put(COL_AMP, user.amp)
        cv.put(COL_TEXT, user.text)
        cv.put(COL_NOTIFYME, user.notifyMe)
        db.update(TABLE_NAME, cv, "$COL_ID=?", arrayOf(user.id.toString()))
        db.close()
    }
}