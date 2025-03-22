package org.example;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Main extends WebSocketServer{
    private static final Set<WebSocket> clients = Collections.synchronizedSet(new HashSet<>());

    public Main(int port) {
        super(new InetSocketAddress("192.168.100.14",port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake){
        clients.add(conn);
        System.out.println("New client connected: "+ conn.getRemoteSocketAddress());
    }
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote){
        clients.remove(conn);
        System.out.println("client disconnected: "+ conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message){
        System.out.println("Received: "+ message);
        broadcastMessage(message, conn);
    }

    @Override
    public void onError(WebSocket con, Exception ex){
        ex.printStackTrace();
    }

    @Override
    public void onStart(){
        System.out.println("Server has Started");
    }

    private void broadcastMessage(String message, WebSocket sender){
        synchronized (clients){
            for (WebSocket client : clients){
                if(client != sender){
                    client.send(message);
                }
            }
        }
    }

    public static void main(String[] args){
        int port = 9999;
        Main server = new Main(port);
        server.start();
        System.out.println("Server has Started on port "+ port);
    }
}
