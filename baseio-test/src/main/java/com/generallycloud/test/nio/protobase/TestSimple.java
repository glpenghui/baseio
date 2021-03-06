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
package com.generallycloud.test.nio.protobase;

import com.generallycloud.baseio.codec.protobase.ProtobaseProtocolFactory;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseReadFutureImpl;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.SharedBundle;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.connector.SocketChannelConnector;
import com.generallycloud.baseio.protocol.ReadFuture;

public class TestSimple {
	
	
	public static void main(String[] args) throws Exception {

		SharedBundle.instance().loadAllProperties("nio");

		String serviceKey = "/test-simple";
		
		String param = "ttt";
		
		IoEventHandleAdaptor eventHandle = new IoEventHandleAdaptor() {
			
			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {
				System.out.println("________________________"+future.getReadText());
			}
		};
		
		SocketChannelContext context = new NioSocketChannelContext(new ServerConfiguration(18300));
		
		SocketChannelConnector connector = new SocketChannelConnector(context);

		context.setProtocolFactory(new ProtobaseProtocolFactory());
		
		context.addSessionEventListener(new LoggerSocketSEListener());
		
		context.setIoEventHandleAdaptor(eventHandle);
		
		SocketSession session = connector.connect();

		ProtobaseReadFuture f = new ProtobaseReadFutureImpl(connector.getContext(),serviceKey);
		
		f.write(param);
		
		session.flush(f);
		
		ThreadUtil.sleep(500);
		
		CloseUtil.close(connector);
	}
}
