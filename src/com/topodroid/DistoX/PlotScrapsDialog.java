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
package com.topodroid.DistoX;

// import com.topodroid.utils.TDLog;
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

  private final DrawingWindow mParent;

  PlotScrapsDialog( Context context, DrawingWindow parent )
  {
    super( context, R.string.PlotScrapsDialog );
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

    mTvScraps = (TextView) findViewById( R.id.scraps_nr );
    int idx = mParent.getScrapIndex() + 1; // people count from 1
    int max = mParent.getScrapMaxIndex();
    mTvScraps.setText( String.format( mContext.getResources().getString( R.string.scrap_string ), idx, max ) );


    if ( idx < max ) {
      mBtnNext.setOnClickListener( this );
    } else {
      mBtnNext.setBackgroundColor( TDColor.MID_GRAY );
    }
    if ( idx > 1 ) {
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
    // } else if ( b == mBtnBack ) {
      /* nothing */
    }
    dismiss();
  }

}



