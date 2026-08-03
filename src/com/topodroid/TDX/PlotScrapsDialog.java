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

import com.topodroid.util.TDLog;
import com.topodroid.util.TDColor;
import com.topodroid.ui.MyDialog;

// import android.app.Dialog;
// import android.app.Activity;
import android.os.Bundle;

// import android.content.Intent;
import android.content.Context;

import android.widget.TextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import android.view.View;
import android.view.ViewGroup;

class PlotScrapsDialog extends MyDialog
                       implements View.OnClickListener
{
  private TextView mTvScraps;
  private Button   mBtnNext;
  private Button   mBtnBack;
  private Button   mBtnPrev;
  private Button   mBtnNew;
  private Button   mBtnDelete;
  private Button   mBtnSelect;

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
    int nr0 = mParent.getCurrentScrapNumber() + 1; // indices are displyed from 1 to NR included
    mTvScraps.setText( String.format( resString( R.string.scrap_string ), nr0, nr ) );

    if ( nr > 2 ) {
      final RadioGroup r_grp = new RadioGroup( mContext ); // Create RadioGroup for selecting scraps
      r_grp.setOrientation( RadioGroup.VERTICAL );
      for ( int i = 1; i <= nr; i++ ) {
        final RadioButton r_btn = new RadioButton( mContext );
        r_btn.setText( String.format( mContext.getResources().getString(R.string.scrap_index), i) );
        r_btn.setId( i - 1 ); // indices start from 0, so subtract 1
        r_btn.setChecked( (i == nr0) );
        r_grp.addView( r_btn );
      }

      mBtnSelect = new Button( mContext ); // Create the "Select" button
      mBtnSelect.setText( R.string.button_select );

      mBtnSelect.setOnClickListener( new View.OnClickListener() {
        @Override public void onClick( View v )
        {
          int id = r_grp.getCheckedRadioButtonId(); // Find out which ID (index) is currently checked in the RadioGroup
          if ( id != -1 ) {
            mParent.setScrapIndex( id ); 
          }
          dismiss();
        }
      });

      // Container for the radio buttons and the new Select button, arranged vertically
      LinearLayout container = new LinearLayout( mContext );
      container.setOrientation( LinearLayout.VERTICAL );
      container.addView( r_grp );
      container.addView( mBtnSelect );

      // Insert the container below the mTvScraps title
      if (mTvScraps != null && mTvScraps.getParent() instanceof ViewGroup) {
        ViewGroup parentGroup = (ViewGroup) mTvScraps.getParent();
        int index = parentGroup.indexOfChild(mTvScraps);
        parentGroup.addView(container, index + 1);
      } else {
        View dialogView = findViewById(android.R.id.content);
        if (dialogView instanceof ViewGroup) {
          ((ViewGroup) dialogView).addView(container);
        }
      }
    }
    // ///////////////////////////////////////////////////////////


    // TDLog.v("plot scrap dialog " + nr0 + " of " + nr );

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
      // TDLog.v(" next scrap");
      mParent.scrapNext( );
    } else if ( b == mBtnPrev ) {
      // TDLog.v(" prev scrap");
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



