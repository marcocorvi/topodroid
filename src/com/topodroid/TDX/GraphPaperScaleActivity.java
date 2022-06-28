/* @file GraphPaperScaleActivity.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid graph-paper density adjustment activity
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDTag;
// import com.topodroid.ui.MyDialog;
// import com.topodroid.ui.MotionEventWrap;
import com.topodroid.prefs.TDSetting;
// import com.topodroid.prefs.TDPrefActivity;
import com.topodroid.help.UserManualActivity;

import android.content.Context;

import android.graphics.PointF;
// import android.graphics.Path;

import android.os.Bundle;
import android.content.Intent;
import android.app.Activity;

import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 */
public class GraphPaperScaleActivity extends Activity
                          implements View.OnClickListener
{
  // private TDPrefActivity mParent;

  private GraphPaperScaleSurface mSurface;
  private Button  mBtnOk;
  private Button  mBtnCancel;
  private Button  mBtnPlus;
  private Button  mBtnDoublePlus;
  private Button  mBtnDoubleMinus;
  private Button  mBtnMinus;
  private Button  mBtnHelp;
  private TextView mTVdensity;

  // private float mGraphPaperScale; // in GraphPaperScaleSurface

  // /** cstr
  //  * @param context context
  //  */
  // GraphPaperScaleActivity( Context context, TDPrefActivity parent )
  // {
  //   super( context, null, R.string.GraphPaperScaleDialog ); // FIXME // null app
  //   mParent = parent;
  //   // mGraphPaperScale = TDSetting.mGraphPaperScale;
  //   // mApp     = mParent.getApp();
  // }

  // -------------------------------------------------------------------

  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    // mIsNotMultitouch = ! TDandroid.checkMultitouch( this );

    setContentView( R.layout.graph_paper_scale_dialog );

    mTVdensity = (TextView) findViewById( R.id.graph_paper_density );

    mBtnOk     = (Button) findViewById( R.id.btn_ok );
    mBtnCancel = (Button) findViewById( R.id.btn_cancel );
    mBtnPlus   = (Button) findViewById( R.id.btn_plus );
    mBtnDoublePlus  = (Button) findViewById( R.id.btn_double_plus );
    mBtnDoubleMinus = (Button) findViewById( R.id.btn_double_minus );
    mBtnMinus  = (Button) findViewById( R.id.btn_minus );
    mBtnHelp   = (Button) findViewById( R.id.button_help );

    mBtnOk.setOnClickListener( this );
    mBtnCancel.setOnClickListener( this );
    mBtnPlus.setOnClickListener( this );
    mBtnDoublePlus.setOnClickListener( this );
    mBtnDoubleMinus.setOnClickListener( this );
    mBtnMinus.setOnClickListener( this );
    mBtnHelp.setOnClickListener( this );

    mSurface = (GraphPaperScaleSurface) findViewById(R.id.graph_paper_surface);
    mSurface.setGraphPaperScaleActivity( this );

    setDensityTextView( TDSetting.mGraphPaperScale );

    setTitle( R.string.title_graph_paper );
  }

  // ----------------------------------------------------------------------------

  // /** set the size - empty: nothing to do
  //  * @param w width
  //  * @param h height
  //  */
  // public void setSize( int w, int h )
  // {
  //   TDLog.v("GRAPH_PAPER activity set size " + w + " " + h );
  // }

  /** change the graph-paper density
   * @param change   density change
   */
  private void changeScale( int change )
  {
    if ( mSurface != null ) mSurface.changeDensity( change );
  }

  /** display the density adjustment value
   * @param density    adjustment value
   */
  void setDensityTextView( int density )
  {
    mTVdensity.setText( Integer.toString( density ) );
  }

  /** reac t to a user tap
   * @param view tapped view
   *   - button OK: tell the parent to create the projection profile with the current azimuth
   *   - button PLUS: increase azimuth
   *   - button MINUS: decrease azimuth
   */
  @Override
  public void onClick(View view)
  {
    Button b = (Button)view;
    if ( b == mBtnPlus ) {
      changeScale( 1 );
    } else if ( b == mBtnDoublePlus ) {
      changeScale( 10 );
    } else if ( b == mBtnDoubleMinus ) {
      changeScale( -10 );
    } else if ( b == mBtnMinus ) {
      changeScale( -1 );
    } else if ( b == mBtnOk ) {
      int density = mSurface.getGraphPaperDensity();
      // TDLog.v("GRAPH_PAPER return density " + density );
      mSurface.stopDrawingThread();
      Intent intent = new Intent();
      intent.putExtra( TDTag.TOPODROID_GRAPH_PAPER_SCALE, density );
      setResult( RESULT_OK, intent );
      finish();
    } else if ( b == mBtnCancel ) {
      onBackPressed();
    } else if ( b == mBtnHelp ) {
      doHelp();
    }
  }

  /** react to a user tap on BACK: stop drawing and close the dialog with no further action
   */
  @Override
  public void onBackPressed()
  {
    super.onBackPressed();
    mSurface.stopDrawingThread();
    setResult( RESULT_CANCELED );
    finish();
  }

  /** display help
   */
  private void doHelp()
  {
    String help_page = getResources().getString( R.string.GraphPaperScaleActivity );
    /* if ( help_page != null ) */ UserManualActivity.showHelpPage( this, help_page );

  }

}
