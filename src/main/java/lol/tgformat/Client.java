package lol.tgformat;

import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class Client {

    /**
     * 处理服务端发回的对象，可实现该接口。
     */
    public interface ObjectAction{
        void doAction(Object obj,Client client);
    }

    public static final class DefaultObjectAction implements ObjectAction{
        public void doAction(Object obj,Client client) {
            System.out.println("Dispatch：\t"+obj.toString());
        }
    }


    public static void main(String[] args) throws IOException {
        String serverIp = "127.0.0.1";
        int port = 11451;
        Client client = new Client(serverIp,port);
        client.start();
    }

    private final String serverIp;
    private final int port;
    private static Socket socket;
    private boolean running=false; //连接状态

    private long lastSendTime; //最后一次发送数据的时间

    //用于保存接收消息对象类型及该类型消息处理的对象
    private final ConcurrentHashMap<Class, ObjectAction> actionMapping = new ConcurrentHashMap<>();

    public Client(String serverIp, int port) {
        this.serverIp=serverIp;
        this.port=port;
    }

    public void start() throws IOException {
        if(running)return;
        socket = new Socket(serverIp,port);
        System.out.println("LocalPort："+socket.getLocalPort());
        lastSendTime=System.currentTimeMillis();
        running=true;
        new Thread(new KeepAliveListener()).start();  //保持长连接的线程，每隔2秒向服务器发一个心跳
        new Thread(new ReceiveListener()).start();    //接受消息的线程，处理消息
    }

    public void stop(){
        if(running)running=false;
    }

    /**
     * 添加接收对象的处理对象。
     * @param cls 待处理的对象，其所属的类。
     * @param action 处理过程对象。
     */
    public void addActionMap(Class<Object> cls,ObjectAction action){
        actionMapping.put(cls, action);
    }

    public static void sendObject(Object obj) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(obj);
        System.out.println("Send：\t"+obj);
        oos.flush();
    }

    class KeepAliveListener implements Runnable{
        long checkDelay = 10;
        long keepAliveDelay = 1000;
        public void run() {
            while(running){
                if(System.currentTimeMillis()-lastSendTime>keepAliveDelay){
                    try {
                        sendObject(new KeepAlive());
                        /*JSONObject json = new JSONObject();
                        json.put("type","msg");
                        json.put("msg","test123123");
                        Client.this.sendObject(json);*/
                    } catch (IOException e) {
                        e.printStackTrace();
                        Client.this.stop();
                    }
                    lastSendTime = System.currentTimeMillis();
                }else{
                    try {
                        Thread.sleep(checkDelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Client.this.stop();
                    }
                }
            }
        }
    }

    class ReceiveListener implements Runnable{
        public void run() {
            while(running){
                try {
                    InputStream in = socket.getInputStream();

                        InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);//以utf-8读
                        BufferedReader br = new BufferedReader(isr);
                        Object obj = br.readLine();
                        System.out.println("Receive：\t"+obj);
                        ObjectAction oa = actionMapping.get(obj.getClass());
                        oa = oa==null?new DefaultObjectAction():oa;
                        oa.doAction(obj, Client.this);

                } catch (Exception e) {
                    e.printStackTrace();
                    Client.this.stop();
                }
            }
        }
    }

}
