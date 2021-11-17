### 安装与配置
1. 拖入 `mods` 文件夹，启动并关闭服务器，打开 `.minecraft/config/chloebot.json`
2. 将 `port` 改为你想要使用的端口。切记不要和 Minecraft 服务器使用同一个端口。
3. 将你想要授权的使用者的 QQ 号和群的群号分别填入 `authorizedUsers` 和 `authorizedGroups` 中。
4. 再次打开服务器。
### 配置文件的其他内容
* `botName`: bot 在命令逻辑中的显示名称。
* > `forceLocalHost`: 是否只允许搭建在 MC 服务器主机上的 QQ Bot 向服务器执行指令。<br>
  > 建议保持为 `true`
* `commandResponseWaitTime`: 服务器从接收到 HTTP 请求到发送命令返回值的间隔，用于接收某些插件异步返回的内容。
* > `commandRedirects`: 命令重定向表。<br>
  > 本 bot 会优先执行原版命令，所以如果你想要让某些指令走插件指令的话，需要在此重定向。<br>
  > 例如，可以把 `list` 命令重定向至 `essentials:list` 使得 `/list` 命令返回使用权限组分类的在线玩家列表。（需要服务器安装 Essentials 插件）