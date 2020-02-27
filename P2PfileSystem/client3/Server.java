
import java.rmi.*;
import java.util.List;

public interface Server extends Remote {
	public List<Integer> search(String file_name) throws RemoteException; // find the peer nodes who have the file
	public void registry(List<String> files, int port) throws RemoteException; // let a peer node register a file into the system
}

