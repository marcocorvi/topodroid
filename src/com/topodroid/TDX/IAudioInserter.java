/* @file IAudioInserter.java
 *
 * @author marco corvi
 * @date june 2017
 *
 * @brief TopoDroid audio parent interfare
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

interface IAudioInserter
{
    void deletedAudio( long bid );
    // void startRecordAudio( long bid );
    void stopRecordAudio( long bid );
}
