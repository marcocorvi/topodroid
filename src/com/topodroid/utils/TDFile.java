/** @file TDFile.java
 *
 * @author marco corvi
 * @date apr 2021
 *
 * @brief TopoDroid File layer
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

import com.topodroid.DistoX.TDInstance;

import java.io.File;
// import java.io.FileFilter;
// import java.io.FilenameFilter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TDFile
{
  public static File getManDir( )    { return new File( TDInstance.context.getFilesDir(), "man" ); }
  public static File getManFile( String name )    { return new File( getManDir(), name ); }
  public static boolean hasManFile( String name ) { return getManFile( name ).exists(); }
  public static FileReader getManFileReader( String name ) throws IOException { return new FileReader( getManFile(name) ); }
 
  public static boolean hasFile( String name ) { return name != null && (new File( name )).exists(); }

  public static long getFileLength( String name ) { return (name == null)? 0 : (new File(name)).length(); }

  // @param name     absolute filename
  public static File getExternalFile( String name ) { return new File( name ); }

  // @param name     TopoDroid-relative filename
  public static File getFile( String name ) { return new File( name ); }

  public static File getFile( String dirname, String name ) { return new File( dirname, name ); }

  // public static File getFile( File dir, String name )
  // {
  //   return new File( dir, name );
  // }

  public static FileInputStream getFileInputStream( String name ) throws IOException { return new FileInputStream( name ); }

  // public static FileInputStream getFileInputStream( File file ) throws IOException { return new FileInputStream( file ); }

  public static FileOutputStream getFileOutputStream( String name ) throws IOException { return new FileOutputStream( name ); }

  public static FileOutputStream getFileOutputStream( File file ) throws IOException { return new FileOutputStream( file ); }

  public static FileWriter getFileWriter( String name, boolean append ) throws IOException { return new FileWriter( name, append ); }

  public static FileWriter getFileWriter( String name ) throws IOException { return new FileWriter( name ); }

  public static FileWriter getFileWriter( File file ) throws IOException { return new FileWriter( file ); }

  public static FileReader getExternalFileReader( String dirname, String filename ) throws IOException
  {
    File file = new File( dirname, filename );
    return new FileReader( file );
  }

  public static FileReader getFileReader( String name ) throws IOException { return new FileReader( name ); }

  public static FileReader getFileReader( File file ) throws IOException { return new FileReader( file ); }

  public static void deleteFile( File f ) // DistoX-SAF
  {
    if ( f != null && f.exists() ) {
      if ( ! f.delete() ) TDLog.Error("file delete failed " + f.getName() );
    }
  }

  public static void deleteDir( File dir ) // DistoX-SAF
  {
    if ( dir != null && dir.exists() ) {
      File[] files = dir.listFiles();
      if ( files != null ) {
        for ( File file : files ) {
          if (file.isFile()) {
            if ( ! file.delete() ) TDLog.Error("file delete failed " + file.getName() ); 
          }
        }
      }
      if ( ! dir.delete() ) TDLog.Error("dir delete failed " + dir.getName() );
    }
  }

  public static void deleteFile( String pathname ) 
  { 
    deleteFile( getFile( pathname ) ); // DistoXFile;
  }

  public static void deleteDir( String dirname ) 
  { 
    deleteDir( getFile( dirname ) ); // DistoX-SAF
  }

  // @pre oldname exists && ! newname exists
  public static void renameFile( String oldname, String newname )
  {
    File f1 = getFile( oldname ); // DistoX-SAF
    File f2 = getFile( newname );
    if ( f1.exists() && ! f2.exists() ) {
      if ( ! f1.renameTo( f2 ) ) TDLog.Error("file rename: failed " + oldname + " to " + newname );
    } else {
      TDLog.Error("file rename: no-exist " + oldname + " or exist " + newname );
    }
  }

  // @pre oldname exists
  public static void moveFile( String oldname, String newname )
  {
    File f1 = getFile( oldname ); // DistoX-SAF
    File f2 = getFile( newname );
    if ( f1.exists() ) {
      if ( ! f1.renameTo( f2 ) ) TDLog.Error("file move: failed " + oldname + " to " + newname );
    } else {
      TDLog.Error("file move: no-exist " + oldname );
    }
  }

  public static void makeDir( String pathname )
  {
    File f = getFile( pathname ); // DistoX-SAF
    if ( f.exists() ) return;
    if ( ! f.isDirectory() ) {
      if ( ! f.mkdirs() ) TDLog.Error("Mkdir failed " + pathname );
    }
  }

} 
