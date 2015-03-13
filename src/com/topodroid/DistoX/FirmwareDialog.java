/** @file FirmwareDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX X310 device firmware dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 201312   created to distinguish from A3 memory dialog
 * 20140416 setError for required EditText inputs
 * 20140719 save dump to file
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.IOException;
// import java.io.StringWriter;
// import java.io.PrintWriter;

import android.os.Bundle;
import android.app.Dialog;

// import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;

import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.EditText;
// import android.widget.TextView;
import android.widget.Toast;

import android.text.method.KeyListener;

import android.util.Log;

class FirmwareDialog extends Dialog
                             implements View.OnClickListener
{
  private RadioButton mBtnDump;
  private RadioButton mBtnUpload;
  private Button mBtnOK;
  // private Button mBtnClose;

  private EditText mETfile;

  Context mContext;
  DeviceActivity mParent;
  TopoDroidApp   mApp;
  KeyListener    mETkeyListener;

  FirmwareDialog( Context context, DeviceActivity parent, TopoDroidApp app )
  {
    super( context );
    mContext = context;
    mParent  = parent;
    mApp     = app;
  }

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    setContentView( R.layout.firmware_dialog );

    mETfile  = (EditText) findViewById( R.id.firmware_file );

    mBtnUpload = (RadioButton) findViewById(R.id.firmware_upload );
    mBtnDump   = (RadioButton) findViewById(R.id.firmware_dump );
    mBtnOK = (Button) findViewById(R.id.firmware_ok);
    // mBtnClose = (Button) findViewById(R.id.firmware_close);

    mETkeyListener = mETfile.getKeyListener();
    mETfile.setOnClickListener( this );
    // mETfile.setEnabled( false );
    mETfile.setFocusable( false );
    mETfile.setFocusableInTouchMode( false );
    // mETfile.setClickable( true );
    mETfile.setKeyListener( null );

    mBtnUpload.setOnClickListener( this );
    mBtnDump.setOnClickListener( this );
    mBtnOK.setOnClickListener( this );
    // mBtnClose.setOnClickListener( this );
    
    setTitle( mParent.getResources().getString( R.string.firmware_title ) );
  }

  void setFile( String filename )
  {
    mETfile.setText( filename );
  }

  @Override
  public void onClick( View view )
  {
    switch ( view.getId() ) {
      case R.id.firmware_file:
        if ( mBtnUpload.isChecked() ) {
          (new FirmwareFileDialog( mContext, this, mApp)).show(); // select file from bin directory
        }
        break;
      case R.id.firmware_upload:
        // mETfile.setEnabled( false );
        mETfile.setFocusable( false );
        mETfile.setFocusableInTouchMode( false );
        // mETfile.setClickable( true );
        mETfile.setKeyListener( null );
        break;
      case R.id.firmware_dump:
        // mETfile.setEnabled( true );
        mETfile.setFocusable( true );
        mETfile.setFocusableInTouchMode( true );
        // mETfile.setClickable( true );
        mETfile.setKeyListener( mETkeyListener );
        break;
      case R.id.firmware_ok:
        String filename = null;
        if ( mETfile.getText() != null ) { 
          filename = mETfile.getText().toString();
          if ( filename != null ) {
            filename = filename.trim();
            if ( filename.length() == 0 ) filename = null;
          }
        }
        if ( filename == null ) {
          Toast.makeText( mParent, mParent.getResources().getString(R.string.firmware_file_missing), Toast.LENGTH_SHORT).show();
          return;
        }
        if ( mBtnDump.isChecked() ) {
          TopoDroidLog.LogFile( "Firmware dump to " + filename );
          File fp = new File( TopoDroidPath.getBinFile( filename ) );
          if ( fp.exists() ) {
            Toast.makeText( mParent, mParent.getResources().getString(R.string.firmware_file_exists), Toast.LENGTH_SHORT).show();
            return;    
          }
          askDump( filename );
        } else if ( mBtnUpload.isChecked() ) {
          TopoDroidLog.LogFile( "Firmware upload from " + filename );
          File fp = new File( TopoDroidPath.getBinFile( filename ) );
          if ( ! fp.exists() ) {
            TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "inexistent upload firmware file " + filename );
            return;    
          }
          int fw = FirmwareUtils.readFirmwareFirmware( fp );
          // int hw = mApp.readFirmwareHardware();
          // TopoDroidLog.LogFile( "Firmware version " + fw + " Hardware version " + hw );
          // // Log.v( "DistoX", "HW " + hw + " FW " + fw );
          // // Toast.makeText( mParent, "HARDWARE " + hw, Toast.LENGTH_LONG ).show();
          // askUpload( filename, areCompatible(hw,fw) );
          askUpload( filename, (fw == 21 || fw == 22 || fw == 23) );
        }
        break;
    }
  }

  void askDump( final String filename )
  {
    String title = mParent.getResources().getString( R.string.ask_dump );
    new TopoDroidAlertDialog( mContext, mParent.getResources(), title,
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          TopoDroidLog.LogFile( "Firmware dumping to file " + filename );
          int ret = mApp.dumpFirmware( filename );
          TopoDroidLog.LogFile( "Firmware dump to " + filename + " result: " + ret );
          Toast.makeText( mParent, 
            String.format( mParent.getResources().getString(R.string.firmware_file_dumped), filename, ret ),
            Toast.LENGTH_SHORT).show();
          // finish(); 
        }
      }
    );
  }

  void askUpload( final String filename, final boolean compatible )
  {
    TopoDroidLog.LogFile( "FW/HW compatibility " + (compatible? "yes" : "no") );
    String title = mParent.getResources().getString( compatible? R.string.ask_upload : R.string.ask_upload_not_compatible );
    new TopoDroidAlertDialog( mContext, mParent.getResources(), title,
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          TopoDroidLog.LogFile( "Firmware uploading from file " + filename );
          int ret = mApp.uploadFirmware( filename );
          TopoDroidLog.LogFile( "Firmware upload result " + ret );
          Toast.makeText( mParent, 
            String.format( mParent.getResources().getString(R.string.firmware_file_uploaded), filename, ret ),
            Toast.LENGTH_SHORT).show();
          // finish(); 
        }
      }
    );
  }

}
