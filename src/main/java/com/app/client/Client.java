package com.app.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private Socket clientSocket;
    private PrintWriter output;
    private BufferedReader input;
    private String requestedAddress;

    public void connectToServer(String requestedAddress) throws IOException {
        this.requestedAddress = requestedAddress;
        clientSocket = new Socket("localhost", 8080);
        output = new PrintWriter(clientSocket.getOutputStream(), true);
        input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        getResponse();
    }

    public void getResponse() throws IOException {
        output.println(requestedAddress);
        String serverResponse = input.readLine();
        System.out.println(serverResponse);
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        //System.out.println(args[0]);
        client.connectToServer(args[0]);
    }
}
