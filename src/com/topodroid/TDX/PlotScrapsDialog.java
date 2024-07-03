/* @file PlotScrapsDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid sketch scraps dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.ui.MyDialog;

// import android.app.Dialog;
// import android.app.Activity;
import android.os.Bundle;

// import android.content.Intent;
import android.content.Context;

import android.widget.TextView;
import android.widget.Button;
import android.view.View;

class PlotScrapsDialog extends MyDialog
                       implements View.OnClickListener
{
  private TextView mTvScraps;
  private Button   mBtnNext;
  private Button   mBtnBack;
  private Button   mBtnPrev;
  private Button   mBtnNew;
  private Button   mBtnDelete;

  private final DrawingWindow mParent;

  PlotScrapsDialog( Context context, DrawingWindow parent )
  {
    super( context, null, R.string.PlotScrapsDialog ); // null app
    mParent = parent;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.plot_scraps_dialog, R.string.title_plot_scraps );
    
    mBtnNext   = (Button) findViewById(R.id.btn_next );
    mBtnPrev   = (Button) findViewById(R.id.btn_prev );
    mBtnNew    = (Button) findViewById(R.id.btn_new );
    mBtnBack   = (Button) findViewById(R.id.btn_back );
    mBtnDelete = (Button) findViewById(R.id.btn_delete );

    mTvScraps = (TextView) findViewById( R.id.scraps_nr );
    // int idx = mParent.getScrapIndex() + 1; // people count from 1
    // int max = mParent.getScrapMaxIndex();
    int nr  = mParent.getScrapNumber();
    int nr0 = mParent.getCurrentScrapNumber() + 1;
    mTvScraps.setText( String.format( mContext.getResources().getString( R.string.scrap_string ), nr0, nr ) );

    // TDLog.v("Scrap " + nr0 + " of " + nr );

    if ( nr <= 1 ) { 
      mBtnDelete.setVisibility( View.GONE );
    } else {
      mBtnDelete.setOnClickListener( this );
    }

    if ( nr0 < nr /* idx < max */ ) { 
      mBtnNext.setOnClickListener( this );
    } else {
      mBtnNext.setBackgroundColor( TDColor.MID_GRAY );
    }
    if ( nr0 > 1 /* idx > 1 */ ) {
      mBtnPrev.setOnClickListener( this );
    } else {
      mBtnPrev.setBackgroundColor( TDColor.MID_GRAY );
    }
    mBtnNew.setOnClickListener( this );
    mBtnBack.setOnClickListener( this );
  }

  @Override
  public void onClick(View v) 
  {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    Button b = (Button) v;

    if ( b == mBtnNext ) {
      mParent.scrapNext( );
    } else if ( b == mBtnPrev ) {
      mParent.scrapPrev( );
    } else if ( b == mBtnNew ) {
      mParent.scrapNew( );
    } else if ( b == mBtnDelete ) {
      mParent.scrapDelete( );
    // } else if ( b == mBtnBack ) {
      /* nothing */
    }
    dismiss();
  }

}



