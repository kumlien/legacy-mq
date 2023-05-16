package se.arbetsformedlingen.mqtest;


import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

import jakarta.jms.Queue;


@SpringBootApplication
public class MqtestApplication {

	public static void main(String[] args) {
		SpringApplication.run(MqtestApplication.class, args);
	}


	@Bean
	public Queue testQueue() {
		return new ActiveMQQueue("test_queue");
	}

	
	@Component
	public static class MessageCreator implements CommandLineRunner {

		final JmsMessagingTemplate messagingTemplate;

		final Queue testQueue;

		public MessageCreator(JmsMessagingTemplate messagingTemplate, Queue testQueue) {
			this.messagingTemplate = messagingTemplate;
			this.testQueue = testQueue;
		}

		@Override
		public void run(String... args) throws Exception {
			if(args.length == 0) {
				args = new String[]{"This is a default message..."};
			}
			System.out.println("Sending '" + args[0] + "'");
			messagingTemplate.convertAndSend(testQueue, args[0]);
		}

	}

	@Component
	public static class MessageReceiver {
		@JmsListener(destination = "test_queue")
		public void onMessage(Object message)  {
			System.out.println("Got a message..." + message);
		}
	}

	

}
