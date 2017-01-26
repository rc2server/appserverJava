package edu.wvu.stat.rc2;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

public class SynchronousExecutor extends AbstractExecutorService {
	private volatile boolean terminated;
	
	@Override
	public void execute(Runnable command) {
		command.run();
	}

	@Override
	public void shutdown() {
		terminated = true;
	}

	@Override
	public List<Runnable> shutdownNow() {
		return Collections.emptyList();
	}

	@Override
	public boolean isShutdown() {
		return terminated;
	}

	@Override
	public boolean isTerminated() {
		return terminated;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return false;
	}

}
