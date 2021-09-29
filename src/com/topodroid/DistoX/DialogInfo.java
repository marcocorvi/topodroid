/* @file DialogInfo.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D drawing infos dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.Locale;


class DialogInfo extends MyDialog 
                 implements OnItemClickListener
                 , View.OnClickListener
{
  // private Button mBtnOk;

  private TopoGL mApp;
  private TglParser mParser;
  private GlRenderer   mRenderer;

  private ArrayAdapter<String> mArrayAdapter;
  private ListView mList;
  private Button   mBTclose;


  public DialogInfo( TopoGL app, TglParser parser, GlRenderer renderer )
  {
    super( app, R.string.DialogInfo );
    mApp   = app;
    mParser = parser;
    mRenderer = renderer;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.cave3d_info_dialog, R.string.INFO );


    Resources res = mApp.getResources();

    TextView tv = ( TextView ) findViewById(R.id.info_grid);
    tv.setText( String.format(Locale.US, res.getString(R.string.info_grid_value),  mParser.getGridSize() ) );

    tv = ( TextView ) findViewById(R.id.info_azimuth);
    tv.setText( String.format(Locale.US, res.getString(R.string.info_view_value), mRenderer.getYAngle(), mRenderer.getXAngle() ) );

    tv = ( TextView ) findViewById(R.id.info_shot);
    tv.setText( String.format(Locale.US, res.getString(R.string.info_shot_value), mParser.getShotNumber(), mParser.getSplayNumber() ) );

    tv = ( TextView ) findViewById(R.id.info_station);
    tv.setText( String.format(Locale.US, res.getString(R.string.info_station_value), mParser.getStationNumber() ) );

    tv = ( TextView ) findViewById(R.id.info_survey);
    tv.setText( String.format(Locale.US, res.getString(R.string.info_survey_value), mParser.getSurveyNumber() ) );

    tv = ( TextView ) findViewById(R.id.info_length);
    tv.setText( String.format(Locale.US, res.getString(R.string.info_length_value), mParser.getCaveLength() ) );

    tv = ( TextView ) findViewById(R.id.info_depth);
    tv.setText( String.format(Locale.US, res.getString(R.string.info_depth_value), mParser.getCaveDepth() ) );

    tv = ( TextView ) findViewById(R.id.info_volume);
    tv.setText( String.format(Locale.US, res.getString(R.string.info_volume_value), mParser.getPowercrustVolume(), mParser.getConvexHullVolume() ) );

    tv = ( TextView ) findViewById(R.id.info_east);
    tv.setText( String.format(Locale.US, res.getString(R.string.info_east_value), mParser.emin, mParser.emax ) );

    tv = ( TextView ) findViewById(R.id.info_north);
    tv.setText( String.format(Locale.US, res.getString(R.string.info_north_value), mParser.nmin, mParser.nmax ) );

    tv = ( TextView ) findViewById(R.id.info_z);
    tv.setText( String.format(Locale.US, res.getString(R.string.info_z_value), mParser.zmin, mParser.zmax ) );

    int nr = mParser.getSurveyNumber();
    ListView mList = ( ListView ) findViewById(R.id.surveys_list );
    mArrayAdapter = new ArrayAdapter<String>( mApp, R.layout.message );
    ArrayList< Cave3DSurvey > surveys = mParser.getSurveys();
    if ( surveys != null ) {
      for ( Cave3DSurvey s : surveys ) {
        mArrayAdapter.add( s.name );
      }
    }
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    mBTclose = (Button) findViewById( R.id.button_close );
    mBTclose.setOnClickListener( this );
  }

  @Override
  public void onClick(View view)
  {
    // TDLog.v( "Info onClick()" );
    dismiss();
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String name = item.toString();
    Cave3DSurvey survey = mParser.getSurvey( name );
    if ( survey != null ) {
      ( new DialogSurvey( mApp, survey ) ).show();
    } else {
      // TODO Toast.makeText( );
    }
  }

}

