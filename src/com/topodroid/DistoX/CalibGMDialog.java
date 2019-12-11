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
package com.topodroid.DistoX;

import java.util.Locale;
import java.lang.ref.WeakReference;

// import android.app.Dialog;
import android.os.Bundle;

// import android.content.Intent;
import android.content.Context;
// import android.content.DialogInterface;
import android.inputmethodservice.KeyboardView;

import android.text.method.KeyListener;
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

class CalibGMDialog extends MyDialog
                    implements OnClickListener
{
  private final WeakReference<GMActivity> mParent; 
  private final CalibCBlock mBlk;
  private final String mErrorGroupRequired;
  private final String mErrorGroupNonInt;

  // private EditText mETbearing;
  // private EditText mETclino;
  // private EditText mETroll;

  // private TextView mTVerror;
  
  private EditText mETname;  // group number
  private Button   mButtonOK;
  private Button   mButtonDelete;
  private MyCheckBox mCBregroup = null;
  private MyStateBox mSBregroup = null;
  private TextView   mTVregroup = null;
  private Button   mButtonCancel;

  private MyKeyboard mKeyboard = null;

  /**
   * @param context   context
   * @param parent    parent GM activity
   * @param blk       calibration data
   */
  CalibGMDialog( Context context, GMActivity parent, CalibCBlock blk )
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

    EditText eTbearing = (EditText) findViewById( R.id.gm_bearing );
    EditText eTclino   = (EditText) findViewById( R.id.gm_clino   );
    EditText eTroll    = (EditText) findViewById( R.id.gm_roll    );
    TextView tVerror   = (TextView) findViewById( R.id.gm_error );

    mETname = (EditText) findViewById(R.id.gm_name);

    LinearLayout layout2 = (LinearLayout) findViewById( R.id.layout2 );
    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    layout2.setMinimumHeight( size + 20 );
    
    LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 0, 10, 20, 10 );

    // mButtonOK     = new MyCheckBox( mContext, size, R.drawable.iz_save, R.drawable.iz_save ); 
    mButtonDelete = new MyCheckBox( mContext, size, R.drawable.iz_delete, R.drawable.iz_delete ); 
    layout2.addView( mButtonDelete, lp );

    if ( TDLevel.overExpert ) {
      mSBregroup = new MyStateBox( mContext, R.drawable.iz_numbers_no, R.drawable.iz_numbers_ok, R.drawable.iz_numbers_ok );
      mSBregroup.setState( 0 );
      mSBregroup.setOnClickListener( this );
      layout2.addView( mSBregroup, lp );
      mTVregroup = new TextView( mContext );
      mTVregroup.setText( R.string.regroup );
      layout2.addView( mTVregroup, lp );
    } else {
      mCBregroup    = new MyCheckBox( mContext, size, R.drawable.iz_numbers_ok, R.drawable.iz_numbers_no ); 
      mCBregroup.setState( false );
      layout2.addView( mCBregroup, lp );
    }

    // layout2.addView( mButtonOK, lp );

    mButtonOK     = (Button) findViewById(R.id.gm_ok );
    mButtonCancel = (Button) findViewById(R.id.gm_cancel );
    // mButtonDelete = (Button) findViewById(R.id.gm_delete );

    eTbearing.setText( String.format(Locale.US, "%.1f", mBlk.mBearing ) );
    eTclino.setText( String.format(Locale.US, "%.1f", mBlk.mClino ) );
    eTroll.setText( String.format(Locale.US, "%.1f", mBlk.mRoll ) );
    tVerror.setText( String.format(Locale.US, "%.4f", mBlk.mError ) );

    mETname.setText( String.format(Locale.US, "%d", mBlk.mGroup ) );
    mButtonOK.setOnClickListener( this );
    mButtonDelete.setOnClickListener( this );
    mButtonCancel.setOnClickListener( this );

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ), R.xml.my_keyboard_base_sign, -1 );
    if ( TDSetting.mKeyboard ) {
      MyKeyboard.registerEditText( mKeyboard, mETname, MyKeyboard.FLAG_SIGN );
    } else {
      mETname.setInputType( TDConst.NUMBER_SIGNED );
    }
    setEditable( eTbearing ); // , null, false, MyKeyboard.FLAG_POINT );
    setEditable( eTclino ); // ,   null, false, MyKeyboard.FLAG_POINT );
    setEditable( eTroll ); // ,    null, false, MyKeyboard.FLAG_POINT );
  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TDLog.Log( TDLog.LOG_INPUT, "GM dialog onClick button " + b.getText().toString() );
    if ( b == mButtonOK ) {
      MyKeyboard.close( mKeyboard );
      String name = mETname.getText().toString();
      // if ( name == null || name.length() == 0 ) {
      //   name = mETname.getHint().toString();
      // }
      GMActivity parent = mParent.get();
      if ( parent != null ) {
        if ( /* name == null || */ name.length() == 0 ) { // name == null always false
          mETname.setError( mErrorGroupRequired );
          return;
        } else {
          try {
            long value = Long.parseLong( name );
            parent.updateGM( value, name );
          } catch ( NumberFormatException e ) {
            mETname.setError( mErrorGroupNonInt );
            return;
          }
        }
        if ( TDLevel.overExpert ) {
          switch ( mSBregroup.getState() ) {
            case 1: parent.resetAndComputeGroups( mBlk.mId, TDSetting.GROUP_BY_FOUR ); break;
            case 2: parent.resetAndComputeGroups( mBlk.mId, TDSetting.GROUP_BY_ONLY_16 ); break;
          }
        } else {
          if ( mCBregroup.isChecked() ) {
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
    } else if ( TDLevel.overExpert && b == mSBregroup ) {
      switch ( mSBregroup.getState() ) {
        case 0: mSBregroup.setState(1); mTVregroup.setText("TopoDroid"); break; 
        case 1: mSBregroup.setState(2); mTVregroup.setText("PocketTopo"); break; 
        case 2: mSBregroup.setState(0); mTVregroup.setText(R.string.regroup); break; 
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

