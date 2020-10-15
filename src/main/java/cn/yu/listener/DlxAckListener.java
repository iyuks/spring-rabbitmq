package cn.yu.listener;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

/**
 * 测试死信队列,
 * 消息拒收
 * 监听正常队列
 */

public class DlxAckListener implements ChannelAwareMessageListener {
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            //1.接受转换消息
            System.out.println(new String(message.getBody(),"utf-8"));
            //2.处理业务逻辑
            System.out.println("处理业务逻辑...");
            int i = 2 / 0;
            //3.手动签收
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            e.printStackTrace();
            //拒收
            channel.basicNack(deliveryTag,false,false);
        }
    }
}
