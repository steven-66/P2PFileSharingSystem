

import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;

class RegistryThread extends Thread{ // a thread for file registration.
	 private List<String> files = null;
	 private Map<Integer, List<String>> fileList = null;
	 private List<Integer> portList = null;
	 String myclient;
	 int port;
	 public RegistryThread(List<Integer> portList, List<String> files, Map<Integer, List<String>> fileList, int port){
		 this.portList = portList;
	     this.files = files;
	     this.fileList = fileList;
	     this.port = port;
	 }   // initiate the thread
	
	 public void run(){
		 try{
			if(!portList.contains(port))portList.add(port);
			fileList.put(port,files); // add user file mapping onto indexServer
			 
		 }catch (Exception ex){
			 ex.printStackTrace( );
		 }
	 }
}



class SearchThread implements Callable<List<Integer>>{ // a thread for file searching

	List<Integer> portList = null;
	Map<Integer, List<String>> fileList = null;
	String file_name = null;
	
	public SearchThread(List<Integer> portList, Map<Integer, List<String>> fileList, String file_name){
		this.portList = portList;
		this.fileList = fileList;
		this.file_name = file_name;
	} // initiate the thread

	@Override
	public List<Integer> call() throws Exception {// fine all the users who have the file, and return their IP addresses
		List<Integer> targetNodes = new ArrayList<>();
		for(int p:portList){
			if(fileList.getOrDefault(p,new ArrayList<>()).contains(file_name)) {
				targetNodes.add(p);
			}
		} 
		return targetNodes;
	}
}

public class ServerImpl extends UnicastRemoteObject implements Server {  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

// rmi server at the indexserver
	
	public ServerImpl() throws RemoteException{
		super();
	}
	
	List<Integer> portList = Collections.synchronizedList(new ArrayList<>());
	Map<Integer,List<String>> fileList = new ConcurrentHashMap<>();
	public List<Integer> search(String file_name) throws RemoteException{ // search in the registered node and get the target node
		List<Integer> targetNodes =null;
		try{
//			String CurClient  = RemoteServer.getClientHost();
			Callable<List<Integer>> callable = new SearchThread(this.portList, this.fileList, file_name); // start the file searching thread
			FutureTask<List<Integer>> future = new FutureTask<>(callable);
	        new Thread(future).start();
	        targetNodes =  future.get();
		}catch(Exception e){
			e.printStackTrace();
		}
		return targetNodes;
	}
	
	 public void registry(List<String> files,int port) throws RemoteException
	 {
		 try{
//			 String CurClient = RemoteServer.getClientHost();
			 new RegistryThread(portList, files, fileList, port).start(); // start the registration thread
		 }catch(Exception e){
			 e.printStackTrace();
		 }
	 }
	
	public static void main(String args[]) throws Exception{
		try{
			ServerImpl myserver = new ServerImpl();
			Naming.rebind("IndexServer", myserver);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
