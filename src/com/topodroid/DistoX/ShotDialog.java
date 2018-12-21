/* @file ShotDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey shot dialog to enter FROM-TO stations etc.
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.regex.Pattern;

// import android.app.Dialog;
import android.os.Bundle;
// import android.widget.RadioButton;

import android.text.method.KeyListener;
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
import android.widget.CheckBox;
import android.widget.LinearLayout;
// import android.widget.PopupWindow;
// import android.widget.Toast;

import android.view.View;
// import android.view.ViewGroup.LayoutParams;
// import android.view.Window;
// import android.view.WindowManager;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;

import android.util.Log;

class ShotDialog extends MyDialog
                 implements View.OnClickListener
                          , View.OnLongClickListener
{
  private final ShotWindow mParent;
  private DBlock mBlk;
  private DBlock mPrevBlk;
  private DBlock mNextBlk;
  private int mPos; // item position in the parent' adapter list

  private Pattern mPattern; // name pattern

  // private TextView mTVdata;
  private EditText mETdistance; // distance | depth
  private EditText mETbearing;
  private EditText mETclino;    // clino | distance

  private TextView mTVextra;

  private EditText mETfrom;
  private EditText mETto;
  private EditText mETcomment;
  
  private MyCheckBox mRBdup   = null;
  private MyCheckBox mRBsurf  = null;
  private MyCheckBox mRBcmtd  = null;  // FIXME_COMMENTED 
  private MyStateBox mRBsplay = null;  // inhibit splay plan/profile display

  private MyCheckBox mCBlegPrev;
  private MyCheckBox mCBlegNext;
  private MyCheckBox mCBrenumber;
  private MyCheckBox mCBallSplay;
  private MyCheckBox mCBxSplay = null;
  // private MyCheckBox mCBhighlight;
  private MyCheckBox mCBbackLeg = null;

  private HorizontalListView mListView;
  private HorizontalButtonView mButtonView;
  private Button[] mButton;

  private CheckBox mRBleft;
  private CheckBox mRBvert;
  private CheckBox mRBright;
  // private CheckBox mRBignore;

  private Button   mButtonOK;
  private Button   mButtonSave;
  private Button   mButtonMore;
  private Button   mButtonBack;

  private Button   mButtonPrev;
  private Button   mButtonNext;
  private Button mButtonReverse;

  private String shot_from;
  private String shot_to;
  private String shot_distance;
  private String shot_bearing;
  private String shot_clino;
  private boolean shot_manual;
  private float shot_stretch; // FIXME_STRETCH

  private String shot_extra;
  private int  shot_extend;
  private long shot_flag;
  private String shot_comment;

  private MyKeyboard mKeyboard = null;

  private KeyListener mKLdistance;
  private KeyListener mKLbearing;
  private KeyListener mKLclino;

  private static final int flagDistance = MyKeyboard.FLAG_POINT;
  private static final int flagBearing  = MyKeyboard.FLAG_POINT;
  private static final int flagClino    = MyKeyboard.FLAG_POINT | MyKeyboard.FLAG_SIGN;
  private static final int flagDepth    = MyKeyboard.FLAG_POINT;

  private boolean mFirst;

  ShotDialog( Context context, ShotWindow parent, int pos, DBlock blk,
              DBlock prev, DBlock next
            )
  {
    super( context, R.string.ShotDialog );
    mParent = parent;
    mPos = pos;
    mFirst = true;
    mPrevBlk     = prev;
    mNextBlk     = next;
    mBlk         = blk;
    // loadDBlock( blk, prev, next );
    // TDLog.Log( TDLog.LOG_SHOT, "Shot Dialog " + blk.toString(true) );
  }


  private void loadDBlock( DBlock blk, DBlock prev, DBlock next )
  {
    mPrevBlk     = prev;
    mNextBlk     = next;
    mBlk         = blk;
    // TDLog.Log( TDLog.LOG_SHOT, "Shot Dialog LOAD " + blk.toString(true) );
    // TDLog.Log( TDLog.LOG_SHOT, "  prev " + ((prev != null)? prev.toString(true) : "null") );
    // TDLog.Log( TDLog.LOG_SHOT, "  next " + ((next != null)? next.toString(true) : "null") );

    shot_from    = blk.mFrom;
    shot_to      = blk.mTo;
    
    if ( blk.isTypeBlank() && prev != null && prev.isMainLeg() ) {
      if ( DistoXStationName.isLessOrEqual( prev.mFrom, prev.mTo ) ) {
        shot_from = prev.mTo;
	if ( mFirst ) {
          shot_to = DistoXStationName.incrementName( prev.mTo, mParent.getStationNames() );
	  mFirst  = false;
	} else {
          shot_to   = DistoXStationName.incrementName( prev.mTo );
	}
      } else {
        shot_to = prev.mFrom;
	if ( mFirst ) {
          shot_from = DistoXStationName.incrementName( prev.mFrom, mParent.getStationNames() );
	  mFirst  = false;
	} else {
          shot_from = DistoXStationName.incrementName( prev.mFrom );
	}
      }
    }
    
    // shot_data    = blk.dataString();
    if ( TDInstance.datamode == SurveyInfo.DATAMODE_DIVING ) {
      shot_distance = blk.depthString();
      shot_bearing  = blk.bearingString();
      shot_clino    = blk.distanceString();
      shot_manual   = (blk.mShotType > 0);
    } else { // SurveyInfo.DATAMODE_NORMAL
      shot_distance = blk.distanceString();
      shot_bearing  = blk.bearingString();
      shot_clino    = blk.clinoString();
      shot_manual   = (blk.mShotType > 0);
    }

    // Log.v("DistoX", "shot is manual " + shot_manual + " length " + shot_distance );

    // shot_extra   = blk.extraString( mParent.mDistoXAccuracy );
    shot_extra   = mParent.getBlockExtraString( blk );
    shot_extend  = blk.getExtend();
    shot_stretch = blk.getStretch(); // FIXME_STRETCH
    shot_flag    = blk.getFlag();
    // shot_secleg  = blk.isSecLeg(); // DBlock.BLOCK_SEC_LEG;
    // shot_backleg = blk.isBackLeg();
    // shot_xsplay  = blk.isOtherSplay(); // DBlock.BLOCK_X_SPLAY | H_SPLAY | V_SPLAY
    shot_comment = blk.mComment;

    // if ( blk.type() != DBlock.BLOCK_MAIN_LEG ) mCBdeleteLeg.setVisibility( View.GONE );
    updateView();
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
      // mRBfrom.setText( shot_from );
    // } else {
    //   mRBfrom.setVisibility( View.GONE );
    }
    if ( shot_to.length() > 0 ) {
      mETto.setText( shot_to );
      // mRBto.setText( shot_to );
    // } else {
    //   mRBto.setVisibility( View.GONE );
    }
    if ( shot_comment != null ) {
      mETcomment.setText( shot_comment );
    } else {
      mETcomment.setText( "" );
    }
   
    // if ( DBlock.isSurvey(shot_flag) ) { mRBreg.setChecked( true ); }
    if ( TDLevel.overNormal ) {
      mRBdup.setState(  false );
      mRBsurf.setState( false );
      mRBcmtd.setState( false ); // FIXME_COMMENTED
      if ( DBlock.isDuplicate(shot_flag) )      { mRBdup.setState(  true ); }
      else if ( DBlock.isSurface(shot_flag) )   { mRBsurf.setState( true ); }
      else if ( DBlock.isCommented(shot_flag) ) { mRBcmtd.setState( true ); } // FIXME_COMMENTED
      else if ( mRBsplay != null ) {
        if ( DBlock.isNoProfile(shot_flag) )   { mRBsplay.setState( 1 ); }
        else if ( DBlock.isNoPlan(shot_flag) ) { mRBsplay.setState( 2 ); }
      }
      // else if ( DBlock.isBackshot(shot_flag) ) { mRBback.setChecked( true ); }
    }
    if ( /* TDLevel.overAdvanced && */  mCBxSplay != null ) {
      if ( mBlk.isOtherSplay() ) {
        mCBxSplay.setState( false ); // other-splay type can only be cleared (from this dialog), not set
      } else {
        mCBxSplay.setVisibility( View.GONE );
      }
    } 

    mCBlegPrev.setChecked( mBlk.isSecLeg() );
    if ( mCBbackLeg != null ) mCBbackLeg.setState( mBlk.isBackLeg() );

    mRBleft.setChecked(  false );
    mRBvert.setChecked(  false );
    mRBright.setChecked( false );
    // mRBignore.setChecked( false );
    if ( shot_extend == DBlock.EXTEND_LEFT )       { mRBleft.setChecked(  true ); }
    else if ( shot_extend == DBlock.EXTEND_VERT )  { mRBvert.setChecked(  true ); }
    else if ( shot_extend == DBlock.EXTEND_RIGHT ) { mRBright.setChecked( true ); }
    // else if ( shot_extend == DBlock.EXTEND_IGNORE ) { mRBignore.setChecked( true ); }

    // Spinner
    // switch ( shot_extend ) {
    //   case DBlock.EXTEND_LEFT: break;
    //   case DBlock.EXTEND_VERT: break;
    //   case DBlock.EXTEND_RIGHT: break;
    //   case DBlock.EXTEND_IGNORE: break;
    // }

    mButtonNext.setEnabled( mNextBlk != null );
    mButtonPrev.setEnabled( mPrevBlk != null );

    // do at the very end
    if ( TDInstance.datamode == SurveyInfo.DATAMODE_DIVING ) {
      MyKeyboard.setEditable( mETdistance, mKeyboard, mKLdistance, shot_manual, flagDepth );
      MyKeyboard.setEditable( mETbearing,  mKeyboard, mKLbearing,  shot_manual, flagBearing );
      MyKeyboard.setEditable( mETclino,    mKeyboard, mKLclino,    shot_manual, flagDistance );
    } else {
      MyKeyboard.setEditable( mETdistance, mKeyboard, mKLdistance, shot_manual, flagDistance );
      MyKeyboard.setEditable( mETbearing,  mKeyboard, mKLbearing,  shot_manual, flagBearing );
      MyKeyboard.setEditable( mETclino,    mKeyboard, mKLclino,    shot_manual, flagClino );
    }

    mETcomment.requestFocus();
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

    initLayout( R.layout.shot_dialog, null );

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

    // mRBfrom = (RadioButton) findViewById( R.id.station_from );
    // mRBto   = (RadioButton) findViewById( R.id.station_to );

    // mButtonLRUD = {Button} findViewById( R.id.btn_lrud );
    // mETleft  = (EditText) findViewById(R.id.shot_left );
    // mETright = (EditText) findViewById(R.id.shot_right);
    // mETup    = (EditText) findViewById(R.id.shot_up   );
    // mETdown  = (EditText) findViewById(R.id.shot_down );
   
    mETfrom.setOnLongClickListener( this );
    mETto.setOnLongClickListener( this );

    // mCBdeleteLeg = (CheckBox) findViewById(R.id.delete_leg );
    // mButtonLRUD.setOnClickListener( this );

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ),
                                R.xml.my_keyboard_base_sign, R.xml.my_keyboard_qwerty );
    if ( TDSetting.mKeyboard ) {
      int flag = MyKeyboard.FLAG_POINT_LCASE_2ND;
      if ( TDSetting.mStationNames == 1 ) flag = MyKeyboard.FLAG_POINT;
      MyKeyboard.registerEditText( mKeyboard, mETfrom,  flag );
      MyKeyboard.registerEditText( mKeyboard, mETto,    flag );
      // MyKeyboard.registerEditText( mKeyboard, mETleft,  MyKeyboard.FLAG_POINT );
      // MyKeyboard.registerEditText( mKeyboard, mETright, MyKeyboard.FLAG_POINT );
      // MyKeyboard.registerEditText( mKeyboard, mETup,    MyKeyboard.FLAG_POINT );
      // MyKeyboard.registerEditText( mKeyboard, mETdown,  MyKeyboard.FLAG_POINT );
      // mKeyboard.hide();
    } else {
      mKeyboard.hide();
      if ( TDSetting.mStationNames == 1 ) {
        mETfrom.setInputType( InputType.TYPE_CLASS_NUMBER );
        mETto.setInputType( InputType.TYPE_CLASS_NUMBER );
      }
      // mETleft.setInputType( InputType.TYPE_CLASS_NUMBER );
      // mETright.setInputType( InputType.TYPE_CLASS_NUMBER );
      // mETup.setInputType( InputType.TYPE_CLASS_NUMBER );
      // mETdown.setInputType( InputType.TYPE_CLASS_NUMBER );
    }

    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    // TDToast.make( "SIZE " + size );
    
    LinearLayout layout4 = (LinearLayout) findViewById( R.id.layout4 );
    // LinearLayout layout9 = (LinearLayout) findViewById( R.id.layout9 );
    layout4.setMinimumHeight( size + 20 );
    // layout9.setMinimumHeight( size + 20 );
    
    mCBlegPrev   = new MyCheckBox( mContext, size, R.drawable.iz_leg2_ok, R.drawable.iz_leg2_no );
    mCBlegNext   = new MyCheckBox( mContext, size, R.drawable.iz_legnext_ok, R.drawable.iz_legnext_no );
    mCBrenumber  = new MyCheckBox( mContext, size, R.drawable.iz_numbers_ok, R.drawable.iz_numbers_no );
    mCBallSplay  = new MyCheckBox( mContext, size, R.drawable.iz_splays_ok, R.drawable.iz_splays_no );
    int nr_buttons = 4;

    if ( TDLevel.overNormal ) {
      mRBdup  = new MyCheckBox( mContext, size, R.drawable.iz_dup_ok, R.drawable.iz_dup_no );
      mRBsurf = new MyCheckBox( mContext, size, R.drawable.iz_surface_ok, R.drawable.iz_surface_no );
      mRBcmtd = new MyCheckBox( mContext, size, R.drawable.iz_comment_ok, R.drawable.iz_comment_no );
      mRBdup.setOnClickListener( this );
      mRBsurf.setOnClickListener( this );
      mRBcmtd.setOnClickListener( this );
      nr_buttons += 3;
    }

    // FIXME_X2_SPLAY
    // if ( TDLevel.overAdvanced ) {
    //   if ( shot_xsplay ) {
    //     mCBxSplay = new MyCheckBox( mContext, size, R.drawable.iz_xsplays_ok, R.drawable.iz_ysplays_no );
    //   } else {
    //     mCBxSplay = new MyCheckBox( mContext, size, R.drawable.iz_ysplays_ok, R.drawable.iz_xsplays_no );
    //   }
    //   mCBxSplay.setOnClickListener( this );
    // }
    if ( TDLevel.overAdvanced && TDSetting.mSplayClasses ) {
      mCBxSplay = new MyCheckBox( mContext, size, R.drawable.iz_xsplays_ok, R.drawable.iz_ysplays_no );
      mCBxSplay.setOnClickListener( this );
      nr_buttons ++;
    }

    if ( TDLevel.overBasic ) {
      mCBbackLeg = new MyCheckBox( mContext, size, R.drawable.iz_backleg_ok, R.drawable.iz_backleg_no );
      mCBbackLeg.setOnClickListener( this );
      nr_buttons ++;
    }

    if ( TDLevel.overExpert && TDSetting.mSplayClasses ) {
      mRBsplay = new MyStateBox( mContext, R.drawable.iz_plan_profile, R.drawable.iz_plan, R.drawable.iz_extended );
      mRBsplay.setOnClickListener( this );
      nr_buttons ++;
    } // else mRBsplay = null;
    // mCBhighlight = new MyCheckBox( mContext, size, R.drawable.iz_highlight_ok, R.drawable.iz_highlight_no );

    mButton = new Button[nr_buttons];
    // Log.v("DistoX", "nr buttons " + nr_buttons );

    int k = 0;
    if ( mRBdup  != null ) mButton[k++] = mRBdup;
    if ( mRBsurf != null ) mButton[k++] = mRBsurf;
    if ( mRBcmtd != null ) mButton[k++] = mRBcmtd;
    mButton[k++] = mCBlegPrev;
    mButton[k++] = mCBlegNext;
    if ( mCBbackLeg != null ) mButton[k++] = mCBbackLeg;
    mButton[k++] = mCBrenumber;
    mButton[k++] = mCBallSplay;
    if ( mRBsplay  != null ) mButton[k++] = mRBsplay;
    if ( mCBxSplay != null ) mButton[k++] = mCBxSplay;

    mListView = (HorizontalListView) findViewById(R.id.listview);
    // mListView.setEmptyPlacholder( true );
    /* size = */ TopoDroidApp.setListViewHeight( mContext, mListView );
    mButtonView = new HorizontalButtonView( mButton );
    mListView.setAdapter( mButtonView.mAdapter );

    layout4.invalidate();

    mCBlegPrev.setOnClickListener( this );
    mCBlegNext.setOnClickListener( this );
    mCBallSplay.setOnClickListener( this );

    mButtonReverse = (Button)  findViewById(R.id.shot_reverse );

    mRBleft   = (CheckBox) findViewById(R.id.left );
    mRBvert   = (CheckBox) findViewById(R.id.vert );
    mRBright  = (CheckBox) findViewById(R.id.right );
    // mRBignore = (CheckBox) findViewById(R.id.ignore );

    // if ( TDSetting.mLoopClosure == TDSetting.LOOP_NONE ) {
    //   mRBignore.setClickable( false );
    //   mRBignore.setTextColor( TDColor.MID_GRAY );
    // }

    mButtonPrev = (Button) findViewById(R.id.btn_prev );
    mButtonNext = (Button) findViewById(R.id.btn_next );

    mButtonSave = (Button) findViewById(R.id.btn_save );
    mButtonOK   = (Button) findViewById(R.id.btn_ok );
    mButtonMore = (Button) findViewById(R.id.btn_more );
    mButtonBack = (Button) findViewById(R.id.btn_back );

    // mETfrom.setRawInputType( InputType.TYPE_CLASS_NUMBER );
    // mETfrom.setKeyListener( NumberKeyListener );
    // mETto.setRawInputType( InputType.TYPE_CLASS_NUMBER );

    if ( TDSetting.mPrevNext ) {
      mButtonPrev.setOnClickListener( this );
      mButtonNext.setOnClickListener( this );
      mButtonSave.setOnClickListener( this );
    } else {
      mButtonPrev.setVisibility( View.INVISIBLE );
      mButtonNext.setVisibility( View.INVISIBLE );
      mButtonSave.setVisibility( View.GONE );
    }
    mButtonOK.setOnClickListener( this );
    mButtonMore.setOnClickListener( this );
    mButtonBack.setOnClickListener( this );


    mButtonReverse.setOnClickListener( this );

    mRBleft.setOnClickListener( this );
    mRBvert.setOnClickListener( this );
    mRBright.setOnClickListener( this );
    // mRBignore.setOnClickListener( this );

    loadDBlock( mBlk, mPrevBlk, mNextBlk );
    // updateView();
    mParent.mOnOpenDialog = false;
  }

  private void saveDBlock()
  {
    // add LRUD at station if any is checked and data have been entered
    // String station = null;
    // if ( mRBfrom.isChecked() ) {
    //   station = mBlk.mFrom;
    // } else if ( mRBto.isChecked() ) {
    //   station = mBlk.mTo;
    // }
    // if ( station != null ) {
    //   // check the data
    //   mParent.insertLRUDatStation( station, mBlk.mBearing, mBlk.mClino, 
    //     mETleft.getText().toString().replace(',','.') ,
    //     mETright.getText().toString().replace(',','.') ,
    //     mETup.getText().toString().replace(',','.') ,
    //     mETdown.getText().toString().replace(',','.') 
    //   );
    // }

    boolean do_backleg = false;
    boolean backleg_val = mCBbackLeg != null && mCBbackLeg.isChecked();
    boolean all_splay = mCBallSplay.isChecked();
    // FIXME_X2_SPLAY boolean x_splay = (mCBxSplay != null) && mCBxSplay.isChecked();
    boolean clear_xsplay = ( mCBxSplay != null ) && mCBxSplay.isChecked(); // FIXME_X3_SPLAY whether to clear other-splay value
    // Log.v("DistoX", "clear xsplay = " + clear_xsplay );
    boolean leg_next = false;
    boolean shot_secleg = false;
    if ( mCBlegPrev.isChecked() ) {
      shot_from = "";
      shot_to   = "";
      shot_secleg  = true;
      // do_backleg = false;
      all_splay = false;
      clear_xsplay = false;
    } else if ( mCBlegNext.isChecked() ) {
      leg_next  = true;
      // shot_secleg  = false;
      // do_backleg = false;
      all_splay = false;
      clear_xsplay = false;
    } else {
      shot_from = TopoDroidUtil.noSpaces( mETfrom.getText().toString() );
      // if ( shot_from == null ) { shot_from = ""; }
      shot_to = TopoDroidUtil.noSpaces( mETto.getText().toString() );
      // shot_secleg = false;
      do_backleg = ( shot_from.length() > 0 ) && ( shot_to.length() > 0 );
    }
    // Log.v("DistoXX", "do backleg " + do_backleg + " value " + backleg_val );

    shot_flag = DBlock.FLAG_SURVEY;
    if ( TDLevel.overNormal ) {
      if ( mRBdup.isChecked() )       { shot_flag = DBlock.FLAG_DUPLICATE; }
      else if ( mRBsurf.isChecked() ) { shot_flag = DBlock.FLAG_SURFACE; }
      else if ( mRBcmtd.isChecked() ) { shot_flag = DBlock.FLAG_COMMENTED; } // FIXME_COMMENTED
      else if ( mRBsplay != null ) {
        if ( mRBsplay.getState() == 1 )      { shot_flag = DBlock.FLAG_NO_PROFILE; }
        else if ( mRBsplay.getState() == 2 ) { shot_flag = DBlock.FLAG_NO_PLAN; }
      }
    }
    // else if ( mRBback.isChecked() ) { shot_flag = DBlock.FLAG_BACKSHOT; }
    // else                            { shot_flag = DBlock.FLAG_SURVEY; }
    // Log.v("DistoXA", "shot flag " + shot_flag );

    shot_extend = mBlk.getExtend();
    if ( mRBleft.isChecked() )       { shot_extend = DBlock.EXTEND_LEFT; }
    else if ( mRBvert.isChecked() )  { shot_extend = DBlock.EXTEND_VERT; }
    else if ( mRBright.isChecked() ) { shot_extend = DBlock.EXTEND_RIGHT; }
    else                             { shot_extend = DBlock.EXTEND_IGNORE; }

    mBlk.resetFlag( shot_flag );

    if ( shot_secleg ) {
      // Log.v("DistoX", "block set sec-leg type ");
      mBlk.setTypeSecLeg();
    } else if ( leg_next ) {
      // do_backleg = false; // not neceessary
      long id = mParent.mergeToNextLeg( mBlk );
      if ( id >= 0 ) {
        shot_from = mBlk.mFrom;
        shot_to   = mBlk.mTo;
      }
    }
 
    int extend = shot_extend;
    boolean sflen = shot_from.length() > 0;
    boolean stlen = shot_to.length() > 0;
    if ( mBlk.getExtend() != shot_extend ) {
      if ( leg_next || ( sflen && stlen ) ) { // leg
        mBlk.setExtend( extend, DBlock.STRETCH_NONE ); // FIXME_STRETCH
      } else if ( ( sflen && ! stlen ) || ( stlen && ! sflen ) ) { // splay
        // extend = shot_extend + DBlock.EXTEND_FVERT;
        mBlk.setExtend( extend, DBlock.STRETCH_NONE ); // FIXME_STRETCH
      }
    }

    String comment = mETcomment.getText().toString();
    if ( comment != null ) mBlk.mComment = comment.trim();

    boolean renumber  = false;
    boolean highlight = false;
    if ( shot_from.length() > 0 ) {
      if ( shot_to.length() > 0 ) {
        renumber = mCBrenumber.isChecked();
        all_splay = false;
        clear_xsplay = false;
      // } else { // this is useless: replaced by long-tap on shot list 
      //   if ( mCBhighlight.isChecked() ) {
      //     Log.v("DistoX", "parent to highlight " + mBlk.mFrom + " " + mBlk.mTo );
      //     mParent.highlightBlock( mBlk );
      //   }

      }
    }

    if ( all_splay ) {
      long leg1 = clear_xsplay ? LegType.NORMAL : mBlk.getLegType();
      mParent.updateSplayShots( shot_from, shot_to, extend, shot_flag, leg1, comment, mBlk );
    // FIXME_X2_SPLAY
    // } else if ( x_splay ) {
    //   mParent.updateSplayLeg( mPos );
    } else if ( clear_xsplay ) {
      // Log.v("DistoX", "clear leg type");
      mParent.updateSplayLegType( mBlk, LegType.NORMAL );
    } else {
      // mBlk.setName( shot_from, shot_to ); // done by parent.updateShot
      // if ( shot_secleg ) mBlk.setTypeSecLeg(); // FIXME maybe not necessary
      long leg = mBlk.isSplay() ? mBlk.getLegType()  // HERE clear_xsplay = false 
               : shot_secleg ? LegType.EXTRA 
               : LegType.NORMAL ;
      if ( do_backleg && backleg_val ) {
        leg = LegType.BACK;
      }
      // Log.v("DistoX", "Block is splay " + mBlk.isSplay() + " leg " + leg + " blk type " + mBlk.getBlockType() );
      mParent.updateShot( shot_from, shot_to, extend, shot_stretch, shot_flag, leg, comment, mBlk );
    }
    // mParent.scrollTo( mPos );

    if ( shot_manual ) {
      try {
	if ( TDInstance.datamode == SurveyInfo.DATAMODE_DIVING ) {
          float p = Float.parseFloat( mETdistance.getText().toString() ) / TDSetting.mUnitLength;
	  float b = Float.parseFloat( mETbearing.getText().toString() )  / TDSetting.mUnitAngle;
          float d = Float.parseFloat( mETclino.getText().toString() )    / TDSetting.mUnitLength;
          mParent.updateShotDepthBearingDistance( p, b, d, mBlk );
	} else { // SurveyInfo.DATAMODE_DEPTH
          float d = Float.parseFloat( mETdistance.getText().toString() ) / TDSetting.mUnitLength;
	  float b = Float.parseFloat( mETbearing.getText().toString() )  / TDSetting.mUnitAngle;
          float c = Float.parseFloat( mETclino.getText().toString() )    / TDSetting.mUnitAngle;
          mParent.updateShotDistanceBearingClino( d, b, c, mBlk );
	}
      } catch (NumberFormatException e ) { }
    }

    if ( renumber ) {
      // Log.v("DistoX", "renumber shots after block id " + mBlk.mId );
      mParent.renumberShotsAfter( mBlk );
    }
  }


  @Override
  public boolean onLongClick(View v) 
  {
    CutNPaste.makePopup( mContext, (EditText)v );
    return true;
  }
    

  @Override
  public void onClick(View v) 
  {
    CutNPaste.dismissPopup();
    MyKeyboard.close( mKeyboard );

    Button b = (Button) v;
    // TDLog.Log( TDLog.LOG_INPUT, "ShotDialog onClick button " + b.getText().toString() );

    if ( b == mRBleft ) {
      mRBvert.setChecked( false );
      mRBright.setChecked( false );
      // shot_extend = mRBleft.isChecked() ? DBlock.EXTEND_LEFT : DBlock.EXTEND_IGNORE;
    } else if ( b == mRBvert ) {
      mRBleft.setChecked( false );
      mRBright.setChecked( false );
      // shot_extend = mRBvert.isChecked() ? DBlock.EXTEND_VERT : DBlock.EXTEND_IGNORE;
    } else if ( b == mRBright ) {
      mRBleft.setChecked( false );
      mRBvert.setChecked( false );
      // shot_extend = mRBright.isChecked() ? DBlock.EXTEND_RIGHT : DBlock.EXTEND_IGNORE;

    } else if ( b == mCBlegPrev ) {
      // Log.v("DistoX", "CB leg clicked ");
      if ( mCBlegPrev.toggleState() ) {
        mCBallSplay.setState( false );
        if ( mCBxSplay != null ) mCBxSplay.setState( false );
        mCBlegNext.setState( false );
      }
    } else if ( b == mCBallSplay ) {
      // Log.v("DistoX", "CB all_splay clicked ");
      if ( mCBallSplay.toggleState() ) {
        if ( mCBxSplay != null ) mCBxSplay.setState( false );
        mCBlegPrev.setState( false );
        mCBlegNext.setState( false );
      }
    } else if ( mCBbackLeg != null && b == mCBbackLeg ) {
      mCBbackLeg.toggleState();
    } else if ( mCBxSplay != null && b == mCBxSplay ) {
      if ( mCBxSplay.toggleState() ) {
        mCBallSplay.setState( false );
        mCBlegPrev.setState( false );
        mCBlegNext.setState( false );
      }
    } else if ( b == mCBlegNext ) {
      if ( mCBlegNext.toggleState() ) {
        mCBlegPrev.setState( false );
        mCBallSplay.setState( false );
        if ( mCBxSplay != null ) mCBxSplay.setState( false );
      }

    } else if ( mRBdup != null  && b == mRBdup ) {
      if ( mRBdup.toggleState() ) {
        mRBsurf.setState( false );
        mRBcmtd.setState( false ); // FIXME_COMMENTED
        if ( mRBsplay != null ) mRBsplay.setState( 0 );
      }
    } else if ( mRBsurf != null  && b == mRBsurf ) {
      if ( mRBsurf.toggleState() ) {
        mRBdup.setState( false );
        mRBcmtd.setState( false ); // FIXME_COMMENTED
        if ( mRBsplay != null ) mRBsplay.setState( 0 );
      }
    } else if ( mRBcmtd != null  && b == mRBcmtd ) { // FIXME_COMMENTED
      if ( mRBcmtd.toggleState() ) {
        mRBdup.setState( false );
        mRBsurf.setState( false );
        if ( TDLevel.overExpert ) mRBsplay.setState( 0 );
      }
    } else if ( mRBsplay != null && b == mRBsplay ) {
      mRBsplay.setState( ( mRBsplay.getState() + 1 ) % 3 );
      if ( mRBsplay.getState() > 0 ) {
        mRBdup.setState( false );
        mRBsurf.setState( false );
        mRBcmtd.setState( false ); // FIXME_COMMENTED
      }

    } else if ( b == mButtonBack ) {
      CutNPaste.dismissPopup();
      dismiss();
    } else if ( b == mButtonMore ) {
      CutNPaste.dismissPopup();
      dismiss();
      mParent.onBlockLongClick( mBlk );
    } else if ( b == mButtonOK ) { // OK and SAVE close the keyboard
      saveDBlock();
      dismiss();
    } else if ( b == mButtonSave ) {
      saveDBlock();

    } else if ( b == mButtonPrev ) {
      mCBallSplay.setVisibility( View.GONE );
      if ( mCBxSplay != null ) mCBxSplay.setVisibility( View.GONE );
      // shift:
      //               prev -- blk -- next
      // prevOfPrev -- prev -- blk
      //
      // saveDBlock();
      if ( mPrevBlk != null ) {
        DBlock prevBlock = mParent.getPreviousLegShot( mPrevBlk, true );
        // TDLog.Log( TDLog.LOG_SHOT, "PREV " + mPrevBlk.toString(true ) );
        loadDBlock( mPrevBlk, prevBlock, mBlk );
        // updateView();
      } else {
        TDLog.Log( TDLog.LOG_SHOT, "PREV is null" );
      }

    } else if ( b == mButtonNext ) {
      mCBallSplay.setVisibility( View.GONE );
      if ( mCBxSplay != null ) mCBxSplay.setVisibility( View.GONE );
      // shift:
      //        prev -- blk -- next
      //                blk -- next -- nextOfNext
      // saveDBlock();
      if ( mNextBlk != null ) {
        DBlock next = mParent.getNextLegShot( mNextBlk, true );
        // TDLog.Log( TDLog.LOG_SHOT, "NEXT " + mNextBlk.toString(true ) );
        loadDBlock( mNextBlk, mBlk, next );
        // updateView();
      } else {
        TDLog.Log( TDLog.LOG_SHOT, "NEXT is null" );
      }

    } else if ( b == mButtonReverse ) {
      shot_from = mETfrom.getText().toString();
      shot_from = TopoDroidUtil.noSpaces( shot_from );
      shot_to = mETto.getText().toString();
      shot_to = TopoDroidUtil.noSpaces( shot_to );
      if ( shot_to.length() > 0 && shot_from.length() > 0 ) {
        String temp = shot_from; // new String( shot_from );
        shot_from = shot_to;
        shot_to = temp;
        mETfrom.setText( shot_from );
        mETto.setText( shot_to );
	// FIXME_EXTEND swap extend if set
        if ( mRBleft.isChecked() ) {
	  mRBleft.setChecked( false );
	  mRBright.setChecked( true );
          // shot_extend = DBlock.EXTEND_RIGHT;
	} else if ( mRBright.isChecked() ) {
	  mRBleft.setChecked( true );
	  mRBright.setChecked( false );
          // shot_extend = DBlock.EXTEND_LEFT;
	}
      }
    // } else if ( b == mButtonDrop ) {
    //   mParent.dropShot( mBlk );
    //   onBackPressed();
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

