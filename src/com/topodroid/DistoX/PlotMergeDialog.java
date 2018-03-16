/** @file PlotMergeDialog.java
 *
 * @author marco corvi
 * @date jul 2017
 *
 * @brief TopoDroid list of plots to merge in the active plot
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

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;

import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.widget.AdapterView.OnItemLongClickListener;

import android.widget.Toast;

class PlotMergeDialog extends MyDialog
                             implements OnItemClickListener
                                        , OnClickListener
{
  private DrawingWindow mParent;
  private ArrayAdapter<String> mArrayAdapter;

  // private ListView mList;
  private List<PlotInfo> mPlots;

  PlotMergeDialog( Context context, DrawingWindow parent, List<PlotInfo> plots )
  {
    super( context, R.string.PlotMergeDialog );
    mParent = parent;
    mPlots  = plots;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.plot_merge_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mArrayAdapter = new ArrayAdapter<>( mContext, R.layout.message );

    ListView list = (ListView) findViewById(R.id.list);
    list.setAdapter( mArrayAdapter );
    list.setOnItemClickListener( this );
    list.setDividerHeight( 2 );

    ((Button) findViewById( R.id.button_cancel )).setOnClickListener( this );

    setTitle( R.string.title_plot_merge );
    for ( PlotInfo plt : mPlots ) {
      mArrayAdapter.add( plt.name );
    }
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
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String name = item.toString();
    // int len = name.indexOf(" ");
    // name = name.substring(0, len);
    for ( PlotInfo plt : mPlots ) {
      if ( plt.name.equals( name ) ) {
        mParent.doMergePlot( plt );
        break;
      }
    }
    dismiss();
  }

  @Override 
  public void onClick( View v ) 
  {
    dismiss();
  }

}
