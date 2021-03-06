package com.bond.android_ipc;
/*
 * This is the source code of SpecNet project
 * It is licensed under MIT License.
 *
 * Copyright (c) Dmitriy Bondarenko
 * feel free to contact me: specnet.messenger@gmail.com
 */
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bond.android_ipc.gui.frags.FragmentKey;
import com.bond.android_ipc.gui.MainWindow;
import com.bond.android_ipc.gui.SpecTheme;
import com.bond.android_ipc.gui.frags.UiFragment;
import com.bond.android_ipc.gui.frags.UiHistoryFrag;
import com.bond.android_ipc.gui.frags.UiMainFrag;
import com.bond.android_ipc.gui.frags.UiSettingsFrag;
import com.bond.android_ipc.i.IActivityForResult;
import com.bond.android_ipc.i.IView;
import com.bond.android_ipc.objs.ByteUtils;
import com.bond.android_ipc.objs.StaticConsts;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements IView {
  static final String TAG = "MainActivity";
  final GuiHandler guiHandler = new GuiHandler(Looper.getMainLooper());
  Toolbar toolbar  =  null;
  FloatingActionButton fab  =  null;

  static final FragmentKey FirstFragKey = new FragmentKey(StaticConsts.FirstFragTAG);
  final Map<FragmentKey, UiFragment> uiFrags  =  new HashMap<FragmentKey,UiFragment>();
  final Deque<FragmentKey> uiFragsControl  =  new ArrayDeque<FragmentKey>();
  UiFragment curActiveFrag  =  null;
  MainWindow mainWindow  =  null;
  boolean guiNotStarted  =  true;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mainWindow = (MainWindow) findViewById(R.id.mainWindow);
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onFABclick(view);
      }
    });

    restoreState(savedInstanceState);
