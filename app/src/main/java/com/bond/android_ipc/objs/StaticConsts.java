package com.bond.android_ipc.objs;

import android.view.Menu;

public class StaticConsts {
  public static final int START_ITEMS  =  1000;
  public static final int MAX_ITEMS  =  10000000;
  public static final int MAX_INT  =  2147000647;
  public static final long MSEC_ISDEAD  =  25000;  // millisec
  public static final long MSEC_SLEEP  =  100;

  //Test Case Types:
  public static final int CASE_Big_Byte_Array  =  0;
  public static final int CASE_Strings  =  1;

  public static final String UI_BRO_URI = "com.bond.android_ipc.UI.BRO.URI";
  public static final String EXTR_TIME = "time";
  public static final String FILES_URI_LIST= "files URI list";

  public static final int RQS_GET_PERMITIONS  =  1;
  public static final int RQS_GET_CONTENT  =  2;
  public static final int FrgActivityForResult  =  1;

  public static final String PARM_test_case = "test case";
  public static final String PARM_max_items = "max items";
  public static final String PARM_byte_data = "byte data";
  public static final String PARM_check = "check by recipient";
  public static final String PARM_testers = "testers";
  public static final String PARM_1tester = "tester";
  public static final String PARM_results = "results";
  public static final String PARM_save_time = "save time";
  public static final String PARM_save_file = "save file";

  public static final String  json_params[] = new String[] {
      PARM_test_case,
      PARM_max_items,
  };
  public static final String HistoryFolder = "history";
  public static final String DefConfig = "default_cfg";

  public static final int MENU_UiMain  =  Menu.FIRST;
  public static final String FirstFragTAG = "UiMainFrag";
  public static final int MENU_UiHistory  =  Menu.FIRST  +  1;
  public static final String UiHistoryTAG = "UiHistoryFrag";
  public static final int MENU_UiHistoryClear  =  Menu.FIRST  +  2;
  public static final int MENU_UiSettings  =  Menu.FIRST  +  3;
  public static final String UiSettingsTAG = "UiSettingsFrag";
  public static final int MENU_UiSettingsDef  =  Menu.FIRST  +  4;
  public static final int MENU_Exit =  Menu.FIRST  +  5;
}
