package info.unterrainer.commons.overmindjsonata4javaserver.implementations;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import info.unterrainer.commons.overmindjsonata4javaserver.Server;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocketServer {

	public void run() throws IOException {

		try (ZContext context = new ZContext()) {
			// Socket to talk to clients
			ZMQ.Socket socket = context.createSocket(SocketType.REP);
			socket.bind("tcp://*:5555");

			while (!Thread.currentThread().isInterrupted())
				try {
					// Block until a message is received
					ZMsg msg = ZMsg.recvMsg(socket);
					log.info("New message received.");
					if (msg != null) {
						String query = new String(msg.pop().getData(), StandardCharsets.UTF_8);
						String json = new String(msg.pop().getData(), StandardCharsets.UTF_8);
						msg.destroy();
						log.info("Received query [{}]", query);
						log.debug("Received json [{}]", json);

						String result = Server.jsonataQuery(query, json);

						log.info("Sending result [{}].", result);
						ZMsg reply = new ZMsg();
						reply.add(result == null ? null : result.getBytes(StandardCharsets.UTF_8));
						reply.send(socket);
						reply.destroy();
					} else
						log.warn("Message was null.");
				} catch (Exception e) {
					log.error("uncaught exception", e);
				}
		}
	}
}
