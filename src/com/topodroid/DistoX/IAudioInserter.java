/** @file IAudioInserter.java
 *
 * @author marco corvi
 * @date june 2017
 *
 * @brief TopoDroid audio parent interfare
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

public interface IAudioInserter
{
    public void deletedAudio( long bid );
    public void startRecordAudio( long bid );
    public void stopRecordAudio( long bid );
}
