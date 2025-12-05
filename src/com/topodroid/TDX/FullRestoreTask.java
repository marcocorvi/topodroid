/* @file FullRestoreTask.java
 *
 * @author claude
 * @date dec 2024
 *
 * @brief TopoDroid full restore task - restores all surveys and database from backup
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDsafUri;
import com.topodroid.utils.TDVersion;

import android.os.AsyncTask;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.app.Activity;

/**
 * AsyncTask to perform a full restore of all TopoDroid data from a backup.
 * Restores:
 * - distox14.sqlite (main database)
 * - All survey folders (tdr, photo, audio, etc.)
 */
class FullRestoreTask extends AsyncTask<Void, Integer, Boolean>
{
  private static final int BUF_SIZE = 4096;
  private byte[] mBuffer;

  private final Context mContext;
  private final TopoDroidApp mApp;
  private final Uri mUri;
  private int mSurveyCount = 0;
  private int mFileCount = 0;
  private String mErrorMessage = null;
  private boolean mDatabaseRestored = false;

  /**
   * Constructor
   * @param context  Application context
   * @param app      TopoDroidApp instance
   * @param uri      Input URI for the backup file
   */
  FullRestoreTask(Context context, TopoDroidApp app, Uri uri)
  {
    mContext = context;
    mApp = app;
    mUri = uri;
    mBuffer = new byte[BUF_SIZE];
  }

