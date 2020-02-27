
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class RetrieveFile implements Runnable{  // a thread for retrieving a file
    	private int SeederPort;
    	private String filename;
    
	public RetrieveFile(int targetNode, String filename){
		SeederPort = targetNode;
		this.filename = filename;
	}
    public void run(){
        // TODO Auto-generated method stub
        try{
            Socket s = new Socket("127.0.0.1",SeederPort);// set up a socket connection with the targeted node who has the file
            OutputStream out = s.getOutputStream();
            InputStream in = s.getInputStream();
            out.write((filename).getBytes()); // send the file name to the targeted node
            FileOutputStream fos = new FileOutputStream("./file/"+"new"+filename);
            int data;
            System.out.println("Downloading data......");
            System.out.println("contents are below:\n");
            while ((data = in.read())!=-1)
            {
                fos.write(data);
                System.out.print((char)data);
            } // receiving the data
            System.out.println("\nFile has been received successfully.");
            fos.close();
            out.close();
            in.close();
            s.close();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
class SendFile implements Runnable{// a thread for file transfer
    private Socket s;
    public SendFile(Socket s){
        this.s = s;
    }
    public void run(){   	
        try{
            OutputStream os = s.getOutputStream();
            InputStream is = s.getInputStream();
            byte[] buf = new byte[1024];
            int off = is.read(buf);  // receive file name
            String fileName = new String(buf,0,off);
            System.out.println( "The client from port: " +s.getPort()+" tries to retrieve "+fileName);
            FileInputStream fins = new FileInputStream("./file/"+fileName);
            int data;
            while((data =fins.read())!=-1){
                os.write(data);
            } // output file to the node who wants the file
            fins.close();
            os.close();
            is.close();
            s.close();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}

class Seeder implements Runnable{ // a thread for waiting for the socket connection coming from a node who wants to retrieve the file
	private int port; // port for listening
	public Seeder(int port) {
		this.port = port;
	}
	public void run(){
		try{
	        ServerSocket ss = new ServerSocket(port);
	        while (true){
	            Socket s = ss.accept( );
	            new Thread(new SendFile(s)).start( ); // start a file transferring thread
	        }
	    } catch (Exception ex){
	        ex.printStackTrace( );
	    }
	}
}

class UpdateFile extends Thread{	// a thread to periodically update the filelist in index server to avoid client modify or delete files
	private List<String> files = new ArrayList<String>();
	private Client myclient;
	private int port;
	public UpdateFile(Client myclient, int port){
		this.myclient = myclient;
		this.port = port;
	}
	
	public void run(){
		while(true){
			File curDir = new File("./file"); // current file directory
			File[] oldFiles = curDir.listFiles();
			this.files.clear();
			for(File f:oldFiles){
				if(f.isFile()){
					this.files.add(f.getName());
				}
			}
			try{
				myclient.getIndexServer().registry(this.files,port); // register onto server
				sleep(1000);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
}


public  class Client { // peer nodes
	private int targetNode;
	private String fileName;
	private Server indexServer;
	private List<String> files = new ArrayList<String>();
	private static final int[] PORTS = new int[] {8080,8081,8082,8083,8084}; //assign local port for peer nodes
	private int port;
	public Client() {
		findFiles(); // get all the files the peer have
		this.setPort(getAvailablePort()); //set port
		new Thread(new Seeder(this.getPort())).start(); //set peer node server for listening  
	}
	public int getTargetNode() {
		return targetNode;
	}
	public void setTargetNode(Integer Target) {
		this.targetNode = Target;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public List<String> getFiles() {
		return files;
	}
	public Server getIndexServer() {
		return indexServer;
	}
	public void setIndexServer(Server indexServer) {
		this.indexServer = indexServer;
	}
	public void setFiles(ArrayList<String> files) {
		this.files = files;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getAvailablePort(){ // get the available port
		for(int port : PORTS) {
			if(!Util.isLocalPortUsing(port))return port;
		}
		System.out.println("No availabel port");
		return -1;
	}
	
	public void receive(){
		new Thread(new RetrieveFile(targetNode, fileName)).start();
	}
	public void Update() {
		UpdateFile up = new UpdateFile(this,this.getPort()); 
		up.setPriority(1);
		up.start();
	}

	public void findFiles(){ // store all of the files in the current folder to files
		try{
			File curDir = new File("./file");
			File[] files2 = curDir.listFiles();
			for(File f: files2){
				if(f.isFile()){
					this.getFiles().add(f.getName());
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	

	


}
