package ftp;

import javax.sound.midi.Soundbank;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class client {

//    myftp inet.cis.fiu.edu
    public static  String FTP_HOST ="inet.cis.fiu.edu";
    public static final int FTP_PORT = 21;
    public static final String FTP_USER = "demo";
    public static final String FTP_PASSWORD = "demopass";
//    static {
//        Scanner sca=new Scanner(System.in);
//        System.out.print(">");
//        if (sca.next().startsWith("myftp")){
//            String i=sca.next();
//            FTP_HOST=i;
//
//        }else {
//            System.out.println("Request Denied! Format is incorrect!");
//        }
//
//
//    }
    static ftp_instructions fc=new ftp_instructions();

    private synchronized void FunctionSet(String in) throws IOException {
        String set=in;
        if (set.startsWith("cd ..")){
            fc.toParent();
        }else if (set.startsWith("cd ")){
            fc.cwd(set.substring(3));
        }else if (set.startsWith("get ")){
            fc.download(set.substring(4));
        }else if (set.startsWith("delete ")){
            fc.delete(set.substring(7));
        }else if (set.startsWith("put ")){
            String current = new java.io.File( "." ).getCanonicalPath();
//            System.out.println(current+"\\"+set.substring(4));
            File f=new File(current+"\\"+set.substring(4));
            System.out.println(fc.stor(f));
        }else {
            switch (set)
            {
                case "ls":
                    fc.ls_passive();
                    break;
                case "pwd":
                    System.out.println(fc.pwd());
                    break;
                case "quit":
                    fc.disconnect();
                    break;
            }
            }

    }
    public static void main(String[] args) {


        try {
            fc.connect(FTP_HOST,FTP_PORT,FTP_USER,FTP_PASSWORD);
//            InputStream i=new FileInputStream("C:\\Users\\huan9\\IdeaProjects\\FTP\\src\\ftp\\aa.txt");
//            System.out.println(fc.stor(i,"aa.txt"));
//            fc.cwd("folder/Abdul/");
//            System.out.println(fc.pwd());
//            fc.ls_passive();
//            File f=new File("C:\\Users\\huan9\\IdeaProjects\\FTP\\src\\ftp\\Get_input.java");
//            System.out.println(fc.stor(f));
//            fc.download("aa.txt");
//            fc.delete("aa.txt");
//            fc.toParent();
//            fc.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        client c=new client();

        while(true){
            Scanner sc= new Scanner(System.in);
            System.out.print("\n"+"myftp>");
            try {
                c.FunctionSet(sc.nextLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
