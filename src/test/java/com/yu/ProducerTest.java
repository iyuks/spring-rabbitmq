package com.yu;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring-rabbitmq-producer.xml")
public class ProducerTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * hello world
     */
    @Test
    public void testHelloWorld() {
        rabbitTemplate.convertAndSend("spring_queue", "helllo world spring...");
    }
    /**
     *fanout
     */
    @Test
    public void testFanout() {
        rabbitTemplate.convertAndSend("spring_fanout_exchange","","spring fanout ...");
    }

    /**
     * routing
     */
    @Test
    public void testRouting() {
        rabbitTemplate.convertAndSend("spring_topic_exchange","wen.haha.ha","spring routing ...");
    }
}
