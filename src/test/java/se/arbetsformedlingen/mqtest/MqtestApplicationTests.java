package se.arbetsformedlingen.mqtest;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { MqtestApplicationTests.class })
@Configuration
@Testcontainers
class MqtestApplicationTests {

	@Autowired
	JmsTemplate jmsTemplate;

	@Container
	public static GenericContainer<?> activeMQContainer = new GenericContainer<>(
			DockerImageName.parse("rmohr/activemq:5.15.9")).withExposedPorts(61616);

	@Test
	void assert_running() {
		assertTrue(activeMQContainer.isRunning());
		System.out.println(
				"ActiveMQ running on " + activeMQContainer.getHost() + ":" + activeMQContainer.getFirstMappedPort());
	}

	@Test
	void send_and_receive_messages() throws JMSException {
		String queueName = "queue-2";
		String messageText = "Test message";

		System.out.println("Sending text message '" + messageText + "' to queue '" + queueName + "'");
		jmsTemplate.convertAndSend(queueName, messageText);

		Object receivedMessage = jmsTemplate.receive(queueName);
		Assertions.assertThat(receivedMessage)
				.isInstanceOf(TextMessage.class);
			
				System.out.println("Received a message from queue '" + queueName + "' with text message '" + ((TextMessage) receivedMessage).getText() + "'");
		assertEquals(messageText, ((TextMessage) receivedMessage).getText());
	}

	@Bean
	public JmsListenerContainerFactory<?> jmsListenerContainerFactory() {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory());
		return factory;
	}

	@Bean
	public ConnectionFactory connectionFactory() {
		String brokerUrlFormat = "tcp://%s:%d";
		String brokerUrl = String.format(brokerUrlFormat, activeMQContainer.getHost(),
				activeMQContainer.getFirstMappedPort());

		return new ActiveMQConnectionFactory(brokerUrl);
	}

	@Bean
	public JmsTemplate jmsTemplate() {
		return new JmsTemplate(connectionFactory());
	}
}
