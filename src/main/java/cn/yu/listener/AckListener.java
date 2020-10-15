package cn.yu.listener;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

/**
 * 消息可靠性投递(消费端)
 * Consumer ack机制
 * 1.设置手动签收,acknowledge="manual"
 * 2.监听器实现ChannelAwareMessageListener接口
 * 3.如果消息陈工处理,则调用channel的basicAck()签收
 * 4.如果消息处理失败,则调用channel的basicNack()拒绝签收,broker重新发送给consumer
 */
@Component
public class AckListener implements ChannelAwareMessageListener {
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            //1.接受转换消息
            System.out.println(new String(message.getBody(),"utf-8"));
            //2.处理业务逻辑
            System.out.println("处理业务逻辑...");
//            int i = 2 / 0;
            //3.手动签收
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            e.printStackTrace();
            //拒收
            channel.basicNack(deliveryTag,false,false);
        }
    }
}
