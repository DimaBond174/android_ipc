package com.bond.android_ipc.gui.charts;

import android.content.Context;
import android.widget.FrameLayout;

import com.bond.android_ipc.gui.widgets.WSimpleTable;
import com.bond.android_ipc.i.ITester;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Map;

public abstract class UiChart extends FrameLayout  {
  protected final String  TAG = "UiChart";

  public UiChart(Context context) {
    super(context);
  }

  public abstract  void clearData();

  public abstract void setData(int start_items, WSimpleTable simple_table,
      ArrayList<ITester> testers,  Map<ITester, ArrayList<Entry>> results);
}
