package de;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(new File("./settings.json"));

        Mail mail = new Mail(node.get("destination").asText(),
                node.get("host").asText(),
                node.get("port").asText(),
                node.get("username").asText(),
                node.get("password").asText()
        );
        mail.getMails();

    }
}