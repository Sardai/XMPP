package classes;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 */

/**
 * @author chris, Viradj
 *
 */
public class Server {
	private List<ClientThread> clientThreadList;

	public void start() {
		clientThreadList = new ArrayList<>();
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(300);

			System.out.println("Server started, waiting for clients.");
			while (!serverSocket.isClosed()) {
				Socket connectionSocket = serverSocket.accept();
				ClientThread clientThread = new ClientThread(connectionSocket);
				clientThreadList.add(clientThread);
				// socketList.add(connectionSocket);
				clientThread.start();
				System.out.println("Client connected");
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private class ClientThread extends Thread {
		private Socket socket;
		private String nickname;

		public ClientThread(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			InputStream inputStream;
			OutputStream outputStream;
			
			try {
				while (!socket.isClosed()) {
					inputStream = socket.getInputStream();
					outputStream = socket.getOutputStream();
					PrintWriter printWriter = new PrintWriter(outputStream);
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

					String line = "";
					Stream stream = null;
					while ((line = reader.readLine()) != null) {
						System.out.println(line);
						line+="</stream:stream>";
						try{
						stream = JAXB.unmarshal(new StringReader(line),Stream.class);
						System.out.println(stream.to);
						
						Stream out = new Stream();
						out.from = stream.to;
						int id = 1;
						StringWriter writer = new StringWriter();									
						JAXB.marshal(out, writer);
						 
						printWriter.write("<?xml version='1.0'?>");
						printWriter.write(writer.toString().replace("/", ""));
					
						
					     printWriter.write("<stream:features>");
					     printWriter.write("</stream:features>");
						//printWriter.write("<stream:features> <starttls xmlns='urn:ietf:params:xml:ns:xmpp-tls'> <required/> </starttls> <mechanisms xmlns='urn:ietf:params:xml:ns:xmpp-sasl'> <mechanism>DIGEST-MD5</mechanism> <mechanism>PLAIN</mechanism> <mechanism>EXTERNAL</mechanism> </mechanisms> </stream:features>");
					     printWriter.flush();
					     System.out.println(writer);
						}catch(Exception e){
						}
						
//						Scanner scanner = new Scanner(line);
//						while(scanner.hasNext()){
//							String part = scanner.next();
//							if(part.startsWith("to")){
//								
//							}
//						}

					}
				 

				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}

 

	}

}
