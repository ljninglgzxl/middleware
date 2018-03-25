package springbootmq.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springbootmq.service.SendMessage;

@RestController
public class TestSpringBootMQ {
    @Autowired
    SendMessage sendMessage;
    @RequestMapping("/send")
    public void sendMessage(){
        System.out.println(sendMessage.sendMessage("1"));
    }
}
