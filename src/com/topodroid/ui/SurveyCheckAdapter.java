/* @file SurveyCheckAdapter.java
 *
 * @author marco corvi
 * @date jul 2020
 *
 * @brief Cave3D adapter for surveys
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.ui;

// import com.topodroid.util.TDLog;
import com.topodroid.TDX.R;

// import com.topodroid.ui.MyColorPicker;
// import com.topodroid.ui.MyButton;

import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Button;
import android.widget.TextView;
import android.widget.AdapterView;
import android.view.View;
// import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.LayoutInflater;

// import android.graphics.drawable.BitmapDrawable;

import java.util.List;
import java.util.ArrayList;

public class SurveyCheckAdapter extends ArrayAdapter< String >
{
  // TODO button size = TopoDroidApp.widthPixels / 40 instead of 27

  private Context mContext;
  private List< String > mItems;
  private boolean[] mChecked;
  private final LayoutInflater mLayoutInflater;
  private final int mSize;

  public SurveyCheckAdapter( Context ctx, int res_id, List< String > items )
  {
    super( ctx, res_id, items );
    mContext = ctx;
    mItems   = items;
    mSize    = items.size();
    mChecked = new boolean[ mSize ];
    for ( int k=0; k<mSize; ++k ) mChecked[k] = true;
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
  }

  // /** @return the name at the given position - UNUSED
  //  * @param pos   index in the array of names
  //  */
  // public String get( int pos ) { return ( pos < 0 || pos >= mSize )? null : mItems.get(pos); }

  // /** @return true if the name at the given position id checked - UNUSED
  //  * @param pos   index in the array of names
  //  */
  // public boolean isChecked( int pos ) { return ( pos < 0 || pos >= mSize )? false : mChecked[pos]; }

  private class ViewHolder // implements OnClickListener
                           // , MyColorPicker.IColorChanged
  { 
    int      pos;
    String   mSurvey;   // used to make sure blocks do not hold ref to a view, that does not belong to them REVISE_RECENT
    CheckBox cbShow;
    // Button   colorBtn;
    TextView cbText;

    ViewHolder( CheckBox show, /* Button color, */ TextView text )
    {
      pos      = 0;
      mSurvey  = null; 
      cbShow   = show;
      // colorBtn = color;
      cbText   = text;
    }

    // // IColorChanged
    // public void colorChanged( int color )
    // {
    //   if ( mSurvey != null ) mSurvey.setTmpColor( color );
    //   colorBtn.setBackgroundColor( color );
    // }

    private void setCbShow( boolean checked ) { cbShow.setChecked( checked ); }

    // @Override
    // public void onClick( View v )
    // {
    //   if ( mSurvey == null ) return;
    //   if ( v.getId() == R.id.cb_show ) {
    //     setCbShow( mSurvey.switchVisible() );
    //   } else if ( v.getId() == R.id.btn_color ){
    //     (new MyColorPicker( mContext, this, mSurvey.getTmpColor() )).show();
    //   }
    // }

    void setSurvey( String name, boolean checked, int p )
    {
      // TDLog.v("holder set survey " + b.mName + " pos " + p );
      mSurvey = name;
      pos     = p;
      setCbShow( checked );
      cbText.setText( name );
      // colorBtn.setBackgroundColor( b.getTmpColor() );
    }
  }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    // TDLog.v("sketch adapter get view " + pos );
    String b = mItems.get( pos );
    boolean c = mChecked[ pos ];
    ViewHolder holder = null;
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.survey_check_row, parent, false );
      holder = new ViewHolder( 
        (CheckBox)convertView.findViewById( R.id.cb_show ),
        // (Button)convertView.findViewById( R.id.btn_color ),
        (TextView)convertView.findViewById( R.id.cb_text )
      );
      // holder.colorBtn = (Button) convertView.findViewById( R.id.btn_color );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    holder.setSurvey( b, c, pos );
    // holder.colorBtn.setBackgroundColor( b.getTmpColor() );
    // holder.colorBtn.setOnClickListener( holder );
    // b.mView = convertView;
    return convertView;
  }

  @Override
  public int getCount() { return mItems.size(); }

  // public int size() { return mItems.size(); }

  @Override
  public int getItemViewType(int pos) { return AdapterView.ITEM_VIEW_TYPE_IGNORE; }

  // public boolean addSurvey( String survey, boolean checked ) 
  // { 
  //   if ( survey == null || survey.length() == 0 ) return false;
  //   for ( String item : mItems ) if ( item.name.equals( survey ) ) return false;
  //   ++ mSize;
  //   mItems.add( survey );
  //   // TODO extend the mChecked array and set the last to checked
  //   return true;
  // }

  public List< String > getSelectedSurveys()
  {
    ArrayList< String > ret = new ArrayList<>();
    for ( int k = 0; k < mSize; ++ k ) {
      if ( mChecked[k] ) ret.add( mItems.get(k) );
    }
    return ret;
  }
 
}

