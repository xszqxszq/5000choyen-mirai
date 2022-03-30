@file:Suppress("SpellCheckingInspection")

package xyz.xszq

import com.soywiz.kds.iterators.fastForEach
import com.soywiz.korim.format.PNG
import com.soywiz.korim.format.encode
import com.soywiz.korio.file.std.toVfs
import com.soywiz.korio.file.writeToFile
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.permission.PermitteeId.Companion.permitteeId
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.info
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

object FiveThousandChoyenPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "xyz.xszq.fivethousand-choyen",
        name = "5000choyen",
        version = "1.0.0",
    ) {
        author("xszqxszq")
    }
) {
    private val denied by lazy {
        PermissionService.INSTANCE.register(
            PermissionId("5000choyen", "deny"), "禁用5000choyen的所有功能")
    }
    private val resourcesDataDirs = listOf("font")
    private lateinit var generator: FiveThousandChoyen
    override fun onEnable() {
        runBlocking {
            extractResources()
        }
        FiveThousandChoyenConfig.reload()
        generator = FiveThousandChoyen(
            FiveThousandChoyenConfig.topFont, FiveThousandChoyenConfig.bottomFont,
            FiveThousandChoyenConfig.transparency
        )
        FiveThousandChoyenConfig.commands.forEach {
            if (it.isNotBlank()) {
                GlobalEventChannel.subscribeMessages {
                    startsWith(it) { raw ->
                        requireNot(denied) {
                            handle(raw, this)
                        }
                    }
                }
            }
        }
        logger.info { "5000choyen 插件已成功加载" }
    }
    private suspend fun handle(raw: String, event: MessageEvent) = event.run {
        val args = raw.toArgsListByLn()
        when (args.size) {
            0 -> quoteReply("使用方法：\n生成5k 第一行文本\n第二行文本（可选）")
            1 -> quoteReply(generator.generate(args.first().trim(), " ").encode(PNG)
                .toExternalResource().use {
                    it.uploadAsImage(subject)
                })
            else -> quoteReply(generator.generate(args[0].trim(), args[1].trim()).encode(PNG)
                .toExternalResource().use {
                    it.uploadAsImage(subject)
                })
        }
    }
    private suspend fun extractResources() {
        resourcesDataDirs.fastForEach { dir -> extractResourceDir(dir) }
    }
    private suspend fun extractResourceDir(dir: String) {
        val now = resolveDataFile(dir).toVfs()
        if (!now.isDirectory())
            now.delete()
        now.mkdir()
        getResourceFileList(dir).fastForEach {
            val target = now[it]
            if (!target.exists())
                getResourceAsStream("$dir/$it")!!.readBytes().writeToFile(target)
        }
    }
    private fun getResourceFileList(path: String): List<String> {
        val result = mutableListOf<String>()
        javaClass.getResource("/$path") ?.let {
            val uri = it.toURI()
            kotlin.runCatching {
                FileSystems.newFileSystem(uri, buildMap<String, String> {
                    put("create", "true")
                })
            }.onFailure {}
            Files.walk(Paths.get(uri)).forEach { file ->
                if (file.isRegularFile())
                    result.add(file.name)
            }
        }
        return result
    }
}
object FiveThousandChoyenConfig: AutoSavePluginConfig("config") {
    val topFont by value("Source Han Sans CN Bold Bold")
    val bottomFont by value("Source Han Serif SC Bold")
    val transparency by value(false)
    val commands by value(listOf("/生成5k", "生成5k", "/gocho", "gocho", "/choyen", "choyen", "/5k"))
}
private fun String.toArgsListByLn(): List<String> = this.trim().split("\n").toMutableList().filter {
    isNotBlank()
}
private suspend fun MessageEvent.quoteReply(message: Message): MessageReceipt<Contact> =
    this.subject.sendMessage(this.message.quote() + message)
private suspend fun MessageEvent.quoteReply(message: String): MessageReceipt<Contact> = quoteReply(message.toPlainText())
private suspend fun <T> MessageEvent.requireNot(permission: Permission, block: suspend () -> T): T? = when {
    this is GroupMessageEvent -> {
        if (group.permitteeId.hasPermission(permission))
            null
        else
            block.invoke()
    }
    sender.permitteeId.hasPermission(permission) -> null
    else -> block.invoke()
}