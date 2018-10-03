/* @file MultishotDialog.java
 *
 * @author marco corvi
 * @date jun 2018
 *
 * @brief TopoDroid survey multi-shot dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.regex.Pattern;
import java.util.List;

// import android.app.Dialog;
import android.os.Bundle;
// import android.widget.RadioButton;

// import android.text.method.KeyListener;
import android.text.InputType;

// import android.widget.Spinner;
// import android.widget.ArrayAdapter;


import android.content.Context;
// import android.content.res.Resources;
// import android.content.DialogInterface;
import android.inputmethodservice.KeyboardView;
// import android.graphics.Paint.FontMetrics;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
// import android.widget.Toast;

import android.view.View;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;

// import android.util.Log;

class MultishotDialog extends MyDialog
                      implements View.OnClickListener
			       , MyColorPicker.IColorChanged
{
  private final ShotWindow mParent;
  private List<DBlock> mBlks;
  private DBlock mBlk = null;

  private Pattern mPattern; // name pattern

  // private TextView mTVdata;

  private EditText mETfrom;
  private EditText mETto;
  private TextView mTVstrikeDip;
  
  private Button   mButtonRenumber;
  // private Button   mButtonHighlight; FIXME_HIGHLIGHT
  private Button   mButtonColor;
  private Button   mButtonSplays;
  private Button   mButtonBedding;
  private Button   mButtonBack;

  private MyKeyboard mKeyboard = null;

  MultishotDialog( Context context, ShotWindow parent, List<DBlock> blks )
  {
    super( context, R.string.MultishotDialog );
    mParent = parent;
    mBlks = blks;
    mBlk  = mBlks.get(0); // blks is guaranteed non-null and non-empty
  }

// -------------------------------------------------------------------

  // @Override
  // public void onRestoreInstanceState( Bundle icicle )
  // {
  //   // FIXME DIALOG mKeyboard.hide();
  // }
 
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.multishot_dialog, null );

    mETfrom    = (EditText) findViewById(R.id.shot_from );
    mETto      = (EditText) findViewById(R.id.shot_to );
    mTVstrikeDip = (TextView) findViewById(R.id.strike_dip );

    mETfrom.setText( mBlk.mFrom );
    mETto.setText( mBlk.mTo );
   
    // mETfrom.setOnLongClickListener( this );
    // mETto.setOnLongClickListener( this );

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ),
                                R.xml.my_keyboard_base_sign, R.xml.my_keyboard_qwerty );
    if ( TDSetting.mKeyboard ) {
      int flag = MyKeyboard.FLAG_POINT_LCASE_2ND;
      if ( TDSetting.mStationNames == 1 ) flag = MyKeyboard.FLAG_POINT;
      MyKeyboard.registerEditText( mKeyboard, mETfrom,  flag );
      MyKeyboard.registerEditText( mKeyboard, mETto,    flag );
    } else {
      mKeyboard.hide();
      if ( TDSetting.mStationNames == 1 ) {
        mETfrom.setInputType( InputType.TYPE_CLASS_NUMBER );
        mETto.setInputType( InputType.TYPE_CLASS_NUMBER );
      }
    }

    // int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    // TDToast.make( mContext, "SIZE " + size );
    
    // LinearLayout layout4 = (LinearLayout) findViewById( R.id.layout4 );
    // layout4.setMinimumHeight( size + 20 );

    mButtonRenumber  = (Button) findViewById(R.id.renumber );
    // mButtonHighlight = (Button) findViewById(R.id.highlight ); FIXME_HIGHLIGHT
    mButtonColor     = (Button) findViewById(R.id.color );
    mButtonSplays    = (Button) findViewById(R.id.splays );
    mButtonBedding   = (Button) findViewById(R.id.bedding );
    mButtonBack      = (Button) findViewById(R.id.btn_back );

    // boolean renumber = true;
    // mBlk = mBlks.get(0);
    // if ( mBlk != null && mBlk.isLeg() ) {
      mButtonRenumber.setOnClickListener( this );
    // } else {
    //   renumber = false;
    //   ((LinearLayout) findViewById( R.id.layout_renumber )).setVisibility( View.GONE );
    // }

    boolean highlight = true;
    for ( DBlock blk : mBlks ) {
      if ( blk.mTo != null && blk.mTo.length() > 0 ) {
        highlight = false;
        break;
      }
    }
    if ( highlight ) {
      // mButtonHighlight.setOnClickListener( this ); FIXME_HIGHIGHT
      mButtonColor.setOnClickListener( this );
      mButtonSplays.setOnClickListener( this );
    } else {
      // ((LinearLayout) findViewById( R.id.layout_highlight )).setVisibility( View.GONE ); FIXME_HIGHIGHT
      ((LinearLayout) findViewById( R.id.layout_color )).setVisibility( View.GONE );
      ((LinearLayout) findViewById( R.id.layout_splays )).setVisibility( View.GONE );
    }

    String from = mBlk.mFrom;
    boolean bedding = ( mBlks.size() > 1 && from != null && from.length() > 0 );
    if ( bedding ) {
      for ( DBlock blk : mBlks ) {
        if ( ! from.equals( blk.mFrom ) ) {
	  bedding = false;
	  break;
	}
      }
    }
    if ( bedding ) {
      mButtonBedding.setOnClickListener( this );
    } else {
      ((LinearLayout) findViewById( R.id.layout_bedding )).setVisibility( View.GONE );
    }

    mButtonBack.setOnClickListener( this );

  }

  // @Override
  // public boolean onLongClick(View v) 
  // {
  //   CutNPaste.makePopup( mContext, (EditText)v );
  //   return true;
  // }
    

  @Override
  public void onClick(View v) 
  {
    // CutNPaste.dismissPopup();
    MyKeyboard.close( mKeyboard );

    Button b = (Button) v;
    if ( b == mButtonRenumber ) {
      String from = TopoDroidUtil.noSpaces( mETfrom.getText().toString() );
      // if ( /* from == null || */ from.length() == 0 ) {
      //   mETfrom.setError( mContext.getResources().getString( R.string.error_station_required ) );;
      //   return;
      // }
      String to   = TopoDroidUtil.noSpaces( mETto.getText().toString() );
      // if ( /* to == null || */ to.length() == 0 ) {
      //   mETto.setError( mContext.getResources().getString( R.string.error_station_required ) );;
      //   return;
      // }
      //
      // FROM and TO can be empty, but this means no renumbering is made (anly station assignment to first block)
      mParent.renumberBlocks( mBlks, from, to );
    // } else if ( b == mButtonHighlight ) { FIXME_HIGHLIGHT
    //   mParent.highlightBlocks( mBlks );
    } else if ( b == mButtonColor ) {
      hide();
      (new MyColorPicker( mContext, this, 0 )).show();
      return;
    } else if ( b == mButtonSplays ) {
      int leg_type = LegType.NORMAL;
      if ( ((RadioButton)findViewById( R.id.rb_xsplay )).isChecked() ) {
        leg_type = LegType.XSPLAY;
      } else if ( ((RadioButton)findViewById( R.id.rb_hsplay )).isChecked() ) {
        leg_type = LegType.HSPLAY;
      } else if ( ((RadioButton)findViewById( R.id.rb_vsplay )).isChecked() ) {
        leg_type = LegType.VSPLAY;
      }
      mParent.updateSplaysLegType( mBlks, leg_type );
    } else if ( b == mButtonBedding ) {
      mTVstrikeDip.setText( mParent.computeBedding( mBlks ) );
      return;
    // } else {
    }
    dismiss();
  }

  @Override
  public void onBackPressed()
  {
    // if ( CutNPaste.dismissPopup() ) return;
    if ( MyKeyboard.close( mKeyboard ) ) return;
    dismiss();
  }

  @Override
  public void colorChanged( int color )
  {
    mParent.colorBlocks( mBlks, color );
    dismiss();
  }

}

