package com.company;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ftp_client {
    public static final String FTP_HOST = "192.168.0.111";
    public static final int FTP_PORT = 21;
    public static final String FTP_USER = "huan94220@gmail.com";
    public static final String FTP_PASSWORD = "zhy18680710204";
    FTPClient fc=new FTPClient();
    //login
     void login() throws IOException{
        try {

            fc.connect(FTP_HOST,FTP_PORT);
            boolean lg=fc.login(FTP_USER,FTP_PASSWORD);
            if (lg)
            {
                System.out.println("Login in successfully!");
            }else{
                System.out.println("Can not login in!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //ls command
    public void ls() throws IOException {
        FTPFile[] files = fc.listFiles();
        DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (FTPFile file : files) {
            String details = file.getName();
            if (file.isDirectory()) {
                details = "[" + details + "]";
            }
            details += "\t\t" + file.getSize();
            details += "\t\t" + dateFormater.format(file.getTimestamp().getTime());
            System.out.println(details);
        }

        fc.logout();
        fc.disconnect();
        fc.pasv();
    }

    //




    public static void main(String[] args) throws IOException {
         ftp_client ftp=new ftp_client();
         ftp.login();
         ftp.ls();
    }
}
