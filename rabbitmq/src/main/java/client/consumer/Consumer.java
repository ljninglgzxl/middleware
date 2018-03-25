package client.consumer;
import client.RabbitMQConfig;
import com.rabbitmq.client.*;
import java.io.IOException;
public class Consumer {
    public static void main(String[] args) {
        Connection connection;
        Channel channel;
        try {
            connection = RabbitMQConfig.getConnection();
            channel = connection.createChannel();
            Channel finalChannel = channel;
            /**
             * 定义了一个Consumer对象
             *重载了DefaultCustomer类 的handleDelivery方法:方法体为具体消费逻辑
             */
            DefaultConsumer defaultConsumer = new DefaultConsumer(finalChannel) {
                /**
                 * @param consumerTag:接收到消息时的消费者Tag，如果我们没有在basicConsume方法中指定Consumer Tag，RabbitMQ将使用随机生成的Consumer Tag
                 * @param envelope:包含了四个属性: 1._deliveryTag，消息发送的编号，表示这条消息是RabbitMQ发送的第几条消息。
                                                    2._redeliver，重传标志，确认在收到对消息的失败确认后，是否需要重发这条消息。
                                                    3._exchange，消息发送到的Exchange名称，exchange名称为空，使用的是Default Exchange。
                                                    4._routingKey,消息发送的路由Key
                 * @param properties:basicPublish方法发送消息时的props参数
                 * @param body:消息体
                 */
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    super.handleDelivery(consumerTag, envelope, properties, body);
                    System.out.println(new String(body, "utf-8"));
                    /**
                     * 手动确认消息给消息队列
                     * basicAck方法有两个参数，第一个参数deliverTag是消息的发送编号，第二个参数multiple是消息确认方式，如果值为true,表示对消息队列里所有编号小于或等于当前消息编号的未确认消息进行手动确认，如果为false，表示仅确认当前消息
                     */
                    finalChannel.basicAck(envelope.getDeliveryTag(),false);
                }
            };
            /**
             * 调用Channel.basicConsume方法将Consumer与消息队列绑定，否则Consumer无法从消息队列获取消息
             * basicConsume方法的第一个参数是Consumer绑定的队列名，第二个参数是自动确认标志，如果为true，表示Consumer接受到消息后，会自动发确认消息(Ack消息)给消息队列，消息队列会将这条消息从消息队列里删除，第三个参数就是Consumer对象，用于处理接收到的消息
             */
            channel.basicConsume("client.queue.send1", defaultConsumer);
        }
        catch (Exception e)
        {
            System.out.println("consumer error");
        }
        finally {
            System.out.println("保持监听，不关闭connection");
        }
    }
    }

