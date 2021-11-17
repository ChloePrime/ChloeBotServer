package chloeprime.botserver.common

/**
 * @param fullCommandIn 含有命令参数，但不含开头 '/' 的完整命令
 */
internal fun redirectCommand(fullCommandIn: String): String {
    val cmd = fullCommandIn.substringBefore(' ')
    val newCmd = ModConfig.INSTANCE.commandRedirects[cmd] ?: return fullCommandIn
    return fullCommandIn.replaceFirst(cmd, newCmd)
}