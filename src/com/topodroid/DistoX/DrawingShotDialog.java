/* @file DrawingShotDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: dialog for a survey shot: editing comment, extend and flag
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.os.Bundle;
// import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;

// import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import android.view.View;
// import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
// import android.widget.RadioButton;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

// import android.text.InputType;
import android.inputmethodservice.KeyboardView;

// import android.util.Log;

class DrawingShotDialog extends MyDialog
                               implements View.OnClickListener
                               , View.OnLongClickListener
			       , MyColorPicker.IColorChanged
{
  // private TextView mLabel;
  private Button mBtnOK;
  private Button mBtnCancel;
  private Button mBtnColor;  // user-set color (tester level)
  private EditText mETfrom;
  private EditText mETto;
  private EditText mETcomment;

  private CheckBox mRBleft;
  private CheckBox mRBvert;
  private CheckBox mRBright;
  // private RadioButton mRBignore;
  private SeekBar mStretchBar;

  // private RadioButton mRBsurvey;
  private MyCheckBox mRBdup  = null;
  private MyCheckBox mRBsurf = null;
  private MyCheckBox mRBcmtd = null;
  private MyCheckBox mCBxSplay = null;

  private CheckBox mCBfrom   = null;
  private CheckBox mCBto     = null;
  // private CheckBox mRBbackshot;
  private Button mRBwalls;

  private boolean mSplayColor;
  private boolean mFracExtend;

  private final DrawingWindow mParent;
  private DBlock mBlock;
  private int mColor;    // bock color
  private DrawingPath mPath;
  private int mFlag; // can barrier/hidden FROM and TO
  private int mIntExtend;
  private float mStretch; // FIXME_STRETCH use a slider

  // 0x01 can barrier FROM
  // 0x02 can hidden  FROM
  // 0x04 can barrier TO
  // 0x08 can hidden  TO

  private MyKeyboard mKeyboard = null;

  DrawingShotDialog( Context context, DrawingWindow parent, DrawingPath shot, int flag )
  {
    super(context, R.string.DrawingShotDialog );
    mParent  = parent;
    mBlock   = shot.mBlock;
    mColor   = mBlock.getPaintColor();
    mPath    = shot;
    mFlag    = flag;
    mIntExtend = mBlock.getReducedIntExtend();
    mStretch = mBlock.getStretch();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.drawing_shot_dialog,
      String.format( mContext.getResources().getString( R.string.shot_title ), mBlock.mFrom, mBlock.mTo ) );

    TextView mLabel     = (TextView) findViewById(R.id.shot_label);
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
    mStretchBar = (SeekBar) findViewById(R.id.stretchbar );

    // mRBsurvey    = (RadioButton) findViewById( R.id.survey );
    // mRBdup  = (CheckBox) findViewById( R.id.duplicate );
    // mRBsurf = (CheckBox) findViewById( R.id.surface );
    // mRBbackshot  = (CheckBox) findViewById( R.id.backshot );

    mSplayColor = TDSetting.mSplayClasses;
    mFracExtend = TDSetting.mExtendFrac;

    mBtnColor = (Button) findViewById( R.id.btn_color );
    if ( TDLevel.overExpert ) {
      if ( mBlock.isSplay() ) {
	if ( mSplayColor ) {
          mBtnColor.setBackgroundColor( mColor ); 
          mBtnColor.setOnClickListener( this );
	}
      } else if ( mParent.isExtendedProfile() && mBlock.isMainLeg() ) {
	if ( mFracExtend ) {
	  Bitmap bitmap =  MyButton.getLVRseekbarBackGround( mContext, (int)(TopoDroidApp.mDisplayWidth), (int)(20) );
	  if ( bitmap != null ) {
	    BitmapDrawable background = new BitmapDrawable( mContext.getResources(), bitmap );
            mStretchBar.setBackground( background ); 
	  }
          mStretchBar.setProgress( (int)(150 + 100 * mIntExtend + 100 * mStretch ) );
          mStretchBar.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
              public void onProgressChanged( SeekBar stretchbar, int progress, boolean fromUser) {
                if ( fromUser ) {
	  	if ( progress < 100 )      { 
                    mIntExtend = -1;
	  	  mRBleft.setChecked(  true );
	  	  mRBvert.setChecked(  false );
	  	  mRBright.setChecked( false );
	  	  mStretch = (progress- 50)/100.0f; 
	  	} else if ( progress > 200 ) {
                    mIntExtend = 1;
	  	  mRBleft.setChecked(  false );
	  	  mRBvert.setChecked(  false );
	  	  mRBright.setChecked( true );
	  	  mStretch = (progress-250)/100.0f;
	          } else { 
                    mIntExtend = 0;
	  	  mRBleft.setChecked(  false );
	  	  mRBvert.setChecked(  true );
	  	  mRBright.setChecked( false );
	  	  mStretch = (progress-150)/100.0f;
	         	}
                  // mStretch = (progress - 100)/200.0f;
                  if ( mStretch < -0.5f ) mStretch = -0.5f;
                  if ( mStretch >  0.5f ) mStretch =  0.5f;
                }
              }
              public void onStartTrackingTouch(SeekBar stretchbar) { }
              public void onStopTrackingTouch(SeekBar stretchbar) { }
          } );
          mStretchBar.setEnabled( true );
	}
      }
    }

    if ( ! mSplayColor ) mBtnColor.setVisibility( View.GONE );
    if ( ! mFracExtend ) mStretchBar.setVisibility( View.GONE );

    LinearLayout layout3  = (LinearLayout) findViewById( R.id.layout3 );
    LinearLayout layout3b = (LinearLayout) findViewById( R.id.layout3b );
    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    layout3.setMinimumHeight( size + 20 );
    layout3b.setMinimumHeight( size + 20 );

    LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 0, 10, 20, 10 );

    if ( TDLevel.overNormal ) {
      mRBdup      = new MyCheckBox( mContext, size, R.drawable.iz_dup_ok, R.drawable.iz_dup_no );
      mRBsurf     = new MyCheckBox( mContext, size, R.drawable.iz_surface_ok, R.drawable.iz_surface_no );
      mRBcmtd     = new MyCheckBox( mContext, size, R.drawable.iz_comment_ok, R.drawable.iz_comment_no );
      layout3.addView( mRBdup,  lp );
      layout3.addView( mRBsurf, lp );
      layout3.addView( mRBcmtd, lp );
      mRBdup.setOnClickListener( this );
      mRBsurf.setOnClickListener( this );
      mRBcmtd.setOnClickListener( this );
      if ( TDLevel.overAdvanced && mBlock.isOtherSplay() ) {
        mCBxSplay = new MyCheckBox( mContext, size, R.drawable.iz_xsplays_ok, R.drawable.iz_ysplays_no );
        mCBxSplay.setChecked( false );
        layout3.addView( mCBxSplay, lp );
        mCBxSplay.setOnClickListener( this );
      }
    } else {
      layout3.setVisibility( View.GONE );
    }

    boolean hide3b = true;
    if ( TDLevel.overAdvanced ) {
      if ( mBlock.isMainLeg() ) {
        if ( ( mFlag & 0x03 ) != 0 ) { // FROM can be barrier/hidden
          mCBfrom = new CheckBox( mContext );
          mCBfrom.setText( mBlock.mFrom );
          mCBfrom.setChecked( false );
          layout3b.addView( mCBfrom, lp );
          hide3b = false;
        }
        if ( ( mFlag & 0x0c ) != 0 ) { // TO can be barrier/hidden
          mCBto   = new CheckBox( mContext );
          mCBto.setText( mBlock.mTo );
          mCBto.setChecked( false );
          layout3b.addView( mCBto, lp );
          hide3b = false;
        }
      }
    }
    if ( hide3b ) layout3b.setVisibility( View.GONE );

    mRBwalls  = (Button) findViewById( R.id.walls );

    // if ( TDSetting.mLoopClosure == TDSetting.LOOP_NONE ) {
    //   mRBignore.setClickable( false );
    //   mRBignore.setTextColor( TDColor.MID_GRAY );
    // }
    if ( TDInstance.datamode == SurveyInfo.DATAMODE_NORMAL ) {
      mLabel.setText( mBlock.dataStringNormal( mContext.getResources().getString(R.string.shot_data) ) );
    } else { // SurveyInfo.DATAMODE_DIVING
      mLabel.setText( mBlock.dataStringDiving( mContext.getResources().getString(R.string.shot_data) ) );
    }

    mRBleft.setOnClickListener( this );
    mRBvert.setOnClickListener( this );
    mRBright.setOnClickListener( this );

    // mRBbackshot.setOnClickListener( this );
    if ( TDSetting.mWallsType != TDSetting.WALLS_NONE 
      && TDLevel.overExpert 
      && mBlock.isMainLeg()
      && ( PlotInfo.isSketch2D( mParent.getPlotType() ) ) ) {
      mRBwalls.setOnClickListener( this );
    } else {
      mRBwalls.setVisibility( View.GONE );
    }

    mBtnOK.setOnClickListener( this );
    mBtnCancel.setOnClickListener( this );

    if ( mBlock != null ) { // block cannot be null 
      mETfrom.setText( mBlock.mFrom );
      mETto.setText( mBlock.mTo );
      if ( mBlock.mComment != null && mBlock.mComment.length() > 0 ) {
        mETcomment.setText( mBlock.mComment );
      } else {
        mETcomment.setHint( R.string.comment );
      }

      switch ( mBlock.getExtend() ) {
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
      // if ( mBlock.isSurvey() ) {
      //   mRBsurvey.setChecked( true );
      if ( TDLevel.overNormal ) {
        if ( mBlock.isDuplicate() ) {
          mRBdup.setChecked( true );
        } else if ( mBlock.isSurface() ) {
          mRBsurf.setChecked( true );
        } else if ( mBlock.isCommented() ) { // FIXME_COMMENTED
          mRBcmtd.setChecked( true );
        // } else if ( mBlock.isBackshot() ) {
        //   mRBbackshot.setChecked( true );
        }
      }
    }
    
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
  public void colorChanged( int color )
  {
    if ( ! mSplayColor ) return;
    mColor = color;
    mParent.updateBlockColor( mBlock, mColor );
    mBtnColor.setBackgroundColor( mColor );
  }

  @Override
  public void onClick(View view)
  {
    // TDLog.Log( TDLog.LOG_INPUT, "DrawingShotDialog onClick() " + view.toString() );
    CutNPaste.dismissPopup();

    Button b = (Button)view;

    if ( mSplayColor && b == mBtnColor ) {
      new MyColorPicker( mContext, this, mColor ).show();
      return;
    } else if ( b == mRBleft ) {
      mRBvert.setChecked( false );
      mRBright.setChecked( false );
      if ( mFracExtend ) {
        mIntExtend = mRBleft.isChecked() ? -1 : 0;
        mStretch = 0;
        mStretchBar.setProgress(  150+100*mIntExtend );
      }
    } else if ( b == mRBvert ) {
      mRBleft.setChecked( false );
      mRBright.setChecked( false );
      if ( mFracExtend ) {
        mIntExtend = 0;
        mStretch = 0;
        mStretchBar.setProgress( 150 );
      }
    } else if ( b == mRBright ) {
      mRBleft.setChecked( false );
      mRBvert.setChecked( false );
      if ( mFracExtend ) {
        mIntExtend = mRBright.isChecked() ? 1 : 0;
        mStretch = 0;
        mStretchBar.setProgress(  150+100*mIntExtend );
      }

    } else if ( TDLevel.overNormal && b == mRBdup ) {
      mRBdup.toggleState();
      if ( mRBdup.isChecked() ) {
        mRBsurf.setState( false );
        mRBcmtd.setState( false );
      }
    } else if ( TDLevel.overNormal && b == mRBsurf ) {
      mRBsurf.toggleState();
      if ( mRBsurf.isChecked() ) {
        mRBdup.setState( false );
        mRBcmtd.setState( false );
      }
    } else if ( TDLevel.overNormal && b == mRBcmtd ) {
      mRBcmtd.toggleState();
      if ( mRBcmtd.isChecked() ) {
        mRBdup.setState( false );
        mRBsurf.setState( false );
      }
    } else if ( TDLevel.overNormal && b == mCBxSplay ) {
      mCBxSplay.toggleState();

    // } else if ( b == mRBbackshot ) {
    //   mRBdup.setChecked( false );
    //   mRBsurf.setChecked( false );

    } else if ( TDLevel.overExpert && b == mRBwalls ) {
      mParent.drawWallsAt( mBlock );
      dismiss();

    } else if ( b == mBtnCancel ) {
      dismiss();
    } else if ( b == mBtnOK ) {
      MyKeyboard.close( mKeyboard );

      if ( TDLevel.overAdvanced ) {
        if ( mCBfrom != null && mCBfrom.isChecked() ) {
          if ( ( mFlag & 0x01 ) == 0x01 ) { // can barrier FROM
            mParent.toggleStationBarrier( mBlock.mFrom, false );
          } else if ( ( mFlag & 0x02 ) == 0x02 ) { // can hidden FROM
            mParent.toggleStationHidden( mBlock.mFrom, false );
          }
        }
        if ( mCBto != null && mCBto.isChecked() ) {
          if ( ( mFlag & 0x04 ) == 0x04 ) { // can barrier TO
            mParent.toggleStationBarrier( mBlock.mTo, false );
          } else if ( ( mFlag & 0x08 ) == 0x08 ) { // can hidden TO
            mParent.toggleStationHidden( mBlock.mTo, false );
          }
        }
      }

      // int extend = mBlock.getExtend();
      int extend = DBlock.EXTEND_IGNORE;
      if ( mRBleft.isChecked() )       { extend = DBlock.EXTEND_LEFT; }
      else if ( mRBvert.isChecked() )  { extend = DBlock.EXTEND_VERT; }
      else if ( mRBright.isChecked() ) { extend = DBlock.EXTEND_RIGHT; }
      // Log.v("DistoX", "Extend " + extend + " Stretch " + mStretch );
      mParent.updateBlockExtend( mBlock, extend, mStretch ); // FIXME_STRETCH equal extend checked by the method

      if ( TDLevel.overNormal ) {
        long flag  = mBlock.getFlag();
        if ( mRBdup.isChecked() )       { flag = DBlock.FLAG_DUPLICATE; }
        else if ( mRBsurf.isChecked() ) { flag = DBlock.FLAG_SURFACE; }
        else if ( mRBcmtd.isChecked() ) { flag = DBlock.FLAG_COMMENTED; }
        // else if ( mRBbackshot.isChecked() ) { flag = DBlock.FLAG_BACKSHOT; }
        else /* if ( mRBsurvey.isChecked() ) */ { flag = DBlock.FLAG_SURVEY; }
        mParent.updateBlockFlag( mBlock, flag, mPath ); // equal flag is checked by the method
      }

      if ( TDLevel.overAdvanced && mCBxSplay != null ) {
	if ( mCBxSplay.isChecked() ) {
          mParent.clearBlockSplayLeg( mBlock, mPath );
	}
      }

      String from = mETfrom.getText().toString().trim();
      String to   = mETto.getText().toString().trim();

      if ( ! from.equals( mBlock.mFrom ) || ! to.equals( mBlock.mTo ) ) { // FIXME revert equals
        mParent.updateBlockName( mBlock, from, to );
      }

      if ( mETcomment.getText() != null ) {
        String comment = mETcomment.getText().toString().trim();
        mParent.updateBlockComment( mBlock, comment ); // equal comment checked by the method
      }

      // } else if (view.getId() == R.id.button_cancel ) {
      //   /* nothing */
 
      dismiss();
    }
  }

  @Override
  public void onBackPressed()
  {
    if ( CutNPaste.dismissPopup() ) return;
    if ( MyKeyboard.close( mKeyboard ) ) return;
    dismiss();
  }

}
        


