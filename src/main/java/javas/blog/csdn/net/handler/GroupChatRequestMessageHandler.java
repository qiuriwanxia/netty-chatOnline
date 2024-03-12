package javas.blog.csdn.net.handler;

import javas.blog.csdn.net.message.GroupChatRequestMessage;
import javas.blog.csdn.net.message.GroupChatResponseMessage;
import javas.blog.csdn.net.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@ChannelHandler.Sharable
public class GroupChatRequestMessageHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, GroupChatRequestMessage groupChatRequestMessage) throws Exception {
        //发送群聊
        String groupName = groupChatRequestMessage.getGroupName();
        List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);

        GroupChatResponseMessage groupChatResponseMessage = new GroupChatResponseMessage(groupChatRequestMessage.getFrom(), groupChatRequestMessage.getContent());

        for (Channel channel : membersChannel) {
                channel.writeAndFlush(groupChatResponseMessage);
        }

    }

}
