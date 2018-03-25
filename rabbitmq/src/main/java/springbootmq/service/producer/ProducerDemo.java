package springbootmq.service.producer;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import springbootmq.config.AppConstants;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 发送请求消息和接收返回消息
 */

public class ProducerDemo implements Supplier<String> {
    private String message;
    private MessagePropertiesConverter messagePropertiesConverter;
    private RabbitTemplate rabbitTemplate;

    public ProducerDemo(String message) {
        this.message = message;
    }

    public ProducerDemo(String message, MessagePropertiesConverter messagePropertiesConverter, RabbitTemplate rabbitTemplate) {
        this.message = message;
        this.messagePropertiesConverter = messagePropertiesConverter;
        this.rabbitTemplate = rabbitTemplate;

    }


    @Override
    public String get() {
        Date sendTime = new Date();
        String correlationId = UUID.randomUUID().toString();
        AMQP.BasicProperties props =
                new AMQP.BasicProperties("text/plain",
                        "UTF-8",
                        null,
                        2,
                        0, correlationId, AppConstants.SPRING_EXCHANGE_REPLY1, null,
                        null, sendTime, null, null,
                        "SpringProducer", null);

        MessageProperties sendMessageProperties =
                messagePropertiesConverter.toMessageProperties(props, null, "UTF-8");
        sendMessageProperties.setReceivedExchange(AppConstants.SPRING_EXCHANGE_REPLY1);
        sendMessageProperties.setReceivedRoutingKey(AppConstants.SPRING_KEY_REPLY1);
        sendMessageProperties.setRedelivered(true);

        Message sendMessage = MessageBuilder.withBody(message.getBytes())
                .andProperties(sendMessageProperties)
                .build();

        Message replyMessage =
                rabbitTemplate.sendAndReceive(AppConstants.SPRING_EXCHANGE_SEND1,
                        AppConstants.SPRING_KEY_SEND1, sendMessage);

        String replyMessageContent = null;
        try {
            replyMessageContent = new String(replyMessage.getBody(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return replyMessageContent;
    }
}
