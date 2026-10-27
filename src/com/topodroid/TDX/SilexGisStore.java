/* @file SilexGisStore.java
 *
 * @author apgeo
 * @date oct 2026
 *
 * @brief TopoDroid upload to a SilexGIS server: what is remembered between uploads
 *
 * The switches of the last upload are pre-set in the next one (the settings give them
 * only the first time), and each TopoDroid survey remembers where it was uploaded:
 * the survey of the cave the server answered, so that a new upload is a new version of it.
 * The class uses only java and org.json, so that it can be tested on the JVM.
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import org.json.JSONObject;
import org.json.JSONException;

class SilexGisStore
{
  static final String KEY_SWITCHES = "SILEXGIS_LAST_SWITCHES";
  static final String KEY_TARGET   = "SILEXGIS_TARGET_"; // followed by the survey name
  static final String KEY_LINKS    = "SILEXGIS_LINKS";   // what the server last said of the links of the account

  /** the key-value storage (the app shared preferences)
   */
  interface Prefs
  {
    /** @return the stored value, null if none
     * @param key   key
     */
    String get( String key );

    /** store a value
     * @param key    key
     * @param value  value (null to remove it)
     */
    void put( String key, String value );
  }

  private final Prefs mPrefs;

  /** cstr
   * @param prefs   storage
   */
  SilexGisStore( Prefs prefs ) { mPrefs = prefs; }

  /** @return the upload to pre-set for a survey: the last switches (else the defaults) and the survey target (else automatic)
   * @param survey    survey name
   * @param defaults  switches of the settings, used if there is no last upload
   */
  SilexGisUpload load( String survey, SilexGisUpload defaults )
  {
    SilexGisUpload ret = ( defaults == null )? new SilexGisUpload() : defaults.copy();
    JSONObject switches = parse( mPrefs.get( KEY_SWITCHES ) );
    if ( switches != null ) ret.switchesFromJson( switches );
    ret.presetLinks( loadLinks(), switches == null || ! switches.has( "link" ) ); // the first time, the account's audience
    ret.targetFromJson( parse( mPrefs.get( targetKey( survey ) ) ) ); // automatic if none
    return ret;
  }

  /** remember what the server says of the links of the account, for the next export dialog
   * @param caps   capabilities of the server (null: nothing changes)
   */
  void saveLinks( JSONObject caps )
  {
    if ( caps == null ) return;
    JSONObject links = SilexGisUploader.publicLinks( caps );
    mPrefs.put( KEY_LINKS, ( links == null )? null : links.toString() );
  }

  /** @return what the server last said of the links of the account: { enabled, defaultAudience }, null if not known
   */
  JSONObject loadLinks() { return parse( mPrefs.get( KEY_LINKS ) ); }

  /** @return true if the link choice of an upload has been remembered
   */
  boolean hasLinkChoice()
  {
    JSONObject switches = parse( mPrefs.get( KEY_SWITCHES ) );
    return switches != null && switches.has( "link" );
  }

  /** remember an upload: its switches as the last ones, and its target for the survey
   * @param survey   survey name
   * @param upload   upload
   */
  void save( String survey, SilexGisUpload upload )
  {
    if ( upload == null ) return;
    mPrefs.put( KEY_SWITCHES, upload.switchesToJson().toString() );
    saveTarget( survey, upload );
  }

  /** remember the target of a survey
   * @param survey   survey name
   * @param upload   upload
   */
  void saveTarget( String survey, SilexGisUpload upload )
  {
    if ( survey == null || upload == null ) return;
    if ( upload.target == SilexGisUpload.TARGET_AUTO ) {
      mPrefs.put( targetKey( survey ), null );
    } else {
      mPrefs.put( targetKey( survey ), upload.targetToJson().toString() );
    }
  }

  /** remember the survey of the cave the server answered for an upload of a survey
   * @param survey      survey name
   * @param id          cave-survey id
   * @param title       survey title
   * @param cave_id     cave id (can be null)
   * @param cave_name   cave name (can be null)
   * @note a later upload of the survey is a new version of that survey of the cave, unless another target is picked
   */
  void saveAnswer( String survey, String id, String title, String cave_id, String cave_name )
  {
    if ( survey == null || id == null || id.isEmpty() ) return;
    SilexGisUpload upload = new SilexGisUpload();
    upload.setCaveSurvey( id, title, cave_id, cave_name );
    saveTarget( survey, upload );
  }

  /** @return the remembered target of a survey, null if none
   * @param survey   survey name
   */
  SilexGisUpload loadTarget( String survey )
  {
    JSONObject json = parse( mPrefs.get( targetKey( survey ) ) );
    if ( json == null ) return null;
    SilexGisUpload ret = new SilexGisUpload();
    ret.targetFromJson( json );
    return ( ret.target == SilexGisUpload.TARGET_AUTO )? null : ret;
  }

  /** @return the key of the target of a survey
   * @param survey   survey name
   */
  static String targetKey( String survey ) { return KEY_TARGET + ( ( survey == null )? "" : survey ); }

  /** @return the JSON object of a text, null if the text is not a JSON object
   * @param text   text
   */
  private static JSONObject parse( String text )
  {
    if ( text == null || text.isEmpty() ) return null;
    try {
      return new JSONObject( text );
    } catch ( JSONException e ) {
      return null;
    }
  }

}
