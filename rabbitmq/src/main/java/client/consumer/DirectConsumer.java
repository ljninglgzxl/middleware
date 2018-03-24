package client.consumer;

import client.RabbitMQConfig;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class DirectConsumer {
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection=null;
        Channel channel=null;
        try {
            connection = RabbitMQConfig.getConnection();
            channel = connection.createChannel();
            Channel finalChannel = channel;
            //Direct Exchange消费
            DefaultConsumer directConsumer = new DefaultConsumer(finalChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    System.out.println(envelope.getExchange());
                }
            };
            finalChannel.basicConsume("client.queue.direct.send", true, directConsumer);
        }
        catch (Exception e)
        {
            System.out.println("consumer error");
        }
        finally {


        }
    }
    }

