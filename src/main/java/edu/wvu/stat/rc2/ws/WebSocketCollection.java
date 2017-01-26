package edu.wvu.stat.rc2.ws;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wvu.stat.rc2.ws.response.BaseResponse;

public class WebSocketCollection {
	static final Logger log = LoggerFactory.getLogger("rc2.WebSocketCollection");

	private final List<RCSessionSocket> _webSockets;
	private final ObjectMapper _mapper;
	private final ObjectMapper _msgPackMapper;
	private final ExecutorService _executor;

	public WebSocketCollection(ObjectMapper mapper, ObjectMapper msgPackMapper) {
		_webSockets = new ArrayList<RCSessionSocket>();
		_mapper = mapper;
		_msgPackMapper = msgPackMapper;
		_executor = Executors.newSingleThreadExecutor();
	}
	
	public int size() { return _webSockets.size(); }
	
	public void add(RCSessionSocket socket) {
		_executor.execute(() -> _webSockets.add(socket));
	}
	
	public void remove(RCSessionSocket socket) {
		_executor.execute(() -> _webSockets.remove(socket));
	}

	private void sendMessage(Object message, RCSessionSocket socket) {
		try {
			socket.sendResponse(message);
		} catch (Exception e) {
			log.warn("error sending message", e);
		}
	}
	
	private Object serializeResponse(BaseResponse response) throws JsonProcessingException {
		if (response.isBinaryMessage())
			return _msgPackMapper.writeValueAsBytes(response);
		return _mapper.writeValueAsString(response);
	}
	
	public void broadcastToAllClients(BaseResponse response) {
		try {
			Object serializedResponse = serializeResponse(response);
			log.info("broadcasting: " + response.getMsg());
			_executor.execute( () -> {
				_webSockets.forEach(socket -> {
					sendMessage(serializedResponse, socket);
				});
			});
		} catch (JsonProcessingException e) {
			log.warn("error broadcasting a all users", e);
		}
	}

	public void broadcastToSingleClient(BaseResponse response, int socketId) {
		try {
			Object serializedResponse = serializeResponse(response);
			RCSessionSocket socket = _webSockets.stream().filter(p -> p.getSocketId() == socketId).findFirst().get();
			log.info(String.format("sending to %d: %s", socket.getUserId(), response.getMsg()));
			_executor.execute(() -> socket.sendResponse(serializedResponse));
		} catch (Exception e) {
			log.warn("error sending single message", e);
		}
	}
}
