package com.bond.android_ipc.gui.frags;
/*
 * This is the source code of SpecNet project
 * It is licensed under MIT License.
 *
 * Copyright (c) Dmitriy Bondarenko
 * feel free to contact me: specnet.messenger@gmail.com
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bond.android_ipc.R;
import com.bond.android_ipc.TestPresenter;
import com.bond.android_ipc.gui.SpecTheme;
import com.bond.android_ipc.gui.widgets.WDualProgress;
import com.bond.android_ipc.gui.widgets.WJsonConfig;
import com.bond.android_ipc.gui.widgets.WSimpleTable;
import com.bond.android_ipc.gui.charts.UiChart;
import com.bond.android_ipc.gui.charts.UiChartBars;
import com.bond.android_ipc.gui.charts.UiChartLines;
import com.bond.android_ipc.i.ITestCase;
import com.bond.android_ipc.i.ITester;
import com.bond.android_ipc.objs.StaticConsts;
import com.bond.android_ipc.objs.TJsonToCfg;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Map;

public class UiMainFrag extends UiFragment {

  ScrollView scrollView;
  ScrollView scrollViewTable;
  InnerPapirus innerPapirus;
  OuterPapirus outerPapirus;
  TextView txt_case_caption;
  WDualProgress progressBar;
  WJsonConfig wJsonConfig;
  TextView txt_result_caption;

  UiChart chart;
  WSimpleTable simple_table;

  @Override
  public void onDestroy() {
    removeAllViews();
    wJsonConfig = null;
    scrollView = null;
    scrollViewTable = null;
    innerPapirus = null;
    outerPapirus = null;
    txt_case_caption = null;
    txt_result_caption = null;
    progressBar = null;
  }


  @Override
  public String getTitle() {
    //return TestPresenter.stringFromJNI();
    return TestPresenter.getRstring(R.string.strUiMainFrag);
  }

  @Override
  public long getType() {
    return 0;
  }

  public UiMainFrag(Context context, FragmentKey fragmentKey) {
    super(context, fragmentKey);

    setWillNotDraw(false);
    outerPapirus  =  new OuterPapirus(context);
    scrollView  =  new ScrollView(context);
    scrollView.setScrollbarFadingEnabled(false);
    scrollView.addView(outerPapirus, new LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.WRAP_CONTENT));

    txt_case_caption = new TextView(context);
    txt_case_caption.setSingleLine(true);
    txt_case_caption.setMaxLines(1);
    txt_case_caption.setTextSize(TypedValue.COMPLEX_UNIT_DIP, SpecTheme.PTextSize);
    txt_case_caption.setTextColor(SpecTheme.PTextColor);
    txt_case_caption.setText(TestPresenter.getRstring(R.string.strCaseCaption));
    outerPapirus.addView(txt_case_caption);

    wJsonConfig = new WJsonConfig(context);
    outerPapirus.addView(wJsonConfig);

    txt_result_caption = new TextView(context);
    txt_result_caption.setSingleLine(true);
    txt_result_caption.setMaxLines(1);
    txt_result_caption.setTextSize(TypedValue.COMPLEX_UNIT_DIP, SpecTheme.PTextSize);
    txt_result_caption.setTextColor(SpecTheme.PTextColor);
    txt_result_caption.setText(TestPresenter.getRstring(R.string.strResultCaption));
    outerPapirus.addView(txt_result_caption);


    simple_table  =  new WSimpleTable(context,  SpecTheme.PWhiteColor, SpecTheme.PTextColor);
    outerPapirus.addView(simple_table);

    innerPapirus  =  new InnerPapirus(context);
    outerPapirus.addView(innerPapirus);

    chart = new UiChartLines(context);
    innerPapirus.addView(chart);

    addView(scrollView, new LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.MATCH_PARENT));
    //add progress bar

    progressBar  =  new WDualProgress(context);
    progressBar.setVisibility(GONE);
    addView(progressBar, new LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.MATCH_PARENT));

    //setChartData();
  }

  @Override
  public Drawable getFABicon() {
    int prog = TestPresenter.getProgress1();
    Drawable re;
    if  (0 == prog || 100 == prog)  {
      re = SpecTheme.play_icon;
    }  else {
      re = SpecTheme.stop_icon;
    }
    return  re;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event)  {
    simple_table.onTouchEvent(event);
    scrollView.onTouchEvent(event);
    chart.onTouchEvent(event);
    return super.onTouchEvent(event);
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {

    simple_table.onTouchEvent(ev);
    scrollView.onTouchEvent(ev);
    chart.onTouchEvent(ev);

    return false;
  }

  @Override
  public void prepareLocalMenu(Menu menu) {

  }

  @Override
  public boolean onSelectLocalMenu(int menu_item_id) {
    return false;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width  =  MeasureSpec.getSize(widthMeasureSpec);
    int height  =  MeasureSpec.getSize(heightMeasureSpec);
    if (progressBar.getVisibility() == VISIBLE) {
      progressBar.measure(
          MeasureSpec.makeMeasureSpec(
              width  -  SpecTheme.dpButtonImgSize, MeasureSpec.EXACTLY),
          MeasureSpec.makeMeasureSpec(
                SpecTheme.dpButtonImgSize, MeasureSpec.EXACTLY)
      );
    }
    scrollView.measure(widthMeasureSpec, heightMeasureSpec);
    setMeasuredDimension(width, height);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    int height = bottom - top;
    int widht = right - left;
    scrollView.layout(0, 0, widht, height);
    if (progressBar.getVisibility() == VISIBLE) {
      progressBar.layout(SpecTheme.dpButtonImgSizeHalf,
          SpecTheme.dpButtonImgSizeHalf,
          SpecTheme.dpButtonImgSizeHalf + progressBar.getMeasuredWidth(),
          SpecTheme.dpButtonImgSizeHalf + progressBar.getMeasuredHeight());
    }
  }


  @Override
  public void onClick(View view) {

  }

  @Override
  public void onFABclick() {
    int prog = TestPresenter.getProgress1();
    if  (0 == prog || 100 == prog)  {
      TestPresenter.getGUInterface().showMessage(
          TestPresenter.getRstring(R.string.strTest_try_start));
      TestPresenter.startProgress();
    }  else {
      TestPresenter.getGUInterface().showMessage(
          TestPresenter.getRstring(R.string.strTest_try_stop));
      TestPresenter.stopProgress();
    }
  }

  @Override
  public void onPause() {
    super.onPause();

  }

  @Override
  public void onResume() {
    super.onResume();
    wJsonConfig.setConfig(TestPresenter.getConfig());
    prepareChart();
  }

  @Override
  public void onStop() {
    super.onStop();
  }


  @Override
  public void onPresenterChange() {
    int progress  =  TestPresenter.getProgress1();
    if (100 == progress) {
      //Data ready , draw
      progressBar.setVisibility(GONE);
      prepareChart();
    }  else if (0 == progress) {
      //No Data - hide table
      clearData();
      progressBar.setVisibility(GONE);
    }  else  {
      //Update progress bar
      updateProgress();
    }
  }

  void  prepareChart() {
    clearData();
    TJsonToCfg  cfg  =  TestPresenter.getConfig();
    if (null ==  cfg  ||  !cfg.is_valid)  return;
    ITestCase testCase =  cfg.getCase();
    if (null == testCase) return;
    try {
      innerPapirus.removeView(chart);
      if (testCase.get_case_type() == StaticConsts.CASE_Strings)  {
        chart = new UiChartLines(SpecTheme.context);
      }  else  {
        chart = new UiChartBars(SpecTheme.context);
      }
      innerPapirus.addView(chart);
      int start_items = cfg.get_case_START_ITEMS_val();
      ArrayList<ITester> testers = cfg.getTesters();
      Map<ITester, ArrayList<Entry>> results  =  cfg.getResults();
      chart.setData(start_items, simple_table, testers, results);

    } catch (Exception e) {
      Log.e(getTAG(), "prepareChart() error: ", e);
    }
  }

  void  clearData() {
    chart.clearData();
    simple_table.clear();
  }

  void  updateProgress() {
    progressBar.setVisibility(VISIBLE);
    progressBar.setProgress1(TestPresenter.getProgress1());
    progressBar.setProgress2(TestPresenter.getProgress2());
    progressBar.requestLayout();
  }

  private class OuterPapirus extends FrameLayout {
    public OuterPapirus(Context context) {
      super(context);
    }

    int chart_height = 0;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      int height = 0;
      int widht = MeasureSpec.getSize(widthMeasureSpec);
      int widhtSpec = MeasureSpec.makeMeasureSpec(widht - SpecTheme.dpButton2Padding, MeasureSpec.AT_MOST);
      int count = getChildCount();
      for (int i = 0; i < count; ++i) {
        View child = getChildAt(i);
        if (innerPapirus == child) continue;
        measureChildWithMargins(child, widhtSpec, 0,
            heightMeasureSpec, 0);
        height += child.getMeasuredHeight() + SpecTheme.dpButtonPadding;
      }
      chart_height  =  MeasureSpec.getSize(heightMeasureSpec) >> 2;
      if  (chart_height < SpecTheme.dpMaxEmojiKeyboard) {  chart_height = SpecTheme.dpMaxEmojiKeyboard;  }
      measureChildWithMargins(innerPapirus, widhtSpec, 0,
          MeasureSpec.makeMeasureSpec(chart_height, MeasureSpec.AT_MOST), 0);
      setMeasuredDimension(widht, height + chart_height + SpecTheme.dpButton2Padding);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
      //super.onLayout(changed, left, top, right, bottom);
//      int centerX = (right - left)>>1;
//      View view = getChildAt(0);
//      int half = view.getMeasuredWidth()>>1;
//      view.layout(centerX-half,SpecTheme.dpButtonPadding,centerX+half,SpecTheme.dpButtonPadding+view.getMeasuredHeight());
//
      int widht = right - left - SpecTheme.dpButtonPadding;
      int count = getChildCount();
      int curTop = SpecTheme.dpButtonPadding;
      for (int i = 0; i < count; ++i) {
        View child = getChildAt(i);
        if (innerPapirus == child) continue;
        //Все один над другим ака Vertical Layout:
        int h = child.getMeasuredHeight();
        child.layout(SpecTheme.dpButtonPadding, curTop, widht, curTop + h);
        curTop += h + SpecTheme.dpButtonPadding;
      }
//      chart.mViewPortHandler.restrainViewPort(
//          SpecTheme.dpButtonPadding,
//          curTop,
//          widht,
//          curTop+100);
      innerPapirus.layout(SpecTheme.dpButtonPadding, curTop, widht, curTop + chart_height);

    }
  }  // OutrePapirus




  private class InnerPapirus extends FrameLayout {
    public InnerPapirus(Context context) {
      super(context);
      setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      int widht = MeasureSpec.getSize(widthMeasureSpec);
      int height = MeasureSpec.getSize(heightMeasureSpec);
//      measureChildWithMargins(chart, MeasureSpec.makeMeasureSpec(100, MeasureSpec.EXACTLY), 0,
//          MeasureSpec.makeMeasureSpec(100, MeasureSpec.EXACTLY), 0);
      measureChildWithMargins(chart, widthMeasureSpec, 0,
          heightMeasureSpec, 0);
      setMeasuredDimension(widht, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
      chart.layout(0, 0,
          right - left, bottom - top);
    }

    @Override
    protected void onDraw(Canvas canvas) {
      chart.draw(canvas);
    }
  }  //  InnerPapirus


}
