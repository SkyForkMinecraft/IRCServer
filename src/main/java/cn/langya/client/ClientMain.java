package cn.langya.client;

import cn.langya.client.events.EventPacketReceive;
import cn.langya.client.events.EventPacketSend;
import cn.langya.util.LogUtil;
import com.cubk.event.EventManager;

import java.io.*;
import java.net.*;

public class ClientMain extends LogUtil{
    private static PrintWriter out;
    private static final EventManager eventManager = new EventManager();

    public static void main(String[] args) throws IOException {
        String host = "127.0.0.1";
        int port = 11451;

        logInfo("正在初始化Socket/PrintWriter/BufferedReader");
        Socket echoSocket = new Socket(host, port);
        out = new PrintWriter(echoSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        logInfo("初始化Socket/PrintWriter/BufferedReader成功!!!");
        String userInput;
        while ((userInput = stdIn.readLine()) != null) {
                eventManager.call(new EventPacketSend(in.readLine()));
                logInfo("注册事件: EventPacketSend: " + in.readLine());
                println(userInput);

                eventManager.call(new EventPacketReceive(in.readLine()));
                logInfo("注册事件: EventPacketReceive: " + in.readLine());

        }

    }

    public static void println(String str) {
        out.println(str);
    }
}