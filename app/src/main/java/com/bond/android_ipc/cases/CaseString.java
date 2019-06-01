package com.bond.android_ipc.cases;
/*
 * This is the source code of SpecNet project
 * It is licensed under MIT License.
 *
 * Copyright (c) Dmitriy Bondarenko
 * feel free to contact me: specnet.messenger@gmail.com
 */

import android.util.Log;
import com.bond.android_ipc.TestPresenter;
import com.bond.android_ipc.i.ITestCase;
import com.bond.android_ipc.i.ITester;
import com.bond.android_ipc.objs.StaticConsts;
import com.bond.android_ipc.objs.TJsonToCfg;
import com.bond.android_ipc.objs.TestParam;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


public class CaseString implements ITestCase {
  // Public Java Interface :
  /////////////////////////////////////////////////////////////////
  volatile int  expected_max_items = 0;

  @Override
  public String get_case_name() {
    return "Case send Strings";
  }

  @Override
  public int get_case_type() {
    return StaticConsts.CASE_Strings;
  }

  @Override
  public boolean get_bool_param_val(String param_name) {
    return false;
  }

  @Override
  public byte[] get_bytes_param_val(String param_name) {
    return new byte[0];
  }

  @Override
  public int get_int_param_val(String param_name) {
    return expected_max_items;
  }

  @Override
  public String get_string_param_val(String param_name) {
    return null;
  }

  @Override
  public int get_START_ITEMS_val() {
    return StaticConsts.START_ITEMS;
  }

  @Override
  public TestParam[] get_required_params() {
    return new TestParam[] {
        new TestParam( TestParam.TYPE_ITEMS,
            StaticConsts.PARM_max_items, "10000",
            1000,  1000000)
    };
  }

  @Override
  public boolean startTestCase(TJsonToCfg cfg) {
    boolean  re  =  false;
    try {
      //faux loop
      do  {
        String str  =  cfg.getParam(StaticConsts.PARM_max_items);
        if (null == str)  {  break; }
        int max_items  =  Integer.parseInt(str);

        synchronized (TAG) {
          caseThread  =  new CaseThread(id_case_start,
              max_items, cfg);
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
    sb.append(",\"").append(StaticConsts.PARM_max_items)
        .append("\":\"")
        .append(expected_max_items).append("\"") ;
    return sb.toString();
  }

  //  Private Incapsulation :
  /////////////////////////////////////////////////////////////////
  private final String TAG = "CaseString";
  volatile int  id_case_start  =  0;
  CaseThread  caseThread  =  null;


  private class CaseThread implements Runnable  {
    // Public Java Interface :
    /////////////////////////////////////////////////////////////////


    CaseThread(int  start_id,  int  max_items_, TJsonToCfg cfg_)  {
      i_work_for_id  =  start_id;
      expected_max_items  =  max_items_;
      cfg  =  cfg_;
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
    final int  progress_step;
    volatile int  cur_progress  =  0;  // 0  to 1000000  (/10000 for %)
    Thread local_thread  =  null;

    // Local usage only:
    final int i_work_for_id;
    static final String HTAG = "CaseThread";

    @Override
    public void run() {
      try {
        provideTests();
      } catch (Exception e) {
        Log.e(HTAG,"Exception:", e);
      }
    }

    private void provideTests() throws Exception  {
      Map<ITester, ArrayList<Entry>> results = new HashMap<>();
      ITester cur_tester;
      cfg.setllTesters();
      while (keep_run  && null  != (cur_tester = cfg.getNextTester())) {
        CompletableFuture future = cur_tester.start(CaseString.this);
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
