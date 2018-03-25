package springbootmq.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import springbootmq.service.producer.ProducerDemo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
@Service
public class SendMessage {
    @Autowired
    private ThreadPoolTaskExecutor executor;
    @Autowired
    MessagePropertiesConverter messagePropertiesConverter;
    @Autowired
    RabbitTemplate rabbitTemplate;

    public String sendMessage(String message)
    {
        CompletableFuture<String> resultCompletableFuture =
                CompletableFuture.supplyAsync(new ProducerDemo(message,messagePropertiesConverter,rabbitTemplate), executor);
        try
        {
            String result = resultCompletableFuture.get();
            return result;
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
