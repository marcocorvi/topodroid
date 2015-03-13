/** @file HorizontalButtonView.java
 *
 * @author marco corvi (adapted from 
 * http://sandyandroidtutorials.blogspot.it/2013/06/horizontal-listview-tutorial.html
 *
 * @date nov 2013
 *
 * @brief TopoDroid button bar
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 *
 */
package com.topodroid.DistoX;

// import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
// import android.view.View.OnClickListener;

class HorizontalButtonView
{
  Button[] mButtons;

  HorizontalButtonView( Button[] buttons )
  {
    mButtons  = buttons;
  }

  void setButtons( Button[] buttons )
  {
    mButtons = buttons;
  }
 
  BaseAdapter mAdapter = new BaseAdapter()
  {
    @Override
    public int getCount() {
      return mButtons.length;
    }

    @Override
    public Object getItem(int position) {
      return mButtons[position];
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      // View retval = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewitem, null);
      return (View)mButtons[position];
    }
  };
}

