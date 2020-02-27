
import java.rmi.Naming;
import java.util.List;
import java.util.Scanner;

public class ClientMain {
	public static void menu() {
		System.out.println("==============P2P File System=============");
		System.out.println("1 Registry\n2 Search\n3 Download\n4 Multiple Request\n5 Quit");
		System.out.println("==========================================");
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			
			Client myclient = new Client();	
			Server myserver = (Server)Naming.lookup("rmi://127.0.0.1/IndexServer");
			
			int choice=1;
			 /*
			  * The below is the User Interface
			  * 
			  * */
			menu();
			while(choice!=5){
				 Scanner in = new Scanner(System.in);
				 choice = in.nextInt();
				switch (choice) {
				case 1:
					myclient.findFiles();
					myserver.registry(myclient.getFiles(),myclient.getPort());
					
					break;

				case 2:
					System.out.print("Please input the filename: ");
					String fileName = in.next();
					List<Integer> targetNodes = myserver.search(fileName);
					if(targetNodes.size() == 0){
						 System.out.println("There is no such file in the system");
						 
						 break;
					}
					myclient.setFileName(fileName);
					System.out.println("Available nodes are blow, which one you choose to download?");
					int i=0;
					for(Integer targetNode:targetNodes) {
						System.out.println(i+". "+targetNode+"\t");
						i++;
					}
					System.out.println();
					int choosedNode = in.nextInt();;
					System.out.println("Searching successfully done, go and download it!!");
					while(choosedNode>=targetNodes.size()) {
						System.out.println("out of range, please choose again");
						choosedNode = in.nextInt();
					}
					myclient.setTargetNode(targetNodes.get(choosedNode));
					myclient.receive();
					break;
				case 3:
					if(myclient.getTargetNode() == 0){
						 System.out.println("You should search the file first using the Search menu");
						 break;
					}
					myclient.receive();
					
					break;
				case 4:
					int testTimes[] = new int[] {0,100,200,300,400,500,600,700,800,900,1000};
					for(int x=0;x<testTimes.length;x++) {
						double averageTime = 0;
						for(int j=0; j<testTimes[x]; j++) {
							long startTime = System.currentTimeMillis(); //current time
							myserver.search("non-exiting file"); //testing program
							long endTime = System.currentTimeMillis(); //ending time
							averageTime += endTime-startTime;
						}
						averageTime /= testTimes[x];
						System.out.println("average running time under " + testTimes[x] + " requests :" + averageTime + "ms");
					}
					break;
				}
			}
					
				menu();
			 System.out.println("Bye!");
			 System.exit(0);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
