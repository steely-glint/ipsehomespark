package com.ipseorama.ipsehomespark;

import com.ciscospark.*;
import com.phono.srtplight.Log;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

class App {

    private final Spark _spark;
    String _analogFile;
    static String EMH = "LightAlarm";
    Timer _tick;

    App() {
        String accessToken = "N2JkZTVkYjQtMzdlZS00NWVmLTgxZGItZmJkY2E5ODJjNjZkYjVjNDkzYjgtMjFg";
        // To obtain a developer access token, visit http://developer.ciscospark.com
        Log.setLevel(Log.ALL);
        // Initialize the client
        Spark spark = Spark.builder()
                .baseUrl(URI.create("https://api.ciscospark.com/v1"))
                .accessToken(accessToken)
                .build();
        _spark = spark;
        Log.debug("Spark " + _spark.toString());

        _analogFile = "/sys/devices/ocp.3/helper.15/AIN1";
        Log.debug("Using " + _analogFile);
        _tick = new Timer();
        TimerTask t = mkTask();
        _tick.schedule(t, 1, 1000);
    }

    public void emergency(String volt) {
        // Create a new room
        // List the rooms that I'm in
        Room room = null;
        Message message = null;
        Iterator<Room> it = _spark.rooms().iterate();
        while (it.hasNext()) {
            Room r = it.next();
            if (r.getTitle().equalsIgnoreCase(EMH)) {
                room = r;
                break;
            }
        }
        if (room == null) {
            Log.debug("no room");
            room = new Room();
            room.setTitle("LightAlarm");
            room = _spark.rooms().post(room);
            Log.debug("made room");

            // Add a coworker to the room
            Membership membership = new Membership();
            membership.setRoomId(room.getId());
            membership.setPersonEmail("thp@westhawk.co.uk");
            _spark.memberships().post(membership);
            Log.debug("Added tim to room");

            // Post a text message to the room
            // Share a file with the room
            message = new Message();
            message.setRoomId(room.getId());
            message.setFiles(URI.create("https://upload.wikimedia.org/wikipedia/en/e/e1/TheDoctor.jpg"));
            _spark.messages().post(message);
            message = new Message();
            message.setRoomId(room.getId());
            message.setText("Please state the nature of your emergency.");
            _spark.messages().post(message);
            Log.debug("Added EMH");

        }
        message = new Message();
        message.setRoomId(room.getId());

        message.setText("Voltage is currently " + volt + "mV");
        _spark.messages().post(message);
        Log.debug("Posted voltage");

    }

    public static void main(String[] args) {
        App me = new App();
    }

    public String readVoltage() {
        String volt = "0";
        FileInputStream fin = null;
        try {
            File f = new File(_analogFile);
            fin = new FileInputStream(f);
            DataInputStream din = new DataInputStream(fin);
            volt = din.readLine();
            Log.debug("V = " + volt + "mv");
        } catch (Exception ex) {
            Log.error("Problem with " + _analogFile + " : " + ex.getMessage());
        } finally {
            try {
                fin.close();
            } catch (IOException ex) {
                Log.error("Problem with " + _analogFile + " : " + ex.getMessage());
            }
        }
        return volt;
    }

    private TimerTask mkTask() {
        return new TimerTask() {

            @Override
            public void run() {
                JsonObjectBuilder ret = Json.createObjectBuilder();
                try {
                    String v = readVoltage();
                    int volt = Integer.parseInt(v);
                    if ((volt > 1300) || (volt < 500)) {
                        emergency(v);
                    }
                } catch (Exception ex) {
                    Log.error("Json error" + ex.getMessage());
                    ex.printStackTrace();
                }
            }

        };
    }

}
