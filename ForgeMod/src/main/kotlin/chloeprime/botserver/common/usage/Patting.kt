/**
 * 用户名 / 群名是绿色的，
 * 括号是黄色的，
 * QQ号 / 群号是 a 号色的
 */
package chloeprime.botserver.common.usage

import chloeprime.botserver.protocol.RequestContext
import chloeprime.botserver.protocol.RequestContext.Pat.Companion.Animations
import chloeprime.botserver.protocol.RequestPO
import net.minecraft.command.CommandPlaySound
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.play.server.SPacketAnimation
import net.minecraft.network.play.server.SPacketCustomSound
import net.minecraft.util.SoundCategory
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.event.HoverEvent
import net.minecraft.world.WorldServer

/**
 * 视觉反馈，目前只有屏幕抖动（
 */
internal fun patAnimation(player: EntityPlayerMP, actions: Int) {
    val serverWorld = player.world as? WorldServer ?: return

    if ((actions and Animations.SHAKE_SCREEN) > 0) {
        val animType = 1
        serverWorld.entityTracker.sendToTrackingAndSelf(
            player, SPacketAnimation(player, animType)
        )
    }
}

/**
 * 播放自定义声音。
 * 参考 [CommandPlaySound.execute]
 */
internal fun EntityPlayerMP.playSound(snd: String) {
    this.connection.sendPacket(
        SPacketCustomSound(snd, SoundCategory.PLAYERS, posX, posY, posZ, 1F, 1F)
    )
}

internal fun pat0(player: EntityPlayerMP, requestPO: RequestPO, ctx: RequestContext.Pat) {
    patAnimation(player, ctx.animation)
    ctx.soundFx?.let { player.playSound(it) }
    sendPatMessage(player, requestPO, ctx)
}

/**
 * 发送文本消息
 */
internal fun sendPatMessage(player: EntityPlayerMP, requestPO: RequestPO, ctx: RequestContext.Pat) {

    val action = ctx.actionOverload ?: "拍了拍"

    // "{QQ昵称}"
    val formattedMsg = TextComponentString("").appendSibling(
        TextComponentString(ctx.userName, TextFormatting.GREEN).apply {
            style = style.apply {
                color = TextFormatting.GREEN
                val qqInfo = getQqUserInfo(requestPO, ctx)
                hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, qqInfo)
            }
        }
    )

    if (action.isEmpty()) {
        // "{QQ昵称}对你说: {消息内容}"
        formattedMsg
            .appendText("对你说: ", TextFormatting.WHITE)
            .appendText(ctx.text.toString(), TextFormatting.WHITE)

        player.sendMessage(formattedMsg.appendGroupNameIfNeeded(ctx.groupName))
        return
    }

    // "{QQ昵称}拍了拍你"
    formattedMsg.appendText(action + "你", TextFormatting.WHITE)

    val text = ctx.text

    // 无附加文本的情况，
    // 输出 "{QQ昵称}拍了拍你" 或 "来自{群名称}的{QQ昵称}拍了拍你"
    if (text == null) {
        player.sendMessage(formattedMsg.appendGroupNameIfNeeded(ctx.groupName))
        return
    }

    formattedMsg.appendText("并对你说: ").appendText(text, TextFormatting.WHITE)
    player.sendMessage(formattedMsg)
}

/**
 * {QQ昵称} -> 来自{群名称}的{QQ昵称}
 */
internal fun ITextComponent.appendGroupNameIfNeeded(groupName: String?): ITextComponent {
    return if (groupName != null) {
        TextComponentString("来自", TextFormatting.WHITE)
            .appendText(groupName, TextFormatting.AQUA)
            .appendText("的", TextFormatting.WHITE)
            .appendSibling(this)
    } else {
        this
    }
}


internal fun getQqUserInfo(requestPO: RequestPO, ctx: RequestContext.Pat): ITextComponent {
    // Line1（QQ用户信息）
    val text = TextComponentString("QQ用户    ", TextFormatting.WHITE)
        .appendText(ctx.userName, TextFormatting.GREEN)
        .appendText(" (", TextFormatting.YELLOW)
        .appendText(requestPO.user.toString(), TextFormatting.AQUA)
        .appendText(")", TextFormatting.YELLOW)
    // Line2（群信息）
    val ctxGroupName = ctx.groupName
    if (ctxGroupName != null) {
        text.appendText("\n来自群    ", TextFormatting.WHITE)
            .appendText(ctxGroupName, TextFormatting.GREEN)
            .appendText(" (", TextFormatting.YELLOW)
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