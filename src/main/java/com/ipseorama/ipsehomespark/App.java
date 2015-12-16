package com.ipseorama.ipsehomespark;

import com.ciscospark.*;
import com.phono.srtplight.Log;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

class App {

    private final Spark _spark;
    String _analogFile;
    String _tty;
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
        _tty = "/dev/ttyO4";
        Log.debug("Using " + _analogFile);
        _tick = new Timer();
        TimerTask t = mkTask();
        _tick.schedule(t, 1, 1000);
        setupScreen();
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
            /*message = new Message();
            message.setRoomId(room.getId());
            message.setFiles(URI.create("https://upload.wikimedia.org/wikipedia/en/e/e1/TheDoctor.jpg"));
            _spark.messages().post(message);*/
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
    /*
    
     Command
     Function	Parameters	Notes
     0	Clear Screen	none	 
     1	Foreground Colour	Colour value (0-7)	 
     2	Background Colour	Colour value (0-7)	 
     3	Screen Rotation	Display Rotation value (0 - 3)	0 - Portrait (left)
     1 - Landscape (upside down)
     2 - Portrait (right)
     3 - Landscape (default)
     4	Font Size	Size Value (0 - 2)	1 - Small
     2 - Medium (default)
     3 - Large
     5	Goto beginning of line	none	Based on current text size and position
     6	Goto X, Y position (Text)	X, Y	Depends on currently selected font size for positioning
     7	Goto X, Y position (Pixel)	X, Y	Pixel Positioning 
     (X = 0 to 159, Y = 0 to 127) - Lanscape
     (X = 0 to 127, Y = 0 to 159) - Portrait
     0,0 is top left corner
     8	Draw Line	X1, Y1, X2, Y2	Draws line in currently selected foreground colour between X1,Y1 and X2,Y2
     9	Draw Box	X1, Y1, X2, Y2	Draws a box (outline) in currently selected foreground colour
     10	Draw Filled Box	X1, Y1, X2, Y2	Draws a box (filled) in currently selected background colour
     11	Draw Circle	X, Y, Radius	Draws a circle (outline) in currently selected foreground colour with centre at X, Y with radius R
     12	Draw Filled Circle	X, Y, Radius	Draws a circle (filled) in currently selected background colour with centre at X, Y with radius R
     13	Display Bitmap	X, Y, Filename	Display Bitmap (BMP) at coordinates X, Y
     14	Backlight Brightness	0 - 100	Set backlight brightness (0 = OFF, 100 = Max).
     */

    public void writeScreen(byte[] bytes) {
        try {
            FileOutputStream tout = new FileOutputStream(_tty);
            tout.write(bytes);
            tout.close();
        } catch (FileNotFoundException ex) {
            Log.error("can't open tty " + _tty + " because" + ex);
        } catch (IOException ex) {
            Log.error("can't write to tty " + _tty + " because" + ex);
        }
    }

    public void showVoltage(String v) {
        while (v.length() < 9) {
            v += " ";
        }
        byte[] vb = v.getBytes();
        byte[] cls = {(byte) 0x1B, (byte) 6, (byte) 0, (byte) 2, (byte) 0xff};
        byte[] message = new byte[cls.length + vb.length];
        System.arraycopy(cls, 0, message, 0, cls.length);
        System.arraycopy(vb, 0, message, cls.length, vb.length);
        writeScreen(message);
    }

    public void setupScreen() {
        byte[] setup = {(byte) 0x1B, (byte) 0, (byte) 0xff,
            (byte) 0x1B, (byte) 14, (byte) 40, (byte) 0xff,
            (byte) 0x1B, (byte) 4, (byte) 3, (byte) 0xff,
            'W', 'e', 's', 't', 'h', 'a', 'w', 'k'};
        writeScreen(setup);
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
            showVoltage(" " + volt + "mv");
        } catch (Exception ex) {
            Log.error("Problem with " + _analogFile + " : " + ex.getMessage());
            ex.printStackTrace();
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