  @Override
  protected Boolean doInBackground(Void... voids)
  {
    TDLog.v("RESTORE starting full restore...");

    if (mUri == null) {
      mErrorMessage = "No backup file selected";
      return false;
    }

    String basePath = TDPath.getPathBase();
    if (basePath == null) {
      mErrorMessage = "Base path is null";
      return false;
    }

    // Close database BEFORE starting restore to avoid file locking issues
    try {
      if (TopoDroidApp.mData != null) {
        TopoDroidApp.mData.closeDatabase();
        TopoDroidApp.mData = null;
        TDLog.v("RESTORE closed database before restore");
      }
    } catch (Exception e) {
      TDLog.e("RESTORE error closing database: " + e.getMessage());
    }

    ZipInputStream zis = null;
    try {
      // Open the backup ZIP file
      ParcelFileDescriptor pfd = TDsafUri.docReadFileDescriptor(mUri);
      if (pfd == null) {
        mErrorMessage = "Cannot open backup file";
        return false;
      }
      InputStream is = TDsafUri.docFileInputStream(pfd);
      zis = new ZipInputStream(new BufferedInputStream(is, BUF_SIZE));

      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        String entryName = entry.getName();
        TDLog.v("RESTORE entry: " + entryName);

        if (entry.isDirectory()) {
          // Create directory
          File dir = new File(basePath, entryName);
          if (!dir.exists()) {
            dir.mkdirs();
          }
        } else {
          // Handle different file types
          if (entryName.equals("distox14.sqlite")) {
            // Restore database - needs special handling
            restoreDatabase(zis, basePath);
            mDatabaseRestored = true;
          } else if (entryName.equals("backup_manifest.txt")) {
            // Skip manifest, just read to count surveys
            readManifest(zis);
          } else if (entryName.equals("preferences.xml")) {
            // Restore preferences
            restorePreferences(zis);
          } else {
            // Restore regular file (survey data)
            restoreFile(zis, basePath, entryName);
            mFileCount++;

            // Count surveys by looking at top-level directories
            if (entryName.contains("/") && !entryName.startsWith("/")) {
              String surveyName = entryName.substring(0, entryName.indexOf("/"));
              // Survey count is tracked via manifest
            }
          }
        }
        zis.closeEntry();
        publishProgress(mFileCount);
      }

      TDLog.v("RESTORE completed: " + mFileCount + " files");
      return true;

    } catch (FileNotFoundException e) {
      mErrorMessage = "File not found: " + e.getMessage();
      TDLog.e("RESTORE " + mErrorMessage);
      return false;
    } catch (IOException e) {
      mErrorMessage = "IO error: " + e.getMessage();
      TDLog.e("RESTORE " + mErrorMessage);
      return false;
    } catch (Exception e) {
      mErrorMessage = "Error: " + e.getMessage();
      TDLog.e("RESTORE " + mErrorMessage);
      return false;
    } finally {
      if (zis != null) {
        try { zis.close(); } catch (IOException e) { /* ignore */ }
      }
    }
  }

  /**
   * Read manifest to get survey count
   */
  private void readManifest(ZipInputStream zis) throws IOException
  {
    StringBuilder sb = new StringBuilder();
    int count;
    while ((count = zis.read(mBuffer, 0, BUF_SIZE)) != -1) {
      sb.append(new String(mBuffer, 0, count, "UTF-8"));
    }
    String manifest = sb.toString();
    String[] lines = manifest.split("\n");
    for (String line : lines) {
      if (line.startsWith("surveys ")) {
        try {
          mSurveyCount = Integer.parseInt(line.substring(8).trim());
        } catch (NumberFormatException e) {
          // ignore
        }
      }
    }
    TDLog.v("RESTORE manifest: " + mSurveyCount + " surveys");
  }

  /**
   * Restore preferences from XML
   */
  private void restorePreferences(ZipInputStream zis) throws IOException
  {
    StringBuilder sb = new StringBuilder();
    int count;
    while ((count = zis.read(mBuffer, 0, BUF_SIZE)) != -1) {
      sb.append(new String(mBuffer, 0, count, "UTF-8"));
    }
    String xml = sb.toString();

    try {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
      SharedPreferences.Editor editor = prefs.edit();

      // Simple XML parsing - look for <pref key="..." type="...">value</pref>
      int pos = 0;
      while ((pos = xml.indexOf("<pref ", pos)) >= 0) {
        int endTag = xml.indexOf("</pref>", pos);
        if (endTag < 0) break;

        String tag = xml.substring(pos, endTag + 7);

        // Extract key
        int keyStart = tag.indexOf("key=\"") + 5;
        int keyEnd = tag.indexOf("\"", keyStart);
        String key = unescapeXml(tag.substring(keyStart, keyEnd));

        // Extract type
        int typeStart = tag.indexOf("type=\"") + 6;
        int typeEnd = tag.indexOf("\"", typeStart);
        String type = tag.substring(typeStart, typeEnd);

        // Extract value
        int valueStart = tag.indexOf(">") + 1;
        int valueEnd = tag.indexOf("</pref>");
        String value = tag.substring(valueStart, valueEnd);

        // Apply preference based on type
        switch (type) {
          case "boolean":
            editor.putBoolean(key, Boolean.parseBoolean(value));
            break;
          case "int":
            editor.putInt(key, Integer.parseInt(value));
            break;
          case "long":
            editor.putLong(key, Long.parseLong(value));
            break;
          case "float":
            editor.putFloat(key, Float.parseFloat(value));
            break;
          case "string":
            editor.putString(key, unescapeXml(value));
            break;
        }

        pos = endTag + 7;
      }

      editor.apply();
      TDLog.v("RESTORE preferences restored");
    } catch (Exception e) {
      TDLog.e("RESTORE failed to restore preferences: " + e.getMessage());
    }
  }

  /**
   * Unescape XML special characters
   */
  private String unescapeXml(String str)
  {
    if (str == null) return "";
    return str.replace("&lt;", "<")
              .replace("&gt;", ">")
              .replace("&quot;", "\"")
              .replace("&apos;", "'")
              .replace("&amp;", "&");
  }

  /**
   * Restore the database file
   * Note: Database should already be closed before calling this method
   */
  private void restoreDatabase(ZipInputStream zis, String basePath) throws IOException
  {
    String dbPath = TDPath.getDatabase();
    File dbFile = new File(dbPath);

    // Backup existing database just in case
    File backupDb = new File(dbPath + ".bak");
    if (dbFile.exists()) {
      if (backupDb.exists()) backupDb.delete();
      dbFile.renameTo(backupDb);
    }

    // Write new database
    BufferedOutputStream bos = null;
    try {
      bos = new BufferedOutputStream(new FileOutputStream(dbPath), BUF_SIZE);
      int count;
      while ((count = zis.read(mBuffer, 0, BUF_SIZE)) != -1) {
        bos.write(mBuffer, 0, count);
      }
      bos.flush();
      TDLog.v("RESTORE database written to: " + dbPath);
    } finally {
      if (bos != null) {
        try { bos.close(); } catch (IOException e) { /* ignore */ }
      }
    }
  }

  /**
   * Restore a regular file
   */
  private void restoreFile(ZipInputStream zis, String basePath, String entryName) throws IOException
  {
    File outFile = new File(basePath, entryName);

    // Create parent directories if needed
    File parent = outFile.getParentFile();
    if (parent != null && !parent.exists()) {
      parent.mkdirs();
    }

    BufferedOutputStream bos = null;
    try {
      bos = new BufferedOutputStream(new FileOutputStream(outFile), BUF_SIZE);
      int count;
      while ((count = zis.read(mBuffer, 0, BUF_SIZE)) != -1) {
        bos.write(mBuffer, 0, count);
      }
      bos.flush();
    } finally {
      if (bos != null) {
        try { bos.close(); } catch (IOException e) { /* ignore */ }
      }
    }
  }

  @Override
  protected void onProgressUpdate(Integer... values)
  {
    if (values.length >= 1) {
      TDLog.v("RESTORE progress: " + values[0] + " files");
    }
  }

  @Override
  protected void onPostExecute(Boolean result)
  {
    if (result) {
      // Don't try to reopen database here - just show toast and restart
      String msg = mContext.getResources().getString(R.string.restore_completed) +
                   " (" + mSurveyCount + " surveys, " + mFileCount + " files)";
      TDToast.make(msg);

      // Auto-restart after a short delay to let the toast show
      new android.os.Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          restartApp();
        }
      }, 1500);
    } else {
      String msg = mContext.getResources().getString(R.string.restore_failed);
      if (mErrorMessage != null) {
        msg += ": " + mErrorMessage;
      }
      TDToast.makeBad(msg);
    }
  }

  /**
   * Restart the application
   */
  private void restartApp()
  {
    if (mContext instanceof Activity) {
      Activity activity = (Activity) mContext;
      Intent intent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
      if (intent != null) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
        // Force process termination to ensure clean restart
        android.os.Process.killProcess(android.os.Process.myPid());
      }
    }
  }

  /** @return true if database was restored */
  boolean isDatabaseRestored() { return mDatabaseRestored; }

  /** @return the number of surveys restored */
  int getSurveyCount() { return mSurveyCount; }

  /** @return the number of files restored */
  int getFileCount() { return mFileCount; }
}
