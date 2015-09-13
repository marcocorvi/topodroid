/* @file ShotDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey shot dialog to enter FROM-TO stations etc.
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.regex.Pattern;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.RadioButton;

import android.text.method.KeyListener;
import android.text.InputType;

// import android.widget.Spinner;
// import android.widget.ArrayAdapter;


import android.content.Context;
import android.content.DialogInterface;
import android.inputmethodservice.KeyboardView;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;

import android.view.View.OnKeyListener;
import android.view.KeyEvent;

import android.util.Log;

public class ShotDialog extends Dialog
                        implements View.OnClickListener
{
  private Context mContext;
  private ShotActivity mParent;
  private DistoXDBlock mBlk;
  private DistoXDBlock mPrevBlk;
  private DistoXDBlock mNextBlk;
  private int mPos; // item position in the parent' list

  private Pattern mPattern; // name pattern

  // private TextView mTVdata;
  private EditText mETdistance;
  private EditText mETbearing;
  private EditText mETclino;

  private TextView mTVextra;

  private EditText mETfrom;
  private EditText mETto;
  private EditText mETcomment;
  
  private MyCheckBox mRBdup;
  private MyCheckBox mRBsurf;
  private MyCheckBox mCBleg;
  private MyCheckBox mCBall_splay;
  private MyCheckBox mCBrenumber;

  private Button mButtonReverse;

  private CheckBox mRBleft;
  private CheckBox mRBvert;
  private CheckBox mRBright;
  // private CheckBox mRBignore;

  // private Spinner mFlag;
  // private Spinner mExtend;
  // ArrayAdapter< CharSequence > mExtendAdapter;
  // ArrayAdapter< CharSequence > mFlagAdapter;

  // private Button   mButtonDrop;
  private Button   mButtonOK;
  private Button   mButtonSave;
  // private Button   mButtonBack;
  private Button   mButtonPrev;
  private Button   mButtonNext;

  String shot_from;
  String shot_to;
  boolean shot_leg;
  // String shot_data;
  String shot_distance;
  String shot_bearing;
  String shot_clino;
  boolean shot_manual;

  String shot_extra;
  long shot_extend;
  long shot_flag;
  String shot_comment;

  MyKeyboard mKeyboard = null;

  private KeyListener mKLdistance;
  private KeyListener mKLbearing;
  private KeyListener mKLclino;

  private static int flagDistance = MyKeyboard.FLAG_POINT;
  private static int flagBearing  = MyKeyboard.FLAG_POINT;
  private static int flagClino    = MyKeyboard.FLAG_POINT | MyKeyboard.FLAG_SIGN;
  public ShotDialog( Context context, ShotActivity parent, int pos,
                     DistoXDBlock blk, DistoXDBlock prev, DistoXDBlock next )
  {
    super(context);
    mContext = context;
    mParent = parent;
    mPos = pos;
    loadDBlock( blk, prev, next );
    TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "Shot Dialog " + blk.toString(true) );
  }


  private void loadDBlock( DistoXDBlock blk, DistoXDBlock prev, DistoXDBlock next )
  {
    mPrevBlk     = prev;
    mNextBlk     = next;
    mBlk         = blk;
    TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "Shot Dialog LOAD " + blk.toString(true) );
    if ( prev != null ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "           prev " + prev.toString(true) );
    }
    if ( next != null ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "           next " + next.toString(true) );
    }

    shot_from    = blk.mFrom;
    shot_to      = blk.mTo;
    if ( (blk.mType == DistoXDBlock.BLOCK_BLANK || blk.mType == DistoXDBlock.BLOCK_BLANK_LEG) 
         && prev != null && prev.type() == DistoXDBlock.BLOCK_MAIN_LEG ) {
      if ( DistoXStationName.isLessOrEqual( prev.mFrom, prev.mTo ) ) {
        shot_from = prev.mTo;
        shot_to   = DistoXStationName.increment( prev.mTo );
      } else {
        shot_to = prev.mFrom;
        shot_from = DistoXStationName.increment( prev.mFrom );
      }
    }
    
    // shot_data    = blk.dataString();
    shot_distance = blk.distanceString();
    shot_bearing  = blk.bearingString();
    shot_clino    = blk.clinoString();
    shot_manual   = (blk.mShotType > 0);

    // Log.v("DistoX", "shot is manual " + shot_manual + " length " + shot_distance );

    shot_extra   = blk.extraString();
    shot_extend  = blk.mExtend;
    shot_flag    = blk.mFlag;
    shot_leg     = blk.mType == DistoXDBlock.BLOCK_SEC_LEG;
    shot_comment = blk.mComment;
  }

  private void setEditable( EditText et, KeyListener kl, boolean editable, int flag )
  {
    if ( TopoDroidSetting.mKeyboard ) {
      et.setKeyListener( null );
      et.setClickable( true );
      et.setFocusable( editable );
      if ( editable ) {
        MyKeyboard.registerEditText( mKeyboard, et, flag );
        // et.setKeyListener( mKeyboard );
        et.setBackgroundResource( android.R.drawable.edit_text );
      } else {
        MyKeyboard.registerEditText( mKeyboard, et, flag | MyKeyboard.FLAG_NOEDIT );
        et.setBackgroundColor( 0xff999999 );
      }
    } else {
      if ( editable ) {
        et.setKeyListener( kl );
        et.setBackgroundResource( android.R.drawable.edit_text );
        et.setClickable( true );
        et.setFocusable( true );
      } else {
        // et.setFocusable( false );
        // et.setClickable( false );
        et.setKeyListener( null );
        et.setBackgroundColor( 0xff999999 );
      }
    }
  }

  private void updateView()
  {
    // mTVdata.setText( shot_data );
    mETdistance.setText( shot_distance );
    mETbearing.setText( shot_bearing );
    mETclino.setText( shot_clino );

    mTVextra.setText( shot_extra );
    if ( shot_from.length() > 0 ) {
      mETfrom.setText( shot_from );
    }
    if ( shot_to.length() > 0 ) {
      mETto.setText( shot_to );
    }
    if ( shot_comment != null ) {
      mETcomment.setText( shot_comment );
    } else {
      mETcomment.setText( "" );
    }
   
    // if ( shot_flag == DistoXDBlock.BLOCK_SURVEY ) { mRBreg.setChecked( true ); }
    if ( shot_flag == DistoXDBlock.BLOCK_DUPLICATE ) { mRBdup.setChecked( true ); }
    else if ( shot_flag == DistoXDBlock.BLOCK_SURFACE ) { mRBsurf.setChecked( true ); }
    // else if ( shot_flag == DistoXDBlock.BLOCK_BACKSHOT ) { mRBback.setChecked( true ); }

    mCBleg.setChecked( shot_leg );

    mRBleft.setChecked( false );
    mRBvert.setChecked( false );
    mRBright.setChecked( false );
    // mRBignore.setChecked( false );
    if ( shot_extend == DistoXDBlock.EXTEND_LEFT ) { mRBleft.setChecked( true ); }
    else if ( shot_extend == DistoXDBlock.EXTEND_VERT ) { mRBvert.setChecked( true ); }
    else if ( shot_extend == DistoXDBlock.EXTEND_RIGHT ) { mRBright.setChecked( true ); }
    // else if ( shot_extend == DistoXDBlock.EXTEND_IGNORE ) { mRBignore.setChecked( true ); }

    // Spinner
    // switch ( shot_extend ) {
    //   case DistoXDBlock.EXTEND_LEFT: break;
    //   case DistoXDBlock.EXTEND_VERT: break;
    //   case DistoXDBlock.EXTEND_RIGHT: break;
    //   case DistoXDBlock.EXTEND_IGNORE: break;
    // }

    mButtonNext.setEnabled( mNextBlk != null );
    mButtonPrev.setEnabled( mPrevBlk != null );

    // do at the very end
    setEditable( mETdistance, mKLdistance, shot_manual, flagDistance );
    setEditable( mETbearing,  mKLbearing,  shot_manual, flagBearing );
    setEditable( mETclino,    mKLclino,    shot_manual, flagClino );
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
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    // getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );

    // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "Shot Dialog::onCreate" );
    setContentView(R.layout.shot_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );


    // mTVdata    = (TextView) findViewById(R.id.shot_data );
    mETdistance = (EditText) findViewById(R.id.shot_distance);
    mETbearing  = (EditText) findViewById(R.id.shot_bearing);
    mETclino    = (EditText) findViewById(R.id.shot_clino);

    mKLdistance = mETdistance.getKeyListener();
    mKLbearing  = mETbearing .getKeyListener();
    mKLclino    = mETclino   .getKeyListener();

    mTVextra   = (TextView) findViewById(R.id.shot_extra );
    // mETname = (EditText) findViewById(R.id.shot_name );
    mETfrom    = (EditText) findViewById(R.id.shot_from );
    mETto      = (EditText) findViewById(R.id.shot_to );
    mETcomment = (EditText) findViewById(R.id.shot_comment );

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ),
                                R.xml.my_keyboard_base_sign, R.xml.my_keyboard_qwerty );
    if ( TopoDroidSetting.mKeyboard ) {
      int flag = MyKeyboard.FLAG_POINT_LCASE_2ND;
      if ( TopoDroidSetting.mStationNames == 1 ) flag = MyKeyboard.FLAG_POINT;
      MyKeyboard.registerEditText( mKeyboard, mETfrom, flag );
      MyKeyboard.registerEditText( mKeyboard, mETto,   flag );
      // mKeyboard.hide();
    } else {
      mKeyboard.hide();
      if ( TopoDroidSetting.mStationNames == 1 ) {
        mETfrom.setInputType( InputType.TYPE_CLASS_NUMBER );
        mETto.setInputType( InputType.TYPE_CLASS_NUMBER );
      }
    }
    
    // // mRBreg  = (CheckBox) findViewById( R.id.shot_reg );
    // mRBdup  = (CheckBox) findViewById( R.id.shot_dup );
    // mRBsurf = (CheckBox) findViewById( R.id.shot_surf );
    // // mRBback = (CheckBox) findViewById( R.id.shot_back );
    // mCBleg = (CheckBox)  findViewById(R.id.shot_leg );
    // mCBall_splay = (CheckBox)  findViewById(R.id.shot_all_splay );
    // mCBrenumber  = (CheckBox)  findViewById(R.id.shot_renumber  );

    LinearLayout layout4 = (LinearLayout) findViewById( R.id.layout4 );
    int size = TopoDroidApp.getScaledSize( mContext );
    layout4.setMinimumHeight( size + 10 );

    mRBdup       = new MyCheckBox( mContext, size, R.drawable.iz_dup_ok, R.drawable.iz_dup_no );
    mRBsurf      = new MyCheckBox( mContext, size, R.drawable.iz_surface_ok, R.drawable.iz_surface_no );
    mCBleg       = new MyCheckBox( mContext, size, R.drawable.iz_leg2_ok, R.drawable.iz_leg2_no );
    mCBall_splay = new MyCheckBox( mContext, size, R.drawable.iz_splays_ok, R.drawable.iz_splays_no );
    mCBrenumber  = new MyCheckBox( mContext, size, R.drawable.iz_numbers_ok, R.drawable.iz_numbers_no );

    layout4.addView( mRBdup );
    layout4.addView( mRBsurf );
    layout4.addView( mCBleg );
    layout4.addView( mCBall_splay );
    layout4.addView( mCBrenumber );

    mCBleg.setOnClickListener( this );
    mCBall_splay.setOnClickListener( this );

    layout4.invalidate();

    mButtonReverse = (Button)  findViewById(R.id.shot_reverse );

    mRBleft   = (CheckBox) findViewById(R.id.left );
    mRBvert   = (CheckBox) findViewById(R.id.vert );
    mRBright  = (CheckBox) findViewById(R.id.right );
    // mRBignore = (CheckBox) findViewById(R.id.ignore );

    // if ( ! TopoDroidApp.mLoopClosure ) {
    //   mRBignore.setClickable( false );
    //   mRBignore.setTextColor( 0xff999999 );
    // }

    // mButtonDrop = (Button) findViewById(R.id.btn_drop );
    mButtonSave = (Button) findViewById(R.id.btn_save );
    mButtonOK   = (Button) findViewById(R.id.btn_ok );
    // mButtonBack = (Button) findViewById(R.id.btn_back );

    mButtonPrev = (Button) findViewById(R.id.btn_prev );
    mButtonNext = (Button) findViewById(R.id.btn_next );

    // mETfrom.setRawInputType( InputType.TYPE_CLASS_NUMBER );
    // mETfrom.setKeyListener( NumberKeyListener );
    // mETto.setRawInputType( InputType.TYPE_CLASS_NUMBER );

    // mButtonDrop.setOnClickListener( this );
    mButtonSave.setOnClickListener( this );
    mButtonOK.setOnClickListener( this );
    // mButtonBack.setOnClickListener( this );

    // mRBdup.setOnClickListener( this );
    // mRBsurf.setOnClickListener( this );

    mButtonPrev.setOnClickListener( this );
    mButtonNext.setOnClickListener( this );
    mButtonReverse.setOnClickListener( this );

    mRBleft.setOnClickListener( this );
    mRBvert.setOnClickListener( this );
    mRBright.setOnClickListener( this );
    // mRBignore.setOnClickListener( this );

    updateView();

  }

  private void saveDBlock()
  {
    boolean all_splay = mCBall_splay.isChecked();
    if ( mCBleg.isChecked() ) {
      shot_from = "";
      shot_to = "";
      shot_leg = true;
      all_splay = false;
    } else {
      shot_from = mETfrom.getText().toString();
      shot_from = TopoDroidApp.noSpaces( shot_from );
      // if ( shot_from == null ) { shot_from = ""; }

      shot_to = mETto.getText().toString();
      shot_to = TopoDroidApp.noSpaces( shot_to );
      shot_leg = false;
    }

    shot_flag = DistoXDBlock.BLOCK_SURVEY;
    if ( mRBdup.isChecked() )       { shot_flag = DistoXDBlock.BLOCK_DUPLICATE; }
    else if ( mRBsurf.isChecked() ) { shot_flag = DistoXDBlock.BLOCK_SURFACE; }
    // else if ( mRBback.isChecked() ) { shot_flag = DistoXDBlock.BLOCK_BACKSHOT; }
    // else                            { shot_flag = DistoXDBlock.BLOCK_SURVEY; }

    shot_extend = mBlk.mExtend;
    if ( mRBleft.isChecked() )       { shot_extend = DistoXDBlock.EXTEND_LEFT; }
    else if ( mRBvert.isChecked() )  { shot_extend = DistoXDBlock.EXTEND_VERT; }
    else if ( mRBright.isChecked() ) { shot_extend = DistoXDBlock.EXTEND_RIGHT; }
    else                             { shot_extend = DistoXDBlock.EXTEND_IGNORE; }

    mBlk.mFlag = shot_flag;
    mBlk.mExtend = shot_extend;
    if ( shot_leg ) mBlk.mType = DistoXDBlock.BLOCK_SEC_LEG;

    String comment = mETcomment.getText().toString();
    if ( comment != null ) mBlk.mComment = comment;

    boolean renumber = false;
    if ( shot_from.length() > 0 && shot_to.length() > 0 ) {
      renumber = mCBrenumber.isChecked();
      all_splay = false;
    }
    if ( all_splay ) {
      mParent.updateSplayShots( shot_from, shot_to, shot_extend, shot_flag, shot_leg, comment, mBlk );
    } else {
      // mBlk.setName( shot_from, shot_to ); // done by parent.updateShot
      mParent.updateShot( shot_from, shot_to, shot_extend, shot_flag, shot_leg, comment, mBlk );
    }
    // mParent.scrollTo( mPos );

    if ( shot_manual ) {
      try {
        float d = Float.parseFloat( mETdistance.getText().toString() ) / TopoDroidSetting.mUnitLength;
        float b = Float.parseFloat( mETbearing.getText().toString() )  / TopoDroidSetting.mUnitAngle;
        float c = Float.parseFloat( mETclino.getText().toString() )    / TopoDroidSetting.mUnitAngle;
        mParent.updateShotDistanceBearingClino( d, b, c, mBlk );
      } catch (NumberFormatException e ) { }
    }

    if ( renumber ) {
      mParent.renumberShotsAfter( mBlk );
    }
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "ShotDialog onClick button " + b.getText().toString() );

    if ( b == mRBleft ) {
      mRBvert.setChecked( false );
      mRBright.setChecked( false );
    } else if ( b == mRBvert ) {
      mRBleft.setChecked( false );
      mRBright.setChecked( false );
    } else if ( b == mRBright ) {
      mRBleft.setChecked( false );
      mRBvert.setChecked( false );

    // } else if ( b == mRBdup ) {
    //   mRBsurf.setChecked( false );
    //   // mRBback.setChecked( false );
    // } else if ( b == mRBsurf ) {
    //   mRBdup.setChecked( false );
    //   // mRBback.setChecked( false );

    } else if ( b == mCBleg ) {
      Log.v("DistoX", "CB leg clicked ");
      mCBleg.toggleState();
      if ( mCBleg.isChecked() ) {
        mCBall_splay.setState( false );
      }
    } else if ( b == mCBall_splay ) {
      Log.v("DistoX", "CB all_splay clicked ");
      mCBall_splay.toggleState();
      if ( mCBall_splay.isChecked() ) {
        mCBleg.setState( false );
      }
    
    } else if ( b == mButtonOK ) {
      saveDBlock();
      onBackPressed();
    } else if ( b == mButtonSave ) {
      // FIXME DIALOG mKeyboard.hide();
      saveDBlock();
    } else if ( b == mButtonPrev ) {
      // shift:
      //               prev -- blk -- next
      // prevOfPrev -- prev -- blk
      //
      // saveDBlock();
      if ( mPrevBlk != null ) {
        DistoXDBlock prevBlock = mParent.getPreviousLegShot( mPrevBlk, true );
        TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "PREV " + mPrevBlk.toString(true ) );
        loadDBlock( mPrevBlk, prevBlock, mBlk );
        updateView();
      } else {
        TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "PREV is null" );
      }
    } else if ( b == mButtonNext ) {
      // shift:
      //        prev -- blk -- next
      //                blk -- next -- nextOfNext
      // saveDBlock();
      if ( mNextBlk != null ) {
        DistoXDBlock next = mParent.getNextLegShot( mNextBlk, true );
        TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "NEXT " + mNextBlk.toString(true ) );
        loadDBlock( mNextBlk, mBlk, next );
        updateView();
      } else {
        TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "NEXT is null" );
      }
    } else if ( b == mButtonReverse ) {
      shot_from = mETfrom.getText().toString();
      shot_from = TopoDroidApp.noSpaces( shot_from );
      shot_to = mETto.getText().toString();
      shot_to = TopoDroidApp.noSpaces( shot_to );
      if ( shot_to.length() > 0 && shot_from.length() > 0 ) {
        String temp = new String( shot_from );
        shot_from = shot_to;
        shot_to = temp;
        mETfrom.setText( shot_from );
        mETto.setText( shot_to );
      }
    // } else if ( b == mButtonDrop ) {
    //   mParent.dropShot( mBlk );
    //   onBackPressed();
    // } else if ( b == mButtonBack ) {
    //   onBackPressed();
    }
  }

  @Override
  public void onBackPressed()
  {
    if ( TopoDroidSetting.mKeyboard ) {
      if ( mKeyboard.isVisible() ) {
        mKeyboard.hide();
        return;
      }
    }
    dismiss();
  }

}

