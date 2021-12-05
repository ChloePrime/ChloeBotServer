/**
 * 用户名 / 群名是绿色的，
 * 括号是黄色的，
 * QQ号 / 群号是 a 号色的
 */
package chloeprime.botserver.common.usage

import chloeprime.botserver.protocol.RequestContext
import chloeprime.botserver.protocol.RequestPO
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.event.HoverEvent

internal fun pat0(player: EntityPlayerMP, requestPO: RequestPO, ctx: RequestContext.Pat) {
    val action = ctx.actionOverload ?: "拍了拍"

    // "{QQ昵称}"
    val formattedMsg = TextComponentString(ctx.userName, TextFormatting.GREEN).apply {
        style = style.apply {
            color = TextFormatting.GREEN
            val qqInfo = getQqUserInfo(requestPO, ctx)
            hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, qqInfo)
        }
    }
    if (action.isEmpty()) {
        // "{QQ昵称}对你说: {消息内容}"
        formattedMsg
            .appendText("对你说: ", TextFormatting.WHITE)
            .appendText(ctx.text.toString(), TextFormatting.WHITE)

        player.sendMessage(formattedMsg)
        return
    }

    // "{QQ昵称}拍了拍你"
    formattedMsg.appendText(action + "你", TextFormatting.WHITE)

    val text = ctx.text
    val groupName = ctx.groupName

    // 无附加文本的情况，
    // 输出 "{QQ昵称}拍了拍你" 或 "来自{群名称}的{QQ昵称}拍了拍你"
    if (text == null) {
        val actualMsg = if (groupName != null) {
            TextComponentString("来自", TextFormatting.WHITE)
                .appendText(groupName, TextFormatting.AQUA)
                .appendText("的", TextFormatting.WHITE)
                .appendSibling(formattedMsg)
        } else {
            formattedMsg
        }
        player.sendMessage(actualMsg)
        return
    }

    formattedMsg.appendText("并对你说: ").appendText(text, TextFormatting.WHITE)
    player.sendMessage(formattedMsg)
}


internal fun getQqUserInfo(requestPO: RequestPO, ctx: RequestContext.Pat): ITextComponent {
    // Line1（QQ用户信息）
    val text = TextComponentString("QQ用户 ", TextFormatting.WHITE)
        .appendText(ctx.userName, TextFormatting.GREEN)
        .appendText("(", TextFormatting.YELLOW)
        .appendText(requestPO.user.toString(), TextFormatting.AQUA)
        .appendText(")", TextFormatting.YELLOW)
    // Line2（群信息）
    val ctxGroupName = ctx.groupName
    if (ctxGroupName != null) {
        text.appendText("\n来自群 ", TextFormatting.WHITE)
            .appendText(ctxGroupName, TextFormatting.GREEN)
            .appendText("(", TextFormatting.YELLOW)
            .appendText(requestPO.group.toString(), TextFormatting.AQUA)
            .appendText(")", TextFormatting.YELLOW)
    }

    return text
}

private fun ITextComponent.setColor(color: TextFormatting): ITextComponent {
    return this.apply {
        style = style.setColor(color)
    }
}

private fun TextComponentString(text: String, color: TextFormatting): TextComponentString {
    return TextComponentString(text).apply {
        setColor(color)
    }
}

private fun ITextComponent.appendText(text: String, color: TextFormatting): ITextComponent {
    return this.appendSibling(TextComponentString(text).setColor(color))
}