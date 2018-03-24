package client.producter;

import client.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class DirectProducer {
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = null;
        Channel channel = null;
        try {

            connection = RabbitMQConfig.getConnection();
            channel = connection.createChannel();
            /**
             * 创建Direct Exchange,消息队列和绑定关系
             */
            channel.queueDeclare("client.queue.direct.send", true, false, false, null);
            /**exchangeDeclare方法的第一个参数exchange是exchange名称，第二个参数type是Exchange类型，有“direct”,“fanout”,“topic”,“headers”四种,分别对应RabbitMQ的四种Exchange。第三个参数durable是设置Exchange是否持久化*/
            channel.exchangeDeclare("client.exchange.direct.send", "direct", true);
            /** 添加创建Direct Exchange和 将Exchange和消息队列绑定
             * queueBind方法第一个参数queue是消息队列的名称，第二个参数exchange是Exchange的名称，第三个参数routingKey是消息队列和Exchange之间绑定的路由key，从Exchange过来的消息，只有routing key为“directMessage”的消息会被转到消息队列“directQueue”，其他消息将不会被转发*/
            channel.queueBind( "client.queue.direct.send","client.exchange.direct.send", "client.direct.key");
            /**
             * 向Exchange发送的消息如果和绑定的routing key不一致，没有被转发到“client.queue.direct.send”消息队列,就会被RabbitMQ丢弃
             */
            channel.basicPublish("client.exchange.direct.send", "client.direct.key", null, "client.sendDirectMessage".getBytes());
        } catch (Exception e) {
            System.out.println("send error");
        } finally {
            /**
             * 发送完毕关闭连接
             */
            if(channel!=null)
                channel.close();
            if(connection!=null)
                connection.close();
        }
    }
}
