package cn.langya.client;

import cn.langya.util.LogUtil;

import java.io.*;
import java.net.*;

public class ClientMain {
    private static PrintWriter out;
    public static void main(String[] args) throws IOException {
        String host = "127.0.0.1";
        int port = 11451;

        Socket echoSocket = new Socket(host, port);
        out = new PrintWriter(echoSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        String userInput;
        while ((userInput = stdIn.readLine()) != null) {
            out.println(userInput);
            LogUtil.logInfo("echo: " + in.readLine());
        }
    }
    public static void println(String str) {
        out.println(str);
    }
}