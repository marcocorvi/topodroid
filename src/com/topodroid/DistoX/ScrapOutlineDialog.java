/** @file ScrapOutlineDialog.java
 *
 * @author marco corvi
 * @date june 2017
 *
 * @brief TopoDroid list of scrap outlines
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
import android.content.res.Resources;

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

public class ScrapOutlineDialog extends MyDialog
                        implements OnItemClickListener
                                // , OnItemLongClickListener
                                , View.OnClickListener
{
  private DrawingWindow mParent;
  private TopoDroidApp mApp;
  private ArrayAdapter<String> mArrayAdapter;
  // private ListItemAdapter mArrayAdapter;
  private Button mBtnBack;
  private Button mBtnClear;
  private ListView mList;
  private List< PlotInfo > mPlots;

  public ScrapOutlineDialog( Context context, DrawingWindow parent, TopoDroidApp app, List< PlotInfo > plots )
  {
    super( context, R.string.ScrapOutlineDialog );
    mParent = parent;
    mApp    = app;
    mPlots  = plots;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.scrap_outline_dialog );
    mArrayAdapter = new ArrayAdapter<String>( mContext, R.layout.message );
    // mArrayAdapter = new ListItemAdapter( mContext, R.layout.message );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    // mList.setDividerHeight( 2 );

    mBtnBack = (Button) findViewById(R.id.btn_back);
    mBtnBack.setOnClickListener( this );
    mBtnClear = (Button) findViewById(R.id.btn_clear);
    mBtnClear.setOnClickListener( this );

    updateList();
    setTitle( R.string.title_scraps_outline );
  }

  private void updateList()
  {
    Resources res = mApp.getResources();
    mArrayAdapter.clear();
    for ( PlotInfo item : mPlots ) {
      String name = item.name.substring( 0, item.name.length() - 1 );
      mArrayAdapter.add( String.format("<%s> %s", name, PlotInfo.plotTypeString( (int)PlotInfo.PLOT_PLAN, res ) ) );
    }
    mList.setAdapter( mArrayAdapter );
  }
 
  // @Override
  public void onClick(View v) 
  {
    // TDLog.Log(  TDLog.LOG_INPUT, "ScrapOutlineDialog onClick() " );
    Button b = (Button) v;
    if ( b == mBtnClear ) {
      mParent.addScrap( null );
    } else if ( b == mBtnBack ) {
      /* nothing */
    }
    dismiss();
  }

  // ---------------------------------------------------------------
  // list items click

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    // CharSequence item = ((TextView) view).getText();
    // String value = item.toString();
    // int from = value.indexOf('<');
    // if ( from < 0 ) return;
    // int to = value.lastIndexOf('>');
    // String plot_name = value.substring( from+1, to );
    // String type = value.substring( to+2 );

    // long plot_type = PlotInfo.PLOT_PLAN;
    // Resources res = mApp.getResources();
    // if ( res.getString( R.string.plan ).equals( type ) ) {
    //   plot_type = PlotInfo.PLOT_PLAN;
    // } else if ( res.getString( R.string.extended ).equals( type ) ) {
    //   plot_type = PlotInfo.PLOT_EXTENDED;
    // }
    mParent.addScrap( mPlots.get( pos ) );
    dismiss();
  }

}

