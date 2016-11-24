package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.concurrent.EventLoopThread;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.ChannelConnector;

public abstract class AbstractChannelService implements ChannelService {


	protected InetSocketAddress		serverAddress		= null;
	protected boolean				active			= false;
	protected ReentrantLock			activeLock		= new ReentrantLock();
	protected BaseContext			context			= null;
	protected SelectableChannel		selectableChannel	= null;
	protected SelectorLoop[]		selectorLoops		= null;
	protected EventLoopThread[]		selectorLoopThreads	= null;

	public AbstractChannelService(BaseContext context) {
		this.context = context;
	}

	public BaseContext getContext() {
		return context;
	}

	public SelectableChannel getSelectableChannel() {
		return selectableChannel;
	}

	protected abstract void initselectableChannel() throws IOException;

	protected abstract SelectorLoop newSelectorLoop(SelectorLoop[] selectorLoops) throws IOException;

	protected void initSelectorLoops() throws IOException {

		ServerConfiguration configuration = context.getServerConfiguration();

		int core_size = configuration.getSERVER_CORE_SIZE();

		this.selectorLoops = new SelectorLoop[core_size];

		this.selectorLoopThreads = new EventLoopThread[core_size];

		for (int i = 0; i < core_size; i++) {

			selectorLoops[i] = newSelectorLoop(selectorLoops);
		}

		for (int i = 0; i < core_size; i++) {
			selectorLoops[i].startup();
		}

		for (int i = 0; i < core_size; i++) {

			selectorLoopThreads[i] = new EventLoopThread(selectorLoops[i], getServiceDescription(i));

			selectorLoops[i].setMonitor(selectorLoopThreads[i].getMonitor());

			selectorLoopThreads[i].startup();
		}
	}
	
	protected void cancelService(){
		
		ReentrantLock lock = this.activeLock;

		lock.lock();

		try {

			if (!active) {
				return;
			}

			destroySelectorLoops();

		} finally {

			active = false;

			LifeCycleUtil.stop(context);

			lock.unlock();
		}
		
	}

	protected void service() throws IOException {

		ReentrantLock lock = this.activeLock;

		lock.lock();

		try {

			if (active) {
				return ;
			}

			if (context == null) {
				throw new IllegalArgumentException("null nio context");
			}
			
			this.context.setChannelService(this);

			ServerConfiguration configuration = context.getServerConfiguration();
			
			if (this instanceof ChannelConnector) {
				configuration.setSERVER_CORE_SIZE(1);
			}

			LifeCycleUtil.start(context);

			this.initselectableChannel();
			
			this.initService(configuration);

			this.active = true;

		} finally {

			lock.unlock();
		}
	}
	
	public InetSocketAddress getServerSocketAddress() {
		return serverAddress;
	}
	
	protected abstract void initService(ServerConfiguration configuration) throws IOException;

	protected void destroySelectorLoops() {

		if (selectorLoopThreads == null) {
			return;
		}

		for (int i = 0; i < selectorLoopThreads.length; i++) {

			LifeCycleUtil.stop(selectorLoopThreads[i]);
		}
	}

	private String getServiceDescription(int i) {
		return "io-process-" + i;
	}
}
