package javas.blog.csdn.net.handler;

import javas.blog.csdn.net.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@ChannelHandler.Sharable
@Slf4j
public class QuitHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //Channel断开连接
        Channel channel = ctx.channel();
        SessionFactory.getSession().unbind(channel);
        log.info("{} 正常退出",channel);

        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //Channel断开连接
        Channel channel = ctx.channel();
        SessionFactory.getSession().unbind(channel);
        log.info("{} 异常退出",channel);

        super.exceptionCaught(ctx, cause);
    }
}
