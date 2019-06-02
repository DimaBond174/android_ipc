package com.bond.android_ipc.testers;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.bond.android_ipc.TestPresenter;
import com.bond.android_ipc.gui.SpecTheme;
import com.bond.android_ipc.i.ITestCase;
import com.bond.android_ipc.i.ITester;
import com.bond.android_ipc.objs.FileAdapter;
import com.bond.android_ipc.objs.StaticConsts;
import com.bond.android_ipc.servs.ContentPullService;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TestContentProvider  implements ITester  {

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

  @Override
  public String get_test_name() {
    return TAG;
  }

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
  final String TAG = "Java.ContentProvider";
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

            switch (curCase.get_case_type()) {
              case StaticConsts.CASE_Big_Byte_Array:
                doCASE_Big_Byte_Array();
                break;
              case StaticConsts.CASE_Strings:
                doCASE_Strings();
                break;
            }

        } catch (Exception e) {
          Log.e(TAG, "LocalThread.run():", e);
        }
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

        Intent intent = new Intent(SpecTheme.context, ContentPullService.class);
        intent.putExtra(StaticConsts.PARM_test_case, curCase.get_case_type());
        intent.putExtra(StaticConsts.PARM_1tester, ContentPullService.TESTER_TestContentProvider);
        intent.putExtra(StaticConsts.PARM_byte_data, curCase.get_string_param_val(null));
        intent.putExtra(StaticConsts.PARM_check, curCase.get_bool_param_val(null));

        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileAdapter.create_export_Uri(
            curCase.get_string_param_val(null), SpecTheme.context);
        ClipData clipData = new ClipData(
            new ClipDescription(StaticConsts.FILES_URI_LIST,
                new String[]{ClipDescription.MIMETYPE_TEXT_URILIST}),
            new ClipData.Item(uri));
        intent.setClipData(clipData);

        SpecTheme.context.startService(intent);

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

        Intent intent = new Intent(SpecTheme.context, ContentPullService.class);
        intent.putExtra(StaticConsts.PARM_test_case, curCase.get_case_type());
        intent.putExtra(StaticConsts.PARM_1tester, ContentPullService.TESTER_TestContentProvider);
        intent.putExtra(StaticConsts.PARM_max_items, cur_test_size);
        SpecTheme.context.startService(intent);

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


  }  //  LocalThread

}
