/* @file PlotListDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid option list
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDStatus;
import com.topodroid.ui.MyDialog;
import com.topodroid.common.PlotType;

import java.util.List;
// import java.util.ArrayList;

import android.os.Bundle;
// import android.app.Dialog;

import android.content.Context;
import android.content.res.Resources;

import android.view.View;
// import android.view.View.OnClickListener;

import android.widget.ArrayAdapter;
// import android.widget.ListView;
import android.widget.GridView;
import android.widget.Button;

import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.widget.AdapterView.OnItemLongClickListener;

class PlotListDialog extends MyDialog
                        implements OnItemClickListener
                        // , OnItemLongClickListener
                        , View.OnClickListener
{
  private final ShotWindow    mParent;
  private final DrawingWindow mParent2;
  private final TopoDroidApp mApp;
  private ArrayAdapter<String> mArrayAdapter;
  // private ListItemAdapter mArrayAdapter;
  private Button mBtnPlotNew;
  private Button mBtnClose;
  private Button mBtnBack;

  private final boolean mDoNew;

  /* FIXME_SKETCH_3D *
  private Button mBtnSketch3dNew;
   * END_SKETCH_3D */
  private int mPlots = 0; // not useful, but it's ok

  // private ListView mList;
  // private GridView mList;

  PlotListDialog( Context context, ShotWindow parent, TopoDroidApp app, DrawingWindow parent2 )
  {
    super( context, R.string.PlotListDialog );
    mParent  = parent;
    mParent2 = parent2;
    mApp = app;
    mDoNew = ( mParent2 == null );
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.plot_list_dialog, R.string.title_scraps );
    mArrayAdapter = new ArrayAdapter<>( mContext, R.layout.message );
    // mArrayAdapter = new ListItemAdapter( mContext, R.layout.message );

    GridView list = (GridView) findViewById(R.id.list);
    list.setAdapter( mArrayAdapter );
    list.setOnItemClickListener( this );
    // list.setDividerHeight( 2 );
    list.setHorizontalSpacing( 2 );

    mBtnPlotNew = (Button) findViewById(R.id.plot_new);
    mBtnClose   = (Button) findViewById(R.id.btn_close);
    mBtnBack    = (Button) findViewById(R.id.btn_back);

    if ( mDoNew ) {
      mBtnPlotNew.setOnClickListener( this );
      mBtnClose.setVisibility( View.GONE );
    } else {
      mBtnPlotNew.setEnabled( false );
      mBtnPlotNew.setVisibility( View.GONE );
      mBtnClose.setOnClickListener( this );
    }
    mBtnBack.setOnClickListener( this );

    /* FIXME_SKETCH_3D *
      mBtnSketch3dNew = (Button) findViewById(R.id.sketch3d_new);
      if ( TopoDroidApp.mSketches && mDoNew ) {
        mBtnSketch3dNew.setOnClickListener( this );
      } else {
        mBtnSketch3dNew.setEnabled( false );
        mBtnSketch3dNew.setVisibility( View.GONE );
        // mBtnSketch3dNew.  <-- hide
      }
     * END_SKETCH_3D */
    // // mBtnSketch3dNew.setEnabled( false );

    updateList();
  }

  private void updateList()
  {
    if ( TopoDroidApp.mData != null && TDInstance.sid >= 0 ) {
      // setTitle( R.string.title_scraps );

      Resources res = mApp.getResources();

      List< PlotInfo > list = TopoDroidApp.mData.selectAllPlots( TDInstance.sid, TDStatus.NORMAL ); 
      /* FIXME_SKETCH_3D *
        List< Sketch3dInfo > slist = null;
        if ( TopoDroidApp.mSketches ) {
          slist = TopoDroidApp.mData.selectAllSketches( TDInstance.sid, TDStatus.NORMAL );
        }
      if ( list.size() == 0 && ( slist == null || slist.size() == 0 ) ) 
       * END_SKETCH_3D */
      // TDLog("DistoX-PLOT", "list size " + list.size() );
      if ( list.size() == 0 )
      {
        TDToast.make( R.string.no_plots );
        dismiss();
      }
      // mList.setAdapter( mArrayAdapter );
      mArrayAdapter.clear();
      // mArrayAdapter.add( res.getString(R.string.back_to_survey) );
      mPlots = 0;
      for ( PlotInfo item : list ) {
        // if ( item.type == PlotType.PLOT_PLAN /* || item.type == PlotType.PLOT_EXTENDED */ ) 
        if ( PlotType.isProfile( item.type ) )
        {
          String name = item.name.substring( 0, item.name.length() - 1 );
          mArrayAdapter.add( PlotType.plotTypeString( name, (int)PlotType.PLOT_PLAN, res ) );
          mArrayAdapter.add( PlotType.plotTypeString( name, item.type, res ) );
          mPlots += 2;
        }
      }
      /* FIXME_SKETCH_3D *
        if ( slist != null ) {
          for ( Sketch3dInfo sketch : slist ) {
            mArrayAdapter.add(
              String.format( PlotType.plotTypeString( name, (int)PlotType.PLOT_SKETCH_3D, res ) );
          }
        }
       * END_SKETCH_3D */
    // } else {
    //   // TDLog.Log( TDLog.LOG_PLOT, "null data or survey (" + TDInstance.sid + ")" );
    }
  }
 
  // @Override
  public void onClick(View v) 
  {
    // TDLog.Log(  TDLog.LOG_INPUT, "PlotListDialog onClick() " );
    Button b = (Button) v;
    if ( b == mBtnPlotNew ) {
      hide();
      if ( mParent != null ) {
        int idx = 1 + TopoDroidApp.mData.maxPlotIndex( TDInstance.sid );
        new PlotNewDialog( mContext, mApp, mParent, idx ).show();
      }
    } else if ( b == mBtnClose ) {
      if ( mParent2 != null ) {
        mParent2.doClose();
      }
    } else if ( b == mBtnBack ) {
      /* nothing */
    /* FIXME_SKETCH_3D *
    } else if ( TopoDroidApp.mSketches && b == mBtnSketch3dNew ) {
      if ( mParent != null ) {
        // new Sketch3dNewDialog( mContext, mParent, mApp ).show();
        new Sketch3dNewDialog( mContext, mParent ).show();
      }
     * END_SKETCH_3D */

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
    if ( ! ( view instanceof TextView ) ) {
      TDLog.Error("plot list view instance of " + view.toString() );
      return;
    }
    String value = ((TextView) view).getText().toString();
    // TDLog.Log(  TDLog.LOG_INPUT, "PlotList dialog item click() " + value );

    // int from = value.indexOf('<');
    // if ( from < 0 ) return;
    int to = value.lastIndexOf(':');
    if ( position < mPlots ) {
      // String plot_name = value.substring( from+1, to );
      String plot_name = value.substring( 0, to );
      String type = value.substring( to+2 );

      long plot_type = PlotType.stringToPlotType( type );

      // long plot_type = (( position % 2 ) == 0 )? PlotType.PLOT_PLAN : PlotType.PLOT_EXTENDED;
      if ( mParent != null ) {
        mParent.startExistingPlot( plot_name, plot_type, null ); // context of current SID
      } else {
        mParent2.switchNameAndType( plot_name, plot_type ); // context of current SID
      }
    /* FIXME_SKETCH_3D *
    } else {
      if ( mParent != null ) {
        String sketch_name = value.substring( from+1, to );
        mParent.startSketchWindow( sketch_name ); // context of current SID
      }
     * END_SKETCH_3D */
    }
    dismiss();
  }

}
