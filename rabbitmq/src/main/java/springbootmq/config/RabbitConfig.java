package springbootmq.config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring AMQP是一个Spring子项目，它提供了访问基于AMQP协议的消息服务器的解决方案。它包含两部分，springbootmq-ampq是基于AMQP协议的消息发送和接收的高层实现，springbootmq-rabbit是基于RabbitMQ的具体实现
 * 通过spring管理Connection,Channel对象，Consumer对象的创建,销毁
 */
@Configuration
public class RabbitConfig {
    @Resource
    private ChannelAwareMessageListener consumerDemo;
    /**
     * 创建RabbitMQ连接工厂对象，如果未修改默认配置，无需额外配置对象属性
     */
    @Bean("connectionFactory")
    public ConnectionFactory getConnectionFactory() {
        com.rabbitmq.client.ConnectionFactory rabbitConnectionFactory = new com.rabbitmq.client.ConnectionFactory();
        ConnectionFactory connectionFactory = new CachingConnectionFactory(rabbitConnectionFactory);
        return connectionFactory;
    }

    /**
     * 构建RabbitAmdin对象，它负责创建Queue/Exchange/Bind对象
     */
    @Bean(name = "rabbitAdmin")
    public RabbitAdmin getRabbitAdmin() {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(getConnectionFactory());
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    /**
     * RabbitMQ消息转化器，用于将RabbitMQ消息转换为AMQP消息，我们这里使用基本的Message Converter
     */
    @Bean(name = "serializerMessageConverter")
    public MessageConverter getMessageConverter() {
        return new SimpleMessageConverter();
    }

    /**
     * Message Properties转换器，用于在spring-amqp Message对象中的Message Properties和RabbitMQ的Message Properties对象之间互相转换
     */
    @Bean(name = "messagePropertiesConverter")
    public MessagePropertiesConverter getMessagePropertiesConverter() {
        return new DefaultMessagePropertiesConverter();
    }

    /**
     *定义发送消息所用的RabbitTemplate对象，RPC场景要求发送之后立即从消费者处获得返回消息，因此在RabbitTemplate对象中设置了ReplyAddress，而且在下面的MessageListenerContainer中将这个对象作为Listener设置到Container中
     */
    @Bean(name="rabbitTemplate")
    public RabbitTemplate getRabbitTemplate()
    {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(getConnectionFactory());
        rabbitTemplate.setUseTemporaryReplyQueues(false);
        rabbitTemplate.setMessageConverter(getMessageConverter());
        rabbitTemplate.setMessagePropertiesConverter(getMessagePropertiesConverter());
        rabbitTemplate.setReplyAddress(AppConstants.SPRING_QUEUE_REPLY1);
        rabbitTemplate.setReceiveTimeout(60000);
        return rabbitTemplate;
    }

    /**
     * 使用RabbitAdmin对象定义发送消息Exchange/Queue/Binding,返回消息Exchange/Queue/Binding对象
     */
    @Bean(name="springMessageQueue")
    public Queue createQueue(@Qualifier("rabbitAdmin")RabbitAdmin rabbitAdmin)
    {
        Queue sendQueue = new Queue(AppConstants.SPRING_QUEUE_SEND1,true,false,false);
        rabbitAdmin.declareQueue(sendQueue);
        return sendQueue;
    }

    @Bean(name="springMessageExchange")
    public Exchange createExchange(@Qualifier("rabbitAdmin")RabbitAdmin rabbitAdmin)
    {
        DirectExchange sendExchange = new DirectExchange(AppConstants.SPRING_EXCHANGE_SEND1,true,false);
        rabbitAdmin.declareExchange(sendExchange);
        return sendExchange;
    }

    @Bean(name="springMessageBinding")
    public Binding createMessageBinding(@Qualifier("rabbitAdmin")RabbitAdmin rabbitAdmin,@Qualifier("springMessageQueue")Queue queue)
    {
        Map<String,Object> arguments = new HashMap<String,Object>();
        Binding sendMessageBinding =
                new Binding(AppConstants.SPRING_QUEUE_SEND1, Binding.DestinationType.QUEUE,
                        AppConstants.SPRING_EXCHANGE_SEND1, AppConstants.SPRING_KEY_SEND1, arguments);
        rabbitAdmin.declareBinding(sendMessageBinding);
        return sendMessageBinding;
    }

    @Bean(name="springReplyMessageQueue")
    public Queue createReplyQueue(@Qualifier("rabbitAdmin")RabbitAdmin rabbitAdmin)
    {
        Queue replyQueue = new Queue(AppConstants.SPRING_QUEUE_REPLY1,true,false,false);
        rabbitAdmin.declareQueue(replyQueue);
        return replyQueue;
    }

    @Bean(name="springReplyMessageExchange")
    public Exchange createReplyExchange(@Qualifier("rabbitAdmin")RabbitAdmin rabbitAdmin)
    {
        DirectExchange replyExchange = new DirectExchange(AppConstants.SPRING_EXCHANGE_REPLY1,true,false);
        rabbitAdmin.declareExchange(replyExchange);
        return replyExchange;
    }

    @Bean(name="springReplyMessageBinding")
    public Binding createReplyMessageBinding(@Qualifier("rabbitAdmin")RabbitAdmin rabbitAdmin)
    {
        Map<String,Object> arguments = new HashMap<String,Object>();
        Binding replyMessageBinding =
                new Binding(AppConstants.SPRING_QUEUE_REPLY1, Binding.DestinationType.QUEUE,
                        AppConstants.SPRING_EXCHANGE_REPLY1, AppConstants.SPRING_KEY_REPLY1, arguments);
        rabbitAdmin.declareBinding(replyMessageBinding);
        return replyMessageBinding;
    }

    /**
     * 定义接收返回消息的Message Listener Container，监听对象为consumer
     */
    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(){
        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
        simpleMessageListenerContainer.setConnectionFactory(getConnectionFactory());
        simpleMessageListenerContainer.setQueueNames(AppConstants.SPRING_QUEUE_SEND1);
        simpleMessageListenerContainer.setMessageConverter(getMessageConverter());
        simpleMessageListenerContainer.setMessageListener(consumerDemo);
        simpleMessageListenerContainer.setRabbitAdmin(getRabbitAdmin());
        simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return simpleMessageListenerContainer;
    }
    /**
     * 定义接收返回消息的Message Listener Container,这里的Listener属性设置的是上面创建的RabbitTemplate对象
     */
    @Bean(name="replyMessageListenerContainer")
    public SimpleMessageListenerContainer createReplyListenerContainer() {
        SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer();
        listenerContainer.setConnectionFactory(getConnectionFactory());
        listenerContainer.setQueueNames(AppConstants.SPRING_QUEUE_REPLY1);
        listenerContainer.setMessageConverter(getMessageConverter());
        listenerContainer.setMessageListener(getRabbitTemplate());
        listenerContainer.setRabbitAdmin(getRabbitAdmin());
        listenerContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return listenerContainer;
    }


    /**
     * ThreadPoolExecutor对象，用于RabbitTemplate异步执行，发送和接收消息使用
     */
    @Bean(name="threadExecutor")
    public ThreadPoolTaskExecutor createThreadPoolTaskExecutor()
    {
        ThreadPoolTaskExecutor threadPoolTaskExecutor =
                new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(200);
        threadPoolTaskExecutor.setKeepAliveSeconds(20000);
        return threadPoolTaskExecutor;
    }
}
