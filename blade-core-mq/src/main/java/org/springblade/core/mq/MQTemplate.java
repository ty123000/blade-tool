package org.springblade.core.mq;


import java.util.function.Function;

/**
 * 消息队列发送模板
 */
public interface MQTemplate {

	/**
	 * 发送普通消息
	 */
	void send(String topic, Object message);

	/**
	 * 发送顺序消息
	 */
	void sendOrderly(String topic, Object message, String hashKey);

	/**
	 * 发送事务消息
	 */
	<R> R sendTransaction(String topic, Function<TransactionMQTemplate, R> message);


	/**
	 * 事务消息发送模板
	 */
	interface TransactionMQTemplate {

		/**
		 * 发送普通消息
		 */
		void send(String topic, Object message);

		/**
		 * 发送顺序消息
		 */
		void sendOrderly(String topic, Object message, String hashKey);
	}
}
