package com.bond.android_ipc.i;
/*
 * This is the source code of SpecNet project
 * It is licensed under MIT License.
 *
 * Copyright (c) Dmitriy Bondarenko
 * feel free to contact me: specnet.messenger@gmail.com
 */

import com.bond.android_ipc.objs.TJsonToCfg;
import com.bond.android_ipc.objs.TestParam;


public interface ITestCase  {
  boolean  startTestCase(TJsonToCfg  cfg);
  void  stop();
  String  get_case_name();
  String  get_settings_for_JSON();
  TestParam[]  get_required_params();

  /*
   *  0 == Big Byte Array
   *  1 == Strings
   *  ...
   * */
  int  get_case_type();
  boolean  get_bool_param_val(String  param_name);
  int  get_int_param_val(String  param_name);
  String  get_string_param_val(String  param_name);
  byte[]  get_bytes_param_val(String  param_name);
  int get_START_ITEMS_val();
}
