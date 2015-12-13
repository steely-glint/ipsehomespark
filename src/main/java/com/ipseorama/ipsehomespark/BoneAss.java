/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipseorama.ipsehomespark;

import com.ipseorama.sctp.Association;
import com.ipseorama.sctp.AssociationListener;
import com.ipseorama.sctp.SCTPStream;
import com.ipseorama.sctp.SCTPStreamListener;
import com.phono.srtplight.Log;
import javax.json.Json;

/**
 *
 * @author tim
 */
class BoneAss implements AssociationListener {

    public BoneAss() {
    }

    @Override
    public void onDisAssociated(Association a) {
        Log.debug("Disassociated");
    }
    void sendcommand(String command,SCTPStream s){
        try {
            s.send(Json.createObjectBuilder().add("command", command).build().toString());
        } catch (Exception ex) {
            Log.debug("problem "+ex);
            ex.printStackTrace();
        }
    }
    @Override
    public void onAssociated(Association asctn) {
        try {

            SCTPStream st = asctn.mkStream(1000, "AIN1");
            SCTPStreamListener li = new SCTPStreamListener() {
                @Override
                public void onMessage(SCTPStream stream, String string) {
                    Log.debug("saw "+string);
                }
            };
            st.setSCTPStreamListener(li);
            sendcommand("start",st);
        } catch (Exception ex) {
            Log.debug("problem making AIN1" + ex);
        }
    }

    @Override
    public void onStream(SCTPStream stream) {
        Log.debug("made" + stream.getLabel());
    }

}
