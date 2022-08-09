package info.unterrainer.commons.overmindjsonata4javaserver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import com.api.jsonata4java.expressions.EvaluateException;
import com.api.jsonata4java.expressions.Expressions;
import com.api.jsonata4java.expressions.ParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
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
				log.info("New message received.");
				if (msg != null) {
					String query = new String(msg.pop().getData(), StandardCharsets.UTF_8);
					String json = new String(msg.pop().getData(), StandardCharsets.UTF_8);
					msg.destroy();
					log.info("Received query [{}]", query);
					log.debug("Received json [{}]", json);

					String result = jsonataQuery(query, json);

					log.info("Sending result [{}].", result);
					ZMsg reply = new ZMsg();
					reply.add(result.getBytes(StandardCharsets.UTF_8));
					reply.send(socket);
					reply.destroy();
				} else
					log.warn("Message was null.");
			}
		}
	}

	// Gets a query and a json-array to query against and returns 'true' or not.
	private String jsonataQuery(final String query, final String json) {
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

		Expressions expr = null;
		try {
			expr = Expressions.parse(query);
		} catch (ParseException | IOException e) {
			log.error("Error parsing query-expression.", e);
			return null;
		}
		JsonNode jsonResult = null;
		try {
			jsonResult = expr.evaluate(jsonObj);
		} catch (EvaluateException e) {
			log.error("Error evaluating query-expression on json.", e);
			return null;
		}
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonResult);
		} catch (JsonProcessingException e) {
			log.error("Error serializing result to string.", e);
			return null;
		}
	}
}
