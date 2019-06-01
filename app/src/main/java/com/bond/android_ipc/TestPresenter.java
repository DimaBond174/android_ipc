package com.bond.android_ipc;
/*
 * This is the source code of SpecNet project
 * It is licensed under MIT License.
 *
 * Copyright (c) Dmitriy Bondarenko
 * feel free to contact me: specnet.messenger@gmail.com
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.bond.android_ipc.gui.SpecTheme;
import com.bond.android_ipc.i.ITestCase;
import com.bond.android_ipc.i.ITester;
import com.bond.android_ipc.i.IView;
import com.bond.android_ipc.objs.FileAdapter;
import com.bond.android_ipc.objs.StaticConsts;
import com.bond.android_ipc.objs.TJsonToCfg;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
 *  MVP presenter
 */
public class TestPresenter {
  // Public Java Interface :
  /////////////////////////////////////////////////////////////////
  // public for GUI single thread, thread unsafe:
  public static void  setGUInterface(IView  iView) {
    cur_activity  =  iView;
    gui_on_screen  =  true;
//    if  (null == getConfig())  {
//      TJsonToCfg cfg  =  new TJsonToCfg();
//      cfg.setDefaultTestCase();
//      setConfig(cfg);
//    }
    registerBro();
  }

  public static IView getGUInterface()  {
    return  cur_activity;
  }

  public static void onGUIstop()  {
    gui_on_screen  =  false;
    unregisterBro();
    SpecTheme.onDestroy();
    cur_activity  =  null;
  }

  public static String getRstring(int  id)  {
    IView gui = getGUInterface();
    if (null  !=  gui)  {
      return  gui.getForDialogCtx().getString(id);
    }
    return "";
  }

  public static void onFirstStart()  {
    if  (null == getConfig()) {
      String json = FileAdapter.loadAssetString(SpecTheme.context, StaticConsts.DefConfig);
      if (!json.isEmpty()) {
        TJsonToCfg  cfg  =  new TJsonToCfg();
        cfg.setJSON(json);
        setConfig(cfg);
      }
    }
  }

  /////////////////////////////////////////////////////////////////
  // public for multi thread, thread safe:
  public static volatile boolean  gui_on_screen  =  false;

  public  static void  setSettings(String  json) {
    stopProgress();
  }

  public static int  getProgress1() {  return progress1; }
  public static int  getProgress2() {  return progress2; }

  public static void  stopProgress()  {
    try {
      if (null != cur_test_case) {
        cur_test_case.stop();
      }
      progress1  =  0;
      progress2  =  0;
      cur_activity.onPresenterChange();
    } catch (Exception e) {
      Log.e(TAG, "stopProgress  error:", e);
    }
    progress1  =  0;
    progress2  =  0;

  }

  public static void  setProgress1(int  progress_)  {
    progress1  =  progress_;
    runOnGUIthread(new Runnable() {
      @Override
      public void run() {
        try {
          cur_activity.onPresenterChange();
        } catch (Exception e) {}
      }
    });
  }

  public static void  setProgress2(int  progress_)  {
    progress2  =  progress_;
    runOnGUIthread(new Runnable() {
      @Override
      public void run() {
        try {
          cur_activity.onPresenterChange();
        } catch (Exception e) {}
      }
    });
  }

  public static void saveResultsToHistory(TJsonToCfg cfg) {
    try {
      String json  =  cfg.getJSON();
      if (null != json) {
        FileAdapter.saveJsonHistory(StaticConsts.HistoryFolder,
            json, SpecTheme.context);
      }
    } catch ( Exception e) {
      Log.e(TAG, "On save history error : ", e);
    }
  }

  public static void  startProgress()  {
    if  (0 > progress1  &&  progress1 < 100)  {   return;  }
    TJsonToCfg cfg  =  getConfig();
    if  (null  ==  cfg ||  !cfg.is_valid)  {
      try {
        cur_activity.showMessage(getRstring(R.string.strConfig_error));
      } catch (Exception e) {}
      return;
    }
    w_Cfg_Lock.lock();
    try {
      cur_test_case  =  cfg.getCase();
      if  (cur_test_case.startTestCase(cfg))  {
        progress1  =  1;
        cur_activity.onPresenterChange();
      }  else {
        cur_activity.showMessage(getRstring(R.string.strTestCase_fail));
      }
    } catch (Exception e) {}
    w_Cfg_Lock.unlock();
  }

