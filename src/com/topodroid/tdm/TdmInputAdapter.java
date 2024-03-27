/** @file TdmInputAdapter.java
 *
 * @author marco corvi
 * @date nov 2019
 *
 * @brief TopoDroid Manager adapter for the surveys input objects
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.ui.MyColorPicker;
import com.topodroid.TDX.R;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.Button;
// import android.widget.LinearLayout;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.LayoutInflater;

class TdmInputAdapter extends ArrayAdapter< TdmInput >
{
  // private ArrayList< TdmInput > mItems;
  private TdmConfig mConfig;
  private Context mContext;
  private LayoutInflater mLayoutInflater;

  public TdmInputAdapter( Context ctx, int id, TdmConfig config /* ArrayList< TdmInput > items */ )
  {
    // super( ctx, id, items );
    super( ctx, id, config.getInputs() );
    mContext = ctx;
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    mConfig  = config;
    // if ( items != null ) {
    //   mItems = items;
    // } else {
    //   mItems = new ArrayList< TdmInput >();
    // }
  }

  /** @return the input at a given position in the array of inputs
   * @param pos   input position
   */
  public TdmInput get( int pos ) 
  { 
    return mConfig.getInputAt( pos );
    // return mItems.get(pos);
  }

  /** @return the input with the given name
   * @param name  input name
   */
  public TdmInput get( String name ) 
  {
    return mConfig.getInput( name );
    // for ( TdmInput input : mItems ) {
    //   if ( input.getSurveyName().equals( name ) ) return input;
    // }
    // return null;
  }

  // /** insert a new input UNUSED
  //  * @param input   new TdmInput 
  //  */
  // public void addInput( TdmInput input ) 
  // { 
  //   mConfig.addInput( input );
  //   // mItems.add( input );
  // }

  // /** remove an input UNUSED
  //  * @param input   TdmInput to romove
  //  */
  // public void dropInput( TdmInput input )
  // {
  //   mConfig.dropInput( input );
  //   // mItems.remove( input );
  // }

  // /** remove chacked inputs UNUSED
  //  */
  // public void dropChecked( ) 
  // {
  //   mConfig.dropChecked();
  //   // final Iterator it = mItems.iterator();
  //   // while ( it.hasNext() ) {
  //   //   TdmInput input = (TdmInput) it.next();
  //   //   if ( input.isChecked() ) {
  //   //     mItems.remove( input );
  //   //   }
  //   // }
  // }

  /** @return the number of input items
   */
  public int size()
  {
    return mConfig.getInputsSize();
    // return mItems.size();
  }

  private class ViewHolder implements OnClickListener
                      , MyColorPicker.IColorChanged
  { 
    CheckBox checkBox;
    TextView textView;
    Button   colorBtn;
    TdmInput mInput;

    @Override
    public void onClick( View v ) 
    {
      TDLog.v("HOLDER TODO color picker");
      (new MyColorPicker( mContext, this, mInput.getColor() )).show();
    }

    // IColorChanged
    public void colorChanged( int color )
    {
      mInput.setColor( color );
      colorBtn.setBackgroundColor( color );
    }

  }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    TdmInput b = get(pos); // mItems.get( pos );
    if ( b == null ) return convertView;

    ViewHolder holder = null; 
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.tdinput_adapter, null ); // parent crashes app
      holder = new ViewHolder();
      holder.checkBox = (CheckBox) convertView.findViewById( R.id.tdinput_checked );
      holder.textView = (TextView) convertView.findViewById( R.id.tdinput_name );
      holder.textView.setTextSize( TDSetting.mTextSize );
      holder.colorBtn = (Button) convertView.findViewById( R.id.tdinput_color );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    holder.checkBox.setOnClickListener( b );
    holder.checkBox.setChecked( b.isChecked() );
    holder.textView.setText( b.getSurveyName() );
    holder.colorBtn.setBackgroundColor( b.getColor() );
    holder.colorBtn.setOnClickListener( holder );
    holder.mInput = b;
    return convertView;
  }

}

