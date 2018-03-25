package springbootmq.service.consumer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springbootmq.config.AppConstants;
@Service
public class ConsumerDemo implements ChannelAwareMessageListener {
    @Autowired
    private MessagePropertiesConverter messagePropertiesConverter;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        MessageProperties messageProperties = message.getMessageProperties();
        AMQP.BasicProperties rabbitMQProperties =
                messagePropertiesConverter.fromMessageProperties(messageProperties, "UTF-8");
        String numberContent = null;
        numberContent = new String(message.getBody(), "UTF-8");
        System.out.println("The received number is:" + numberContent);
        String consumerTag = messageProperties.getConsumerTag();
        int number = Integer.parseInt(numberContent);

        String result = ++number + "";

        AMQP.BasicProperties replyRabbitMQProps =
                new AMQP.BasicProperties("text/plain",
                        "UTF-8",
                        null,
                        2,
                        0, rabbitMQProperties.getCorrelationId(), null, null,
                        null, null, null, null,
                        consumerTag, null);
        Envelope replyEnvelope =
                new Envelope(messageProperties.getDeliveryTag(), true,
                        AppConstants.SPRING_EXCHANGE_REPLY1, AppConstants.SPRING_KEY_REPLY1);

        MessageProperties replyMessageProperties =
                messagePropertiesConverter.toMessageProperties(replyRabbitMQProps,
                        replyEnvelope, "UTF-8");

        Message replyMessage = MessageBuilder.withBody(result.getBytes())
                .andProperties(replyMessageProperties)
                .build();

        rabbitTemplate.send(AppConstants.SPRING_EXCHANGE_REPLY1, AppConstants.SPRING_KEY_REPLY1, replyMessage);
        channel.basicAck(messageProperties.getDeliveryTag(), false);
    }
}
