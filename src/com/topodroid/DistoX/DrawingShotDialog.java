/* @file DrawingShotDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: dialog for a survey shot: editing comment, extend and flag
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.graphics.*;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import android.text.InputType;
import android.inputmethodservice.KeyboardView;

public class DrawingShotDialog extends MyDialog 
                               implements View.OnClickListener
                               , View.OnLongClickListener
{
  private TextView mLabel;
  private Button mBtnOK;
  private Button mBtnCancel;
  private EditText mETfrom;
  private EditText mETto;
  private EditText mETcomment;

  private CheckBox mRBleft;
  private CheckBox mRBvert;
  private CheckBox mRBright;
  // private RadioButton mRBignore;

  // private RadioButton mRBsurvey;
  private MyCheckBox mRBdup;
  private MyCheckBox mRBsurf;
  // private CheckBox mRBbackshot;
  private Button mRBwalls;

  private DrawingWindow mParent;
  private DBlock mBlock;

  MyKeyboard mKeyboard = null;

  public DrawingShotDialog( Context context, DrawingWindow parent, DrawingPath shot )
  {
    super(context, R.string.DrawingShotDialog );
    mParent  = parent;
    mBlock   = shot.mBlock;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.drawing_shot_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mLabel     = (TextView) findViewById(R.id.shot_label);
    mETfrom    = (EditText) findViewById(R.id.shot_from );
    mETto      = (EditText) findViewById(R.id.shot_to );
    mETcomment = (EditText) findViewById(R.id.shot_comment );

    mETfrom.setOnLongClickListener( this );
    mETto.setOnLongClickListener( this );

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ),
                                R.xml.my_keyboard_base, R.xml.my_keyboard_qwerty );

    mBtnOK     = (Button) findViewById(R.id.btn_ok);
    mBtnCancel = (Button) findViewById(R.id.btn_cancel);

    mRBleft    = (CheckBox) findViewById( R.id.left );
    mRBvert    = (CheckBox) findViewById( R.id.vert );
    mRBright   = (CheckBox) findViewById( R.id.right );
    // mRBignore  = (RadioButton) findViewById( R.id.ignore );

    // mRBsurvey    = (RadioButton) findViewById( R.id.survey );
    // mRBdup  = (CheckBox) findViewById( R.id.duplicate );
    // mRBsurf = (CheckBox) findViewById( R.id.surface );
    // mRBbackshot  = (CheckBox) findViewById( R.id.backshot );

    LinearLayout layout3 = (LinearLayout) findViewById( R.id.layout3 );
    int size = TopoDroidApp.getScaledSize( mContext );
    layout3.setMinimumHeight( size + 20 );

    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( 
      LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
    lp.setMargins( 0, 10, 20, 10 );

    mRBdup      = new MyCheckBox( mContext, size, R.drawable.iz_dup_ok, R.drawable.iz_dup_no );
    mRBsurf     = new MyCheckBox( mContext, size, R.drawable.iz_surface_ok, R.drawable.iz_surface_no );
    layout3.addView( mRBdup, lp );
    layout3.addView( mRBsurf, lp );

    mRBwalls  = (Button) findViewById( R.id.walls );

    // if ( ! TopoDroidApp.mLoopClosure ) {
    //   mRBignore.setClickable( false );
    //   mRBignore.setTextColor( TDColor.MID_GRAY );
    // }

    mLabel.setText( mBlock.dataString( mContext.getResources().getString(R.string.shot_data) ) );

    mRBleft.setOnClickListener( this );
    mRBvert.setOnClickListener( this );
    mRBright.setOnClickListener( this );

    mRBdup.setOnClickListener( this );
    mRBsurf.setOnClickListener( this );
    // mRBbackshot.setOnClickListener( this );
    if ( TDSetting.mWallsType != TDSetting.WALLS_NONE 
      && TDSetting.mLevelOverAdvanced 
      && mBlock.mType == DBlock.BLOCK_MAIN_LEG
      && ( PlotInfo.isSketch2D( mParent.getPlotType() ) ) ) {
      mRBwalls.setOnClickListener( this );
    } else {
      mRBwalls.setVisibility( View.GONE );
    }

    mBtnOK.setOnClickListener( this );
    mBtnCancel.setOnClickListener( this );

    if ( mBlock != null ) {
      mETfrom.setText( mBlock.mFrom );
      mETto.setText( mBlock.mTo );
      mETcomment.setText( mBlock.mComment );

      switch ( (int)mBlock.mExtend ) {
        case DBlock.EXTEND_LEFT:
          mRBleft.setChecked( true );
          break;
        case DBlock.EXTEND_VERT:
          mRBvert.setChecked( true );
          break;
        case DBlock.EXTEND_RIGHT:
          mRBright.setChecked( true );
          break;
        // case DBlock.EXTEND_IGNORE:
        //   mRBignore.setChecked( true );
        //   break;
      }
      switch ( (int)mBlock.mFlag ) {
        // case DBlock.BLOCK_SURVEY:
        //   mRBsurvey.setChecked( true );
        //   break;
        case DBlock.BLOCK_DUPLICATE:
          mRBdup.setChecked( true );
          break;
        case DBlock.BLOCK_SURFACE:
          mRBsurf.setChecked( true );
          break;
        // case DBlock.BLOCK_BACKSHOT:
        //   mRBbackshot.setChecked( true );
        //   break;
      }
    }
    setTitle( String.format( mContext.getResources().getString( R.string.shot_title ), mBlock.mFrom, mBlock.mTo ) );

    if ( TDSetting.mKeyboard ) {
      int flag = MyKeyboard.FLAG_POINT_LCASE_2ND;
      if ( TDSetting.mStationNames == 1 ) flag = MyKeyboard.FLAG_POINT;
      MyKeyboard.registerEditText( mKeyboard, mETfrom, flag );
      MyKeyboard.registerEditText( mKeyboard, mETto,   flag );
      mKeyboard.hide();
    } else {
      mKeyboard.hide();
      if ( TDSetting.mStationNames == 1 ) {
        mETfrom.setInputType( TDConst.NUMBER_DECIMAL );
        mETto.setInputType( TDConst.NUMBER_DECIMAL );
      }
    }

  }

  @Override
  public boolean onLongClick(View view)
  {
    CutNPaste.makePopup( mContext, (EditText)view );
    return true;
  }

  @Override
  public void onClick(View view)
  {
    // TDLog.Log( TDLog.LOG_INPUT, "DrawingShotDialog onClick() " + view.toString() );
    CutNPaste.dismissPopup();

    Button b = (Button)view;

    if ( b == mRBleft ) {
      mRBvert.setChecked( false );
      mRBright.setChecked( false );
    } else if ( b == mRBvert ) {
      mRBleft.setChecked( false );
      mRBright.setChecked( false );
    } else if ( b == mRBright ) {
      mRBleft.setChecked( false );
      mRBvert.setChecked( false );

    } else if ( b == mRBdup ) {
      mRBdup.toggleState();
      if ( mRBdup.isChecked() ) {
        mRBsurf.setState( false );
      }
    } else if ( b == mRBsurf ) {
      mRBsurf.toggleState();
      if ( mRBsurf.isChecked() ) {
        mRBdup.setState( false );
      }
    // } else if ( b == mRBbackshot ) {
    //   mRBdup.setChecked( false );
    //   mRBsurf.setChecked( false );

    } else if ( b == mRBwalls ) {
      mParent.drawWallsAt( mBlock );
      dismiss();

    } else if ( b == mBtnCancel ) {
      dismiss();
    } else if ( b == mBtnOK ) {
      long extend = mBlock.mExtend;
      long flag   = mBlock.mFlag;

      if ( mRBleft.isChecked() ) { extend = DBlock.EXTEND_LEFT; }
      else if ( mRBvert.isChecked() ) { extend = DBlock.EXTEND_VERT; }
      else if ( mRBright.isChecked() ) { extend = DBlock.EXTEND_RIGHT; }
      else /* if ( mRBignore.isChecked() ) */ { extend = DBlock.EXTEND_IGNORE; }

      if ( mRBdup.isChecked() ) { flag = DBlock.BLOCK_DUPLICATE; }
      else if ( mRBsurf.isChecked() ) { flag = DBlock.BLOCK_SURFACE; }
      // else if ( mRBbackshot.isChecked() ) { flag = DBlock.BLOCK_BACKSHOT; }
      else /* if ( mRBsurvey.isChecked() ) */ { flag = DBlock.BLOCK_SURVEY; }

      mParent.updateBlockExtend( mBlock, extend ); // equal extend checked by the method
      mParent.updateBlockFlag( mBlock,flag ); // equal flag is checked by the method

      String from = mETfrom.getText().toString().trim();
      String to   = mETto.getText().toString().trim();
      String comment = mETcomment.getText().toString().trim();

      if ( ! from.equals( mBlock.mFrom ) || ! to.equals( mBlock.mTo ) ) { // FIXME revert equals
        mParent.updateBlockName( mBlock, from, to );
      }

      mParent.updateBlockComment( mBlock, comment ); // equal comment checked by the method

      // } else if (view.getId() == R.id.button_cancel ) {
      //   /* nothing */
 
      dismiss();
    }
  }

  @Override
  public void onBackPressed()
  {
    if ( CutNPaste.dismissPopup() ) return;
    if ( TDSetting.mKeyboard ) {
      if ( mKeyboard.isVisible() ) {
        mKeyboard.hide();
        return;
      }
    }
    dismiss();
  }

}
        


