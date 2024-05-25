package lol.tgformat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lol.tgformat.entities.ReceiveData;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private List<PrintWriter> oos = new ArrayList<>();
    /**
     * 要处理客户端发来的对象，并返回一个对象，可实现该接口。
     */
    public interface ObjectAction{
        Object doAction(Object rev, Server server);
    }

    public static final class DefaultObjectAction implements ObjectAction{
        public Object doAction(Object rev,Server server) {
            System.out.println("Dispatch & Return："+rev);
            return rev;
        }
    }

    public static void main(String[] args) {
        int port = 11451;
        Server server = new Server(port);
        server.start();
    }

    private final int port;
    private volatile boolean running=false;
    private final long receiveTimeDelay=3000;
    private final ConcurrentHashMap<Class, ObjectAction> actionMapping = new ConcurrentHashMap<>();
    private Thread connWatchDog;

    public Server(int port) {
        this.port = port;
    }

    public void start(){
        if(running)return;
        running=true;
        connWatchDog = new Thread(new ConnectListener());
        connWatchDog.start();
    }

    public void stop(){
        if(running)running=false;
    }

    public void addActionMap(Class<Object> cls,ObjectAction action){
        actionMapping.put(cls, action);
    }

    class ConnectListener implements Runnable{
        public void run(){
            try {
                ServerSocket ss = new ServerSocket(port,5);
                while(running){
                    Socket s = ss.accept();
                    new Thread(new SocketAction(s)).start();
                    OutputStream outputStream = s.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                    PrintWriter pw = new PrintWriter(osw,true);
                    oos.add(pw);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Server.this.stop();
            }

        }
    }

    class SocketAction implements Runnable{
        public Socket connection;
        boolean run=true;
        long lastReceiveTime = System.currentTimeMillis();
        public SocketAction(Socket connection) {
            this.connection = connection;
        }
        private void sendMessage(Object msg){
            for(PrintWriter oo : oos){
                oo.println(msg);
            }
        }
        public void run() {
            PrintWriter pw;
            while(running && run){
                if(System.currentTimeMillis()-lastReceiveTime>receiveTimeDelay){
                    overThis();
                }else{
                    try {
                        InputStream in = connection.getInputStream();
                        if(in.available()>0){
                            ObjectInputStream ois = new ObjectInputStream(in);
                            Object obj = ois.readObject();
                            if (obj.toString().contains("type")) {
                                JSONObject json = JSON.parseObject(obj.toString());
                                ReceiveData data = json.toJavaObject(ReceiveData.class);
                                System.out.println(data);
                            }
                            lastReceiveTime = System.currentTimeMillis();
                            System.out.println("Receive：\t"+obj);
                            ObjectAction oa = actionMapping.get(obj.getClass());
                            oa = oa==null?new DefaultObjectAction():oa;
                            Object out = oa.doAction(obj,Server.this);

                            if(out!=null){
                                sendMessage(obj);
                            }
                        }else{
                            Thread.sleep(10);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        overThis();
                    }
                }
            }
        }

        private void overThis() {
            if(run)run=false;
            if(connection !=null){
                try {
                    OutputStream out = connection.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                    PrintWriter pw = new PrintWriter(osw,true);
                    oos.remove(pw);
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Close："+ connection.getRemoteSocketAddress());
        }

    }

}
