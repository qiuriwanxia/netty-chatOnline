package javas.blog.csdn.net.handler;

import javas.blog.csdn.net.message.ChatRequestMessage;
import javas.blog.csdn.net.message.ChatResponseMessage;
import javas.blog.csdn.net.server.session.Session;
import javas.blog.csdn.net.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ChatRequestMessage chatRequestMessage) throws Exception {
        String from = chatRequestMessage.getFrom();
        String to = chatRequestMessage.getTo();
        String content = chatRequestMessage.getContent();

        Session session = SessionFactory.getSession();

        Channel channel = session.getChannel(to);
        if (channel !=null){
            //向对方发送消息
            channel.writeAndFlush(new ChatResponseMessage(to,content));
        }else {
            //返回状态
            Channel channel1 = channelHandlerContext.channel();
            channel1.writeAndFlush(new ChatResponseMessage(to,"用户不在线"));
        }
    }

}
