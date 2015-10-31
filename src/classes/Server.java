package classes;

import java.io.BufferedReader;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author chris, Viradj
 * Server to which a xmpp client can connect.
 */
public class Server {
	private List<ClientThread> clientThreadList;

	Client client1 = new Client("jay.com", "viri");

	/**
	 * Start the server.
	 */
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

	/**
	 * 
	 * @author chris, Viradj
	 * Thread class to handle incoming and outgoing messages for the connected client.
	 */
	private class ClientThread extends Thread {
		private Socket socket;
		private String nickname;
		private String to;
		private PrintWriter printWriter;

		private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		private Document dom;

		private int step;

		/**
		 * Creates a niew ClientThread with the socket to which the client is connected.
		 * @param socket
		 */
		public ClientThread(Socket socket) {
			this.socket = socket;
		}

		public void run() {

			InputStream inputStream;
			OutputStream outputStream;

			try {
				inputStream = socket.getInputStream();
				outputStream = socket.getOutputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				boolean read = false;
				String incoming = "";
				printWriter = new PrintWriter(outputStream);
				//while socket is not closed keep monitoring the socket.
				while (!socket.isClosed()) {

					String line = "";
					line = reader.readLine();
					//while the buffer is not closed keep monitoring the buffer.
					while ((line = reader.readLine()) != null) {
						try {

							switch (step) {
							case 0:
								//client connects with the socket and request stream features.
								SendStream(line);
								break;							
							case 1:
								//client request the required parameters for authentication.
								incoming += line;
								if (line.equals("</iq>")) {
									sendParameters(incoming);
									incoming = "";
								}
								break;
							case 2:
								//client sends login parameters and request authentication.
								incoming += line;
								if (line.equals("</query>")) {
									sendAuthentication(incoming);
									incoming = "";
								}
								break;
							case 3:
								//client requests friend list(roster) and notifys it is online to the other 
								incoming += line;
								if (line.equals("</iq>")) {
									sendRoster(incoming);
									incoming = "";

								}
								break;
							case 4:
								//client sends message to other client.
								if (line.startsWith("<message")) {
									read = true;
								}

								if (read) {
									incoming += line;
									if (line.equals("</message>")) {
										System.out.println(incoming);
										sendMessage(incoming);
										read = false;
										incoming = "";
									}
								}
								break;
							}				
						} catch (SAXException | ParserConfigurationException | TransformerException e) {
							System.out.println("error: " + line);
						}
					}
					System.out.println("ended");

				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
		
		/**
		 * @return the fullname of the client.
		 */
		private String getFullName() {
			return nickname + "@" + to;
		}

		/**
		 * Lets the client know which stream features this server supports (none).
		 * @param line the string with the request for the stream features from the client  
		 */
		private void SendStream(String line) {
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
				step = 1;
				// System.out.println("</stream>");
			} catch (ParserConfigurationException | SAXException | IOException e1) {
				System.out.println(e1);
			}
		}

		/**
		 * Sends the parameters to the client with the fields the client needs to send back for authentication.
		 * @param incoming the message from the client to request the parameters.
		 * @throws SAXException 
		 * @throws IOException
		 * @throws ParserConfigurationException
		 */
		private void sendParameters(String incoming) throws SAXException, IOException, ParserConfigurationException {
			// System.out.println(incoming);
			DocumentBuilder builder = dbf.newDocumentBuilder();
			dom = builder.parse(new InputSource(new StringReader(incoming)));
			String id = dom.getDocumentElement().getAttribute("id");
			this.to = dom.getDocumentElement().getAttribute("to");
			this.nickname = dom.getDocumentElement().getFirstChild().getFirstChild().getFirstChild().getNodeValue();
			// System.out.println(nickname+"@"+to);
			printWriter.write("<iq type='result' id='" + id + "'>");
			printWriter.write("<query xmlns='jabber:iq:auth'> <username/> <password/> <digest/> <resource/> </query>");
			printWriter.write("</iq>");
			printWriter.flush();
			step = 2;
			// System.out.println("</iq>");
			incoming = "";
		}

		/**
		 * Send an ok message to let the client know it has authenticated.
		 * @param incoming the string from the client
		 * @throws SAXException
		 * @throws IOException
		 * @throws ParserConfigurationException
		 */
		private void sendAuthentication(String incoming) throws SAXException, IOException, ParserConfigurationException {
			incoming += "</iq>";
			DocumentBuilder builder = dbf.newDocumentBuilder();
			dom = builder.parse(new InputSource(new StringReader(incoming)));
			String id = dom.getDocumentElement().getAttribute("id");
			printWriter.write("<iq type='result' id='" + id + "'/>");
			printWriter.flush();
			// System.out.println("</query>");
			step = 3;
			incoming = "";
		}

		/**
		 * Sends the friend list(roster) to the connected client and notify all clients that this client is online.
		 * @param incoming the string for a request for a roster
		 * @throws SAXException
		 * @throws IOException
		 * @throws ParserConfigurationException
		 */
		private void sendRoster(String incoming) throws SAXException, IOException, ParserConfigurationException {
			// remove end tag at beginning of the line.
			incoming = incoming.substring(5);
			DocumentBuilder builder = dbf.newDocumentBuilder();
			dom = builder.parse(new InputSource(new StringReader(incoming)));
			String id = dom.getDocumentElement().getAttribute("id");

			// System.out.println("\n ---------- \n" +
			// client1.getRosterXML(client1.getTo(), id) + "\n ----------\n");

			printWriter.write(client1.getRosterXML(client1.getTo(), id));
			printWriter.flush();
			step = 4;

			for (ClientThread clientThread : clientThreadList) {
				clientThread.printWriter
						.write("<presence from='" + getFullName() + "' to='" + clientThread.getFullName() + "'/>");
				clientThread.printWriter.flush();

				printWriter
						.write("<presence from='" + clientThread.getFullName() + "' to='" + getFullName() + "'/>");
				printWriter.flush();
			}
		}

		/**
		 * receives message from a client and sends it to the right client.
		 * @param incoming the message
		 * @throws ParserConfigurationException
		 * @throws SAXException
		 * @throws IOException
		 * @throws TransformerException
		 */
		private void sendMessage(String incoming) throws ParserConfigurationException, SAXException, IOException, TransformerException {

			// System.out.println(incoming);
			DocumentBuilder builder = dbf.newDocumentBuilder();
			dom = builder.parse(new InputSource(new StringReader(incoming)));
			dom.getDocumentElement().setAttribute("from", getFullName());
			String to = dom.getDocumentElement().getAttribute("to");

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(dom), new StreamResult(writer));
			String output = writer.getBuffer().toString().replaceAll("\n|\r", "");

			for (ClientThread thread : clientThreadList) {
				if ((thread.getFullName()).equals(to)) {
					thread.printWriter.write(output);
					// System.out.println(output);
					thread.printWriter.flush();
					System.out.println("send message to:" + to);
				}
			}
		}
		
		

	}

}

// if (!streamSend) {
// SendStream(line);
//
// } else if (!requestSend) {
//
// incoming += line;
// if (line.equals("</iq>")) {
// sendParameters(incoming);
// incoming = "";
// }
// } else if (!authenticationSend) {
// incoming += line;
// if (line.equals("</query>")) {
// sendAuthentication(incoming);
// incoming = "";
// }
// } else if(!rosterSend){
// incoming += line;
// if (line.equals("</iq>")) {
// sendRoster(incoming);
// incoming = "";
//
// }
// }else{
// //System.out.println(line);
// if(line.startsWith("<message")){
// read = true;
// }
//
//
// if(read){
// incoming += line;
// if(line.equals("</message>")){
// System.out.println(incoming);
// sendMessage();
// read = false;
// incoming = "";
// }
// }
//
// }