package classes;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringBufferInputStream;
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
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			Document dom;

			InputStream inputStream;
			OutputStream outputStream;

			try {
				inputStream = socket.getInputStream();
				outputStream = socket.getOutputStream();
				PrintWriter printWriter = new PrintWriter(outputStream);
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				boolean streamSend = false;
				boolean requestSend = false;
				boolean authenticationSend = false;
				boolean rosterSend = false;
				String incoming = "";
				while (!socket.isClosed()) {

					String line = "";
					Stream stream = null;
					line = reader.readLine();
					while ((line = reader.readLine()) != null) {
						System.out.println("incoming: " + line);
						try {
							if (!streamSend) {
								line += "</stream:stream>";

								try {
									DocumentBuilder builder = dbf.newDocumentBuilder();
									dom = builder.parse(new InputSource(new StringReader(line)));
									String to = dom.getDocumentElement().getAttribute("to");
									printWriter
											.write("<stream:stream  xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' id='c2s_345'  from='"
													+ to + "'  version='1.0'>");
									printWriter.write("<stream:features></stream:features>");
									printWriter.flush();
									streamSend = true;
									System.out.println("</stream>");
								} catch (ParserConfigurationException | SAXException | IOException e1) {
									System.out.println(e1);
								}

							} else if (!requestSend) {

								incoming += line;
								if (line.equals("</iq>")) {

									DocumentBuilder builder = dbf.newDocumentBuilder();
									dom = builder.parse(new InputSource(new StringReader(incoming)));
									String id = dom.getDocumentElement().getAttribute("id");
									printWriter.write("<iq type='result' id='" + id + "'>");
									printWriter.write(
											"<query xmlns='jabber:iq:auth'> <username/> <password/> <digest/> <resource/> </query>");
									printWriter.write("</iq>");
									printWriter.flush();
									requestSend = true;									
									System.out.println("</iq>");
									incoming = "";
								}
							} else if (!authenticationSend) {
								incoming += line;
								if (line.equals("</query>")) {
									incoming += "</iq>";
									DocumentBuilder builder = dbf.newDocumentBuilder();
									dom = builder.parse(new InputSource(new StringReader(incoming)));
									String id = dom.getDocumentElement().getAttribute("id");
									printWriter.write("<iq type='result' id='" + id + "'/>");
									printWriter.flush();
									System.out.println("</query>");
									authenticationSend = true;
									incoming = "";
								}
							} else if(!rosterSend){
								incoming += line;
								if (line.equals("</iq>")) {
									incoming = incoming.substring(5);
									System.out.println(incoming);
									DocumentBuilder builder = dbf.newDocumentBuilder();
									dom = builder.parse(new InputSource(new StringReader(incoming)));
									String id = dom.getDocumentElement().getAttribute("id");

									printWriter.write(
											"<iq id='"+id+"'  type='result'> <query xmlns='jabber:iq:roster' ver='ver7'> </query> </iq>");
									printWriter.flush();
									incoming = "";
									rosterSend = true;
									System.out.println("</roster>");
								}
							}
						} catch (Exception e) {
							System.out.println(e);
						}

					}

				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}

	}

}
