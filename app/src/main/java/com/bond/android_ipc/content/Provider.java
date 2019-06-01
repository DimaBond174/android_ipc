package com.bond.android_ipc.content;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;


public class Provider  extends ContentProvider {
  // Use an int for each URI we will run, this represents the different queries
  private static final int CASE_STRING = 100;
  private static final int CASE_STRING_ID = 101;

  private static final UriMatcher sUriMatcher = buildUriMatcher();


  @Override
  public boolean onCreate() {
    return true;
  }

  /**
   * Builds a UriMatcher that is used to determine witch database request is being made.
   */
  public static UriMatcher buildUriMatcher()  {
    String content = ContentContract.CONTENT_AUTHORITY;

    // All paths to the UriMatcher have a corresponding code to return
    // when a match is found (the ints above).
    // "/#"  == Any numeric
    // "/*"  == Any string
    UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    matcher.addURI(content, ContentContract.PATH_CASE_STRING, CASE_STRING);
    matcher.addURI(content, ContentContract.PATH_CASE_STRING + "/#", CASE_STRING_ID);

    return matcher;
  }

  @Override
  public String getType(Uri uri) {
    switch(sUriMatcher.match(uri)){
      case CASE_STRING:
        return ContentContract.CaseStringEntry.CONTENT_TYPE;
      case CASE_STRING_ID:
        return ContentContract.CaseStringEntry.CONTENT_ITEM_TYPE;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    //final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    Cursor retCursor = null;
    switch(sUriMatcher.match(uri)){
      case CASE_STRING:
        int size = 0;
        if (null  != selectionArgs && selectionArgs.length > 0) {
          try {
            size = Integer.parseInt(selectionArgs[0]);
          } catch (Exception e) {}
        }
        retCursor = new FakeStringCursor(size);
        break;
      case CASE_STRING_ID:
        int size2 = 0;
        if (null  != selectionArgs && selectionArgs.length > 0) {
          try {
            size2 = Integer.parseInt(selectionArgs[0]);
          } catch (Exception e) {}
        }
        retCursor = new FakeStringCursor(size2);
        long _id = ContentUris.parseId(uri);
        //HA-HA-HA: Cursor Interface want's int:
        retCursor.moveToPosition((int)_id);
        break;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }

    // Set the notification URI for the cursor to the one passed into the function. This
    // causes the cursor to register a content observer to watch for changes that happen to
    // this URI and any of it's descendants. By descendants, we mean any URI that begins
    // with this path.
    retCursor.setNotificationUri(getContext().getContentResolver(), uri);
    return retCursor;
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
    return null;
  }

  @Override
  public int delete(@NonNull Uri uri, @Nullable String selection,  @Nullable String[] selectionArgs) {
    return 0;
  }

  @Override
  public int update(@NonNull Uri uri,  @Nullable ContentValues values,
                    @Nullable String selection,  @Nullable String[] selectionArgs) {
    return 0;
  }

  public class FakeStringCursor implements Cursor {
    final String TAG = "FakeStringCursor";
    final int itemsCount;
    volatile  int  curPos  =  0;
    //https://stackoverflow.com/questions/21623714/what-is-cursor-setnotificationuri-used-for

    public FakeStringCursor(int  count) {
      itemsCount  =  count;
    }

    @Override
    public int getCount() {
      return itemsCount;
    }

    @Override
    public int getPosition() {
      return curPos;
    }

    @Override
    public boolean move(int offset) {
      int newPos = curPos + offset;
      if (newPos >= 0 && newPos < itemsCount) {
        curPos = newPos;
        return true;
      }
      return false;
    }

    @Override
    public boolean moveToPosition(int position) {
      if (position >= 0 && position < itemsCount) {
        curPos = position;
        return true;
      }
      return false;
    }

    @Override
    public boolean moveToFirst() {
      curPos = 0;
      return true;
    }

    @Override
    public boolean moveToLast() {
      curPos = itemsCount - 1;
      return true;
    }

    @Override
    public boolean moveToNext() {
      return move(1);
    }

    @Override
    public boolean moveToPrevious() {
      return move(-1);
    }

    @Override
    public boolean isFirst() {
      Log.w(TAG, "isFirst");
      return   0 == curPos;
    }

    @Override
    public boolean isLast() {
      Log.w(TAG, "isLast");
      return (itemsCount - 1) == curPos;
    }

    @Override
    public boolean isBeforeFirst() {
      Log.w(TAG, "isBeforeFirst");
      return false;
    }

    @Override
    public boolean isAfterLast() {
      Log.w(TAG, "isAfterLast");

      return false;
    }

    @Override
    public int getColumnIndex(String columnName) {
      Log.w(TAG, "getColumnIndex");
      return 0;
    }

    @Override
    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
      Log.w(TAG, "getColumnIndexOrThrow");
      return 0;
    }

    @Override
    public String getColumnName(int columnIndex) {
      Log.w(TAG, "getColumnName");
      return ContentContract.CaseStringEntry.COLUMN_NAME;
    }

    @Override
    public String[] getColumnNames() {
      return new String[] { ContentContract.CaseStringEntry.COLUMN_NAME };
    }

    @Override
    public int getColumnCount() {
      return 1;
    }

    @Override
    public byte[] getBlob(int columnIndex) {
      return new byte[0];
    }

    @Override
    public String getString(int columnIndex) {
      return "{" + String.valueOf(curPos) + "}";
    }

    @Override
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
      Log.w(TAG, "copyStringToBuffer");
    }

    @Override
    public short getShort(int columnIndex) {
      return 0;
    }

    @Override
    public int getInt(int columnIndex) {
      return 0;
    }

    @Override
    public long getLong(int columnIndex) {
      return 0;
    }

    @Override
    public float getFloat(int columnIndex) {
      return 0;
    }

    @Override
    public double getDouble(int columnIndex) {
      return 0;
    }

    @Override
    public int getType(int columnIndex) {
      Log.w(TAG, "getType");
      return FIELD_TYPE_STRING;
    }

    @Override
    public boolean isNull(int columnIndex) {
      Log.w(TAG, "isNull");
      return false;
    }

    @Override
    public void deactivate() {
      Log.w(TAG, "deactivate");
    }

    @Override
    public boolean requery() {
      Log.w(TAG, "requery");
      return false;
    }

    @Override
    public void close() {
      Log.w(TAG, "close");
    }

    @Override
    public boolean isClosed() {
      Log.w(TAG, "isClosed");
      return false;
    }

    @Override
    public void registerContentObserver(ContentObserver observer) {
      Log.w(TAG, "registerContentObserver");
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer) {
      Log.w(TAG, "unregisterContentObserver");
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
      Log.w(TAG, "registerDataSetObserver");
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
      Log.w(TAG, "unregisterDataSetObserver");
    }

    @Override
    public void setNotificationUri(ContentResolver cr, Uri uri) {
      Log.w(TAG, "setNotificationUri");
    }

    @Override
    public Uri getNotificationUri() {
      Log.w(TAG, "getNotificationUri");
      return null;
    }

    @Override
    public boolean getWantsAllOnMoveCalls() {
      Log.w(TAG, "getWantsAllOnMoveCalls");
      return false;
    }

    @Override
    public void setExtras(Bundle extras) {
      Log.w(TAG, "setExtras");
    }

    @Override
    public Bundle getExtras() {
      Log.w(TAG, "getExtras");
      return null;
    }

    @Override
    public Bundle respond(Bundle extras) {
      Log.w(TAG, "respond");
      return null;
    }
  }  //FakeStringCursor

}
