package com.yu;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
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
     * fanout
     */
    @Test
    public void testFanout() {
        rabbitTemplate.convertAndSend("spring_fanout_exchange", "", "spring fanout ...");
    }

    /**
     * topic
     */
    @Test
    public void testTopic() {
        rabbitTemplate.convertAndSend("spring_topic_exchange", "wen.haha.ha", "spring topic ...");
    }

    //消息可靠性投递
    //1确认模式
    //2.回退模式

    /**
     * 确认模式
     * 1.开启确认模式connection-factory开启确认模式publisher-confirms="true"
     * 2.rabbitmqTemplate定义ConfirmCallback回调函数
     */
    @Test
    public void testConfirm() {
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * @param correlationData 相关配置信息
             * @param b exchange是否成功接受到消息,true成功
             * @param s 失败原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                if (b) {
                    System.out.println("成功接受消息" + s);
                    System.out.println(correlationData);
                    System.out.println(correlationData.getReturnedMessage());
                    System.out.println(new String(correlationData.getReturnedMessage().getBody()));
                    System.out.println(correlationData.getReturnedMessage().getMessageProperties());
                    System.out.println(correlationData.getId());
                    System.out.println(correlationData.getFuture());
                } else {
                    System.out.println("失败" + s);
                    //处理,让消息再次发送
                }
            }
        });
        //发送消息
        CorrelationData data = new CorrelationData();
        data.setId("1");
        Message msg = new Message(new String("返回的消息内容").getBytes(), new MessageProperties());
        data.setReturnedMessage(msg);
        rabbitTemplate.convertAndSend("test_exchange_confirm", "confirm", "发送消息...", data);
    }

    /**
     * 回退模式:当消息发送给exchange后,exchange路由到queue失败时,才会执行ReturnCallback
     * 1.connection-factory开启回退模式publisher-returns="true"
     * 2.设置ReturnCallback
     * 3.设置exchange处理消息的模式:
     * 1如果消息没有路由到queue,则丢弃消息(默认)
     * 2如果消息没有路由到queue,返回给消息发送方ReturnCallback
     */
    @Test
    public void testReturn() {
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("return执行了...");
//                System.out.println(message);
//                System.out.println(replyCode);
//                System.out.println(replyText);
//                System.out.println(exchange);
//                System.out.println(routingKey);
            }
        });
        //发送消息
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.convertAndSend("test_exchange_confirm", "confirm", "hello");
    }

    @Test
    public void test2() throws InterruptedException {
        while (true) {
//            Thread.sleep(1);
            testReturn();
        }
    }

    /**
     * ttl测试
     */
    @Test
    public void testTtl() {
        // 消息后处理对象，设置一些消息的参数信息
        MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                //1.设置message的信息
                message.getMessageProperties().setExpiration("5");
                return message;
            }
        };
        for (int i = 0; i < 10; i++) {
            if (i == 0) {
                rabbitTemplate.convertAndSend("test_exchange_ttl", "ttl.hello", "这是ttl消息", messagePostProcessor);
            }else {
                rabbitTemplate.convertAndSend("test_exchange_ttl", "ttl.hello", "这是ttl消息");
            }
        }
    }

    /**
     * 测试死信交换机
     */
    @Test
    public void testDlx(){
        //1. 测试过期时间，死信消息
      /*  rabbitTemplate.convertAndSend("test_exchange_dlx","test.dlx.haha","我是一条消息，我会死吗？");*/
        //2. 测试长度限制后，消息死信
       /* for (int i = 0; i < 21; i++) {
            rabbitTemplate.convertAndSend("test_exchange_dlx","test.dlx.haha","我是一条消息，我会死吗？");
        }*/
        //3. 测试消息拒收
        rabbitTemplate.convertAndSend("test_exchange_dlx","test.dlx.haha","我是一条消息，我会死吗？");
    }

    /**
     * 测试延迟队列
     */
    @Test
    public void testDelay(){
        rabbitTemplate.convertAndSend("order_exchange","order.msg","订单信息：id=1,time=2019年8月17日16:41:47");
    }
}
