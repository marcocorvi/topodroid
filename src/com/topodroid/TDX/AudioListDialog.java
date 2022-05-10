/* @file AudioListDialog.java
 *
 * @author marco corvi
 * @date jul 2020
 *
 * @brief TopoDroid audio list dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.ui.MyDialog;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;

import android.os.Bundle;
import android.content.Context;

import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import android.view.View;
// import android.view.View.OnClickListener;
import android.media.MediaPlayer;

import java.util.List;
import java.util.ArrayList;

import java.io.IOException;

class AudioListDialog extends MyDialog
                  implements OnItemClickListener
                  // , OnClickListener
                  , OnItemLongClickListener
{
  private ShotWindow mParent;
  private ListView mList;
  // private Button   mButtonCancel;

  private List< AudioInfo > mAudios;
  private List< AudioInfo > mSurveyAudios;
  private List< DBlock > mShots;
  private MediaPlayer mMP = null;

  /** cstr
   * @param context   context
   * @param perent    parent window
   * @param audios    audio list
   * @param shots     data list
   */
  AudioListDialog( Context context, ShotWindow parent, List< AudioInfo > audios, List< DBlock > shots )
  {
    super( context, R.string.AudioListDialog );
    mParent = parent;
    mSurveyAudios = audios;
    mShots  = shots;
  }


  /** @return description from an audio info
   * @param info   audio info
   */
  private String getAudioDescription( AudioInfo audio )
  {
    if ( audio.fileIdx >= 0 ) {
      for ( DBlock blk : mShots ) if ( blk.mId == audio.fileIdx ) {
        return audio.getFullString( blk.mFrom + " " + blk.mTo );
      }
    }
    // return audio.getFullString( "- -" );
    return null;
  }

  /** @return data block from an audio info
   * @param info   audio info
   */
  private DBlock getAudioBlock( AudioInfo audio )
  {
    if ( audio.fileIdx >= 0 ) {
      for ( DBlock blk : mShots ) if ( blk.mId == audio.fileIdx ) return blk;
    }
    return null;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout(R.layout.audio_list_dialog, R.string.title_audio_list );

    mList = (ListView) findViewById( R.id.list );
    mList.setOnItemClickListener( this );
    mList.setOnItemLongClickListener( this );
    mList.setDividerHeight( 2 );
    // ArrayList< String > names = new ArrayList<>();
    mAudios = new ArrayList< AudioInfo >();
    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>( mContext, R.layout.message );
    for ( AudioInfo af : mSurveyAudios ) { 
      // names.add( getAudioDescription( af );
      String desc = getAudioDescription( af );
      if ( desc != null ) {
        arrayAdapter.add( desc );
        mAudios.add( af );
      }
    }
    mList.setAdapter( arrayAdapter );
    
    ( (Button) findViewById(R.id.button_back ) ).setOnClickListener( new View.OnClickListener() {
      @Override public void onClick(View v) 
      {
        // TDLog.v( "audio list on click");
        dismiss();
      }
    } );
  }

  /** implements user tap on an item
   * @param parent    parent view
   * @param view      tapped view
   * @param pos       item position in the list
   * @param id        ...
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    // TDLog.v( "play audio at pos " + pos );
    playAudio( pos );
  }

  /** implements user long-tap on an item
   * @param parent    parent view
   * @param view      tapped view
   * @param pos       item position in the list
   * @param id        ...
   * @return true if tap has been handled
   */
  @Override
  public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
  {
    DBlock blk = getAudioBlock( mAudios.get( pos ) );
    if ( blk == null ) return false;
    mParent.startAudio( blk );
    return true;
  }

  /** play an audio file
   * @param pos   position of the audio in the list
   */
  private void playAudio( int pos )
  {
    AudioInfo audio = mAudios.get( pos );
    if ( audio != null ) { 
      String subdir = TDInstance.survey + "/audio"; // "audio/" + TDInstance.survey;
      String name   = Long.toString( audio.fileIdx ) + ".wav";
      if ( TDFile.hasMSfile( subdir, name ) ) { // if ( file.exists() )
        String filepath = TDPath.getSurveyWavFile( TDInstance.survey, Long.toString( audio.fileIdx ) );
        startPlay( filepath );
      // } else {
      //   // TDLog.Error("audio file does not exist");
      }
    // } else {
    //   // TDLog.Error("null audio info");
    }
  }

  /** start playing audio
   * @param filepath   file pathname
   */
  private void startPlay( String filepath )
  {
    // TDToast.make( String.format( mContext.getResources().getString( R.string.playing ), filepath ) );
    try {
      mMP = new MediaPlayer();
      mMP.setDataSource( filepath );
      mMP.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion( MediaPlayer mp ) 
        { 
          mp.release();
          mMP = null;
        }
      } );
      mMP.prepare();
      mMP.start();
    } catch ( IllegalStateException e ) {
      TDLog.Error("Illegal state " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error("I/O error " + e.getMessage() );
    }
  }

  /** implements user back-press
   */
  @Override
  public void onBackPressed()
  {
    releaseMP();
    super.onBackPressed();
  }

  /** release the media player
   */
  private void releaseMP()
  {
    if ( mMP != null ) {
      mMP.stop();
      mMP.release();
      mMP = null;
    }
  }

}

