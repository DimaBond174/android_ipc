package com.bond.android_ipc.servs;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;

import com.bond.android_ipc.IAIDLService;
import com.bond.android_ipc.objs.FileAdapter;
import com.bond.android_ipc.objs.StaticConsts;

import java.util.concurrent.atomic.AtomicLong;

public class AIDLService extends Service {

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.w(TAG, "Service:onStartCommand===> called");
    if (null!=intent) {
      Bundle b = intent.getExtras();
      if (null!=b) {
        boolean keepRun = b.getBoolean("keepRun", false);
        Log.w(TAG, "keepRun ="+String.valueOf(keepRun));
//        if (keepRun) {
//          //Log.w(TAG, "startIfNeed - must be started");
//          if (System.currentTimeMillis() - keepAliveThread.lastTimeAcive.get() > LConstants.MSEC_ANDROID_ANR) {
//            keepAliveThread.resume();
//          }
//        } else {
//          keepAliveThread.pause();
//          selfDestroyOnGuiThread();
//        }
      }
    }
    return START_STICKY;
  }//onStartCommand

  @Override
  public IBinder onBind(Intent intent) {
    // Return the interface
    return binder;
  }

  private final IAIDLService.Stub binder = new IAIDLService.Stub() {
    @Override
    public void onStartCASE_Big_Byte_Array(String test_file, boolean check_by_recipient) throws RemoteException {
    //  Log.w(TAG, "Service:onStartCASE_Big_Byte_Array===> called");
      AIDLService.this.onStartCASE_Big_Byte_Array(test_file,  check_by_recipient);
    }

    @Override
    public void onStartCASE_Strings(int test_size) throws RemoteException {
      //Log.w(TAG, "Service:onStartCASE_Strings===> called");
      AIDLService.this.onStartCASE_Strings(test_size);
    }

    @Override
    public void receiveCASE_Big_Byte_Array(String data_encoded_base64) throws RemoteException {
    //  Log.w(TAG, "Service:receiveCASE_Big_Byte_Array===> called");
      AIDLService.this.receiveCASE_Big_Byte_Array(
          Base64.decode(data_encoded_base64, Base64.DEFAULT));
    }

    @Override
    public void receiveCASE_Strings(String str) throws RemoteException {
      //Log.w(TAG, "Service:receiveCASE_Strings===> called");
      AIDLService.this.receiveCASE_Strings(str);
    }

  };

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
  volatile int  curCase = -1;
  volatile int  curCaseSize = -1;
  final AtomicLong   startTime  = new AtomicLong();
  volatile String cur_test_file  =  null;
  volatile boolean check = false;

}
