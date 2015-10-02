/**
 * 
 */
package classes;

/**
 * @author chris
 *
 */
public class Client {
	private String to;
	private int id;
	private static int lastId;
	
	public Client(String to){
		this.to = to;
		lastId++;
		id = lastId;
	}
}
