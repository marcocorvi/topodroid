/* @file TDPrefSearchDialog.java
 *
 * @author marco corvi
 * @date jul 2025
 *
 * @brief TopoDroid search dialog for settings
 *
 * displays the stack of saved stations and allows to push 
 * a station on it or pop one from it
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.prefs;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
import com.topodroid.ui.MyKeyboard;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.R;

import android.os.Bundle;
// import android.app.Dialog;

import android.content.Context;

import android.view.View;
// import android.view.View.OnClickListener;
// import android.view.ViewGroup.LayoutParams;
import android.inputmethodservice.KeyboardView;

import android.widget.EditText;
import android.widget.Button;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.List;

class TDPrefSearchDialog extends MyDialog
                        implements View.OnClickListener
                        , OnItemClickListener
{
  private static String mKey = null;
  private final TDPrefActivity mParent;
  private EditText mName;
  private ListView mList;
  // private LinearLayout mView;
  
  private List<Integer> mResult = null;

  private MyKeyboard mKeyboard = null;

  TDPrefSearchDialog( Context context, TDPrefActivity parent )
  {
    super( context, null, R.string.TDPrefSearchDialog ); // null app
    mParent  = parent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.pref_search_dialog, R.string.title_pref_search );

    // mView = (LinearLayout) findViewById( R.id.parent );
    mName = (EditText) findViewById( R.id.name );
    if ( mKey != null ) mName.setText( mKey );
    mList = (ListView)findViewById( R.id.result );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    ( (Button) findViewById(R.id.btn_search) ).setOnClickListener( this ); // SEARCH
    ( (Button) findViewById(R.id.btn_cancel) ).setOnClickListener( this ); // CANCEL


    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ), 
                                R.xml.my_keyboard_base_sign, R.xml.my_keyboard_qwerty );
    if ( TDSetting.mKeyboard ) {
      int flag = MyKeyboard.FLAG_POINT_LCASE_2ND;
      MyKeyboard.registerEditText( mKeyboard, mName, flag);
    } else {
      mKeyboard.hide();
    }
  }

  /** react to a user tap on an item (menu entry)
   * @param parent   ...
   * @param view     tapped view
   * @param pos      item position
   * @param id       ...
   */
  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    // TODO
    int cat = mResult.get( pos ).intValue();
    TDLog.v("On Item Click pos " + pos + " category " + cat );
    dismiss();
    mParent.loadCategory( cat );
  }

  @Override
  public void onClick(View v) 
  {
    // MyKeyboard.close( mKeyboard );

    // TDLog.v(  "Pref Search Dialog onClick() " );
    if ( v.getId() == R.id.btn_search ) { // SEARCH 
      mKey = mName.getText().toString().trim();
      mResult = TDPrefKey.match( mKey );
      if ( mResult.isEmpty() ) {
        TDToast.make( R.string.no_search_result );
      } else {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>( mContext, R.layout.message );
        // TDLog.v("search results " + mResult.size() );
        for ( Integer res : mResult ) {
          int cat = res.intValue();
          // TDLog.v("search results " + cat + " " + mContext.getResources().getString( TDPrefCat.mTitleRes[ cat ] ) );
          arrayAdapter.add( mContext.getResources().getString( TDPrefCat.mTitleRes[ cat ] ) );
        }
        mList.setAdapter( arrayAdapter );
        mList.invalidate();
        // mView.requestLayout();
      }
    } else if ( v.getId() == R.id.btn_cancel ) {
      dismiss();
    }
  }

  @Override
  public void onBackPressed()
  {
    if ( MyKeyboard.close( mKeyboard ) ) return;
    dismiss();
  }

}

