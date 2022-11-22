package info.unterrainer.commons.overmindjsonata4javaserver;

import java.io.IOException;

import com.api.jsonata4java.expressions.EvaluateException;
import com.api.jsonata4java.expressions.Expressions;
import com.api.jsonata4java.expressions.ParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.unterrainer.commons.overmindjsonata4javaserver.implementations.GRpcServer;
import info.unterrainer.commons.overmindjsonata4javaserver.implementations.SocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Server {

	static ObjectMapper mapper = new ObjectMapper();

	public static void main(final String[] args) throws Exception {
		try {
			GRpcServer gRpcServer = new GRpcServer();
			gRpcServer.run();
			new SocketServer().run();
			
		} catch (Exception e) {
			log.error("uncaught exception", e);
		}
	}

	// Gets a query and a json-array to query against and returns 'true' or not.
	public static synchronized String jsonataQuery(final String query, final String json) {
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
