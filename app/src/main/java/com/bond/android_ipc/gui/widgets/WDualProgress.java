package com.bond.android_ipc.gui.widgets;

import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.bond.android_ipc.gui.SpecTheme;

public class WDualProgress  extends FrameLayout {
  ProgressBar progressBar1;
  ProgressBar progressBar2;

  public WDualProgress(Context context) {
    super(context);

    progressBar1 = create_ProgressBar(context, SpecTheme.PForestGreenColorA);
    addView(progressBar1, new LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.MATCH_PARENT));
    progressBar2 = create_ProgressBar(context, SpecTheme.PBlueColorA);
    addView(progressBar2, new LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.MATCH_PARENT));
  }

  ProgressBar  create_ProgressBar(Context context, int color) {
    ProgressBar  progressBar =  new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
    // Define a shape with rounded corners
    final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
    ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners,     null, null));
    // Sets the progressBar color
    pgDrawable.getPaint().setColor(color);

    // Adds the drawable to your progressBar
    ClipDrawable progress = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
    progressBar.setProgressDrawable(progress);

    // Sets a background to have the 3D effect
    Drawable draw_back_progress = SpecTheme.context.getResources()
        .getDrawable(android.R.drawable.progress_horizontal);
    draw_back_progress.setAlpha(77);
    progressBar.setBackgroundDrawable(draw_back_progress);
    return progressBar;
  }

  public void  setProgress1(int  progress)  {
    progressBar1.setProgress(progress);
  }

  public void  setProgress2(int  progress)  {
    progressBar2.setProgress(progress);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    measureChildWithMargins(progressBar1, widthMeasureSpec, 0,
        heightMeasureSpec, 0);
    measureChildWithMargins(progressBar2, widthMeasureSpec, 0,
        heightMeasureSpec, 0);

    setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
        MeasureSpec.getSize(heightMeasureSpec));
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    int height = bottom - top;
    int widht = right - left;
    progressBar1.layout(0,  0,   widht,  height);
    progressBar2.layout(0,  0,   widht,  height);
  }

}
