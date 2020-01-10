package ftp;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;


public class ftp_instructions {


    public ftp_instructions() {

    }

    /**
     * Connects to the default port of an FTP server and logs in as
     * anonymous/anonymous.
     */
    public synchronized void connect(String host) throws IOException {
        connect(host, 21);
    }

    /**
     * Connects to an FTP server and logs in as anonymous/anonymous.
     */
    public synchronized void connect(String host, int port) throws IOException {
        connect(host, port, "anonymous", "anonymous");
    }

    /**
     * Connects to an FTP server and logs in with the supplied username and
     * password.
     */
    public synchronized void connect(String host, int port, String user,
                                     String pass) throws IOException {
        if (socket != null) {
            throw new IOException("FTP client is already connected. Disconnect first.");
        }
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream()));

        String response = readLine();
        if (!response.startsWith("220 ")) {
            throw new IOException(
                    "FTP client received an unknown response when connecting to the FTP server: "
                            + response);
        }

        sendLine("USER " + user);

        response = readLine();
        if (!response.startsWith("331 ")) {
            throw new IOException(
                    "FTP client received an unknown response after sending the user: "
                            + response);
        }

        sendLine("PASS " + pass);

        response = readLine();
        if (!response.startsWith("230 ")) {
            throw new IOException(
                    "FTP client was unable to log in with the supplied password: "
                            + response);
        }
        System.out.println("Logged in!");

        // Now logged in.
    }

    /**
     * Disconnects from the FTP server.
     */
    public synchronized void disconnect() throws IOException {
        try {
            sendLine("QUIT");
            System.out.println("QUIT");
        } finally {
            socket = null;
        }
    }

    /**
     * Returns the working directory of the FTP server it is connected to.
     */
    public synchronized String pwd() throws IOException {
        sendLine("PWD");
        String dir = null;
        String response = readLine();

        if (response.startsWith("257 ")) {
            int firstQuote = response.indexOf('\"');
            int secondQuote = response.indexOf('\"', firstQuote + 1);
            if (secondQuote > 0) {
                dir = response.substring(firstQuote + 1, secondQuote);
            }
        }
        return dir;
    }

    //    enter pasv mode
    public synchronized void ls_passive() throws IOException {
        sendLine("PASV");
        String response = readLine();
        System.out.println(response);
        if (!response.startsWith("227 ")) {
            throw new IOException("FTP client could not request passive mode: " + response);
        }
        String ip = null;
        int port = -1;
        int opening = response.indexOf('(');
        int closing = response.indexOf(')', opening + 1);
        if (closing > 0) {
            String dataLink = response.substring(opening + 1, closing);
            StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
            try {
                ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
                        + tokenizer.nextToken() + "." + tokenizer.nextToken();
                port = Integer.parseInt(tokenizer.nextToken()) * 256
                        + Integer.parseInt(tokenizer.nextToken());
            } catch (Exception e) {
                throw new IOException("FTP client received bad data link information: " + response);
            }
        }
        socketPlus = new Socket(ip, port);
        sendLine("LIST");
        System.out.println(readLine());
        readerPlus = new BufferedReader(new InputStreamReader(socketPlus.getInputStream()));
        buffOuput(readerPlus);
        readerPlus.close();
        socketPlus.close();
        readLine().isEmpty();
    }

    //    list all file (like ls)
    public synchronized void ls() throws IOException {
        try {
            sendLine("LIST");
            System.out.println(readLine());
            readerPlus = new BufferedReader(new InputStreamReader(socketPlus.getInputStream()));
            buffOuput(readerPlus);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Change to Parent Directory
    public synchronized void toParent(){
        try {

            sendLine("CDUP");
            String response = readLine();
            if (response.startsWith("250 ")){
                System.out.println("Directory successfully changed.");
            }else{
                System.out.println("Directory successfully failed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //delete a file from remote directory
    public synchronized void delete(String dir) throws IOException {
        try {
            sendLine("DELE "+ dir);
            String response = readLine();
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //download a file from remote directory
    public synchronized void download(String dir) throws IOException {
        sendLine("PASV");
        String response = readLine();
        System.out.println(response);
        if (!response.startsWith("227 ")) {
            throw new IOException("FTP client could not request passive mode: "
                    + response);
        }
        String ip = null;
        int port = -1;
        int opening = response.indexOf('(');
        int closing = response.indexOf(')', opening + 1);
        if (closing > 0) {
            String dataLink = response.substring(opening + 1, closing);
            StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
            try {
                ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
                        + tokenizer.nextToken() + "." + tokenizer.nextToken();
                port = Integer.parseInt(tokenizer.nextToken()) * 256
                        + Integer.parseInt(tokenizer.nextToken());
            } catch (Exception e) {
                throw new IOException("FTP client received bad data link information: " + response);
            }
        }
        socketPlus = new Socket(ip, port);

        sendLine("TYPE L");      //enter binary mode
        System.out.println("1:"+reader.readLine());
        sendLine("RETR "+ dir);
        System.out.println("2:"+reader.readLine());

        //process to get file

        BufferedInputStream input = new BufferedInputStream(socketPlus.getInputStream());

        //write file
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(new File(dir)));
        byte[] buff = new byte[32 * 1024];
        int len;
        while ((len = input.read(buff)) > 0)
        output.write(buff, 0, len);
        input.close();
        output.close();
        socketPlus.close();
        readLine().isEmpty();
    }

    /**
     * Changes the working directory (like cd). Returns true if successful.
     */
    public synchronized boolean cwd(String dir) throws IOException {
        sendLine("CWD " + dir);
        String response = readLine();
        return (response.startsWith("250 "));
    }

    /**
     * Sends a file to be stored on the FTP server. Returns true if the file
     * transfer was successful. The file is sent in passive mode to avoid NAT or
     * firewall problems at the client end.
     */
    public synchronized boolean stor(File file) throws IOException {
        if (file.isDirectory()) {
            throw new IOException("FTP client cannot upload a directory.");
        }
        String filename = file.getName();
        return stor(new FileInputStream(file), filename);
    }

    /**
     * Sends a file to be stored on the FTP server. Returns true if the file
     * transfer was successful. The file is sent in passive mode to avoid NAT or
     * firewall problems at the client end.
     */
    public synchronized boolean stor(InputStream inputStream, String filename)
            throws IOException {

        BufferedInputStream input = new BufferedInputStream(inputStream);
        sendLine("PASV");
        String response = readLine();
        if (!response.startsWith("227 ")) {
            throw new IOException("FTP client could not request passive mode: "
                    + response);
        }

        String ip = null;
        int port = -1;
        int opening = response.indexOf('(');
        int closing = response.indexOf(')', opening + 1);
        if (closing > 0) {
            String dataLink = response.substring(opening + 1, closing);
            StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
            try {
                ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
                        + tokenizer.nextToken() + "." + tokenizer.nextToken();
                port = Integer.parseInt(tokenizer.nextToken()) * 256
                        + Integer.parseInt(tokenizer.nextToken());
            } catch (Exception e) {
                throw new IOException("FTP client received bad data link information: "
                        + response);
            }
        }
        sendLine("STOR " + filename);

        Socket dataSocket = new Socket(ip, port);
        System.out.println(ip+" "+port);
        response = readLine();
//        if (!response.startsWith ("125 ")) {
            if (!response.startsWith("150 ")) {
            throw new IOException("FTP client was not allowed to send the file: "
                    + response);
        }

        BufferedOutputStream output = new BufferedOutputStream(dataSocket.getOutputStream());

        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        while ((bytesRead = input.read(buffer)) != -1) {
//            System.out.println(buffer);
            output.write(buffer, 0, bytesRead);
        }
        output.flush();
        output.close();
        input.close();

        response = readLine();
        return response.startsWith("226 ");
    }

    /**
     * Enter binary mode for sending binary files.
     */
    public synchronized boolean bin() throws IOException {
        sendLine("TYPE I");
        String response = readLine();
        return (response.startsWith("200 "));
    }

    /**
     * Enter ASCII mode for sending text files. This is usually the default mode.
     * Make sure you use binary mode if you are sending images or other binary
     * data, as ASCII mode is likely to corrupt them.
     */
    public synchronized boolean ascii() throws IOException {
        sendLine("TYPE A");
        String response = readLine();
        return (response.startsWith("200 "));
    }

    /**
     * Sends a raw command to the FTP server.
     */
    private void sendLine(String line) throws IOException {
        if (socket == null) {
            throw new IOException("FTP client is not connected.");
        }
        try {
            writer.write(line + "\r\n");
            writer.flush();
            if (DEBUG) {
                System.out.println("> " + line);
            }
        } catch (IOException e) {
            socket = null;
            throw e;
        }
    }

    private String readLine() throws IOException {
        String line = reader.readLine();
        if (DEBUG) {
            System.out.println("< " + line);
        }
        return line;
    }

    //output BufferedReader
    private void buffOuput(BufferedReader out) {
        int i;
        try {
            while((i=out.read())!=-1){
                System.out.print((char)i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //For BufferInputReader
    private void buffiReader(BufferedInputStream in) {
        int i;
        try {
            while((i=in.read())!=-1){
                System.out.print((char)i);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private Socket socket = null;

    private Socket socketPlus = null;

    private BufferedReader reader = null;

    private BufferedReader readerPlus = null;

    private BufferedWriter writer = null;

    private static boolean DEBUG = false;


}
