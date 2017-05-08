package com.bear.aithinker.a20camera;

import android.app.Application;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Administrator on 2016/7/28 0028.
 */

public class ApplicationUtil extends Application {

    private Socket socket = null;
    //    private DataOutputStream out = null;
    private PrintWriter out = null;
    private DataInputStream in = null;

    //    private String hostIp = "192.168.43.34";
    private int PORT = 80;


    public void initSocket(String hostIp) throws IOException, Exception {
        this.socket = new Socket(hostIp, PORT);
//        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                socket.getOutputStream())), true);
    }


    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }
}
