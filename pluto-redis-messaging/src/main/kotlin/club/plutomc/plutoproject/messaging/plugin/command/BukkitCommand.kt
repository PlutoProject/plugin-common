package club.plutomc.plutoproject.messaging.plugin.command

import cloud.commandframework.Command
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.paper.PaperCommandManager
import club.plutomc.plutoproject.apiutils.chat.sendTo
import club.plutomc.plutoproject.apiutils.chat.tag
import club.plutomc.plutoproject.messaging.Constants
import club.plutomc.plutoproject.messaging.api.MessageManager
import club.plutomc.plutoproject.messaging.impl.ImplUtils
import club.plutomc.plutoproject.messaging.plugin.BukkitMessagingPlugin
import com.google.gson.JsonObject
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import java.util.function.Function

class BukkitCommand(plugin: Plugin, private val messageManager: MessageManager):
    club.plutomc.plutoproject.messaging.plugin.command.Command {

    val commandManager: PaperCommandManager<CommandSender>
    private val rootBuilder: Command.Builder<CommandSender>

    init {
        ImplUtils.debugLogInfo("Registering commands...")

        commandManager = PaperCommandManager(
            plugin,
            CommandExecutionCoordinator.simpleCoordinator(),
            Function.identity(),
            Function.identity()
        )

        rootBuilder = commandManager.commandBuilder("bukkitmessaging")
    }

    private fun registerRootCommand() {
        commandManager.command(
            rootBuilder.handler {
                ImplUtils.debugLogInfo("Bukkit command root executed!")
                val sender = it.sender
                val version = BukkitMessagingPlugin.plugin.pluginMeta.version

                Constants.MESSAGE_COMMAND
                    .tag(Placeholder.unparsed("version", version))
                    .sendTo(sender)
            }
        )
        ImplUtils.debugLogInfo("Bukkit command root registered!")
    }

    private fun registerSendCommand() {
        commandManager.command(
            rootBuilder.literal("send")
                .argument(StringArgument.of("channel"))
                .argument(StringArgument.of("content"))
                .handler {
                    val sender = it.sender
                    val channel = it.get<String>("channel")
                    val content = it.get<String>("content")

                    if (!messageManager.exist(channel)) {
                        Constants.MESSAGE_CHANNEL_NOT_EXIST
                            .tag(Placeholder.unparsed("channel", channel))
                            .sendTo(sender)
                        return@handler
                    }

                    val contentObject = JsonObject()
                    contentObject.addProperty("type", "message_plugin_publish")
                    contentObject.addProperty("content", content)
                    messageManager.get(channel).publish(JsonObject())

                    Constants.MESSAGE_SEND_MESSAGE
                        .tag(
                            Placeholder.unparsed("channel", channel),
                            Placeholder.unparsed("content", content)
                        )
                }
        )
    }

    override fun register() {
        registerRootCommand()
        registerSendCommand()
        ImplUtils.debugLogInfo("All commands are registered!")
    }

    override fun registerCommandManager() {
        commandManager.registerBrigadier()
        commandManager.registerAsynchronousCompletions()
    }

}