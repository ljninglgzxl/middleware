package client.producter;
import client.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
public class Producer {
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = null;
        Channel channel = null;
        try {
            /**
             *建立Connection,Channel
             */
//            ConnectionFactory connectionFactory = new ConnectionFactory();
//            connectionFactory.setHost("localhost");
//            connectionFactory.setPort(5672);
//            connectionFactory.setVirtualHost("/");
//            connectionFactory.setUsername("guest");
//            connectionFactory.setPassword("guest");
            connection = RabbitMQConfig.getConnection();
            channel = connection.createChannel();
            /**
             * Channel建立后，调用Channel.queueDeclare方法创建消息队列firstQueue
             * 这个方法的第二个参数durable表示建立的消息队列是否是持久化(RabbitMQ重启后仍然存在,并不是指消息的持久化),第三个参数exclusive 表示建立的消息队列是否只适用于当前TCP连接，第四个参数autoDelete表示当队列不再被使用时，RabbitMQ是否可以自动删除这个队列。 第五个参数arguments定义了队列的一些参数信息，主要用于Headers Exchange进行消息匹配时
             */
            channel.queueDeclare("client.queue.send1", true, false, false, null);
            /**生产者发送消息使用Channel.basicPublish方法
             * 第一个参数exchange是消息发送的Exchange名称，如果没有指定，则使用Default Exchange。 第二个参数routingKey是消息的路由Key，是用于Exchange将消息路由到指定的消息队列时使用(如果Exchange是Fanout Exchange，这个参数会被忽略), 第三个参数props是消息包含的属性信息。
             */
            channel.basicPublish("", "client.queue.send1", null, (System.currentTimeMillis() + "sendMessage").getBytes());

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
