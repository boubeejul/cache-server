package com.app.server;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Scanner;

public class CacheServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private final String mainServerUrl = "https://dummyjson.com";
    private final int port = 8080;

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Cache Server started at port " + port);

        while (true) {
            clientSocket = serverSocket.accept();
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            System.out.println("Client connected at: " + clientSocket.getInetAddress());

            String clientRequest = input.readLine();

            if (clientRequest != null) {
                try {
                    if (verifyClientRequest(clientRequest)) {
                        verifyCache(clientRequest);
                    } else {
                        returnErrorMessageToClient("The request was invalid. Try again.");
                    }
                    clientSocket.close();
                } catch (IOException | URISyntaxException e) {
                    returnErrorMessageToClient("There was a problem while retrieving the data: " + e.getMessage());
                }
            }
        }
    }

    public boolean verifyClientRequest(String request) {
        return request.startsWith("/");
    }

    public void verifyCache(String clientRequest) throws IOException, URISyntaxException {
        File fileRequested = new File("./cache" + clientRequest);

        if (!fileRequested.exists()) {
            try {
                fetchFromMainServer(clientRequest);
            } catch (IOException e) {
                returnErrorMessageToClient("There was a problem while retrieving the data: " + e.getMessage());
            }
        }
        
        returnFileToClient(fileRequested);
    }

    public void returnErrorMessageToClient(String errorMessage) throws IOException {
        PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
        output.println(errorMessage);
    }

    public void returnFileToClient(File fileRequested) throws IOException {
        PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
        String data = Files.readString(fileRequested.toPath());
        output.println(data);
        System.out.println("Request sent to client.");
    }

    public void fetchFromMainServer(String request) throws IOException, URISyntaxException {
        System.out.println("Sending request to main server...");
        URL url = new URI(mainServerUrl + request).toURL();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        int status = con.getResponseCode();

        if (status != 200) {
            throw new IOException("Server returned HTTP status " + status);
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String response = in.readLine();
            in.close();
            saveResponse(response, request);
        }
    }

    public void saveResponse(String response, String fileName) throws IOException {
        System.out.println("Saving cache...");

        File cacheDir = new File("./cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }

        try (FileWriter writer = new FileWriter("./cache" + fileName)) {
            writer.write(response);
        }
    }

    public static void main(String[] args) {
        CacheServer server = new CacheServer();
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Couldn't start the server: " + e.getMessage());
        }
    }
}
