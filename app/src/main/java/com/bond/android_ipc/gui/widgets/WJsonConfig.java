package com.bond.android_ipc.gui.widgets;
/*
 * This is the source code of SpecNet project
 * It is licensed under MIT License.
 *
 * Copyright (c) Dmitriy Bondarenko
 * feel free to contact me: specnet.messenger@gmail.com
 */

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bond.android_ipc.gui.SpecTheme;
import com.bond.android_ipc.objs.StaticConsts;
import com.bond.android_ipc.objs.TJsonToCfg;

import java.util.ArrayList;

public class WJsonConfig extends FrameLayout {
  public static final String TAG = "WJsonConfig";
  final ArrayList<TextView> json_params = new ArrayList<>();
  Papirus  papirus;
  int  papirus_height = 0;

  public WJsonConfig(Context context)  {
    super(context);
    papirus  =  new Papirus(context);
    addView(papirus, new LayoutParams(LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT));
  }  // constructor

  public  void clear() {
    papirus.removeAllViews();
    json_params.clear();
    requestLayout();
  }


  public void  setConfig(TJsonToCfg  cfg) {
    if (null == cfg)  return;
    papirus.removeAllViews();
    json_params.clear();
    papirus_height = 0;
    if (null != cfg  && cfg.is_valid) {
      try {
        int  widthMeasureSpec  =  MeasureSpec.makeMeasureSpec(SpecTheme.dpMaxEmojiKeyboard, MeasureSpec.AT_MOST);
        int  heightMeasureSpec  =  MeasureSpec.makeMeasureSpec(SpecTheme.dpMaxEmojiKeyboard, MeasureSpec.AT_MOST);
        for (String param : StaticConsts.json_params) {
          String str = cfg.getParam(param);
          if  (null != str)  {
            TextView txt  =  new TextView(SpecTheme.context);
            txt.setSingleLine(true);
            txt.setMaxLines(1);
            txt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, SpecTheme.InfoTextSize);
            txt.setTextColor(SpecTheme.PDimGrayColor);
            txt.setText(param  + " : " + str);
            papirus.addView(txt);
            json_params.add(txt);
            measureChildWithMargins(txt, widthMeasureSpec, 0,
                heightMeasureSpec, 0);
            papirus_height  +=  txt.getMeasuredHeight() + SpecTheme.dpButtonPadding;
          }
        }
      } catch ( Exception e) {
        Log.e(TAG, "setConfig() error:", e);
      }
    }
    requestLayout();
  }


  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int widht  =  MeasureSpec.getSize(widthMeasureSpec);
    measureChildWithMargins(papirus, widthMeasureSpec, 0,
        heightMeasureSpec, 0);
    setMeasuredDimension(widht, papirus_height);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    papirus.layout(0, 0, right - left, bottom - top);
  }

  private class Papirus extends FrameLayout {
    public Papirus(Context context) {
      super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      int widht  =  MeasureSpec.getSize(widthMeasureSpec);
      setMeasuredDimension(widht,  papirus_height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
      int widht = right - left - SpecTheme.dpButtonPadding;
      int count = getChildCount();
      int curTop = 0;
      for (int i = 0; i < count; ++i) {
        View child = getChildAt(i);
        int h = child.getMeasuredHeight();
        child.layout(SpecTheme.dpButtonPadding, curTop, widht, curTop + h);
        curTop += h + SpecTheme.dpButtonPadding;
      }
    }

  }  //  Papirus

}
