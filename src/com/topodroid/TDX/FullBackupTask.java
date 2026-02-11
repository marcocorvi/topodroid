/* @file FullBackupTask.java
 *
 * @author claude
 * @date dec 2024
 *
 * @brief TopoDroid full backup task - backs up all surveys and database
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDVersion;

import android.os.AsyncTask;
import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * AsyncTask to perform a full backup of all TopoDroid data.
 * Creates a ZIP file containing:
 * - distox14.sqlite (main database)
 * - All survey folders (tdr, photo, audio, etc.)
 * - manifest with backup info
 */
class FullBackupTask extends AsyncTask<Void, Integer, Boolean>
{
  private static final int BUF_SIZE = 4096;
  private byte[] mBuffer;

  private final Context mContext;
  private final Uri mUri;
  private String mBackupPath;
  private int mSurveyCount = 0;
  private String mErrorMessage = null;

  /**
   * Constructor
   * @param context  Application context
   * @param uri      Output URI for the backup file (null for default location)
   */
  FullBackupTask(Context context, Uri uri)
  {
    mContext = context;
    mUri = uri;
    mBuffer = new byte[BUF_SIZE];
  }

  @Override
  protected Boolean doInBackground(Void... voids)
  {
    TDLog.v("BACKUP starting full backup...");

    String basePath = TDPath.getPathBase();
    if (basePath == null) {
      mErrorMessage = "Base path is null";
      return false;
    }

    // Get list of all surveys
    List<String> surveys = TopoDroidApp.getSurveyNames();
    if (surveys == null) {
      surveys = new java.util.ArrayList<>();
    }

    ZipOutputStream zos = null;
    try {
      // Create backup filename with timestamp
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US);
      String timestamp = sdf.format(new Date());
      String backupName = "TopoDroid-backup-" + timestamp + ".zip";

      if (mUri != null) {
        // Use provided URI
        android.os.ParcelFileDescriptor pfd = com.topodroid.utils.TDsafUri.docWriteFileDescriptor(mUri);
        zos = new ZipOutputStream(new BufferedOutputStream(com.topodroid.utils.TDsafUri.docFileOutputStream(pfd)));
        mBackupPath = mUri.getLastPathSegment();
      } else {
        // Create in default location (Documents or TopoDroid folder)
        String zipDir = basePath;
        mBackupPath = zipDir + "/" + backupName;
        File zipFile = new File(mBackupPath);
        if (zipFile.getParentFile() != null && !zipFile.getParentFile().exists()) {
          zipFile.getParentFile().mkdirs();
        }
        zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(mBackupPath)));
      }

      // 1. Add manifest with backup info
      StringBuilder manifest = new StringBuilder();
      manifest.append("TopoDroid Full Backup\n");
      manifest.append("version ").append(TDVersion.string()).append("\n");
      manifest.append("date ").append(timestamp).append("\n");
      manifest.append("surveys ").append(surveys.size()).append("\n");
      for (String survey : surveys) {
        manifest.append("survey ").append(survey).append("\n");
      }

      ZipEntry manifestEntry = new ZipEntry("backup_manifest.txt");
      zos.putNextEntry(manifestEntry);
      zos.write(manifest.toString().getBytes("UTF-8"));
      zos.closeEntry();
      TDLog.v("BACKUP added manifest");

      // 2. Add main database
      String dbPath = TDPath.getDatabase();
      File dbFile = new File(dbPath);
      if (dbFile.exists()) {
        addFileToZip(zos, dbFile, "distox14.sqlite");
        TDLog.v("BACKUP added database");
      } else {
        TDLog.e("BACKUP database not found: " + dbPath);
      }

      // 3. Add each survey folder
      int progress = 0;
      mSurveyCount = surveys.size(); // Count all surveys from database
      for (String survey : surveys) {
        String surveyDir = TDPath.getSurveyDir(survey);
        File surveyFolder = new File(surveyDir);
        if (surveyFolder.exists() && surveyFolder.isDirectory()) {
          addDirectoryToZip(zos, surveyFolder, survey);
          TDLog.v("BACKUP added survey folder: " + survey);
        } else {
          TDLog.v("BACKUP survey without folder: " + survey);
        }
        progress++;
        publishProgress(progress, surveys.size());
      }

      // 4. Add thconfig folder (projects)
      String thconfigDir = TDPath.getTdconfigDir();
      File thconfigFolder = new File(thconfigDir);
      if (thconfigFolder.exists() && thconfigFolder.isDirectory()) {
        addDirectoryToZip(zos, thconfigFolder, "thconfig");
        TDLog.v("BACKUP added thconfig (projects)");
      }

      // 5. Add preferences
      String prefsXml = exportPreferencesToXml();
      if (prefsXml != null) {
        ZipEntry prefsEntry = new ZipEntry("preferences.xml");
        zos.putNextEntry(prefsEntry);
        zos.write(prefsXml.getBytes("UTF-8"));
        zos.closeEntry();
        TDLog.v("BACKUP added preferences");
      }

      zos.finish();
      TDLog.v("BACKUP completed: " + mSurveyCount + " surveys");
      return true;

    } catch (FileNotFoundException e) {
      mErrorMessage = "File not found: " + e.getMessage();
      TDLog.e("BACKUP " + mErrorMessage);
      return false;
    } catch (IOException e) {
      mErrorMessage = "IO error: " + e.getMessage();
      TDLog.e("BACKUP " + mErrorMessage);
      return false;
    } finally {
      if (zos != null) {
        try { zos.close(); } catch (IOException e) { /* ignore */ }
      }
    }
  }

  /**
   * Add a single file to the ZIP
   */
  private void addFileToZip(ZipOutputStream zos, File file, String entryName) throws IOException
  {
    BufferedInputStream bis = null;
    try {
      bis = new BufferedInputStream(new FileInputStream(file), BUF_SIZE);
      ZipEntry entry = new ZipEntry(entryName);
      zos.putNextEntry(entry);
      int count;
      while ((count = bis.read(mBuffer, 0, BUF_SIZE)) != -1) {
        zos.write(mBuffer, 0, count);
      }
      zos.closeEntry();
    } finally {
      if (bis != null) {
        try { bis.close(); } catch (IOException e) { /* ignore */ }
      }
    }
  }

  /**
   * Export SharedPreferences to XML string
   */
  private String exportPreferencesToXml()
  {
    try {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
      Map<String, ?> allPrefs = prefs.getAll();

      StringBuilder xml = new StringBuilder();
      xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      xml.append("<preferences>\n");

      for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();
        String type;
        String valueStr;

        if (value instanceof Boolean) {
          type = "boolean";
          valueStr = value.toString();
        } else if (value instanceof Integer) {
          type = "int";
          valueStr = value.toString();
        } else if (value instanceof Long) {
          type = "long";
          valueStr = value.toString();
        } else if (value instanceof Float) {
          type = "float";
          valueStr = value.toString();
        } else if (value instanceof String) {
          type = "string";
          valueStr = escapeXml((String) value);
        } else {
          continue; // Skip unknown types
        }

        xml.append("  <pref key=\"").append(escapeXml(key)).append("\" type=\"").append(type).append("\">");
        xml.append(valueStr);
        xml.append("</pref>\n");
      }

      xml.append("</preferences>\n");
      return xml.toString();
    } catch (Exception e) {
      TDLog.e("BACKUP failed to export preferences: " + e.getMessage());
      return null;
    }
  }

  /**
   * Escape special XML characters
   */
  private String escapeXml(String str)
  {
    if (str == null) return "";
    return str.replace("&", "&amp;")
              .replace("<", "&lt;")
              .replace(">", "&gt;")
              .replace("\"", "&quot;")
              .replace("'", "&apos;");
  }

  /**
   * Add a directory recursively to the ZIP
   */
  private void addDirectoryToZip(ZipOutputStream zos, File dir, String baseName) throws IOException
  {
    File[] files = dir.listFiles();
    if (files == null) return;

    for (File file : files) {
      String entryName = baseName + "/" + file.getName();
      if (file.isDirectory()) {
        addDirectoryToZip(zos, file, entryName);
      } else {
        addFileToZip(zos, file, entryName);
      }
    }
  }

  @Override
  protected void onProgressUpdate(Integer... values)
  {
    if (values.length >= 2) {
      // Could update a progress dialog here
      TDLog.v("BACKUP progress: " + values[0] + "/" + values[1]);
    }
  }

  @Override
  protected void onPostExecute(Boolean result)
  {
    if (result) {
      String msg = mContext.getResources().getString(R.string.backup_saved) +
                   " (" + mSurveyCount + " surveys)";
      TDToast.make(msg);
    } else {
      String msg = mContext.getResources().getString(R.string.backup_failed);
      if (mErrorMessage != null) {
        msg += ": " + mErrorMessage;
      }
      TDToast.makeBad(msg);
    }
  }

  /** @return the backup file path */
  String getBackupPath() { return mBackupPath; }

  /** @return the number of surveys backed up */
  int getSurveyCount() { return mSurveyCount; }
}
