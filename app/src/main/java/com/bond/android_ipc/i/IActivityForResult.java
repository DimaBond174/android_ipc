package com.bond.android_ipc.i;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

public interface IActivityForResult  {
  void resultActivityForResult(AppCompatActivity activity, int requestCode, int resultCode, Intent data);
}
