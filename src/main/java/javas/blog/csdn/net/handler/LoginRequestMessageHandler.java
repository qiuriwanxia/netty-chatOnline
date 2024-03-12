package javas.blog.csdn.net.handler;

import javas.blog.csdn.net.message.LoginRequestMessage;
import javas.blog.csdn.net.message.LoginResponseMessage;
import javas.blog.csdn.net.server.service.UserServiceFactory;
import javas.blog.csdn.net.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LoginRequestMessage loginRequestMessage) throws Exception {

            String username = loginRequestMessage.getUsername();
            boolean login = UserServiceFactory.getUserService().login(username, loginRequestMessage.getPassword());
            if (login) {
                //存储管理Channel
                SessionFactory.getSession().bind(channelHandlerContext.channel(),username);
                channelHandlerContext.writeAndFlush(new LoginResponseMessage(true, username));
            } else {
                channelHandlerContext.writeAndFlush(new LoginResponseMessage(false, "登录失败"));
            }

    }
}
