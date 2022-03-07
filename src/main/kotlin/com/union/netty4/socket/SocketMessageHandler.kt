package com.union.netty4.socket

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Sharable
@Component
class SocketMessageHandler : ChannelInboundHandlerAdapter() {
    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
        LOGGER.info("链接创建：{}", ctx.channel().remoteAddress())
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)
        LOGGER.info("链接断开：{}", ctx.channel().remoteAddress())
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val msg:ByteArray = msg as ByteArray
        LOGGER.info("收到：{}", msg.toString())
    }

    /**
     * 数据读取完毕
     */
    @Throws(Exception::class)
    override fun channelReadComplete(ctx: ChannelHandlerContext) {
//        val builder = Msg.newBuilder()
//        ctx.writeAndFlush(builder.build())
        if (ctx.channel().isOpen) {
            val bytes: ByteArray = "123".toByteArray()
            ctx.writeAndFlush(bytes)
        }
    }

    /**
     * 处理异常，关闭通道
     */
    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        LOGGER.error("error, {}", cause)
        ctx.channel().close()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SocketMessageHandler::class.java)
    }
}