package com.bond.android_ipc.cases;
/*
 * This is the source code of SpecNet project
 * It is licensed under MIT License.
 *
 * Copyright (c) Dmitriy Bondarenko
 * feel free to contact me: specnet.messenger@gmail.com
 */

import android.util.Log;

import com.bond.android_ipc.R;
import com.bond.android_ipc.TestPresenter;
import com.bond.android_ipc.gui.SpecTheme;
import com.bond.android_ipc.i.ITestCase;
import com.bond.android_ipc.i.ITester;
import com.bond.android_ipc.objs.FileAdapter;
import com.bond.android_ipc.objs.StaticConsts;
import com.bond.android_ipc.objs.TJsonToCfg;
import com.bond.android_ipc.objs.TestParam;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;


public class CaseBigByteArray implements ITestCase {
  // Public Java Interface :
  /////////////////////////////////////////////////////////////////
  volatile String byte_data_file = null;
  volatile byte[]  test_byte_data  =  null;
  volatile  boolean check_by_recipient = false;

  @Override
  public String get_case_name() {
    return "Case send byte array";
  }

  @Override
  public int get_case_type() {
    return StaticConsts.CASE_Big_Byte_Array;
  }

  @Override
  public boolean get_bool_param_val(String param_name) {
    return check_by_recipient;
  }

  @Override
  public int get_int_param_val(String param_name) {
    return 0;
  }

  @Override
  public String get_string_param_val(String param_name) {
    synchronized (TAG) {
      return byte_data_file;
    }
  }

  @Override
  public byte[] get_bytes_param_val(String param_name) {
    synchronized (TAG) {
      return test_byte_data;
    }
  }

  @Override
  public int get_START_ITEMS_val() {
    return 1;
  }

  @Override
  public TestParam[] get_required_params() {
    return new TestParam[] {
        new TestParam( TestParam.TYPE_FILE,
            StaticConsts.PARM_byte_data, "test_data.png"),
        new TestParam( TestParam.TYPE_BOOL,
            StaticConsts.PARM_check, "0"),
    };
  }

  @Override
  public boolean startTestCase(TJsonToCfg cfg) {
    boolean  re  =  false;
    try {
      //faux loop
      do  {
        String str  =  cfg.getParam(StaticConsts.PARM_check);
        if (null == str) {
          check_by_recipient =  false;
        }  else  {
          check_by_recipient =  1
              == Integer.parseInt(cfg.getParam(StaticConsts.PARM_check));
        }

        str  =  cfg.getParam(StaticConsts.PARM_byte_data);
        if (null == str)  break;
        if (!FileAdapter.existsFile(str, SpecTheme.context)) {
          final String  errFile = TestPresenter.getRstring(R.string.strFileNotExist) + " " + str;
          TestPresenter.runOnGUIthreadDelay(new Runnable() {
            @Override
            public void run() {
              TestPresenter.getGUInterface().showMessage(errFile);
            }
          },  1000);
          break;
        }

        synchronized (TAG) {
          byte_data_file  = str;
          caseThread  =  new CaseThread(id_case_start, str, cfg);
        }
        caseThread.start();
        re = true;
      }  while(false);
    } catch (Exception e) {
      Log.e(TAG, "startTestCase  error:", e);
    }
    return re;
  }


  @Override
  public void stop() {
    synchronized (TAG) {
      ++id_case_start;
      if (id_case_start > 1000000) id_case_start  =  1;
      if (null  !=  caseThread) {
        caseThread.stop();
        caseThread  =  null;
      }
    }
  }

  @Override
  public String get_settings_for_JSON() {
    StringBuilder sb = new StringBuilder(128);
    sb.append(",\"").append(StaticConsts.PARM_byte_data)
        .append("\":\"");
    synchronized (TAG) {
      sb.append(byte_data_file).append("\"") ;
    }
    return sb.toString();
  }

