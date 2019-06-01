package com.bond.android_ipc.servs;

import android.app.IntentService;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.bond.android_ipc.content.ContentContract;
import com.bond.android_ipc.objs.FileAdapter;
import com.bond.android_ipc.objs.StaticConsts;

public class ContentPullService extends IntentService {
  static final String TAG = "ContentPullService";
  static public final int TESTER_TestContentProvider = 1;
  static public final int TESTER_TestViaDisk = 2;

  public ContentPullService() {
    super(TAG);
  }

  public ContentPullService(String name) {
    super(name);
  }

  @Override
  protected void onHandleIntent(Intent workIntent) {
    // Gets data from the incoming Intent
    //String dataString = workIntent.getDataString();
    // Do work here, based on the contents of dataString
    try {
      switch (workIntent.getIntExtra(StaticConsts.PARM_test_case, -1)) {
        case StaticConsts.CASE_Big_Byte_Array:
          if (TESTER_TestContentProvider
              == workIntent.getIntExtra(StaticConsts.PARM_1tester, -1)) {
            do_CASE_Big_Byte_Array_ContentProvider(workIntent);
          }  else  {
            do_CASE_Big_Byte_Array_ViaDisk(workIntent);
          }
          break;
        case StaticConsts.CASE_Strings:
          if (TESTER_TestContentProvider
              == workIntent.getIntExtra(StaticConsts.PARM_1tester, -1)) {
            do_CASE_Strings_ContentProvider(workIntent);
          }  else  {

          }
          break;
        default:
          break;
      }
    } catch (Exception e)  {
      Log.e(TAG, "onHandleIntent(): ", e);
    }
  }

  void do_CASE_Big_Byte_Array_ContentProvider(Intent workIntent) {
    long time = 0l;
    long startTime  = System.nanoTime();
    try {
      ClipData clipData = workIntent.getClipData();
      if (clipData.getItemCount() == 1) {
        ClipData.Item item = clipData.getItemAt(0);
        Uri uri = item.getUri();
        if (null != uri) {
          byte[] data = FileAdapter.readBytesFromURI(uri, this);
          if (data.length > 0) {
            if (workIntent.getBooleanExtra(StaticConsts.PARM_check, false)) {
              String file_name = workIntent.getStringExtra(StaticConsts.PARM_byte_data);
              if (null != file_name) {
                byte[] compare = FileAdapter.readFileBytes(file_name, this);
                if (data.length == compare.length
                    &&  java.util.Arrays.equals(compare, data)) {
                  time  =  System.nanoTime()  -  startTime;
                }
              }
            }  else  {
              time  =  System.nanoTime()  -  startTime;
            }
          }
        }
      }
    } catch (Exception e) {
      Log.e(TAG, "do_CASE_Big_Byte_Array(): ", e);
    }
    ServCommon.sendToUI(time, this);
  } //case bytes

  void do_CASE_Strings_ContentProvider(Intent workIntent) {
    long time = 0l;
    long startTime  = System.nanoTime();
    try {
      final int  curCaseSize = workIntent.getIntExtra(StaticConsts.PARM_max_items, 0);
      Cursor cursor = getContentResolver().query(
          ContentContract.CaseStringEntry.CONTENT_URI,
          null,
          null,
          new String[] { String.valueOf(curCaseSize)},
          null
      );
      if (curCaseSize  == cursor.getCount() &&  cursor.moveToFirst())  {
        long  res = 0l;
        do {
          int i = 0;
          String str  =  cursor.getString(0);
          int len = str.length();
          res  =  0l;
          while (i < len  && str.charAt(i) != '{')  { ++i; }
          ++i;
          while (i < len)  {
            char ch = str.charAt(i);
            if (ch < '0' || ch > '9') {  break; }
            res = res * 10 + ch - '0';
            if (res > StaticConsts.MAX_INT) {  break; }
            ++i;
          }
          if (res > StaticConsts.MAX_INT) {  break; }
        } while (cursor.moveToNext());
        if  ( res == (curCaseSize - 1)) {
          time = System.nanoTime() - startTime;
        }
      }

    } catch (Exception e) {
      Log.e(TAG, "do_CASE_Strings(): ", e);
    }
    ServCommon.sendToUI(time, this);
  }  //do_CASE_Strings

}