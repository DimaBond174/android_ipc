package com.bond.android_ipc.servs;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bond.android_ipc.objs.StaticConsts;

public class ServCommon {
  static final String TAG = "ServCommon";

  /* WARN: LocalBroadcastManager does not works with other process !!! */
  public static void sendToUILocal(long time,  Context ctx) {
    Log.w(TAG, "sendToUI->sendBroadcast :" + String.valueOf(time));
      try {
          Intent i = new Intent(StaticConsts.UI_BRO_URI);
          i.putExtra(StaticConsts.EXTR_TIME, time);
          LocalBroadcastManager.getInstance(ctx).sendBroadcast(i);
      } catch (Exception e) {
        Log.e(TAG, "sendToUI->sendBroadcast error:", e);
      }
  }  //sendToUI

  public static void sendToUI(long time,  Context ctx) {
    Log.w(TAG, "sendToUI->sendBroadcast :" + String.valueOf(time));
    try {
      Intent i = new Intent(StaticConsts.UI_BRO_URI);
      i.putExtra(StaticConsts.EXTR_TIME, time);
      ctx.sendBroadcast(i);
    } catch (Exception e) {
      Log.e(TAG, "sendToUI->sendBroadcast error:", e);
    }
  }  //sendToUI
}