  public static TJsonToCfg getConfig() {
    r_Cfg_Lock.lock();
    TJsonToCfg  re  =  cfg;
    r_Cfg_Lock.unlock();
    return  re;
  }

  public  static void setConfig(TJsonToCfg  jsonToCfg) {
    w_Cfg_Lock.lock();
    cfg  =  jsonToCfg;
    w_Cfg_Lock.unlock();
    if (jsonToCfg.is_valid)  {
      setProgress1(100);
    }
  }

  public static void runOnGUIthread (Runnable r) {
    if (gui_on_screen) {
      try {
        IView gui = getGUInterface();
        if (null  !=  gui)  {
          gui.getGuiHandler().post(r);
        }
      } catch (Exception e) {
        Log.e(TAG, "runOnGUIthread error:", e );
      }
    }
  }

  public static void runOnGUIthreadDelay (Runnable r,  long delay) {
    if (gui_on_screen) {
      try {
        IView gui = getGUInterface();
        if (null  !=  gui)  {
          gui.getGuiHandler().postDelayed(r, delay);
        }
      } catch (Exception e) {
        Log.e(TAG, "runOnGUIthread error:", e );
      }
    }
  }

  public static void subscribeOnBRO(ITester  tester) {
    w_Cfg_Lock.lock();
    cur_tester = tester;
    w_Cfg_Lock.unlock();
  }

  //  Private Incapsulation :
  /////////////////////////////////////////////////////////////////
  // private for GUI single thread, thread unsafe:
  static IView cur_activity  =  null;


  /////////////////////////////////////////////////////////////////
  // private for multi thread, thread safe:
  private static final String TAG = "TestPresenter";
  static volatile int progress1  =  0;  // !=0 if test is process
  static volatile int progress2  =  0;  // !=0 if test is process


  ////////////////
  static final ReentrantReadWriteLock rw_Cfg_Lock  =  new ReentrantReadWriteLock();
  static final Lock r_Cfg_Lock = rw_Cfg_Lock.readLock();
  static final Lock w_Cfg_Lock = rw_Cfg_Lock.writeLock();
  static volatile  TJsonToCfg cfg  =  null;
  static volatile  ITestCase cur_test_case  =  null;
  static volatile  ITester cur_tester  =  null;
  ////////////////
  //private static volatile int lastBroTime = 0;
  static final BroadcastReceiver bro = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      //lastBroTime  =  (int)(System.currentTimeMillis() % StaticConsts.MAX_INT);
      r_Cfg_Lock.lock();
      ITester  tester  =  cur_tester;
      r_Cfg_Lock.unlock();
      if (null != tester) {
        long time = intent.getLongExtra(StaticConsts.EXTR_TIME, 0);
        cur_tester.onBroAnswer(time);
      }
    }
  };

  static void unregisterBro() {
    try {
        //not work with other process: LocalBroadcastManager.getInstance((Context) cur_activity).unregisterReceiver(bro);
      ((Context) cur_activity).unregisterReceiver(bro);
    } catch (Exception e) {}
  }

  static void  registerBro() {
    try {
      IntentFilter filter = new IntentFilter();
      filter.addAction(StaticConsts.UI_BRO_URI);
      unregisterBro();
      //not work with other process: LocalBroadcastManager.getInstance((Context) cur_activity).registerReceiver(bro, filter);
      ((Context) cur_activity).registerReceiver(bro, filter);
    } catch (Exception e) {
      Log.e(TAG, "registerBro():", e);
    }
  }

  /*
  *   NDK staff
  * */
  // Used to load the 'native-lib' library on application startup.
  static {
    System.loadLibrary("native-lib");
  }

  /**
   * A native method that is implemented by the 'native-lib' native library,
   * which is packaged with this application.
   */
  public static native String stringFromJNI();
//  public static native void setNDKtestCaseInt3(int  rawData[],  int  rawDataLen);
//  public static native void setNDKtestCaseKeyString(String  strData,  int  maxItems);
//  public static native void warmUP(int cppTesterID,  int  capacity);
//  public static native void runCppTest(int  insert_threads,
//    int  search_threads,  int  max_items);
//  public static native void stopCppTest();
}
