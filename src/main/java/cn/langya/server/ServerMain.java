package cn.langya.server;

import cn.langya.util.LogUtil;

import java.net.*;
import java.io.*;

public class ServerMain {
    public static void main(String[] args) throws IOException {
        int port = 11451;
        LogUtil.logInfo("端口:" + port);
        ServerSocket serverSocket = new ServerSocket(port);
        Socket clientSocket = serverSocket.accept();
        PrintWriter out =
                new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            out.println(inputLine);
        }

    }
}