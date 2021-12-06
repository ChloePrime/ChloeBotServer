package chloeprime.botserver.bot

object Resources {
    const val SERVER_STOP_MSG = "服务器开始关闭"
    const val SOCKET_TIMEOUT_MSG = "连接超时，服务器未响应"

    const val CHANGE_SERVER_SUCCESS = "将当前服务器切换至 %s"
    const val CHANGE_SERVER_NONEXIST = "服务器配置 %s 不存在，将使用默认服务器"
    const val CHANGE_SERVER_RESET = "将当前服务器切换至默认服务器"

    const val PLAYERLIST_COUNT = "服务器当前在线人数: %d / %d"
    const val PLAYERLIST_HEADER = "当前在线玩家列表:"

    const val TPS_FORMAT = "土豆性能状态: %.2f tps (%.2f mspt)"

    const val PAT_SUCCESS = "你成功地%s%s"
    const val TELL_SUCCESS = "消息已成功发送至 %2\$s"
    const val PAT_ERROR_TARGET_OFFLINE = "%2\$s 当前并未上线或从未登录过服务器哦"
}