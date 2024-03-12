package javas.blog.csdn.net.client;


import javas.blog.csdn.net.protocol.MessageCodecSharable;
import javas.blog.csdn.net.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import javas.blog.csdn.net.message.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        AtomicBoolean loginResult = new AtomicBoolean(false);
        Semaphore semaphore = new Semaphore(0);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,3000);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    ch.pipeline().addLast(new IdleStateHandler(0,3,0));
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            log.error("{}",cause);
                            super.exceptionCaught(ctx, cause);
                        }

                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            if (evt instanceof IdleStateEvent){
                                IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                                if (idleStateEvent.state()==IdleState.WRITER_IDLE){
                                    ctx.writeAndFlush(new PingMessage());
                                }
                            }
                            super.userEventTriggered(ctx, evt);
                        }

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.info("{}",msg);
                            if (msg instanceof GroupChatResponseMessage){
                                GroupChatResponseMessage groupChatResponseMessage = (GroupChatResponseMessage)msg;
                                String content = groupChatResponseMessage.getContent();
                                System.out.println("收到群聊消息 ： "+content);
                            }
                            if (msg instanceof GroupCreateResponseMessage){
                                GroupChatResponseMessage groupChatResponseMessage = (GroupChatResponseMessage)msg;
                                String reason = groupChatResponseMessage.getReason();
                                System.out.println("分组创建结果 ： "+reason);
                            }
                            if (msg instanceof ChatResponseMessage){
                                ChatResponseMessage chatResponseMessage = (ChatResponseMessage)msg;
                                String content = chatResponseMessage.getContent();
                                System.out.println("接收到的消息内容 ： "+content);
                            }
                            if (msg instanceof LoginResponseMessage){
                                LoginResponseMessage loginResponseMessage = (LoginResponseMessage) msg;
                                boolean success = loginResponseMessage.isSuccess();
                                semaphore.release();
                                if (success) {
                                    loginResult.set(true);
                                    new Thread(()-> {
                                        while (true) {
                                            String username = loginResponseMessage.getReason();
                                            System.out.println("==================================");
                                            System.out.println("send [username] [content]");
                                            System.out.println("gsend [group name] [content]");
                                            System.out.println("gcreate [group name] [m1,m2,m3...]");
                                            System.out.println("gmembers [group name]");
                                            System.out.println("gjoin [group name]");
                                            System.out.println("gquit [group name]");
                                            System.out.println("quit");
                                            System.out.println("==================================");
                                            String command = null;
                                            try {
                                                System.out.println("请输入你的操作 \n");
                                                Scanner scanner = new Scanner(System.in);
                                                command = scanner.nextLine();
                                            } catch (Exception e) {

                                            }
                                            String[] s = command.split(" ");
                                            switch (s[0]) {
                                                case "send":
                                                    ctx.writeAndFlush(new ChatRequestMessage(username, s[1], s[2]));
                                                    break;
                                                case "gsend":
                                                    ctx.writeAndFlush(new GroupChatRequestMessage(username, s[1], s[2]));
                                                    break;
                                                case "gcreate":
                                                    Set<String> set = new HashSet<>(Arrays.asList(s[2].split(",")));
                                                    set.add(username); // 加入自己
                                                    ctx.writeAndFlush(new GroupCreateRequestMessage(s[1], set));
                                                    break;
                                                case "gmembers":
                                                    ctx.writeAndFlush(new GroupMembersRequestMessage(s[1]));
                                                    break;
                                                case "gjoin":
                                                    ctx.writeAndFlush(new GroupJoinRequestMessage(username, s[1]));
                                                    break;
                                                case "gquit":
                                                    ctx.writeAndFlush(new GroupQuitRequestMessage(username, s[1]));
                                                    break;
                                                case "quit":
                                                    ctx.channel().close();
                                                    return;
                                            }
                                        }
                                    }).start();
                                }
                            }
                            super.channelRead(ctx, msg);
                        }

                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            System.out.println("与服务器建立连接成功。。。");
                            //与服务器建立连接后准备登录
                            new Thread(() -> {
                                while (true) {
                                    Scanner scanner = new Scanner(System.in);
                                    System.out.println("请输入用户名");
                                    String userName = scanner.nextLine();
                                    System.out.println("请输入密码");
                                    String password = scanner.nextLine();
                                    LoginRequestMessage loginRequestMessage = new LoginRequestMessage(userName, password);
                                    ctx.writeAndFlush(loginRequestMessage);
                                    System.out.println("请等待后续操作");
                                    try {
                                        semaphore.acquire();
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    if (loginResult.get()){
                                        System.out.println("登录成功");
                                        break;
                                    }else {
                                        System.out.println("登录失败！");
                                    }

                                }
                            }).start();
                            super.channelActive(ctx);
                        }
                    });
                }
            });
            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
