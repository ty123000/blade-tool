package org.springblade.core.mq;

import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@AllArgsConstructor
public class KafkaMQTemplate implements MQTemplate {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Override
	public void send(String topic, Object message) {
		kafkaTemplate.send(topic, message);
	}

	@Override
	public void sendOrderly(String topic, Object message, String hashKey) {
		kafkaTemplate.send(topic, hashKey, message);
	}


	@Override
	public <R> R sendTransaction(String topic, Function<TransactionMQTemplate, R> message) {
		return kafkaTemplate.executeInTransaction(kafkaOperations -> {
			TransactionMQTemplate transactionMQTemplate = new KafkaTransactionMQTemplate(kafkaOperations);
			return message.apply(transactionMQTemplate);
		});
	}

	static class KafkaTransactionMQTemplate implements TransactionMQTemplate {
		private final KafkaOperations<String, Object> kafkaOperations;

		public KafkaTransactionMQTemplate(KafkaOperations<String, Object> kafkaOperations) {
			this.kafkaOperations = kafkaOperations;
		}

		@Override
		public void send(String topic, Object message) {
			kafkaOperations.send(topic, message);
		}

		@Override
		public void sendOrderly(String topic, Object message, String hashKey) {
			kafkaOperations.send(topic, hashKey, message);
		}
	}

}