  //  Private Incapsulation :
  /////////////////////////////////////////////////////////////////
  private final String TAG = "CaseBigByteArray";
  volatile int  id_case_start  =  0;
//  CompletableFuture<ArrayList<byte[]>> test_data
//      = CompletableFuture.supplyAsync(new Supplier<ArrayList<byte[]>>() {
//    @Override
//    public ArrayList<byte[]> get() {
//      ArrayList<byte[]> re = new ArrayList<byte[]>(4);
//      int len = StaticConsts.START_ITEMS;
//      int i = 0;
//      while (len <= StaticConsts.MAX_ITEMS) {
//        byte[]  arr = new byte[len];
//        byte b = 0;
//        for (int  j = 0 ;  j  <  len;  ++j) {
//          arr[j]  =  b++;
//        }
//        re.add(i, arr);
//        ++i;
//        len *= 10;
//      }
//      return re;
//    }
//  });

  CaseThread  caseThread  =  null;


  private class CaseThread implements Runnable  {
    // Public Java Interface :
    /////////////////////////////////////////////////////////////////


    CaseThread(int  start_id, String byte_data_file, TJsonToCfg cfg_)  {
      i_work_for_id  =  start_id;
      cfg  =  cfg_;
      data_file = byte_data_file;
      int progress_step_ = 900000 / (cfg.getTesters_count() );
      if (0 == progress_step_) progress_step_  =  1;
      progress_step = progress_step_;
    }

    public void start() {
      local_thread = new Thread(this);
      local_thread.start();
    }

    public void stop() {
      keep_run  =  false;
      local_thread  =  null;
    }

    //  Private Incapsulation :
    /////////////////////////////////////////////////////////////////
    volatile boolean keep_run  =  true;

    // Protected by the sequence of the algorithm: read only after all writes completed
    final TJsonToCfg cfg;
    final String data_file;

    final int  progress_step;
    volatile int  cur_progress  =  0;  // 0  to 1000000  (/10000 for %)
    Thread local_thread  =  null;

    // Local usage only:
    final int i_work_for_id;
    static final String HTAG = "CaseThread";

    @Override
    public void run() {
      try {
        if (loadData())  {
          provideTests();
        }
      } catch (Exception e) {
        Log.e(HTAG,"Exception:", e);
      }
    }


    private boolean loadData() {
      boolean re  =  false;
      synchronized (TAG) {
        test_byte_data = null;
      }
      byte[] data  =  FileAdapter.readFileBytes(data_file,  SpecTheme.context);
      if (data.length >0 ) {
        synchronized (TAG) {
          test_byte_data = data;
        }
        re = true;
      }
      if (re && keep_run  &&  i_work_for_id  ==  id_case_start) {
        cur_progress = 100000;
        TestPresenter.setProgress1(10);
      }
      return re;
    }  // loadData

    private void provideTests() throws Exception  {
      Map<ITester, ArrayList<Entry>> results = new HashMap<>();
      ITester cur_tester;
      cfg.setllTesters();
      while (keep_run  && null  != (cur_tester = cfg.getNextTester())) {
        CompletableFuture future = cur_tester.start(CaseBigByteArray.this);
        long[]  res = (long[])future.get();
        ArrayList<Entry>  cur_times = new ArrayList<>();
        for (int i = 0;  i <  res.length; ++i) {
          cur_times.add(new Entry(i, res[i]));
        }
        results.put(cur_tester, cur_times);
        if (keep_run  &&  i_work_for_id  ==  id_case_start) {
          cur_progress += progress_step;
          int progress = cur_progress / 10000;
          if (progress > 99) progress = 99;
          TestPresenter.setProgress1(progress);
        }
      } // tester

      if (keep_run  &&  i_work_for_id  ==  id_case_start) {
        cfg.putResults(results);
        TestPresenter.saveResultsToHistory(cfg);
        TestPresenter.setProgress1(100);
      }
    }

  } // CaseThread

}  //  CaseString
