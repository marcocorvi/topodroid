/* @file TDKey.java
 *
 * @author marco corvi
 * @date feb 2026
 *
 * @brief TopoDroid security key 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

import java.security.KeyStore;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import android.security.keystore.KeyProperties;
import android.security.keystore.KeyGenParameterSpec;

import android.util.Base64;

public class TDKey
{
  final static String ANDROID_KEY_STORE = "AndroidKeyStore";
  final static String GEMINI_KEY_ALIAS = "GeminiKeyAlias";

  private static void checkKey( KeyStore key_store ) throws Exception
  {
    if ( key_store.containsAlias( GEMINI_KEY_ALIAS ) ) return;
    KeyGenerator kg = KeyGenerator.getInstance( KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE );
    kg.init( new KeyGenParameterSpec.Builder( GEMINI_KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT )
              .setBlockModes( KeyProperties.BLOCK_MODE_CBC )
              .setEncryptionPaddings( KeyProperties.ENCRYPTION_PADDING_PKCS7 )
              .build() );
    kg.generateKey();
  }

  public static String encrypt( String plain ) throws Exception
  {
    KeyStore key_store = KeyStore.getInstance( ANDROID_KEY_STORE );
    key_store.load( null );
    checkKey( key_store );
    SecretKey sk = (SecretKey)key_store.getKey( GEMINI_KEY_ALIAS, null );

    Cipher cipher = Cipher.getInstance( "AES/CBC/PKCS7Padding" );
    cipher.init( Cipher.ENCRYPT_MODE, sk );
    byte[] iv = cipher.getIV();
    byte[] enc = cipher.doFinal( plain.getBytes("UTF-8") );
    return Base64.encodeToString( iv, Base64.DEFAULT ) + ":" + Base64.encodeToString( enc, Base64.DEFAULT );
  }

  public static String decrypt( String encrypted ) throws Exception 
  { 
    String[] parts = encrypted.split( ":" );
    byte[] iv = Base64.decode( parts[0], Base64.DEFAULT );
    byte[] enc = Base64.decode( parts[1], Base64.DEFAULT );
    
    KeyStore key_store = KeyStore.getInstance( ANDROID_KEY_STORE );
    key_store.load( null );
    checkKey( key_store );
    SecretKey sk = (SecretKey)key_store.getKey( GEMINI_KEY_ALIAS, null );

    Cipher cipher = Cipher.getInstance( "AES/CBC/PKCS7Padding" );
    IvParameterSpec spec = new IvParameterSpec( iv );
    cipher.init( Cipher.DECRYPT_MODE, sk, spec );
    return new String( cipher.doFinal( enc ), "UTF-8" );
  }
} 
