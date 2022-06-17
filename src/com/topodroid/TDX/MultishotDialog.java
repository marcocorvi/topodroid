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
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.ui.MyKeyboard;
import com.topodroid.ui.MyColorPicker;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.LegType;

import java.util.regex.Pattern;
import java.util.List;

import android.os.Bundle;
import android.text.InputType;

import android.content.Context;
import android.inputmethodservice.KeyboardView;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.CheckBox;

import android.view.View;

class MultishotDialog extends MyDialog
                      implements View.OnClickListener
			       , MyColorPicker.IColorChanged
{
  private final ShotWindow mParent;
  private List< DBlock > mBlks;
  private DBlock mBlk = null;

  private boolean mColoring = false;
  private int mBedding  = 0;

  private Pattern mPattern; // name pattern

  // private TextView mTVdata;

  private EditText mETfrom;
  private EditText mETto;
  private TextView mTVstrikeDip;
  
  private Button   mButtonRenumber;
  private Button   mButtonSwap;
  // private Button   mButtonHighlight; FIXME_HIGHLIGHT
  private Button   mButtonColor;
  private Button   mButtonSplays;
  private Button   mButtonBedding;
  private Button   mButtonBack;

  private MyKeyboard mKeyboard = null;

  MultishotDialog( Context context, ShotWindow parent, List< DBlock > blks )
  {
    super( context, null, R.string.MultishotDialog ); // null app
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
    ((CheckBox)findViewById( R.id.rb_noplan )).setChecked( true );
    ((CheckBox)findViewById( R.id.rb_noprofile )).setChecked( true );

    mButtonRenumber  = (Button) findViewById(R.id.renumber );
    mButtonSwap      = (Button) findViewById(R.id.swap );
    // mButtonHighlight = (Button) findViewById(R.id.highlight ); FIXME_HIGHLIGHT
    mButtonColor     = (Button) findViewById(R.id.color );
    mButtonSplays    = (Button) findViewById(R.id.splays );
    mButtonBedding   = (Button) findViewById(R.id.bedding );
    mButtonBack      = (Button) findViewById(R.id.btn_back );

    // boolean renumber = true;
    // mBlk = mBlks.get(0);
    // if ( mBlk != null && mBlk.isLeg() ) {
      mButtonRenumber.setOnClickListener( this );
      mButtonSwap.setOnClickListener( this );
    // } else {
    //   renumber = false;
    //   ((LinearLayout) findViewById( R.id.layout_renumber )).setVisibility( View.GONE );
    // }

    mColoring = false;
    if ( TDLevel.overExpert && TDSetting.mSplayColor ) {
      mColoring = true;
      for ( DBlock blk : mBlks ) {
        if ( blk.mTo != null && blk.mTo.length() > 0 ) {
          mColoring = false;
          break;
        }
      }
    }
    if ( mColoring ) {
      // mButtonHighlight.setOnClickListener( this ); FIXME_HIGHLIGHT
      mButtonColor.setOnClickListener( this );
      mButtonSplays.setOnClickListener( this );
    } else {
      // ((LinearLayout) findViewById( R.id.layout_highlight )).setVisibility( View.GONE ); FIXME_HIGHLIGHT
      ((LinearLayout) findViewById( R.id.layout_color )).setVisibility( View.GONE );
      ((LinearLayout) findViewById( R.id.layout_splays )).setVisibility( View.GONE );
    }

    mBedding = 0;
    if ( TDLevel.overExpert && TDSetting.mBedding ) {
      String from = mBlk.mFrom;
      if ( mBlks.size() > 1 && from != null && from.length() > 0 ) {
        mBedding = 1;
        for ( DBlock blk : mBlks ) {
          if ( ! from.equals( blk.mFrom ) ) {
            mBedding = 2;
            break;
          }
        }
      }
    }
    if ( mBedding > 0 ) {
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
      String from = TDUtil.noSpaces( mETfrom.getText().toString() );
      // if ( /* from == null || */ from.length() == 0 ) {
      //   mETfrom.setError( mContext.getResources().getString( R.string.error_station_required ) );;
      //   return;
      // }
      String to   = TDUtil.noSpaces( mETto.getText().toString() );
      // if ( /* to == null || */ to.length() == 0 ) {
      //   mETto.setError( mContext.getResources().getString( R.string.error_station_required ) );;
      //   return;
      // }
      //
      if ( from != null ) {
        // FROM and TO can be empty, but this means no renumbering is made (only station assignment to first block)
        mParent.renumberBlocks(mBlks, from, to);
      }
    // } else if ( b == mButtonHighlight ) { FIXME_HIGHLIGHT
    //   mParent.highlightBlocks( mBlks );
    } else if ( b == mButtonSwap ) {
      mParent.swapBlocksName( mBlks );
    } else if ( mColoring ) {
      if ( b == mButtonColor ) {
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
        long flag = DBlock.FLAG_SURVEY;
        if ( ! ((CheckBox)findViewById( R.id.rb_noplan )).isChecked() ) {
          flag |= DBlock.FLAG_NO_PLAN;
        } 
        if ( ! ((CheckBox)findViewById( R.id.rb_noprofile )).isChecked() ) {
          flag |= DBlock.FLAG_NO_PROFILE;
        }
        mParent.updateSplaysLegType( mBlks, leg_type, flag );
      }
    } else if ( mBedding > 0 && b == mButtonBedding ) {
      // if ( mBedding > 1 ) { // warning toast
      //   TDToast.makeWarn( R.string.multistation_plane );
      // }
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

