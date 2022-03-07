package com.union.netty4.websocket.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class WebsocketServiceManager {

    fun handleTextMsg(msg: String) {
        LOGGER.info("文本消息：{}", msg)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(WebsocketServiceManager::class.java)
    }
}