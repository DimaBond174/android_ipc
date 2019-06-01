package com.bond.android_ipc.servs;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.bond.android_ipc.objs.FileAdapter;
import com.bond.android_ipc.objs.StaticConsts;

import java.util.concurrent.atomic.AtomicLong;

public class LocalService extends Service {

  /**
   * Class used for the client Binder.  Because we know this service always
   * runs in the same process as its clients, we don't need to deal with IPC.
   */
  public class LocalBinder extends Binder {
    public LocalService getService() {
      // Return this instance of LocalService so clients can call public methods
      return LocalService.this;
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

//  /** method for clients */
  public void onStartCASE_Big_Byte_Array(String  test_file,
      boolean  check_by_recipient) {
    curCase =  StaticConsts.CASE_Big_Byte_Array;
    check = check_by_recipient;
    synchronized (TAG) {
      cur_test_file = test_file;
    }
    startTime.set(System.nanoTime());
  }

  public void onStartCASE_Strings(int  test_size) {
    curCase =  StaticConsts.CASE_Strings;
    curCaseSize = test_size;
    startTime.set(System.nanoTime());
  }

  public void receiveCASE_Big_Byte_Array(byte[]  data)  {
    long time = 0l;
    if  (StaticConsts.CASE_Big_Byte_Array == curCase)  {
      boolean  pass = !check;
      if (check) {
        String file_name;
        synchronized (TAG) {
          file_name = cur_test_file;
        }
        if (null != file_name) {
          byte[] compare = FileAdapter.readFileBytes(file_name, this);
          if (data.length == compare.length) {
            pass = java.util.Arrays.equals(compare, data);
          }
        }
      }

      if (pass) {
        time  =  (System.nanoTime()  -  startTime.get()) / 1000;
      }
    }
    ServCommon.sendToUI(time, this);
  }

  public void receiveCASE_Strings(String  str)  {
    if  (StaticConsts.CASE_Strings != curCase) {  return; }
    long  res = 0l;
    int len = str.length();
    int i = 0;
    while (i < len  && str.charAt(i) != '{')  { ++i; }
    ++i;
    while (i < len)  {
      char ch = str.charAt(i);
      if (ch < '0' || ch > '9') {  break; }
      res = res * 10 + ch - '0';
      if (res > StaticConsts.MAX_INT) {  break; }
      ++i;
    }
    if (res == curCaseSize)  {
      ServCommon.sendToUI(  (System.nanoTime()  -  startTime.get()) / 1000, this);
    }
  }

  // Private
  ////////////////////////////////////////////////////////////
  final String TAG  =  "LocalService";
  // Binder given to clients
  final IBinder binder = new LocalBinder();
  volatile int  curCase = -1;
  volatile int  curCaseSize = -1;
  final AtomicLong   startTime  = new AtomicLong();
  volatile String cur_test_file  =  null;
  volatile boolean check = false;

}
