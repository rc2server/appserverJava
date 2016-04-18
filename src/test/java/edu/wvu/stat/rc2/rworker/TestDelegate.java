package edu.wvu.stat.rc2.rworker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wvu.stat.rc2.config.SessionConfig;
import edu.wvu.stat.rc2.config.SessionConfigImpl;
import edu.wvu.stat.rc2.persistence.Rc2DAO;
import edu.wvu.stat.rc2.ws.response.BaseResponse;

public class TestDelegate implements RWorker.Delegate {
	int workspaceId;
	int sessionRecordId;
	int socketId;
	ObjectMapper mapper;
	Rc2DAO dao;
	SessionConfig config = new SessionConfigImpl();
	Consumer<BaseResponse> broadcastLambda;
	BiConsumer<BaseResponse, Integer> singleBroadcastLambda;
	Consumer<Exception> connFailedLambda;
	Consumer<Exception> clientErrorLambda;
	final List<BaseResponse> messagesBroadcast;
	
	public TestDelegate() {
		this.workspaceId = 1;
		this.sessionRecordId = 101;
		this.messagesBroadcast = new ArrayList<BaseResponse>();
	}
	
	@Override
	public int getWorkspaceId() {
		return this.workspaceId;
	}

	@Override
	public int getSessionRecordId() {
		return this.sessionRecordId;
	}

	@Override
	public ObjectMapper getObjectMapper() {
		if (null == this.mapper)
			this.mapper = new ObjectMapper();
		return this.mapper;
	}

	@Override
	public Rc2DAO getDAO() {
		return this.dao;
	}

	@Override
	public SessionConfig getSessionConfig() {
		return this.config;
	}
	
	@Override
	public void broadcastToAllClients(BaseResponse response) {
		if (null != this.broadcastLambda) {
			this.broadcastLambda.accept(response);
		} else {
			this.messagesBroadcast.add(response);
		}
	}

	@Override
	public void clientHadError(Exception e) {
		if (null != this.clientErrorLambda)
			this.clientErrorLambda.accept(e);
	}

	@Override
	public void connectionFailed(Exception e) {
		if (null != this.connFailedLambda)
			this.connFailedLambda.accept(e);
	}

	@Override
	public void broadcastToSingleClient(BaseResponse response, int destSocketId) {
		if (socketId > 0 && socketId == destSocketId) {
			if (null != this.singleBroadcastLambda) {
				this.singleBroadcastLambda.accept(response, socketId);
			} else {
				this.messagesBroadcast.add(response);
			}
		}
	}

}
