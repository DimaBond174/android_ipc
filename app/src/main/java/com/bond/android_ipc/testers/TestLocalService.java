package com.bond.android_ipc.testers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.bond.android_ipc.TestPresenter;
import com.bond.android_ipc.gui.SpecTheme;
import com.bond.android_ipc.i.ITestCase;
import com.bond.android_ipc.i.ITester;
import com.bond.android_ipc.objs.StaticConsts;
import com.bond.android_ipc.servs.LocalService;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TestLocalService implements  ITester {
  // Public
  ////////////////////////////////////////////////////////////
  @Override
  public String get_test_name() {
    return TAG;
  }

  @Override
  public CompletableFuture<long[]> start(ITestCase testCase) {
    stop();
    CompletableFuture<long[]> re = new CompletableFuture<>();
    local_start(testCase, re);
    return  re;
  }

  @Override
  public void stop() {
    ++startID;
    synchronized (TAG) {
      thread = null;
      local = null;
    }
  }

//  @Override
//  public void join() {
//    try {
//      thread.join();
//    } catch (Exception e) {
//      Log.e(TAG, "join():", e);
//    }
//  }

  @Override
  public void onBroAnswer(long worktime) {
    try {
      synchronized (TAG) {
        if (null != time_from_bro) {
          time_from_bro.complete(worktime);
        }
      }
    } catch (Exception e) {
      Log.e(TAG, "join():", e);
    }
  }

  // Private
  ////////////////////////////////////////////////////////////
  final String TAG = "Java.LocalService";
  volatile int  startID  =  0;
  volatile Thread thread  =  null;
  volatile LocalThread local  =  null;
  volatile CompletableFuture<Long> time_from_bro = null;


  void local_start(ITestCase testCase, CompletableFuture<long[]> re)  {
    ++startID;
    if (startID > StaticConsts.MAX_INT)  {  startID  =  0;  }
    TestPresenter.subscribeOnBRO(this);
    synchronized (TAG) {
      local = new LocalThread(startID, testCase, re);
      thread = new Thread(local);
      thread.start();
    }
  }

  class LocalThread  implements Runnable {
    final ITestCase curCase;
    final int myID;
    volatile boolean mBound = false;
    volatile LocalService mService = null;
    final  CompletableFuture<long[]> result;
    long[]  times = new long[0];

    public LocalThread (int startID,  ITestCase testCase, CompletableFuture<long[]> re) {
      result  =  re;
      myID  =  startID;
      curCase  =  testCase;
    }

    @Override
    public void run() {
      if  (myID == startID) {
        try {
          if  (doBind()) {
            switch (curCase.get_case_type()) {
              case StaticConsts.CASE_Big_Byte_Array:
                doCASE_Big_Byte_Array();
                break;
              case StaticConsts.CASE_Strings:
                doCASE_Strings();
                break;
            }
          }  // if
        } catch (Exception e) {
          Log.e(TAG, "LocalThread.run():", e);
        }
        doUnBind();
        result.complete(times);
      }
    } // run()

    void  doCASE_Big_Byte_Array() throws  Exception  {
      //faux loop
      do {
        CompletableFuture<Long> future_bro = new CompletableFuture<Long>();
        synchronized (TAG) {
          time_from_bro = future_bro;
        }
        mService.onStartCASE_Big_Byte_Array(curCase.get_string_param_val(null),
            curCase.get_bool_param_val(null));
        if (myID != startID) {  break; }
        mService.receiveCASE_Big_Byte_Array(curCase.get_bytes_param_val(null));
        if (myID != startID) {  break; }
        long re = future_bro.get(StaticConsts.MSEC_ISDEAD,
            TimeUnit.MILLISECONDS);
        if (myID != startID) {  break; }
        long[] res = new long[1];
        res[0] = re;
        synchronized (TAG) {
          times  =  res;
        }
      } while (false);

    } //doCASE_Big_Byte_Array()

    void  doCASE_Strings() throws  Exception  {
      int cur_test_size = StaticConsts.START_ITEMS;
      final int max_test_size = curCase.get_int_param_val(null);
      StringBuilder sb = new StringBuilder(64);
      int id  =  0;
      ArrayList<Long> result = new ArrayList<>();
      while (myID == startID) {
        CompletableFuture<Long> future_bro = new CompletableFuture<Long>();
        synchronized (TAG) {
          time_from_bro = future_bro;
        }
         mService.onStartCASE_Strings(cur_test_size);
        for (int  i  =  0; myID == startID && i <= cur_test_size; ++i) {
          sb.setLength(0);
          sb.append('{').append(i).append('}');
          mService.receiveCASE_Strings(sb.toString());
        }
        if (myID != startID) {  break; }
        result.add(future_bro.get(StaticConsts.MSEC_ISDEAD,
            TimeUnit.MILLISECONDS) / (cur_test_size / StaticConsts.START_ITEMS));
        cur_test_size *= 10;
        ++id;
        if (cur_test_size > max_test_size) {  break; }
      }  //while (myID == startID)
      if (result.size() > 0) {
        int len  =  result.size();
        long[] re  =  new long[result.size()];
        for (int  i = 0;  i < len;  ++i) {
          re[i]  =  result.get(i);
        }
        synchronized (TAG) {
          times  =  re;
        }
      }
    } //doTest()


    boolean doBind() {
      boolean re = false;
      try {
        Intent intent = new Intent(SpecTheme.context, LocalService.class);
        SpecTheme.context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        long  startTime  =  System.currentTimeMillis();
        while (myID == startID) {
          if (mBound)  {
            re = true;
            break;
          }  else if (System.currentTimeMillis() - startTime
              > StaticConsts.MSEC_ISDEAD) {
            break;
          }
          Thread.sleep(StaticConsts.MSEC_SLEEP);
        }
      } catch (Exception e) {
        Log.e(TAG, "doBind():", e);
      }
      return  re;
    }

    void  doUnBind() {
      try {
        SpecTheme.context.unbindService(connection);
        mBound = false;
      } catch (Exception e) {
        Log.e(TAG, "doUnBind():", e);
      }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    final ServiceConnection connection = new ServiceConnection() {

      @Override
      public void onServiceConnected(ComponentName className,
                                     IBinder service) {
        // We've bound to LocalService, cast the IBinder and get LocalService instance
        LocalService.LocalBinder binder = (LocalService.LocalBinder) service;
        mService = binder.getService();
        mBound = true;
      }

      @Override
      public void onServiceDisconnected(ComponentName arg0) {
        mBound = false;
      }
    };
  }  //  LocalThread
}
