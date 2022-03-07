package com.union.netty4.socket

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import io.netty.handler.codec.bytes.ByteArrayDecoder
import io.netty.handler.codec.bytes.ByteArrayEncoder
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
class SocketRunner : ApplicationRunner, ApplicationListener<ContextClosedEvent?>, ApplicationContextAware {
    @Value("\${netty.server-socket.port}")
    private val port = 10188

    @Value("\${netty.server-socket.ip}")
    private val ip: String? = null

    @Value("\${netty.server-socket.path}")
    private val path: String? = null

    @Value("\${netty.server-socket.max-frame-size}")
    private val maxFrameSize: Long = 1024
    private var applicationContext: ApplicationContext? = null
    private var serverChannel: Channel? = null

    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    @Throws(Exception::class)
    override fun run(args: ApplicationArguments) {
        val boss: EventLoopGroup = NioEventLoopGroup(1)
        val work: EventLoopGroup = NioEventLoopGroup()
        try {
            val server = ServerBootstrap()
                .group(boss, work)
                .channel(NioServerSocketChannel::class.java)
                .localAddress(InetSocketAddress(ip, port))
                //.option(ChannelOption.SO_BACKLOG, 128)
                //.childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    @Throws(Exception::class)
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()
                        // Decoders
                        pipeline.addLast("frameDecoder", LengthFieldBasedFrameDecoder(1024*10, 0, 4,0,4))
                        pipeline.addLast("bytesDecoder", ByteArrayDecoder())
                        // Encoder
                        pipeline.addLast("frameEncoder", LengthFieldPrepender(4))
                        pipeline.addLast("bytesEncoder", ByteArrayEncoder())

//                        pipeline.addLast(ProtobufVarint32FrameDecoder())
//                        pipeline.addLast(ProtobufDecoder(ProtoMsg.Msg.getDefaultInstance()))
//
//                        pipeline.addLast(ProtobufVarint32FrameDecoder())
//                        pipeline.addLast(ProtobufEncoder())

                        pipeline.addLast(applicationContext!!.getBean(SocketMessageHandler::class.java))
                    }
                })

            // 绑定端口
            val channel: Channel = server.bind().sync().channel()
            serverChannel = channel
            SocketRunner.LOGGER.info("socket 服务启动，ip={},port={}", ip, port)
            channel.closeFuture().sync()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            boss.shutdownGracefully()
            work.shutdownGracefully()
        }
    }


    override fun onApplicationEvent(event: ContextClosedEvent) {
        if (serverChannel != null) {
            serverChannel!!.close()
        }
        LOGGER.info("socket 服务停止")
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SocketRunner::class.java)
    }
}