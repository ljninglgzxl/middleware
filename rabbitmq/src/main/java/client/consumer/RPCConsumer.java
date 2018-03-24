package client.consumer;

import client.RabbitMQConfig;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RPCConsumer {
    public static void main(String[] args) throws IOException, TimeoutException {
        Channel channel = RabbitMQConfig.getConnection().createChannel();
        /**
         * 接收到消息后回复消息到replyTo对应的exchange，附带correlationId
         */
        DefaultConsumer defaultConsumer=new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println(new String(body, "utf-8") + "        receive success");
                AMQP.BasicProperties replyBasicProperties = new AMQP.BasicProperties().builder().correlationId(properties.getCorrelationId()).build();
                channel.basicPublish(properties.getReplyTo(),"client.direct.replyKey",replyBasicProperties,"replyMessage".getBytes());
            }
        };
        channel.basicConsume("client.queue.direct.send", true, defaultConsumer);
    }
}
