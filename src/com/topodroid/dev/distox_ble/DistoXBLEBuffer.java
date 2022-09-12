package com.topodroid.dev.distox_ble;

import com.topodroid.dev.distox_ble.DistoXBLEBuffer;

import java.util.Arrays;

public class DistoXBLEBuffer {
    byte[] data;
    //int type;
    DistoXBLEBuffer next;

    DistoXBLEBuffer( byte[] bytes )
    {
        data = Arrays.copyOfRange( bytes, 0, bytes.length );
        //type = t;
        next = null;
    }
}
