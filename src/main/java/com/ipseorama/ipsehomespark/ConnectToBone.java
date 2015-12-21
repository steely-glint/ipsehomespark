/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipseorama.ipsehomespark;

import com.ipseorama.base.certHolders.CertHolder;
import com.ipseorama.base.certHolders.JksCertMaker;
import com.ipseorama.base.dataChannel.CandidateSender;
import com.ipseorama.base.dataChannel.IceConnectJSON;
import com.ipseorama.sctp.AssociationListener;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.phono.srtplight.Log;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;

/**
 *
 * @author Westhawk Ltd<thp@westhawk.co.uk>
 */
public class ConnectToBone
        extends WebSocketAdapter implements CandidateSender {

    private final CountDownLatch closeLatch;
    private final String myAddress;
    private final JksCertMaker cert;
    private byte[] nonce;
    private IceConnectJSON ic;
    private WebSocket ws;
    private SecureRandom r;

    public ConnectToBone(CertHolder c) throws IOException {
        this.closeLatch = new CountDownLatch(1);
        cert = (JksCertMaker) c;
        myAddress = c.getPrint(false);
        r = new SecureRandom();
        nonce = new byte[16];
        r.nextBytes(nonce);
    }

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
        return this.closeLatch.await(duration, unit);
    }

    public void onClose(int statusCode, String reason) {
        System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
        this.closeLatch.countDown();
    }

    static String getHex(byte[] in) {
        char cmap[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuffer ret = new StringBuffer();
        int top = in.length;
        for (int i = 0; i < top; i++) {
            ret.append(cmap[0x0f & (in[i] >>> 4)]);
            ret.append(cmap[in[i] & 0x0f]);
        }
        return ret.toString();
    }

    String buildQRText() {
        String all = myAddress + ":" + getHex(nonce);
        return all;
    }

    @Override
    public void onConnected(WebSocket websocket,
            Map<String, List<String>> headers) {
        ws = websocket;
        try {
            Log.debug("Got connect: " + websocket.getURI().toASCIIString());
            cert.listFriends();

            final String far = cert.getFriendPrint("master");
            short port = (short) (10000 + (r.nextInt() % 5000));
            Log.debug("Allocating port :" + port);

            ic = new IceConnectJSON(port, cert) {
                public JsonObject mkOffer() {
                    this.setFarFingerprint(far);
                    return super.mkOffer();
                }
            };
            ic.setSession("new");
            ic.setMid("data");

            ic.cleanup = new Runnable() {
                public void run() {
                    Log.debug("Releasing port :");
                }
            };

            AssociationListener al = new BoneAss();
            ic.setAssociationListener(al);
            JsonObject joff = ic.mkOffer();
            sendJson(joff);
        } catch (Exception ex) {
            Log.debug("failed to find 'bone' " + ex);
            ex.printStackTrace();

        }
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) {
        Log.error("got error " + cause.toString());
    }

    public void sendCandidate(JsonObject jc) {
        sendJson(jc);
    }

    public void sendJson(JsonObject jo) {
        StringWriter stWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject(jo);
        }
        String jsonData = stWriter.toString();
        Log.debug("sending : " + jsonData);
        ws.sendText(jsonData);
    }

    @Override
    public void onTextMessage(WebSocket websocket, String msg) {
        StringReader s = new StringReader(msg);
        JsonReader reader = Json.createReader(s);
        JsonObject message = (JsonObject) reader.read();
        System.out.printf("Got msg: %s%n", message.toString());
        String from = message.getString("from", "unknown");
        StringBuffer fc = new StringBuffer();
        for (int i = 0; i < from.length(); i += 2) {
            fc.append(from.substring(i, i + 2));
            if (i < from.length() - 2) {
                fc.append(":");
            }
        }
        String far = fc.toString();
        try {
            boolean from_friend = cert.isAFriendPrint(far);
            if (from_friend) {
                if (message.containsKey("sdp")) {
                    ic.setAnswer(message);
                    ic.startIce(this);
                }
                if (ic != null) {
                    String type = message.getString("type", "unknown");
                    if (type.equalsIgnoreCase("candidate")) {
                        try {
                            ic.addRemoteCandidate(message);
                        } catch (Exception ex) {
                            Log.error("problem with candidate " + ex.toString());
                            ex.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.error("problem with ice " + ex.toString());
            ex.printStackTrace();
        }
    }

    private boolean checkNonsense(JsonObject message) {
        /*String to = message.getString("to");
         String from = message.getString("from");
         String nonsense = message.getString("nonsense");
         String nonceS = getHex(nonce);
         //                 nonsense = sha256.hash(that.toFinger +":"+that.nonceS+":"+that.myFinger).toUpperCase();
         String sense = to + ":" + nonceS + ":" + from;
         Log.debug("My sense " + sense);

         byte enc[] = sense.getBytes();
         SHA256Digest d = new SHA256Digest();
         d.update(enc, 0, enc.length);
         byte[] result = new byte[d.getDigestSize()];
         d.doFinal(result, 0);
         String myNonsense = getHex(result);
         Log.debug("Your Nonsense " + nonsense);
         Log.debug("My   Nonsense " + myNonsense);

         return myNonsense.equals(nonsense);*/
        return true;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String destUri = "ws://localhost:9000/websocket/?finger=";
        if (args.length > 0) {
            destUri = args[0];
        }
        Log.setLevel(Log.VERB);
        while (true) {
            WebSocketFactory factory = new WebSocketFactory();
            WebSocket ws = null;

            try {
                CertHolder c = new JksCertMaker();
                ((JksCertMaker) c).listFriends();
                String myfinger = c.getPrint(false);
                destUri = destUri.concat(myfinger);
                Log.debug("my finger is :" + myfinger);
                ConnectToBone socket = new ConnectToBone(c);
                URI dcUri = new URI(destUri);
                ws = factory.createSocket(dcUri, 60000);
                Log.debug("ws URI is : " + dcUri.toASCIIString());
                ws.addListener(socket);
                ws.connect();
                Log.debug("Connected to : " + dcUri);
                socket.awaitClose(600, TimeUnit.SECONDS);
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                try {
                    ws.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
