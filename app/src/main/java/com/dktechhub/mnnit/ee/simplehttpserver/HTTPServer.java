package com.dktechhub.mnnit.ee.simplehttpserver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import androidx.preference.PreferenceManager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class HTTPServer {


    private HTTPInterface webShareServerInterface;
    private ConnectionListener connectionListner;
    //private HashMap<String ,String> loggedInStatus=new HashMap<>();
    private final String documentRoot;
    private final int port;
    private final boolean allowDirectoryListing;
    private final String indexPage;
    public HTTPServer(Context context,HTTPInterface webShareServerInterface)
    {   this.webShareServerInterface = webShareServerInterface;
        SharedPreferences sharedPreference= PreferenceManager.getDefaultSharedPreferences(context);
        documentRoot=sharedPreference.getString("document_root", Environment.getExternalStorageDirectory().toString());
        port=Integer.parseInt(sharedPreference.getString("port", String.valueOf(2004)));
        allowDirectoryListing=sharedPreference.getBoolean("allow_directory_listing",true);
        indexPage=sharedPreference.getString("index_page","index.html");
    }







    @SuppressLint("DefaultLocale")
    public void startServing()
    {//webShareServerInterface.notifyOnff(port,true);
        webShareServerInterface.log(String.format("Starting http server with following configuration:\nDocument root:%s\nPort:%d\nIndex page:%s,\nAllow directory listing:%s",documentRoot,port,indexPage,allowDirectoryListing));
        if(null==connectionListner||connectionListner.isCancelled)
        {this.connectionListner= new ConnectionListener(port);
            connectionListner.start();
        }



    }
    public void stopServing()
    {
        if(this.connectionListner!=null)
        {
            try {
                connectionListner.serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connectionListner.isCancelled=true;}
    }

    public class SingleClientHandler extends Thread {
        Socket socket;

        InputStream inputStream;
        OutputStream outputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        PrintWriter  printWriter;
        HashMap<String, String> requestheaders = new HashMap<>();
        String requestMethod,requestUri;
        //private final String root = HTTPServer.this.documentRoot;
        public  String current;
        public SingleClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.inputStream = this.socket.getInputStream();
            this.outputStream = this.socket.getOutputStream();
            this.inputStreamReader = new InputStreamReader(this.inputStream);
            this.bufferedReader = new BufferedReader(inputStreamReader);
            this.printWriter=new PrintWriter(outputStream);
        }

        @Override
        public void run() {
            super.run();
            HTTPServer.this.webShareServerInterface.log("New connection arrived from :"+socket.getRemoteSocketAddress());

            try {
                this.decodeHeaders();
                if(this.requestMethod.equals("GET"))
                    //this.respond("HTTp ok",200);
                    this.respondWithFile(getPathForRequestedUri(requestUri));
                else {

                    this.respond("http ok",200);
                }
                this.finish();
                //System.out.println(this.getString());
                System.out.println("Connection closed");

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void decodeHeaders() throws Exception {
            String header;
            int temp;
            StringBuilder sb = new StringBuilder();

            header = bufferedReader.readLine();
            sb.append(header).append('\n');
            this.requestMethod=(header.substring(0, header.indexOf(' ')));
            this.requestUri= URLDecoder.decode(header.substring(header.indexOf(' ') + 1, header.lastIndexOf(' ')));

            while ((header = bufferedReader.readLine()).length() != 0) {

                if (header.contains(":")) {
                    temp = header.indexOf(':');
                    this.requestheaders.put(header.substring(0, temp), header.substring(temp + 2));
                    sb.append(header).append('\n');
                }

            }
            HTTPServer.this.webShareServerInterface.log(sb.toString());
        }

        public void respond(String message,int statusCode)
        {
            printWriter.print("HTTP/1.1 " + statusCode + " \r\n");
            printWriter.print("Content-Type: text/plain\r\n");
            printWriter.print("Connection: close\r\n");
            printWriter.print("\r\n");
            printWriter.print(message + "\r\n");
            printWriter.close();
        }
        public void respondWithFile(String filePath) {
            HTTPServer.this.webShareServerInterface.log("Returning path"+filePath);
            try {
                File f = new File(filePath);
                if (f.isFile()) {
                    String contentType = guessContentType(filePath);
                    FileInputStream fileInputStream = new FileInputStream(f);
                    byte[] buffer = new byte[1024*1024*6];
                    DataInputStream dis = new DataInputStream(new BufferedInputStream(fileInputStream));

                    outputStream.write(("HTTP/1.1 200 \r\n").getBytes());
                    outputStream.write(("Content-Type: " + contentType + "\r\n").getBytes());
                    outputStream.write(("Content-Length: " + f.length() + "\r\n").getBytes());
                    outputStream.write(("Content-Disposition: inline; filename=\""+f.getName()+"\"\r\n").getBytes());
                    outputStream.write("\r\n".getBytes());

                    long x;

                    while ((x = dis.read(buffer)) > 0) {
                        outputStream.write(buffer,0, (int) x);
                    }

                    outputStream.write("\r\n\r\n".getBytes());
                    outputStream.flush();
                    outputStream.close();
                    fileInputStream.close();
                } else if (f.isDirectory()) {

                    try{
                        File indexFile=new File(f,HTTPServer.this.indexPage);
                        FileInputStream fileInputStream = new FileInputStream(indexFile);
                        byte[] buffer = new byte[1024*1024*6];
                        String contentType = guessContentType((indexFile.getAbsolutePath()));
                        DataInputStream dis = new DataInputStream(new BufferedInputStream(fileInputStream));
                        outputStream.write(("HTTP/1.1 200 \r\n").getBytes());
                        outputStream.write(("Content-Type: " + contentType + "\r\n").getBytes());
                        outputStream.write(("Content-Length: " + f.length() + "\r\n").getBytes());
                        outputStream.write(("Content-Disposition: inline; filename=\""+f.getName()+"\"\r\n").getBytes());
                        outputStream.write("\r\n".getBytes());
                        long x;
                        while ((x = dis.read(buffer)) > 0) {
                            outputStream.write(buffer,0, (int) x);
                        }

                    }catch (Exception e)
                    {
                        if(HTTPServer.this.allowDirectoryListing)
                        {
                            File[] list = f.listFiles();
                            outputStream.write(("HTTP/1.1 200 \r\n").getBytes());
                            outputStream.write(("Content-Type: text/html\r\n").getBytes());
                            outputStream.write("\r\n".getBytes());
                            outputStream.write("<html>\r\n".getBytes());
                            for (File s : list
                            ) {
                                String temp;
                                 temp = s.getAbsolutePath().replace(HTTPServer.this.documentRoot, "");
                                 outputStream.write(("<a href='" + temp + "'>" + s.getName() + "</a><br>" + "\r\n").getBytes());

                            }
                            outputStream.write("</html>\r\n".getBytes());
                            outputStream.write("\r\n\r\n".getBytes());
                            outputStream.flush();
                        }else
                        {
                            this.respond("Directory listing not allowed,enter an existing filename to view", 404);
                        }
                    }


                } else {
                    this.respond("Error 404:Not Found", 404);
                }
            } catch (Exception e) {

                HTTPServer.this.webShareServerInterface.log(e.toString());
                this.respond(e.toString(), 404);
            }


        }
        public void finish() throws IOException {
            bufferedReader.close();
            inputStreamReader.close();
            printWriter.close();
            inputStream.close();
            outputStream.close();
            socket.close();
        }
        public String getPathForRequestedUri(String path) {

            return HTTPServer.this.documentRoot+path;

        }
        private String guessContentType(String filePath) throws IOException {
            String type = null;
            String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
            //if(type!=null)
                return type;
            //return Files.probeContentType(filePath);
        }


    }

    public class ConnectionListener extends Thread{
        boolean isCancelled=false;
        ServerSocket serverSocket;
        int port;
        public ConnectionListener(int port)
        {
        this.port=port;
        }
        @Override
        public void run() {
            super.run();
            try {
                serverSocket = new ServerSocket(this.port);
                while (!isCancelled)
                {   try {
                    new SingleClientHandler(serverSocket.accept()).start();
                }catch (Exception e){
                    e.printStackTrace();
                    HTTPServer.this.webShareServerInterface.log(e.toString());
                }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public interface HTTPInterface{
        void log(String s);
        //void notifyOnff(int port,boolean on);
    }
}
