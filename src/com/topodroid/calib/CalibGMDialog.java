/* @file CalibGMDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid calibration data dialog (to assign the group number)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.calib;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.ui.MyStateBox;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.MyCheckBox;
import com.topodroid.ui.TDLayout;
import com.topodroid.ui.MyKeyboard;
import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.R;
import com.topodroid.TDX.GMActivity;
import com.topodroid.TDX.TDLevel;
import com.topodroid.TDX.TDConst;

import java.util.Locale;
import java.lang.ref.WeakReference;

// import android.app.Dialog;
import android.os.Bundle;

// import android.content.Intent;
import android.content.Context;
// import android.content.DialogInterface;
import android.inputmethodservice.KeyboardView;

// import android.text.method.KeyListener;
// import android.text.InputType;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
// import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.view.View;
import android.view.View.OnClickListener;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;

// import android.view.Window;
// import android.view.WindowManager;

public class CalibGMDialog extends MyDialog
                    implements OnClickListener
{
  private final WeakReference<GMActivity> mParent; 
  private final CBlock mBlk;
  private final String mErrorGroupRequired;
  private final String mErrorGroupNonInt;

  // private EditText mET_bearing;
  // private EditText mET_clino;
  // private EditText mET_roll;
  // private TextView mTV_error;
  
  private EditText mET_name;  // group number
  private Button   mButtonOK;
  private Button   mButtonDelete;
  private MyCheckBox mCB_regroup = null;
  private MyStateBox mSB_regroup = null;
  private TextView   mTV_regroup = null;
  private Button   mButtonCancel;

  private MyKeyboard mKeyboard = null;

  /**
   * @param context   context
   * @param parent    parent GM activity
   * @param blk       calibration data
   */
  public CalibGMDialog( Context context, GMActivity parent, CBlock blk )
  {
    super( context, R.string.CalibGMDialog );
    mParent = new WeakReference<GMActivity>( parent );
    mErrorGroupRequired = parent.getResources().getString( R.string.error_group_required );
    mErrorGroupNonInt   = parent.getResources().getString( R.string.error_group_non_integer );
    mBlk    = blk;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.calib_gm_dialog, null );

    EditText eT_bearing = (EditText) findViewById( R.id.gm_bearing );
    EditText eT_clino   = (EditText) findViewById( R.id.gm_clino   );
    EditText eT_roll    = (EditText) findViewById( R.id.gm_roll    );
    TextView tV_error   = (TextView) findViewById( R.id.gm_error );

    mET_name = (EditText) findViewById(R.id.gm_name);

    LinearLayout layout2 = (LinearLayout) findViewById( R.id.layout2 );
    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    layout2.setMinimumHeight( size + 20 );
    
    LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 0, 10, 20, 10 );

    // mButtonOK     = new MyCheckBox( mContext, size, R.drawable.iz_save, R.drawable.iz_save ); 
    mButtonDelete = new MyCheckBox( mContext, size, R.drawable.iz_delete, R.drawable.iz_delete ); 
    mButtonDelete.setOnClickListener( this );
    layout2.addView( mButtonDelete, lp );

    if ( TDLevel.overExpert ) {
      mSB_regroup = new MyStateBox( mContext, R.drawable.iz_numbers_no, R.drawable.iz_numbers_ok, R.drawable.iz_numbers_ok );
      mSB_regroup.setState( 0 );
      mSB_regroup.setOnClickListener( this );
      layout2.addView( mSB_regroup, lp );
      mTV_regroup = new TextView( mContext );
      mTV_regroup.setText( R.string.regroup );
      layout2.addView( mTV_regroup, lp );
    } else {
      mCB_regroup    = new MyCheckBox( mContext, size, R.drawable.iz_numbers_ok, R.drawable.iz_numbers_no );
      mCB_regroup.setState( false );
      layout2.addView( mCB_regroup, lp );
    }

    // layout2.addView( mButtonOK, lp );

    mButtonOK     = (Button) findViewById(R.id.gm_ok );
    mButtonOK.setOnClickListener( this );
    mButtonCancel = (Button) findViewById(R.id.gm_cancel );
    mButtonCancel.setOnClickListener( this );

    eT_bearing.setText( String.format(Locale.US, "%.1f", mBlk.mBearing ) );
    eT_clino.setText( String.format(Locale.US, "%.1f", mBlk.mClino ) );
    eT_roll.setText( String.format(Locale.US, "%.1f", mBlk.mRoll ) );
    tV_error.setText( String.format(Locale.US, "%.4f", mBlk.mError ) );

    mET_name.setText( String.format(Locale.US, "%d", mBlk.mGroup ) );

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ), R.xml.my_keyboard_base_sign, -1 );
    if ( TDSetting.mKeyboard ) {
      MyKeyboard.registerEditText( mKeyboard, mET_name, MyKeyboard.FLAG_SIGN );
    } else {
      mET_name.setInputType( TDConst.NUMBER_SIGNED );
    }
    setEditable( eT_bearing ); // , null, false, MyKeyboard.FLAG_POINT );
    setEditable( eT_clino ); // ,   null, false, MyKeyboard.FLAG_POINT );
    setEditable( eT_roll ); // ,    null, false, MyKeyboard.FLAG_POINT );
  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TDLog.Log( TDLog.LOG_INPUT, "GM dialog onClick button " + b.getText().toString() );
    if ( b == mButtonOK ) {
      MyKeyboard.close( mKeyboard );
      String name = mET_name.getText().toString();
      // if ( name == null || name.length() == 0 ) {
      //   name = mET_name.getHint().toString();
      // }
      GMActivity parent = mParent.get();
      if ( parent != null ) {
        if ( /* name == null || */ name.length() == 0 ) { // name == null always false
          mET_name.setError( mErrorGroupRequired );
          return;
        } else {
          try {
            long value = Long.parseLong( name );
            parent.updateGM( value, name );
          } catch ( NumberFormatException e ) {
            mET_name.setError( mErrorGroupNonInt );
            return;
          }
        }
        if ( TDLevel.overExpert ) {
          switch ( mSB_regroup.getState() ) {
            case 1: parent.resetAndComputeGroups( mBlk.mId, TDSetting.GROUP_BY_FOUR ); break;
            case 2: parent.resetAndComputeGroups( mBlk.mId, TDSetting.GROUP_BY_ONLY_16 ); break;
          }
        } else {
          if ( mCB_regroup.isChecked() ) {
            parent.resetAndComputeGroups( mBlk.mId, TDSetting.mGroupBy );
          }
        }
      } else {
        TDLog.Error("GM Dialog null parent [1]" );
      }
    } else if ( b == mButtonDelete ) {
      GMActivity parent = mParent.get();
      if ( parent != null ) {
        parent.deleteGM( true );
      } else {
        TDLog.Error("GM Dialog null parent [2]" );
      }
    // } else if ( b == mButtonCancel ) {
      /* nothing */
    } else if ( TDLevel.overExpert && b == mSB_regroup ) {
      switch ( mSB_regroup.getState() ) {
        case 0: mSB_regroup.setState(1); mTV_regroup.setText("TopoDroid"); break;
        case 1: mSB_regroup.setState(2); mTV_regroup.setText("PocketTopo"); break;
        case 2: mSB_regroup.setState(0); mTV_regroup.setText(R.string.regroup); break;
      }
      return;
    }
    dismiss();
  }


  private void setEditable( EditText et ) // , KeyListener kl, boolean editable, int flag )
  {
    if ( TDSetting.mKeyboard ) {
      et.setKeyListener( null );
      et.setClickable( true );
      et.setFocusable( false ); // editable = false
      // if ( editable ) {
      //   MyKeyboard.registerEditText( mKeyboard, et, MyKeyboard.FLAG_POINT ); // flag = MyKeyboard.FLAG_POINT
      //   // et.setKeyListener( mKeyboard );
      //   et.setBackgroundResource( android.R.drawable.edit_text );
      // } else {
        MyKeyboard.registerEditText( mKeyboard, et, MyKeyboard.FLAG_POINT | MyKeyboard.FLAG_NOEDIT );
        et.setBackgroundColor( TDColor.MID_GRAY );
      // }
    } else {
      // if ( editable ) {
      //   et.setKeyListener( kl );
      //   et.setBackgroundResource( android.R.drawable.edit_text );
      //   et.setClickable( true );
      //   et.setFocusable( true );
      // } else {
        // et.setFocusable( false );
        // et.setClickable( false );
        et.setKeyListener( null );
        et.setBackgroundColor( TDColor.MID_GRAY );
      // }
    }
  }

}

