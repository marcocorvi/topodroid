/* @file DrawingAreaDialog.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid sketch line attributes editing dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyStateBox;
import com.topodroid.ui.MyOrientationWidget;
import com.topodroid.ui.TDLayout;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

import android.os.Bundle;
import android.content.Context;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.LinearLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.view.View;

class DrawingAreaDialog extends MyDialog
                        implements View.OnClickListener
                        , AdapterView.OnItemSelectedListener
{
  private DrawingWindow mParent;
  private DrawingAreaPath mArea;  // area item
  private int mAreaType;          // area type initualized from area item's type
  private boolean mOrientable;    // whether the area type is orientable
  // private boolean mDoOptions; // areas do not have options

  // GUI widgets
  private CheckBox mCBbase  = null; // canvas levels
  private CheckBox mCBfloor = null;
  private CheckBox mCBfill  = null;
  private CheckBox mCBceil  = null;
  private CheckBox mCBarti  = null;
  // private CheckBox mCBform  = null;
  // private CheckBox mCBwater = null;
  // private CheckBox mCBtext  = null;

  private CheckBox mCBvisible;
  // private Spinner mETtype;

  private MyOrientationWidget mOrientationWidget; 

  private Button mBtnOk;
  // private Button mBtnCancel;

  private MyStateBox mBtnReduce;

  DrawingAreaDialog( Context context, DrawingWindow parent, DrawingAreaPath line )
  {
    super( context, R.string.DrawingAreaDialog );
    mParent = parent;
    mArea = line;
    mAreaType  = mArea.mAreaType;
    mOrientable = BrushManager.isAreaOrientable( mAreaType );
    // mDoOptions = TDLevel.overAdvanced;
  }

  // -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    String title = String.format( mParent.getResources().getString( R.string.title_draw_area ),
                                  BrushManager.getAreaName( mArea.mAreaType ) );
    initLayout( R.layout.drawing_area_dialog, title );

    mOrientationWidget = new MyOrientationWidget( this, mOrientable, mArea.mOrientation );

    Spinner eTtype = (Spinner) findViewById( R.id.area_type );
    ArrayAdapter adapter = new ArrayAdapter<>( mContext, R.layout.menu, BrushManager.getAreaNames() );
    eTtype.setAdapter( adapter );
    eTtype.setSelection( mAreaType );
    eTtype.setOnItemSelectedListener( this );

    mCBvisible = (CheckBox) findViewById( R.id.area_visible );
    mCBvisible.setChecked( mArea.isVisible() );

    // NOTE area do not have options

    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    mBtnReduce = new MyStateBox( mContext, R.drawable.iz_reduce_no,  R.drawable.iz_reduce_ok, R.drawable.iz_reduce_ok2 );
    mBtnReduce.setOnClickListener( this );

    LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 0, 10, 20, 10 );

    LinearLayout layout3 = (LinearLayout)findViewById( R.id.layout3 );
    layout3.addView( mBtnReduce, lp );

    if ( TDSetting.mWithLevels > 1 ) {
      setCBlayers();
    } else {
      LinearLayout ll = (LinearLayout) findViewById( R.id.layer_layout );
      ll.setVisibility( View.GONE );
    }

    mBtnOk = (Button) findViewById( R.id.button_ok );
    mBtnOk.setOnClickListener( this );
    // mBtnCancel = (Button) findViewById( R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );
    ( (Button) findViewById( R.id.button_cancel ) ).setOnClickListener( this );
  }

  private void setCBlayers()
  {
    mCBbase  = (CheckBox) findViewById( R.id.cb_layer_base  );
    mCBfloor = (CheckBox) findViewById( R.id.cb_layer_floor );
    mCBfill  = (CheckBox) findViewById( R.id.cb_layer_fill  );
    mCBceil  = (CheckBox) findViewById( R.id.cb_layer_ceil  );
    mCBarti  = (CheckBox) findViewById( R.id.cb_layer_arti  );
    // mCBform  = (CheckBox) findViewById( R.id.cb_layer_form  );
    // mCBwater = (CheckBox) findViewById( R.id.cb_layer_water );
    // mCBtext  = (CheckBox) findViewById( R.id.cb_layer_text  );
    int level = mArea.mLevel;
    mCBbase .setChecked( ( level & DrawingLevel.LEVEL_BASE  ) == DrawingLevel.LEVEL_BASE  );
    mCBfloor.setChecked( ( level & DrawingLevel.LEVEL_FLOOR ) == DrawingLevel.LEVEL_FLOOR );
    mCBfill .setChecked( ( level & DrawingLevel.LEVEL_FILL  ) == DrawingLevel.LEVEL_FILL  );
    mCBceil .setChecked( ( level & DrawingLevel.LEVEL_CEIL  ) == DrawingLevel.LEVEL_CEIL  );
    mCBarti .setChecked( ( level & DrawingLevel.LEVEL_ARTI  ) == DrawingLevel.LEVEL_ARTI  );
    // mCBform .setChecked( ( level & DrawingLevel.LEVEL_FORM  ) == DrawingLevel.LEVEL_FORM  );
    // mCBwater.setChecked( ( level & DrawingLevel.LEVEL_WATER ) == DrawingLevel.LEVEL_WATER );
    // mCBtext .setChecked( ( level & DrawingLevel.LEVEL_TEXT  ) == DrawingLevel.LEVEL_TEXT  );
  }

  private void setLevel()
  {
    int level = 0;
    if ( mCBbase .isChecked() ) level |= DrawingLevel.LEVEL_BASE;
    if ( mCBfloor.isChecked() ) level |= DrawingLevel.LEVEL_FLOOR;
    if ( mCBfill .isChecked() ) level |= DrawingLevel.LEVEL_FILL;
    if ( mCBceil .isChecked() ) level |= DrawingLevel.LEVEL_CEIL;
    if ( mCBarti .isChecked() ) level |= DrawingLevel.LEVEL_ARTI;
    // if ( mCBform .isChecked() ) level |= DrawingLevel.LEVEL_FORM;
    // if ( mCBwater.isChecked() ) level |= DrawingLevel.LEVEL_WATER;
    // if ( mCBtext .isChecked() ) level |= DrawingLevel.LEVEL_TEXT;
    mArea.mLevel = level;
  }

  @Override
  public void onItemSelected( AdapterView av, View v, int pos, long id ) { mAreaType = pos; }

  @Override
  public void onNothingSelected( AdapterView av ) { mAreaType = mArea.mAreaType; }


  public void onClick(View v) 
  {
    Button b = (Button)v;
    // TDLog.Log( TDLog.LOG_INPUT, "DrawingAreaDialog onClick() " + b.getText().toString() );

    if ( b == mBtnOk ) {
      if ( mAreaType != mArea.mAreaType ) mArea.setAreaType( mAreaType );

      int reduce = mBtnReduce.getState();
      if ( reduce > 0 ) mParent.reduceArea( mArea, reduce );

      mArea.setVisible( mCBvisible.isChecked() );
      if ( mOrientable ) {
        mArea.setOrientation( mOrientationWidget.mOrient );
      }

      if ( TDSetting.mWithLevels > 1 ) setLevel();
     
    } else if ( b == mBtnReduce ) {
      int reduce = ( mBtnReduce.getState() + 1 ) % 3;
      mBtnReduce.setState( reduce );
      return;
    // } else if ( b == mBtnCancel ) {
    //   /* nothing */
    }
    dismiss();
  }

}

