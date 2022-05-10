/* @file AudioDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid audio dialog to register a comment for a shot
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDUtil;
import com.topodroid.ui.MyStateBox;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDLayout;
import com.topodroid.prefs.TDSetting;

import java.io.IOException;

// import android.app.Dialog;
import android.os.Bundle;
// import android.os.Environment;

import android.content.Context;

import android.media.MediaPlayer;
import android.media.MediaRecorder;

import android.widget.LinearLayout;
import android.view.View;
// import android.view.View.OnClickListener;
// import android.view.Window;
// import android.view.WindowManager;

import android.widget.Button;
import android.widget.TextView;

class AudioDialog extends MyDialog
                  implements View.OnClickListener
{
  private static final int ACTION_NONE      = 0;
  private static final int ACTION_DELETE    = 1;
  private static final int ACTION_OVERWRITE = 2;

  private static final int STATUS_IDLE      = 0;
  private static final int STATUS_RECORD    = 1;
  private static final int STATUS_PLAY      = 2;
  private int mAction = 0;

  private MediaPlayer   mMP;
  private MediaRecorder mMR;

  private MyStateBox mBtnPlay;
  private MyStateBox mBtnRec;
  private MyStateBox mBtnDelete;

  private Button mBtnConfirm;
  // private Button mBtnClose;

  private final IAudioInserter mParent;
  private final long mBid;        // data block ID - each block has only one audio file
  private final String mFilepath; // pathname of audio-file - 
  private boolean hasFile;
  private boolean hadNoFile;
  private boolean canRec;
  private boolean canPlay;
  private DBlock  mBlk;
  private int isRecPlay; // 0: idle, 1: rec, 2: play
  // AudioInfo mAudio;

  /** cstr
   * @param ctx    context
   * @param parent parent window
   * @param bid    block Id
   * @param blk    data block
   */
  AudioDialog( Context ctx, IAudioInserter parent, long bid, DBlock blk )
  {
    super( ctx, R.string.AudioDialog );

    mParent = parent;
    mBid = bid;
    // mAudio = mApp.mData.getAudio( TDInstance.sid, mBid );
    mFilepath = TDPath.getSurveyWavFile( TDInstance.survey, Long.toString(mBid) );
    hasFile   = (TDFile.getTopoDroidFile( mFilepath )).exists();
    hadNoFile = ! hasFile;
    mBlk      = blk;
    isRecPlay = STATUS_IDLE;
    // TDLog.v( "audio dialog " + bid + " file: " + mFilepath );
  }


  @Override
  protected void onCreate( Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.audio_dialog, R.string.title_audio );

    LinearLayout layout2 = (LinearLayout) findViewById( R.id.layout2 );
    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    layout2.setMinimumHeight( size + 40 );
    LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 10, 10, 20, 20 );

    // mBtnDelete = (Button) findViewById( R.id.audio_delete );
    // mBtnClose = (Button) findViewById( R.id.audio_close );
    // mBtnClose.setOnClickListener( this );
    ( (Button) findViewById( R.id.audio_close ) ).setOnClickListener( this );
    if ( mBlk != null ) {
      ( (TextView) findViewById( R.id.audio_id ) ).setText( String.format( mContext.getResources().getString( R.string.audio_id_shot ), mBlk.mFrom, mBlk.mTo ) );
    } else { 
      ( (TextView) findViewById( R.id.audio_id ) ).setText( String.format( mContext.getResources().getString( R.string.audio_id_plot ), mBid ) );
    }

    mBtnRec    = new MyStateBox( mContext, R.drawable.iz_audio_rec, R.drawable.iz_audio_rec_on );
    mBtnPlay   = new MyStateBox( mContext, R.drawable.iz_audio_play_off, R.drawable.iz_audio_play, R.drawable.iz_audio_stop );
    mBtnDelete = new MyStateBox( mContext, R.drawable.iz_audio_delete, R.drawable.iz_audio_delete );
    // mBtnConfirm = new Button( mContext );
    mBtnConfirm = (Button) findViewById( R.id.audio_confirm );

    mBtnRec.setOnClickListener( this );
    mBtnPlay.setOnClickListener( this );
    mBtnDelete.setOnClickListener( this );
    mBtnConfirm.setOnClickListener( this );
    mBtnConfirm.setText( R.string.audio_paused );
    mAction = ACTION_NONE;

    canRec  = true;
    canPlay = hasFile;
    mBtnRec.setState( 0 );
    mBtnPlay.setState( hasFile ? 1 : 0 );

    layout2.addView( mBtnPlay, lp );
    layout2.addView( mBtnRec, lp );
    layout2.addView( mBtnDelete, lp );
    // layout2.addView( mBtnConfirm, lp );
    layout2.invalidate();
  }

  private void stopRecPlay()
  {
    if ( isRecPlay == STATUS_RECORD ) {
      stopRec();
    } else if ( isRecPlay == STATUS_PLAY ) {
      stopPlay();
    }
  }

  /** implements user taps
   * @param v   tapped view
   */
  public void onClick(View v) 
  {
    if ( v.getId() == R.id.audio_close ) {
      stopRecPlay();
      dismiss();
    }
    try {
      MyStateBox b = (MyStateBox)v;
      if ( b == mBtnDelete ) {
	// TDLog.v( "audio delete: is rec " + isRecPlay );
        stopRecPlay();
        if ( hasFile ) { // delete audio file
	  // TDLog.v( "audio delete ask confirm");
          mAction = ACTION_DELETE;
          mBtnConfirm.setText( R.string.audio_delete );
          return;
        }
	// TDLog.v( "audio delete has no file");
      } else if ( b == mBtnPlay ) {
	// TDLog.v( "audio play: is rec " + isRecPlay );
        if ( isRecPlay != STATUS_IDLE ) return;
        mAction = ACTION_NONE;
        mBtnConfirm.setText( R.string.audio_paused );
        if ( canPlay ) {
          int sp = mBtnPlay.getState();
          if ( sp == 2 ) {
	    // TDLog.v( "audio play stop");
            stopPlay();
          } else if ( sp == 1 ) {
	    // TDLog.v( "audio play start");
            startPlay();
          }
        // } else {
	//   TDLog.v( "audio play cannot play");
	}
        return;
      } else if ( b == mBtnRec ) {
	// TDLog.v( "audio rec: is rec " + isRecPlay );
        if ( canRec ) {
          int sr = mBtnRec.getState();
          if ( sr == 1 ) {
	    // TDLog.v( "audio record stop");
            if ( isRecPlay != STATUS_RECORD ) return;
            stopRec();
          } else if ( sr == 0 ) {
            if ( isRecPlay == STATUS_RECORD ) {
              return;
            } else if ( isRecPlay == STATUS_PLAY ) {
              stopPlay();
            }
            if ( hasFile ) {
	      // TDLog.v( "audio record ask overwrite");
              mAction = ACTION_OVERWRITE;
              mBtnConfirm.setText( R.string.audio_overwrite );
            } else {
	      // TDLog.v( "audio record start");
              startRec();
            }
          }
	// } else {
	//   TDLog.v( "audio record cannot record");
        }
        return;
      }
    } catch ( ClassCastException e ) { /* THIS IS OK */ }
    if ( isRecPlay == STATUS_RECORD ) return; // no action while recording
    // TDLog.v( "audio action: " + mAction + " is rec " + isRecPlay );
    if ( mAction > ACTION_NONE ) {
      try {
        if ( (Button)v == mBtnConfirm ) {
          if ( mAction == ACTION_DELETE ) {
	    // TDLog.v( "audio delete");
            deleteAudio();
          } else if ( mAction == ACTION_OVERWRITE ) {
	    // TDLog.v( "audio overwrite start record");
            startRec();
            return;
          }
        // } else {
	//   TDLog.v( "audio confirm undefined");
	}
      } catch ( ClassCastException e ) { /* THIS IS OK */ }
    }
    // TDLog.v( "audio dismiss");
    dismiss();
  }

  @Override
  public void onBackPressed()
  {
    stopRecPlay();
    if ( hadNoFile && hasFile ) deleteAudio();
    super.onBackPressed();
  }

  /** delete audio file
   * @note called only with isRecPlay == STATUS_IDLE
   */
  private void deleteAudio()
  {
    assert( isRecPlay == STATUS_IDLE );
    TDFile.deleteFile( mFilepath );
    TopoDroidApp.mData.deleteAudio( TDInstance.sid, mBid );
    if ( mParent != null ) mParent.deletedAudio( mBid );
  }

  /** start recording audio
   * @note called only with isRecPlay == STATUS_IDLE
   */
  private void startRec()
  {
    assert( isRecPlay == STATUS_IDLE );
    try {
      isRecPlay = STATUS_RECORD;
      // if ( mParent != null ) mParent.startRecordAudio( mBid ); // startRecordAudio has empty implementation
      mMR = new MediaRecorder();
      mMR.setAudioSource(MediaRecorder.AudioSource.MIC);
      mMR.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
      // mMR.setAudioEncoder(MediaRecorder.OutputFormat.DEFAULT); // AMR_NB
      mMR.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT); // AMR_NB
      mMR.setOutputFile( mFilepath );
      mMR.prepare();
      canPlay = false;
      mBtnRec.setState( 1 );
      mAction = ACTION_NONE;
      mBtnConfirm.setText( R.string.audio_recording );
      mMR.start();
    } catch ( IllegalStateException e ) {
      TDLog.Error("Illegal State " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error("I/O error " + e.getMessage() );
    }
  }

  /** stop recording audio
   */
  private void stopRec()
  {
    assert( isRecPlay == STATUS_RECORD );
    try {
      mMR.stop();
      mMR.release();
      mMR = null;
      mBtnRec.setState( 0 );
      mBtnPlay.setState( 1 );
      mAction = ACTION_NONE;
      mBtnConfirm.setText( R.string.audio_paused );
      canPlay = true;
      hasFile = true;
      TopoDroidApp.mData.setAudio( TDInstance.sid, mBid, TDUtil.currentDateTime() );
      if ( mParent != null ) mParent.stopRecordAudio( mBid );
      isRecPlay = STATUS_IDLE;
    } catch ( IllegalStateException e ) {
      TDLog.Error("Illegal state " + e.getMessage() );
    } catch ( RuntimeException e ) {
      TDLog.Error("Runtime error " + e.getMessage() );
    }
  }

  /** start playing audio
   * @note called only when isRecPlay == STATUS_IDLE
   */
  private void startPlay()
  {
    assert( isRecPlay == STATUS_IDLE );
    try {
      isRecPlay = STATUS_PLAY;
      mMP = new MediaPlayer();
      mMP.setDataSource( mFilepath );
      mMP.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion( MediaPlayer mp ) 
        { 
          mp.release();
          canRec = true;
          mBtnPlay.setState( 1 );
          mBtnConfirm.setText(  R.string.audio_paused );
        }
      } );
      mMP.prepare();
      canRec = false;
      mBtnPlay.setState( 2 );
      mAction = ACTION_NONE;
      mBtnConfirm.setText(  R.string.audio_playing );
      mMP.start();
    } catch ( IllegalStateException e ) {
      TDLog.Error("Illegal state " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error("I/O error " + e.getMessage() );
    }
  }

  /** stop playing audio
   * @note called only when isRecPlay == STATUS_PLAY
   */
  private void stopPlay()
  {
    assert( isRecPlay == STATUS_PLAY );
    try {
      canRec = true;
      if ( mMP != null ) {
        mMP.stop();
	mMP.release();
	mMP = null;
      }
      mAction = ACTION_NONE;
      mBtnConfirm.setText( R.string.audio_paused );
      mBtnPlay.setState( 1 );
      isRecPlay = STATUS_IDLE;
    } catch ( IllegalStateException e ) {
      TDLog.Error("Illegal state " + e.getMessage() );
    // } catch ( RuntimeException e ) {
    //   TDLog.Error("Runtime error " + e.getMessage() );
    }
  }

}

