package com.bond.android_ipc.gui.charts;

import android.content.Context;
import android.util.Log;

import com.bond.android_ipc.gui.SpecTheme;
import com.bond.android_ipc.gui.widgets.WSimpleTable;
import com.bond.android_ipc.i.ITester;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.Map;

public class UiChartBars extends UiChart {
  BarChart chart;

  public UiChartBars(Context context) {
    super(context);
    chart = new BarChart(context);
    chart.setDrawGridBackground(false);
    chart.getDescription().setEnabled(false);
    chart.setDrawBorders(false);

    chart.getAxisLeft().setEnabled(false);
    chart.getAxisRight().setDrawAxisLine(false);
    chart.getAxisRight().setDrawGridLines(false);
    chart.getXAxis().setDrawAxisLine(false);
    chart.getXAxis().setDrawGridLines(false);

    // enable touch gestures
    chart.setTouchEnabled(true);

    // enable scaling and dragging
    chart.setDragEnabled(true);
    chart.setScaleEnabled(true);

    // if disabled, scaling can be done on x- and y-axis separately
    chart.setPinchZoom(false);
    chart.setBackgroundColor(SpecTheme.KeyBoardColor);
    addView(chart);
  }

  @Override
  public void clearData() {
    chart.clear();
    chart.invalidate();
  }

  @Override
  public void setData(int start_items, WSimpleTable simple_table,
                      ArrayList<ITester> testers,
                      Map<ITester, ArrayList<Entry>> results) {
    clearData();
    try {
      ArrayList<BarEntry> barEntries = new ArrayList<>();
      ArrayList<Integer> barColors = new ArrayList<>();
      int len_testers  =  testers.size();
      int len_results = 0;
      for (int  i  =  0;  i <  len_testers;  ++i)  {
        ITester cur_tester  =  testers.get(i);
        if (null == cur_tester)  continue;
        ArrayList<Entry> values  =  results.get(cur_tester);
        if (null == values)  continue;
        if (values.size()  >  len_results)  {  len_results =  values.size();  }
        barEntries.add(new BarEntry(i, values.get(0).getY()));
        barColors.add(SpecTheme.color_array[i % SpecTheme.color_array.length]);
      }


      BarDataSet set1 = new BarDataSet(barEntries, "");
      set1.setValueTextColors(barColors);
      set1.setColors(barColors);
      ArrayList<IBarDataSet> dataSets = new ArrayList<>();
      dataSets.add(set1);

      BarData data = new BarData(dataSets);
      data.setValueTextSize(10f);
      data.setBarWidth(0.9f);

      chart.setData(data);

      String  table_data[][] = new String[len_testers + 1][len_results + 1];
      int  table_colors[]  =  new int[len_testers + 1];
      table_colors[0]  = SpecTheme.PBlackColor;
      table_data[0][0] = "Test in micro sec";
      int  cur_items_mult  =  1;
      StringBuilder sb = new StringBuilder(32);
      for (int  j  =  0;  j <  len_results;  ++j)  {
        int  cur_max_items = start_items  *  cur_items_mult;
        sb.append(cur_max_items);
        if  (cur_items_mult  >  1) {
          sb.append('/').append(cur_items_mult);
        }
        table_data[0][j + 1] = sb.toString();
        cur_items_mult *= 10;
        sb.setLength(0);
      }

      for (int  i  =  0;  i <  len_testers;  ++i)  {
        table_colors[i + 1]  = SpecTheme.color_array[i % SpecTheme.color_array.length];
        ITester cur_tester  =  testers.get(i);
        if (null == cur_tester)  continue;
        table_data[i + 1][0] = cur_tester.get_test_name();
        ArrayList<Entry> values  =  results.get(cur_tester);
        if (null == values)  continue;
        int values_len = values.size();
        for (int  j  =  0;  j < values_len  &&  j <  len_results;  ++j)  {
          long  val  =  (long)values.get(j).getY();
          sb.setLength(0);
          sb.append(val);
          table_data[i + 1][j + 1] = sb.toString();
        }
      }
      simple_table.setTable_data(table_data,  table_colors);
      chart.invalidate();
    } catch (Exception e) {
      Log.e(TAG, "prepareChart() error: ", e);
    }
  }


  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    measureChildWithMargins(chart, widthMeasureSpec, 0,
        heightMeasureSpec, 0);
    setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
        MeasureSpec.getSize(heightMeasureSpec));
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    //super.onLayout(changed, left, top, right, bottom);
    chart.layout(0, 0, right - left,  bottom - top);
  }

}


