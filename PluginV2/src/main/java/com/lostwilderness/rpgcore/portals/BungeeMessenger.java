package com.lostwilderness.rpgcore.portals;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * Builds BungeeCord plugin message for Connect subchannel.
 */
public final class BungeeMessenger {

    public static byte[] createConnectMessage(String targetServer) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteOut);
            out.writeUTF("Connect");
            out.writeUTF(targetServer);
            return byteOut.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
