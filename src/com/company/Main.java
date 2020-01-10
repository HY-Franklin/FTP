package com.company;



import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
//import org.junit.Before;
//import org.junit.Test;

import java.io.*;
import java.text.MessageFormat;

public class Main {

    public static final String FTP_HOST = "10.50.2.26";

    public static final int FTP_PORT = 21;

    public static final String FTP_USER = "ftpTest";

    public static final String FTP_PASSWORD = "pa55word";

    public static final String FTP_REMOTE_DIRECTORY = "ftp-data";

    public static final String FTP_LOCAL_DOWNLOAD_DIR = "/tmp/ftp-download/";

    public static final String FTP_LOCAL_UPLOAD_DIR = "/tmp/ftp-upload/";

    public static final String FTP_LOCAL_UPLOAD_FILENAME = "my.log";

    public static final int FTP_TIMEOUT_MILLIS = 20 * 1000;

    public static final boolean FTP_PROTOCOL_DEBUGGING = true;

    /**
     * Tests FTP downloads using the Apache Commons FTP Client.
     *
     * <ul>
     * <li>Connects to the FTP server specified by the FTP_HOST and FTP_HOST variables.</li>
     * <li>Changes directory to FTP_REMOTE_DIRECTORY</li>
     * <li>Iteratively downloads all files in FTP_REMOTE_DIRECTORY to FTP_LOCAL_DOWNLOAD_DIR</li>
     * </ul>
     * @throws IOException
     */
//    @Test
    public void testDownload() throws IOException {
        long startTimeMillis = System.currentTimeMillis();
        FTPClient ftp = new FTPClient();
        PrintWriter writer = new PrintWriter(System.out);

        try {
            // Redirect FTP commands to stdout if flag set.
            if (FTP_PROTOCOL_DEBUGGING) {
                ftp.addProtocolCommandListener(new PrintCommandListener(writer));
            }

            // Connect/login.
            System.out.println(MessageFormat.format("Connecting to ftp host: {0} on port: {1}",
                    FTP_HOST, FTP_PORT));
            ftp.connect(FTP_HOST, FTP_PORT);
            ftp.login(FTP_USER, FTP_PASSWORD);
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                throw new RuntimeException("Cannot connect to FTP_HOST: " + FTP_HOST);
            }

            // Enter pasive (download mode) and set file type as binary data.
            ftp.setDataTimeout(FTP_TIMEOUT_MILLIS);
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();


            // Change directory to the directory containing the files we wish to transfer.
            ftp.changeWorkingDirectory(FTP_REMOTE_DIRECTORY);
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                throw new RuntimeException("Cannot change to FTP directory: " + FTP_REMOTE_DIRECTORY);
            }

            // Iteratively download all files in the directory.
            for (FTPFile file : ftp.listFiles()) {
                System.out.println(MessageFormat.format("Transferring remote file: {0} to local directory: {1}",
                        file.getName(), FTP_LOCAL_DOWNLOAD_DIR));
                File target = new File(FTP_LOCAL_DOWNLOAD_DIR + file.getName());
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(target));
                ftp.retrieveFile(file.getName(), outputStream);
                outputStream.close();

                if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                    System.out.println(MessageFormat.format("Download for file: {0} failed", file.getName()));
                }
            }

        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            // Calculate time taken
            long endTimeMillis = System.currentTimeMillis();
            long totalTimemillis = endTimeMillis - startTimeMillis;
            System.out.println(MessageFormat.format("Upload process took {0} ms", totalTimemillis));
            try {
                if (ftp.isConnected()) {
                    ftp.logout();
                    ftp.disconnect();
                }
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Tests FTP uploads using the Apache Commons FTP Client.
     *
     * <ul>
     * <li>Connects to the FTP server specified by the FTP_HOST and FTP_HOST variables.</li>
     * <li>Changes directory to FTP_REMOTE_DIRECTORY</li>
     * <li>Uploads the file in FTP_LOCAL_UPLOAD_DIR which is named FTP_LOCAL_UPLOAD_FILENAME</li>
     * </ul>
     * @throws IOException
     */
//    @Test
    public void testSend() throws IOException {
        long startTimeMillis = System.currentTimeMillis();
        FTPClient ftp = new FTPClient();
        PrintWriter writer = new PrintWriter(System.out);

        try {
            // Redirect FTP commands to stdout if flag set.
            if (FTP_PROTOCOL_DEBUGGING) {
                ftp.addProtocolCommandListener(new PrintCommandListener(writer));
            }

            // Connect/login
            System.out.println(MessageFormat.format("Connecting to ftp host: {0} on port: {1}",
                    FTP_HOST, FTP_PORT));
            ftp.connect(FTP_HOST, FTP_PORT);
            ftp.login(FTP_USER, FTP_PASSWORD);
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                throw new RuntimeException("Cannot connect to FTP_HOST: " + FTP_HOST);
            }

            // Enter pasive (download mode) and set file type as binary data.
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            // Change directory to the directory containing the files we wish to transfer.
            ftp.changeWorkingDirectory(FTP_REMOTE_DIRECTORY);
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                throw new RuntimeException("Cannot change to FTP directory: " + FTP_REMOTE_DIRECTORY);
            }

            // Create new input stream for the file to transfer.
            InputStream input = new FileInputStream(new File(FTP_LOCAL_UPLOAD_DIR + FTP_LOCAL_UPLOAD_FILENAME));

            // Upload file to FTP server.
            System.out.println(MessageFormat.format("Transferring file: {0} to FTP host from local directory: {1}",
                    FTP_LOCAL_UPLOAD_FILENAME, FTP_LOCAL_UPLOAD_DIR));
            ftp.storeFile(FTP_LOCAL_UPLOAD_FILENAME, input);

            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                System.out.println(MessageFormat.format("Upload for file: {0} failed", FTP_LOCAL_UPLOAD_FILENAME));
            }



        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            // Calculate time taken
            long endTimeMillis = System.currentTimeMillis();
            long totalTimemillis = endTimeMillis - startTimeMillis;
            System.out.println(MessageFormat.format("Upload process took {0} ms", totalTimemillis));
            try {
                if (ftp.isConnected()) {
                    ftp.logout();
                    ftp.disconnect();
                }
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
