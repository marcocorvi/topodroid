/** @file PlotZoomFitDialog.java
 *
 * @author marco corvi
 * @date jul 2017
 *
 * @brief TopoDroid plot zomm-fit / landscape
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.Dialog;

import android.content.Context;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import android.widget.Button;

import android.widget.TextView;
// import android.widget.AdapterView.OnItemLongClickListener;

import android.widget.Toast;

public class PlotZoomFitDialog extends MyDialog
                               implements OnClickListener
{
  private DrawingWindow mParent;
  private Button mBtnPortrait;
  private Button mBtnLandscape;

  public PlotZoomFitDialog( Context context, DrawingWindow parent )
  {
    super( context, R.string.PlotZoomFitDialog );
    mParent = parent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.plot_zoomfit_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    // ((Button) findViewById( R.id.button_zoomfit )).setOnClickListener( this );
    mBtnPortrait  = (Button) findViewById( R.id.button_portrait );
    mBtnLandscape = (Button) findViewById( R.id.button_landscape );
    ((Button) findViewById( R.id.button_cancel )).setOnClickListener( this );
    mBtnPortrait.setOnClickListener( this );
    mBtnLandscape.setOnClickListener( this );
    if ( mParent.isLandscape() ) {
      mBtnLandscape.setText( R.string.button_zoomfit );
    } else {
      mBtnPortrait.setText( R.string.button_zoomfit );
    }

    setTitle( R.string.title_plot_zoomfit );
  }
 
  // ---------------------------------------------------------------

  @Override 
  public void onClick( View v ) 
  {
    // if ( v.getId() == R.id.button_zoomfit ) {
    //   mParent.doZoomFit();
    // } else
    if ( v.getId() == R.id.button_portrait ) {
      mParent.setOrientation( PlotInfo.ORIENTATION_PORTRAIT );
    } else if ( v.getId() == R.id.button_landscape ) {
      mParent.setOrientation( PlotInfo.ORIENTATION_LANDSCAPE );
    }
    dismiss();
  }

}
