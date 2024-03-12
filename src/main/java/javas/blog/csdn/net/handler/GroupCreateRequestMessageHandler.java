package javas.blog.csdn.net.handler;

import javas.blog.csdn.net.message.GroupCreateRequestMessage;
import javas.blog.csdn.net.message.GroupCreateResponseMessage;
import javas.blog.csdn.net.server.session.Group;
import javas.blog.csdn.net.server.session.GroupSession;
import javas.blog.csdn.net.server.session.GroupSessionFactory;
import javas.blog.csdn.net.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Set;


@ChannelHandler.Sharable
public class GroupCreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, GroupCreateRequestMessage groupCreateRequestMessage) throws Exception {
        String groupName = groupCreateRequestMessage.getGroupName();
        Set<String> members = groupCreateRequestMessage.getMembers();
        Channel channel1 = channelHandlerContext.channel();

        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        String userName = SessionFactory.getSession().getUserName(channel1);
        members.add(userName);
        Group group = groupSession.createGroup(groupName, members);
        if (group ==null) {
            //创建成功
            channelHandlerContext.writeAndFlush(new GroupCreateResponseMessage(true,"创建成功："+groupName));
            for (Channel channel : groupSession.getMembersChannel(groupName)) {
                channel.writeAndFlush(new GroupCreateResponseMessage(true,"已加入群组："+groupName));
            }
        }else {
            //如果已经存在 创建失败
            channelHandlerContext.writeAndFlush(new GroupCreateResponseMessage(false,"创建失败 分组已经存在"));
        }
    }
}
