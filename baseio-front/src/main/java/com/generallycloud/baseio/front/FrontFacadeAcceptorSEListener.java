/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.baseio.front;

import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.SocketSessionEventListenerAdapter;
import com.generallycloud.baseio.protocol.ReadFuture;

public class FrontFacadeAcceptorSEListener extends SocketSessionEventListenerAdapter {

	private Logger			logger	= LoggerFactory.getLogger(FrontFacadeAcceptorSEListener.class);

	private FrontContext	balanceContext;

	private FrontRouter		frontRouter;

	public FrontFacadeAcceptorSEListener(FrontContext balanceContext) {
		this.balanceContext = balanceContext;
		this.frontRouter = balanceContext.getFrontRouter();
	}

	@Override
	public void sessionOpened(SocketSession session) {
		frontRouter.addClientSession((FrontFacadeSocketSession) session);
		logger.info("client from [ {} ] connected.",session.getRemoteSocketAddress());
	}

	@Override
	public void sessionClosed(SocketSession session) {

		FrontFacadeSocketSession fs = (FrontFacadeSocketSession) session;

		frontRouter.removeClientSession(fs);

		logger.info("client from [ {} ] disconnected.",session.getRemoteSocketAddress());
		
		ChannelLostReadFutureFactory factory = balanceContext.getChannelLostReadFutureFactory();
		
		if (factory == null) {
			return;
		}

		SocketSession rs = balanceContext.getBalanceFacadeConnector().getSession();

		if (rs == null) {
			return;
		}

		ReadFuture future = factory.createChannelLostPacket(session);

		rs.flush(future);
	}
}
