/** @file HelpAdapter.java
 *
 * @author marco corvi
 * @date nov 2013
 *
 * @brief TopoDroid help dialog items adapter
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
// import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;


import android.widget.LinearLayout;

// import android.util.Log;

class HelpAdapter extends ArrayAdapter< HelpEntry >
{
  private ArrayList< HelpEntry > mItems;
  // private Context mContext;
  private HelpDialog mParent;

  public HelpAdapter( Context ctx, HelpDialog parent, int id, ArrayList< HelpEntry > items )
  {
    super( ctx, id, items );
    mParent = parent;

    if ( items != null ) {
      mItems = items;
    } else {
      mItems = new ArrayList< HelpEntry >();
    }
  }

  public void add( HelpEntry item ) 
  {
    mItems.add( item );
  }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    HelpEntry b = mItems.get( pos );
    if ( b == null ) return convertView;
    return b.mView;
  }

}


