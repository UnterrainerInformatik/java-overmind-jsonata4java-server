package info.unterrainer.commons.overmindjsonata4javaserver;

import java.io.IOException;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import com.api.jsonata4java.expressions.EvaluateException;
import com.api.jsonata4java.expressions.Expressions;
import com.api.jsonata4java.expressions.ParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Server {

	ObjectMapper mapper = new ObjectMapper();

	public static void main(final String[] args) throws Exception {
		try {
			new Server().run();
		} catch (Exception e) {
			log.error("uncaught exception", e);
		}
	}

	public void run() throws IOException {

		try (ZContext context = new ZContext()) {
			// Socket to talk to clients
			ZMQ.Socket socket = context.createSocket(SocketType.REP);
			socket.bind("tcp://*:5555");

			while (!Thread.currentThread().isInterrupted()) {
				// Block until a message is received
				ZMsg msg = ZMsg.recvMsg(socket);
				if (msg != null) {
					String query = msg.popString();
					String json = msg.popString();
					msg.destroy();

					String result = jsonataQuery(query, json);

					// Send a response
					ZMsg reply = new ZMsg();
					reply.add(result);
					reply.send(socket);
					reply.destroy();
				}
			}
		}
	}

	private String jsonataQuery(final String query, final String json) throws IOException {
		if (query == null) {
			log.error("Config-string of check was null.");
			return null;
		}

		if (json == null) {
			log.error("Json with data to query was null.");
			return null;
		}

		JsonNode jsonObj = null;
		try {
			jsonObj = mapper.readTree(json);
		} catch (IOException e) {
			log.error("Error reading state-array-json.", e);
		}

		String result = null;
		try {
			Expressions expr = Expressions.parse(query);
			JsonNode jsonResult = expr.evaluate(jsonObj);
			final Object obj = mapper.treeToValue(jsonResult, Object.class);
			result = mapper.writeValueAsString(obj);
		} catch (ParseException e) {
			log.error("Error parsing expression-json.", e);
			throw new IOException(e);
		} catch (EvaluateException e) {
			log.error("Error evaluating expression-json.", e);
			throw new IOException(e);
		}
		return result;
	}
}
