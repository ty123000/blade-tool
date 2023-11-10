package org.springblade.core.mq;


/**
 * 事务消息发送模板
 */
public interface TransactionMQTemplate {

	/**
	 * 发送普通消息
	 */
	void send(String topic, Object message);

	/**
	 * 发送顺序消息
	 */
	void sendOrderly(String topic, Object message, String hashKey);
}
