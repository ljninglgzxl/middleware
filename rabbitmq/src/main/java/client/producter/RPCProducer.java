package client.producter;

import client.RabbitMQConfig;
import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.AMQBasicProperties;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 生产者和消费者之间的消息发送/接收流程如下:
 1)生产者在发送消息的同时，将返回消息的消息队列名(replyTo中指定)以及消息关联Id(correlationId)附带在消息Properties中发送给消费者。
 2)消费者在接收到消息，处理完成后，将结果作为返回消息发送到replyTo指定的返回消息队列中，同时附带接收消息中的corrleationId, 以便让生产者接收到到返回消息后，根据corrleationId确认是针对1)中发送消息的返回消息，如果correlationId确认一致，则将返回消息 取出，进行后续处理。
 */
public class RPCProducer {
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = RabbitMQConfig.getConnection();
        Channel channel=connection.createChannel();
        /**建立RPC返回消息的Direct Exchange, 消息队列和绑定关系
         * 发送队列使用已创建的DirectQueue:"client.queue.direct.send"
         */
        channel.queueDeclare("client.queue.direct.reply", true, false, false, null);
        channel.exchangeDeclare("client.exchange.direct.reply", "direct", true);
        channel.queueBind( "client.queue.direct.reply","client.exchange.direct.reply", "client.direct.replyKey");
        String correlationId = System.currentTimeMillis() + "correlationId";
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties().builder().correlationId(correlationId).replyTo("client.exchange.direct.reply").build();
        channel.basicPublish("client.exchange.direct.send", "client.direct.key",basicProperties,"senRPCMessage".getBytes());

        /**
         * 创建接收RPC返回消息的消费者，并将它与RPC返回消息队列相关联。
         */
        DefaultConsumer replyConsumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                /**
                 * 如果消息的CorrelationId与发送消息的CorrleationId一致，表示这条消息是发送消息对应的返回消息
                 */
                if(correlationId.equals(properties.getCorrelationId()))
                System.out.println(properties.getCorrelationId()+"          \n"+envelope.toString());

            }
        };
        channel.basicConsume("client.queue.direct.reply", true, replyConsumer);
    }
}
