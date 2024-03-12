package javas.blog.csdn.net.server;


import javas.blog.csdn.net.protocol.MessageCodecSharable;
import javas.blog.csdn.net.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import javas.blog.csdn.net.handler.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.option(ChannelOption.SO_BACKLOG,1);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new IdleStateHandler(5,0,0));
                    ch.pipeline().addLast(new ChannelDuplexHandler(){
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            if (evt instanceof IdleStateEvent){
                                IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                                    if (idleStateEvent.state()== IdleState.READER_IDLE){
                                        //读空闲超时
                                        System.out.println("读空闲超时");
                                    }
                                    if (idleStateEvent.state()==IdleState.WRITER_IDLE){
                                        //写空闲超时
                                        System.out.println("写空闲超时");
                                    }
                                    if (idleStateEvent.state()==IdleState.ALL_IDLE){
                                        //全部空闲超时
                                        System.out.println("全部空闲超时");
                                    }
                            }
                            super.userEventTriggered(ctx, evt);
                        }
                    });
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    ch.pipeline().addLast(new LoginRequestMessageHandler());
                    ch.pipeline().addLast(new ChatRequestMessageHandler());
                    ch.pipeline().addLast(new GroupCreateRequestMessageHandler());
                    ch.pipeline().addLast(new GroupChatRequestMessageHandler());
                    ch.pipeline().addLast(new QuitHandler());
                }
            });
            Channel channel = serverBootstrap.bind(8080).sync().channel();
            ByteBuf buffer = channel.alloc().buffer();
            System.out.println("buffer = " + buffer);
            System.out.println("channel = " + channel);
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
