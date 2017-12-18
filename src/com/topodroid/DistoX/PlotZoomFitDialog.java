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

    ((Button) findViewById( R.id.button_zoomfit )).setOnClickListener( this );
    ((Button) findViewById( R.id.button_portrait )).setOnClickListener( this );
    ((Button) findViewById( R.id.button_landscape )).setOnClickListener( this );
    ((Button) findViewById( R.id.button_cancel )).setOnClickListener( this );

    setTitle( R.string.title_plot_zoomfit );
  }
 
  // ---------------------------------------------------------------

  @Override 
  public void onClick( View v ) 
  {
    if ( v.getId() == R.id.button_zoomfit ) {
      mParent.doZoomFit();
    } else if ( v.getId() == R.id.button_portrait ) {
      mParent.setOrientation( PlotInfo.ORIENTATION_PORTRAIT );
    } else if ( v.getId() == R.id.button_landscape ) {
      mParent.setOrientation( PlotInfo.ORIENTATION_LANDSCAPE );
    }
    dismiss();
  }

}
