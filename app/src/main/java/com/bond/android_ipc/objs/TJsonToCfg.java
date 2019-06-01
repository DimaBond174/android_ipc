package com.bond.android_ipc.objs;
/*
 * This is the source code of SpecNet project
 * It is licensed under MIT License.
 *
 * Copyright (c) Dmitriy Bondarenko
 * feel free to contact me: specnet.messenger@gmail.com
 */

import android.util.Log;

import com.bond.android_ipc.cases.RegistryCases;
import com.bond.android_ipc.i.ITestCase;
import com.bond.android_ipc.i.ITester;
import com.bond.android_ipc.testers.RegistryTesters;
import com.github.mikephil.charting.data.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TJsonToCfg {
  // Public Java Interface :
  /////////////////////////////////////////////////////////////////
  public boolean is_valid  =  false;

  public void setJSON(String  json) {
    try {
       jsonObj  =  new JSONObject(json);
      //faux loop
      do {
        String str = jsonObj.getString(StaticConsts.PARM_test_case);
        if  (null == str)  break;
        cur_test_case  = RegistryCases.getCase(str);
        JSONArray arr_tst  =  jsonObj.getJSONArray(StaticConsts.PARM_testers);
        testers = new ArrayList<>();
        results = new HashMap<>();
        boolean data_exists  =  false;
        for (int  i  =  0;  i < arr_tst.length();  ++i)  {
          JSONObject o = arr_tst.getJSONObject(i);
          str  =  o.getString(StaticConsts.PARM_1tester);
          if  (null == str)  continue;
          ITester tester =  RegistryTesters.getTester(str);
          if  (null == tester)  continue;
          testers.add(tester);
          JSONArray arr_results  = null;
          try {
            arr_results = o.getJSONArray(StaticConsts.PARM_results);
          } catch (Exception e) {
            Log.e(TAG, "setJSON: No results for " + tester.get_test_name(), e);
          }
          if  (null == arr_results)  continue;
          int  len_results  =  arr_results.length();
          if  (len_results  <=  0) continue;
          ArrayList<Entry>  cur_results  =  new ArrayList<>();
          for (int  j = 0 ;  j  <  len_results;  ++j) {
            cur_results.add(new Entry(j,  arr_results.getLong(j)));
          }
          results.put(tester,  cur_results);
          data_exists  =  true;
        }
        cur_tester  =  -1;
        testers_count  =  testers.size();
        if (testers_count  >  0) {
          is_valid  =  true;
//          if (data_exists) {
//            TestPresenter.setProgress(100);
//          }
        }
      } while(false);
    }  catch (Exception e) {
      Log.e(TAG, "setJSON: ", e);
    }
  }

  public String getJSON() {
    if (is_valid) {
      StringBuilder sb = new StringBuilder(2048);
      sb.append("{\"test case\":\"")
          .append(cur_test_case.get_case_name()).append("\"")
          .append(cur_test_case.get_settings_for_JSON())
          .append(",\"testers\":[");
      boolean not_first  =  false;
      for (ITester tester:  testers) {
        if (not_first) {  sb.append(',');  }
        not_first  =  true;
        sb.append("{\"tester\":\"")
            .append(tester.get_test_name()).append("\"")
            .append(",\"results\":[");
        boolean not_first_result  =  false;
        ArrayList<Entry>  result_data  =  results.get(tester);
        if (null != result_data) {
          for (Entry entry : result_data) {
            long  val = (long)entry.getY();
            if (not_first_result) {  sb.append(',');  }
            not_first_result  =  true;
            sb.append(val);
          }
        }
        sb.append("]}");
      }
      sb.append("]}"); //testers
      return  sb.toString();
    }
    return null;
  }

  public String getParam(String str) {
    String  re  =  null;
    try {
      re  =  jsonObj.getString(str);
    } catch (Exception e) {
      //Log.e(TAG, "getParam() error:", e);
    }
    return  re;
  }

  public ITestCase  getCase() {
    if (is_valid)  return cur_test_case;
    return null;
  }

  public ITester getCurTester() {
    if  (is_valid  && cur_tester >=0  &&  cur_tester < testers_count) {
      return testers.get(cur_tester);
    }
    return null;
  }

  public ITester  getNextTester() {
    ++cur_tester;
    return getCurTester();
  }

  public  int getTesters_count()  {
    return  testers_count;
  }

  public  void setllTesters()  {
    cur_tester = -1;
  }

  public ArrayList<ITester> getTesters()  {
    return  testers;
  }

  public void putResults(Map<ITester, ArrayList<Entry>>  cur_results)  {
    results  =  cur_results;
  }

  public Map<ITester, ArrayList<Entry>> getResults()  {
    return results;
  }

  public int get_case_START_ITEMS_val() {
    return cur_test_case.get_START_ITEMS_val();
  }

  //  Private Incapsulation :
  /////////////////////////////////////////////////////////////////
  static final String TAG = "TJsonToCfg";
  ITestCase cur_test_case  =  null;
  JSONObject jsonObj  =  null;
  ArrayList<ITester> testers  =  null;
  Map<ITester, ArrayList<Entry>>  results  =  null;
  volatile int cur_tester  =  -1;
  volatile int testers_count  =  -1;

}
