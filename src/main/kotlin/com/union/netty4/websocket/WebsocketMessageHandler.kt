package com.union.netty4.websocket

import com.union.netty4.websocket.service.WebsocketServiceManager
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Sharable
@Component
class WebsocketMessageHandler : SimpleChannelInboundHandler<WebSocketFrame>() {
    @Autowired
    var websocketServiceManager: WebsocketServiceManager? = null

    @Throws(Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: WebSocketFrame) {
        if (msg is TextWebSocketFrame) {
            // 业务层处理数据
            websocketServiceManager!!.handleTextMsg(msg.text())
            // 响应客户端
            ctx.channel().writeAndFlush(TextWebSocketFrame("我收到了你的消息：" + System.currentTimeMillis()))
        } else {
            // 不接受文本以外的数据帧类型
            ctx.channel().writeAndFlush(WebSocketCloseStatus.INVALID_MESSAGE_TYPE)
                .addListener(ChannelFutureListener.CLOSE)
        }
    }

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

    companion object {
        private val LOGGER = LoggerFactory.getLogger(WebsocketMessageHandler::class.java)
    }
}