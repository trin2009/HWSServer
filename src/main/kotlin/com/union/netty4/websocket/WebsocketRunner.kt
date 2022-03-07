package com.union.netty4.websocket

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler
import io.netty.handler.stream.ChunkedWriteHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent
import org.springframework.stereotype.Component
import java.net.InetSocketAddress

@Component
class WebsocketRunner : ApplicationRunner, ApplicationListener<ContextClosedEvent>, ApplicationContextAware {
    @Value("\${netty.server-websocket.port}")
    private val port = 10088

    @Value("\${netty.server-websocket.ip}")
    private val ip: String? = null

    @Value("\${netty.server-websocket.path}")
    private val path: String? = null

    @Value("\${netty.server-websocket.max-frame-size}")
    private val maxFrameSize: Long = 1024
    private var applicationContext: ApplicationContext? = null
    private var serverChannel: Channel? = null

    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    @Throws(Exception::class)
    override fun run(args: ApplicationArguments) {
        val bossGroup: EventLoopGroup = NioEventLoopGroup()
        val workerGroup: EventLoopGroup = NioEventLoopGroup()
        try {
            val serverBootstrap = ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .localAddress(InetSocketAddress(ip, port))
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    @Throws(Exception::class)
                    override fun initChannel(socketChannel: SocketChannel) {
                        val pipeline = socketChannel.pipeline()
                        pipeline.addLast(HttpServerCodec())
                        pipeline.addLast(ChunkedWriteHandler())
                        pipeline.addLast(HttpObjectAggregator(65536))
                        pipeline.addLast(object : ChannelInboundHandlerAdapter() {
                            @Throws(Exception::class)
                            override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                                if (msg is FullHttpRequest) {
                                    val uri = msg.uri()
                                    if (uri != path) {
                                        // 访问的路径不是 websocket的端点地址，响应404
                                        ctx.channel().writeAndFlush(
                                            DefaultFullHttpResponse(
                                                HttpVersion.HTTP_1_1,
                                                HttpResponseStatus.NOT_FOUND
                                            )
                                        )
                                            .addListener(ChannelFutureListener.CLOSE)
                                        return
                                    }
                                }
                                super.channelRead(ctx, msg)
                            }
                        })
                        pipeline.addLast(WebSocketServerCompressionHandler())
                        pipeline.addLast(WebSocketServerProtocolHandler(path, null, true, maxFrameSize))
                        /**
                         * 从IOC中获取到Handler
                         */
                        pipeline.addLast(applicationContext!!.getBean(WebsocketMessageHandler::class.java))
                    }
                })
            val channel = serverBootstrap.bind().sync().channel()
            serverChannel = channel
            LOGGER.info("websocket 服务启动，ip={},port={}", ip, port)
            channel.closeFuture().sync()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }

    override fun onApplicationEvent(event: ContextClosedEvent) {
        if (serverChannel != null) {
            serverChannel!!.close()
        }
        LOGGER.info("websocket 服务停止")
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(WebsocketRunner::class.java)
    }
}