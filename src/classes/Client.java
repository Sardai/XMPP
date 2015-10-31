/**
 * 
 */
package classes;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chris
 *
 */
public class Client {
	private String to;
	private String name;
	private int id;
	private static int lastId;
	private ArrayList<Client> roster = new ArrayList<Client>();
	
	public Client(String to, String name){
		this.to = to;
		this.name = name;
		lastId++;
		id = lastId;
	}
	
	public String getTo(){
		return to;
	}
	
	public String getName(){
		return name;
	}
	
	public void addFriend(Client client){
		roster.add(client);
	}
	
	public ArrayList<Client> getRoster(){
		return roster;
	}
	
	public String getRosterXML(String to, String id){
		
		String iq = "<iq to=\"" + to + "\" type=\"result\" id=\"" + id + "\">\n";
		String query = "<query xmlns=\"jabber:iq:roster\">\n";
 
		String items = "<item jid=\"test@server.com\" name=\"test\" subscription=\"both\">	\n</item>\n" +
						"<item jid=\"viradj@server.com\" name=\"viradj\" subscription=\"both\">	\n</item>\n";
 
		for(Client c : roster){
			items += "<item jid=\"" + c.getTo() + "\" name=\"" + c.getName() + "\" subscription=\"both\">	\n</item>\n";
		}
		String close = "</query>\n</iq>";
		
		return iq + query + items + close;
		
	}
	
//	public static void main(String[] args){
//		Client client1 = new Client("chris@rötter.com", "Chris");
//		Client client2 = new Client("viri@jay.com", "Viradj");
//		Client client3 = new Client("harm@frielink.com", "Harm");
//		Client client4 = new Client("rogier@hommels.com", "Rogier");
//		
//		client1.addFriend(client2);
//		client1.addFriend(client3);
//		client1.addFriend(client4);
//		
//		System.out.println(client1.getRosterXML(client1.getTo(), "roster_1"));
//
//
//	}
}
