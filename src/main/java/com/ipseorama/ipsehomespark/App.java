package com.ipseorama.ipsehomespark;
import com.ciscospark.*;
import java.net.URI;

class App {
    public static void main(String[] args) {
        // To obtain a developer access token, visit http://developer.ciscospark.com
        String accessToken = "N2JkZTVkYjQtMzdlZS00NWVmLTgxZGItZmJkY2E5ODJjNjZkYjVjNDkzYjgtMjFh";

        // Initialize the client
        Spark spark = Spark.builder()
                .baseUrl(URI.create("https://api.ciscospark.com/v1"))
                .accessToken(accessToken)
                .build();

        // List the rooms that I'm in
        spark.rooms()
                .iterate()
                .forEachRemaining(room -> {
                    System.out.println(room.getTitle() + ", found " + room.getCreated() + ": " + room.getId());
                });

        // Create a new room
        Room room = new Room();
        room.setTitle("BoneYard");
        room = spark.rooms().post(room);


        // Add a coworker to the room
        Membership membership = new Membership();
        membership.setRoomId(room.getId());
        membership.setPersonEmail("thp@westhawk.co.uk");
        spark.memberships().post(membership);


        // List the members of the room
        spark.memberships()
                .queryParam("roomId", room.getId())
                .iterate()
                .forEachRemaining(member -> {
                    System.out.println(member.getPersonEmail());
                });


        // Post a text message to the room
        Message message = new Message();
        message.setRoomId(room.getId());
        message.setText("Hello World!");
        spark.messages().post(message);
        


        // Share a file with the room
        message = new Message();
        message.setRoomId(room.getId());
        message.setFiles(URI.create("http://example.com/hello_world.jpg"));
        spark.messages().post(message);
    }
}
