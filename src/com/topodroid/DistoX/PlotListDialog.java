/** @file PlotListDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid option list
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120520 created
 * 20130215 3D sketches button
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.Dialog;

import android.content.Context;

import android.view.View;
import android.view.View.OnClickListener;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.GridView;
import android.widget.Button;

import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.widget.AdapterView.OnItemLongClickListener;

import android.widget.Toast;

import android.util.Log;

public class PlotListDialog extends Dialog
                        implements OnItemClickListener
                                // , OnItemLongClickListener
                                , View.OnClickListener
{
  private Context mContext;
  private ShotActivity mParent;
  private TopoDroidApp mApp;
  private ArrayAdapter<String> mArrayAdapter;
  // private ListItemAdapter mArrayAdapter;
  private Button mBtnPlotNew;
  // private Button mBtnBack;

  // FIXME_SKETCH_3D
  private Button mBtnSketch3dNew;
  // END_SKETCH_3D

  // private ListView mList;
  private GridView mList;

  public PlotListDialog( Context context, ShotActivity parent, TopoDroidApp app )
  {
    super( context );
    mContext = context;
    mParent = parent;
    mApp = app;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.plot_list_dialog );
    mArrayAdapter = new ArrayAdapter<String>( mContext, R.layout.message );
    // mArrayAdapter = new ListItemAdapter( mContext, R.layout.message );

    mList = (GridView) findViewById(R.id.list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    // mList.setDividerHeight( 2 );
    mList.setHorizontalSpacing( 2 );

    mBtnPlotNew     = (Button) findViewById(R.id.plot_new);
    // mBtnBack = (Button) findViewById(R.id.btn_back);

    mBtnPlotNew.setOnClickListener( this );
    // mBtnBack.setOnClickListener( this );

    // FIXME_SKETCH_3D
      mBtnSketch3dNew = (Button) findViewById(R.id.sketch3d_new);
      if ( mApp.mSketches ) {
        mBtnSketch3dNew.setOnClickListener( this );
      } else {
        mBtnSketch3dNew.setEnabled( false );
        mBtnSketch3dNew.setVisibility( View.GONE );
        // mBtnSketch3dNew.  <-- hide
      }
    // END_SKETCH_3D //
    // // mBtnSketch3dNew.setEnabled( false );

    updateList();
  }

  private void updateList()
  {
    if ( mApp.mData != null && mApp.mSID >= 0 ) {
      setTitle( R.string.title_scraps );

      List< PlotInfo > list = mApp.mData.selectAllPlots( mApp.mSID, TopoDroidApp.STATUS_NORMAL ); 
      List< Sketch3dInfo > slist = null;
      // FIXME_SKETCH_3D
        if ( mApp.mSketches ) {
          slist = mApp.mData.selectAllSketches( mApp.mSID, TopoDroidApp.STATUS_NORMAL );
        }
      // END_SKETCH_3D //
      if ( list.size() == 0 && ( slist == null || slist.size() == 0 ) ) {
        Toast.makeText( mContext, R.string.no_plots, Toast.LENGTH_SHORT ).show();
        dismiss();
      }
      // mList.setAdapter( mArrayAdapter );
      mArrayAdapter.clear();
      // mArrayAdapter.add( getResources().getString(R.string.back_to_survey) );
      for ( PlotInfo item : list ) {
        if ( item.type == PlotInfo.PLOT_PLAN /* || item.type == PlotInfo.PLOT_EXTENDED */ ) {
          String name = item.name.substring( 0, item.name.length() - 1 );

          StringWriter sw1 = new StringWriter();
          PrintWriter pw1  = new PrintWriter(sw1);
          pw1.format("<%s> %s", name, PlotInfo.plotType[ (int)PlotInfo.PLOT_PLAN ] );
          // pw1.format("<%s> %s", name, item.getTypeString() );
          mArrayAdapter.add( sw1.getBuffer().toString() );

          StringWriter sw2 = new StringWriter();
          PrintWriter pw2  = new PrintWriter(sw2);
          pw2.format("<%s> %s", name, PlotInfo.plotType[ (int)PlotInfo.PLOT_EXTENDED ] );
          // pw2.format("<%s> %s", name, item.getTypeString() );
          mArrayAdapter.add( sw2.getBuffer().toString() );
          // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "Data " + result );
        }
      }
      // FIXME_SKETCH_3D
        if ( slist != null ) {
          for ( Sketch3dInfo sketch : slist ) {
            StringWriter sw = new StringWriter();
            PrintWriter pw  = new PrintWriter(sw);
            pw.format("<%s> SKETCH-3D", sketch.name );
            mArrayAdapter.add( sw.getBuffer().toString() );
          }
        }
      // END_SKETCH_3D //
    } else {
      // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "null data or survey (" + mApp.mSID + ")" );
    }
  }
 
  // @Override
  public void onClick(View v) 
  {
    // TopoDroidLog.Log(  TopoDroidLog.LOG_INPUT, "PlotListDialog onClick() " );
    Button b = (Button) v;
    if ( b == mBtnPlotNew ) {
      hide();
      int idx = 1 + mApp.mData.maxPlotIndex( mApp.mSID );
      new PlotNewDialog( mParent, mParent, idx ).show();

    // FIXME_SKETCH_3D
    } else if ( mApp.mSketches && b == mBtnSketch3dNew ) {
      new Sketch3dNewDialog( mParent, mParent, mApp ).show();
    // END_SKETCH_3D //

    // } else if ( b == mBtnBack ) {
    //   /* nothing */
    }
    dismiss();
  }

  // ---------------------------------------------------------------
  // list items click

  // @Override 
  // public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
  // {
  //   CharSequence item = ((TextView) view).getText();
  //   String value = item.toString();
  //   // String[] st = value.split( " ", 3 );
  //   int from = value.indexOf('<');
  //   int to = value.lastIndexOf('>');
  //   String plot_name = value.substring( from+1, to );
  //   String plot_type = value.substring( to+2 );
  //   mParent.startPlotDialog( plot_name, plot_type ); // context of current SID
  //   dismiss();
  //   return true;
  // }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String value = item.toString();
    // TopoDroidLog.Log(  TopoDroidLog.LOG_INPUT, "PlotListDialog onItemClick() " + value );

    int from = value.indexOf('<');
    if ( from < 0 ) return;
    int to = value.lastIndexOf('>');
    String plot_name = value.substring( from+1, to );
    String type = value.substring( to+2 );
    long plot_type = PlotInfo.toPlotType( type );
    mParent.startExistingPlot( plot_name, plot_type ); // context of current SID
    dismiss();
  }

}
