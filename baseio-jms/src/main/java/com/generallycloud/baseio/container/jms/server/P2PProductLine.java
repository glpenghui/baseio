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
package com.generallycloud.baseio.container.jms.server;

import java.io.IOException;
import java.util.List;

import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.container.jms.Message;

public class P2PProductLine extends AbstractProductLine {

	private Logger	logger	= LoggerFactory.getLogger(P2PProductLine.class);

	public P2PProductLine(MQContext context) {
		super(context);
	}

	@Override
	protected ConsumerQueue createConsumerQueue() {
		return new P2PConsumerQueue();
	}

	// FIXME 完善消息匹配机制
	@Override
	public void doLoop() {

		Message message = storage.poll(16);

		if (message == null) {
			return;
		}

		String queueName = message.getQueueName();

		ConsumerQueue consumerQueue = getConsumerQueue(queueName);

		List<Consumer> consumers = consumerQueue.getSnapshot();

		if (consumers.size() == 0) {

			filterUseless(message);

			return;
		}

		for (Consumer consumer : consumers) {
			try {
				consumer.push(message);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				this.offerMessage(message);
			}
		}

		context.consumerMessage(message);
	}

	public int messageSize() {
		return storage.size();
	}
}
