package com.quvideo.demo.automatetasks.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.quvideo.demo.automatetasks.database.model.ClickModel

class ClickDatabaseHelper(context: Context) :
  SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

  companion object {
    private const val DATABASE_NAME = "clicks.db"
    private const val DATABASE_VERSION = 1
    private const val TABLE_NAME = "clicks"
    private const val COLUMN_ID = "id"
    private const val COLUMN_TIMESTAMP = "timestamp"
    private const val COLUMN_X = "x"
    private const val COLUMN_Y = "y"
  }

  override fun onCreate(db: SQLiteDatabase) {
    val createTable = ("CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
      "$COLUMN_TIMESTAMP INTEGER, $COLUMN_X INTEGER, $COLUMN_Y INTEGER)")
    db.execSQL(createTable)
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
    onCreate(db)
  }

  fun insertClick(click: ClickModel) {
    val db = writableDatabase
    val values = ContentValues().apply {
      put(COLUMN_TIMESTAMP, click.timestamp)
      put(COLUMN_X, click.x)
      put(COLUMN_Y, click.y)
    }
    db.insert(TABLE_NAME, null, values)
    db.close()
  }

  fun getAllClicks(): List<ClickModel> {
    val clicks = mutableListOf<ClickModel>()
    val db = readableDatabase
    val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME order by $COLUMN_TIMESTAMP asc", null)

    if (cursor.moveToFirst()) {
      do {
        val columnIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP)
        val columnIndexX = cursor.getColumnIndex(COLUMN_X)
        val columnIndexY = cursor.getColumnIndex(COLUMN_Y)
        clicks.add(
          ClickModel(
            cursor.getLong(columnIndex),
            cursor.getInt(columnIndexX),
            cursor.getInt(columnIndexY)
          )
        )
      } while (cursor.moveToNext())
    }
    cursor.close()
    db.close()
    return clicks
  }
}