//    onNewIntent(getIntent())
  }

  @Override
  public void setFABicon() {
    if (null  !=  curActiveFrag ) {
      fab.setImageDrawable(curActiveFrag.getFABicon());
    }
  }

  @Override
  public void goBack() {
    onBackPressed();
  }


  void onFABclick(View view) {
    if (null != curActiveFrag) {
      curActiveFrag.onFABclick();
    }
  }

  @Override
  public void onPresenterChange() {
    if (null != curActiveFrag) {
      curActiveFrag.onPresenterChange();
    }
    setFABicon();
  }

  @Override
  public void showMessage(String str) {
    Snackbar.make(fab, str, Snackbar.LENGTH_LONG)
        .setAction("Action", null).show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  /**
   * Gets called every time the user presses the menu button.
   * Use if your menu is dynamic.
   */
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.clear();
    if (null != curActiveFrag)  {
      if (!curActiveFrag.getTAG().equals(StaticConsts.FirstFragTAG))  {
        menu.add(0, StaticConsts.MENU_UiMain, Menu.NONE,
            R.string.strUiMainFragM);
      }
      if (!curActiveFrag.getTAG().equals(StaticConsts.UiSettingsTAG))  {
        menu.add(0, StaticConsts.MENU_UiSettings, Menu.NONE,
            R.string.strUiSettingsFrag);
      }
      if (!curActiveFrag.getTAG().equals(StaticConsts.UiHistoryTAG))  {
        menu.add(0, StaticConsts.MENU_UiHistory, Menu.NONE,
            R.string.strUiHistoryFrag);
      }
      curActiveFrag.prepareLocalMenu(menu);
    }

    // Back button:
    menu.add(2, StaticConsts.MENU_Exit, Menu.NONE,
        R.string.strExit);

    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    boolean  was_my_menu = true;
    int menu_item_id = item.getItemId();
    switch (menu_item_id) {
      case StaticConsts.MENU_UiMain:
        setCurActiveFrag(new FragmentKey(StaticConsts.FirstFragTAG));
        break;
      case StaticConsts.MENU_UiSettings:
        setCurActiveFrag(new FragmentKey(StaticConsts.UiSettingsTAG));
        break;
      case StaticConsts.MENU_UiHistory:
        setCurActiveFrag(new FragmentKey(StaticConsts.UiHistoryTAG));
        break;
      case StaticConsts.MENU_Exit:
        exitSpecNetMain();
        break;
      default:
        was_my_menu = null == curActiveFrag? false :
            curActiveFrag.onSelectLocalMenu(menu_item_id);
    }
    if (was_my_menu)  { return  true; }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    if (null  !=  curActiveFrag) {
      outState.putString("curActiveFrag.fragTAG",
          curActiveFrag.getFragmentKey().fragTAG);
    }
    super.onSaveInstanceState(outState);
  }

  private void restoreState (Bundle savedInstanceState) {
    if  (null  ==  savedInstanceState) {  return ;  }
    String fragTAG = savedInstanceState.getString("curActiveFrag.fragTAG");
    if  (null  !=  fragTAG)  {
      setCurActiveFrag(new FragmentKey(fragTAG));
    }//if (null!=fragTAG)
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == StaticConsts.RQS_GET_CONTENT )  {
      if (null  !=  curActiveFrag)  {
        if (ByteUtils.getFlag(curActiveFrag.getType(), StaticConsts.FrgActivityForResult))  {
          ((IActivityForResult) curActiveFrag).resultActivityForResult(this, requestCode, resultCode, data);
        }
      }
    }  else  {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  private void setCurActiveFrag(FragmentKey key)  {
    if  (null  ==  key)  return;
    UiFragment frg  =  uiFrags.get(key);
    if  (null  ==  frg  ||  frg.isDestroyed)  {
      frg  =  createUiFragment(key);
    }
    if  (frg  !=  curActiveFrag)  {

      if (null  !=  curActiveFrag )  {
        mainWindow.checkDelCurFrag(curActiveFrag);
        uiFrags.remove(curActiveFrag.getFragmentKey());
        uiFragsControl.remove(curActiveFrag.getFragmentKey());
        curActiveFrag.onDestroyCommon();
      }
      curActiveFrag = frg;
      mainWindow.setCurActiveFrag(curActiveFrag);

      if (curActiveFrag.getTAG().equals(StaticConsts.FirstFragTAG)) {
        clearUiFrags(FirstFragKey);
      }

      uiFrags.put(curActiveFrag.getFragmentKey(), curActiveFrag);
      uiFragsControl.add(curActiveFrag.getFragmentKey());
    }

    if (null  !=  curActiveFrag)  {
      curActiveFrag.onResume();
      setFABicon();
    }
  }

  UiFragment createUiFragment(FragmentKey key)  {
    Context context  =  MainActivity.this;
    UiFragment frg  =  null;
    switch (key.fragTAG) {
      case StaticConsts.UiSettingsTAG:
        frg  =  new UiSettingsFrag(context,  key);
        break;
      case StaticConsts.UiHistoryTAG:
        frg  =  new UiHistoryFrag(context,  key);
        break;
      case StaticConsts.FirstFragTAG:
      default:
        frg  = new UiMainFrag(context,  key);
        break;
    }
    return frg;
  }

  @Override
  public void setToolbarTittle(String tittle) {
    getSupportActionBar().setTitle(tittle);
  }

  @Override
  public Handler getGuiHandler() {
    return  guiHandler;
  }

  @Override
  public Context getForDialogCtx() {
    return MainActivity.this;
  }

  @Override
  public void onPause() {
    if (null  !=  curActiveFrag) {
      curActiveFrag.onPause();
    }
    super.onPause();
  }

  private void onFirstStart() {
    FragmentKey key=uiFragsControl.peekLast();
    if (null  ==   key) {
      TestPresenter.onFirstStart();
      setCurActiveFrag(FirstFragKey);
    } else {
      setCurActiveFrag(key);
    }
    guiNotStarted  =  false;
  }

  @Override
  public void onStart() {
    TestPresenter.setGUInterface(MainActivity.this);
    SpecTheme.applyMetrics(MainActivity.this);
    super.onStart();

    if (guiNotStarted) {
      onFirstStart();
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    if (null  !=  curActiveFrag) {
      curActiveFrag.onResume();
    }
    onPresenterChange();
  }

  @Override
  public void onStop() {
    if  (null  !=  curActiveFrag)  {
      curActiveFrag.onStop();
    }
    guiNotStarted  =  true;
    TestPresenter.onGUIstop();
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    exitSpecNetMain();
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    if (null  !=  curActiveFrag
        && curActiveFrag.getTAG().equals(StaticConsts.FirstFragTAG)) {
      exitSpecNetMain();
    }  else  {
      setCurActiveFrag(FirstFragKey);
    }
  }


  private void exitSpecNetMain()  {
    try {
      guiNotStarted = true;
      mainWindow.onDestroy();
      clearUiFrags();
      SpecTheme.onDestroy();
      finish();
      System.gc();
    } catch (Exception e) {}
    try {
      super.onBackPressed();
      System.gc();
    } catch (Exception e) {}
  }

  private void clearUiFrags(FragmentKey exeptFragKey)  {
    for (FragmentKey fragKey : uiFragsControl)  {
      if (fragKey.equals(exeptFragKey)) {
        continue;
      }
      UiFragment frag  =  uiFrags.get(fragKey);
      if (null  !=  frag)  {
        mainWindow.checkDelCurFrag(frag);
        frag.onStop();
        frag.onDestroyCommon();
      }
    }
    uiFrags.clear();
    uiFragsControl.clear();
  }

  private void clearUiFrags() {
    for (FragmentKey fragKey : uiFragsControl) {
      UiFragment frag=uiFrags.get(fragKey);
      if (null!=frag) {
        mainWindow.checkDelCurFrag(frag);
        frag.onStop();
        frag.onDestroyCommon();
      }
    }
    uiFragsControl.clear();
    uiFrags.clear();
    curActiveFrag = null;
  }

  class GuiHandler extends Handler {
    public GuiHandler(Looper looper) {
      super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
      try {
        switch (msg.what) {
//                    case MsgTAGs.M_KEEP_ALIVE:
//                        keepAlive();
//                        break;

          default:
            super.handleMessage(msg);
        }
      } catch (Exception e) {
        Log.e(TAG, "GuiHandler: error Message handling",e);
      }
    }
  }

}
