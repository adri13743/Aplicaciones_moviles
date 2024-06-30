package com.example.my_app_00;

import static com.example.my_app_00.MainActivity.getOutputMediaFile;
import static com.example.my_app_00.MainActivity.guardado;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class NetworkExecutor extends Thread {

    String fileStr;
    Integer HTTP_SERVER_PORT = 8081;
    private Handler handlerNetwork;
    public NetworkExecutor(String file,Handler _handlerNetworkExecutorResult) {
        this.fileStr = file;
        this.handlerNetwork = _handlerNetworkExecutorResult;
    }

    public void run() {
        Socket scliente = null;
        ServerSocket unSocket = null;
        try {
            unSocket = new ServerSocket(HTTP_SERVER_PORT); //Creamos el puerto
        while (true) {
                try {
                    scliente = unSocket.accept(); //Aceptando conexiones del navegador Web
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                //System.setProperty("line.separator", "\r\n");
                BufferedReader in = new BufferedReader(new InputStreamReader(scliente.getInputStream()));
                PrintStream out = new PrintStream(new BufferedOutputStream(scliente.getOutputStream()));
                String cadena = in.readLine();
                StringTokenizer st = new StringTokenizer(cadena);
                String commandString = st.nextToken().toUpperCase();
                String urlObjectString = null;
                if (commandString.equals("GET")) {
                    urlObjectString = st.nextToken();
                    Log.v("urlObjectString", urlObjectString);
                }
                if (urlObjectString.toUpperCase().startsWith("/INDEX.HTML") ||
                        urlObjectString.toUpperCase().equals("/INDEX.HTM") ||
                        urlObjectString.equals("/")) {
                    String headerStr = getHTTP_Header(CODE_OK, "text/html", fileStr.length());
                    out.print(headerStr);
                    out.println(fileStr);
                    out.flush();
                }
                if ( urlObjectString.toUpperCase().startsWith("/CAMERA.JPG")||
                        urlObjectString.toUpperCase().startsWith("/CAMERA.") ) {
                    //showDisplayMessage("CAMERA");
                    Message msg = new Message();
                    msg.obj = "CAMERA";
                    handlerNetwork.sendMessage(msg);
                    while (!guardado) {
                        Thread.sleep(100);
                    }
                    guardado = false;
                    File cameraFile = getOutputMediaFile();
                    FileInputStream fis = null;
                    boolean exist = true;
                    try {
                        fis = new FileInputStream(cameraFile);
                    } catch (FileNotFoundException e) {
                        exist = false;
                    }
                    if (exist) {
                        String headerStr = getHTTP_Header(CODE_OK,
                                "image/jpeg", (int) cameraFile.length());
                        out.print(headerStr);
                        byte[] buffer = new byte[4096];
                        int n;
                        while ((n = fis.read(buffer)) > 0) { // enviar archivo
                            out.write(buffer, 0, n);
                        }
                        out.flush();
                        out.close();

                    }
                }
                if (urlObjectString.toUpperCase().startsWith("/FORWARD")) {
                    Message msg = new Message();
                    msg.obj = "FORWARD";
                    handlerNetwork.sendMessage(msg);
                    //showDisplayMessage("FORWARD");
                    String headerStr = getHTTP_Header(CODE_OK, "text/html", fileStr.length());
                    out.print(headerStr);
                    out.println(fileStr);
                    out.flush();
                }
            if (urlObjectString.toUpperCase().startsWith("/BACKWARD")) {
                Message msg = new Message();
                msg.obj = "BACKWARD";
                handlerNetwork.sendMessage(msg);
                //showDisplayMessage("BACKWARD");
                String headerStr = getHTTP_Header(CODE_OK, "text/html", fileStr.length());
                out.print(headerStr);
                out.println(fileStr);
                out.flush();
            }
            if (urlObjectString.toUpperCase().startsWith("/LEFT")) {
                Message msg = new Message();
                msg.obj = "LEFT";
                handlerNetwork.sendMessage(msg);
                //showDisplayMessage("BACKWARD");
                String headerStr = getHTTP_Header(CODE_OK, "text/html", fileStr.length());
                out.print(headerStr);
                out.println(fileStr);
                out.flush();
            }
            if (urlObjectString.toUpperCase().startsWith("/RIGHT")) {
                Message msg = new Message();
                msg.obj = "RIGHT";
                handlerNetwork.sendMessage(msg);
                //showDisplayMessage("BACKWARD");
                String headerStr = getHTTP_Header(CODE_OK, "text/html", fileStr.length());
                out.print(headerStr);
                out.println(fileStr);
                out.flush();
            }
            if (urlObjectString.toUpperCase().startsWith("/STOP")) {
                Message msg = new Message();
                msg.obj = "STOP";
                handlerNetwork.sendMessage(msg);
                //showDisplayMessage("BACKWARD");
                String headerStr = getHTTP_Header(CODE_OK, "text/html", fileStr.length());
                out.print(headerStr);
                out.println(fileStr);
                out.flush();
            }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getHTTP_Header(int headerStatusCode, String headerContentType, int headerFileLength) {
        String result = getHTTP_HeaderStatus(headerStatusCode) +
                "\r\n" +
                getHTTP_HeaderContentLength(headerFileLength) +
                getHTTP_HeaderContentType(headerContentType) +
                "\r\n";
        return result;
    }

    final public int CODE_OK = 200;
    final public int CODE_BADREQUEST = 400;
    final public int CODE_FORBIDDEN = 403;
    final public int CODE_NOTFOUND = 404;
    final public int CODE_INTERNALSERVERERROR = 500;
    final public int CODE_NOTIMPLEMENTED = 501;

    private String getHTTP_HeaderStatus(int headerStatusCode) {
        String result = "";
        switch (headerStatusCode) {
            case CODE_OK:
                result = "200 OK";
                break;
            case CODE_BADREQUEST:
                result = "400 Bad Request";
                break;
            case CODE_FORBIDDEN:
                result = "403 Forbidden";
                break;
            case CODE_NOTFOUND:
                result = "404 Not Found";
                break;
            case CODE_INTERNALSERVERERROR:
                result = "500 Internal Server Error";
                break;
            case CODE_NOTIMPLEMENTED:
                result = "501 Not Implemented";
                break;
        }
        return ("HTTP/1.0 " + result);
    }

    private String getHTTP_HeaderContentLength(int headerFileLength) {
        return "Content-Length: " + headerFileLength + "\r\n";
    }

    private String getHTTP_HeaderContentType(String headerContentType) {
        return "Content-Type: " + headerContentType + "\r\n";
    }

}
