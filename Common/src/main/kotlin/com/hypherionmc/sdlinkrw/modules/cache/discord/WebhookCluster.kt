package com.hypherionmc.sdlinkrw.modules.cache.discord

import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.external.JDAWebhookClient
import com.hypherionmc.sdlink.api.messaging.MessageDestination
import com.hypherionmc.sdlink.util.Debugger
import net.dv8tion.jda.api.entities.Webhook
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import java.util.*

object WebhookCluster {

    val chatWebhooks: MutableMap<Long, JDAWebhookClient?> = mutableMapOf()
    val eventWebhooks: MutableMap<Long, JDAWebhookClient?> = mutableMapOf()
    val consoleWebhooks: MutableMap<Long, JDAWebhookClient?> = mutableMapOf()
    val utilityWebhooks: MutableMap<Long, JDAWebhookClient?> = mutableMapOf()

    fun getClient(type: MessageDestination, id: Long): JDAWebhookClient? {
        Debugger.INSTANCE.log("Getting webhook client for $type $id")

        when (type) {
            MessageDestination.CHAT -> {
                return chatWebhooks.computeIfAbsent(id) {
                    val client = createWebhookClient(id, "Chat")
                    Debugger.INSTANCE.log("Created webhook client for $type $id")
                    return@computeIfAbsent client
                }
            }

            MessageDestination.EVENT -> {
                return eventWebhooks.computeIfAbsent(id) {
                    val client = createWebhookClient(id, "Events")
                    return@computeIfAbsent client
                }
            }

            MessageDestination.CONSOLE -> {
                return consoleWebhooks.computeIfAbsent(id) {
                    val client = createWebhookClient(id, "Console")
                    return@computeIfAbsent client
                }
            }

            MessageDestination.RELAY -> {
                return utilityWebhooks.computeIfAbsent(id) {
                    val client = createWebhookClient(id, "Utility")
                    return@computeIfAbsent client
                }
            }
        }
    }

    private fun createWebhookClient(id: Long, name: String): JDAWebhookClient? {
        Debugger.INSTANCE.log("Creating webhook client for $name $id")
        val channel = SDLCache.getChannel(id) ?: return null

        Debugger.INSTANCE.log("Webhook channel: $channel")
        var threadId: Long? = null
        val webhook: Webhook = SDLCache.getWebhook(channel.idLong) ?: return null

        if (channel is ForumChannel || channel is ThreadChannel) {
            threadId = channel.idLong
        }

        val whc = WebhookClientBuilder(webhook.idLong, webhook.token!!)
            .setThreadFactory { job: Runnable? ->
                val thread = Thread(job)
                thread.setName("$name Webhook Thread")
                thread.setDaemon(true)
                thread
            }.setWait(false)

        if (threadId != null) {
            whc.setThreadId(threadId)
        }

        Debugger.INSTANCE.log("Webhook client created")
        return whc.buildJDA()
    }

    fun isAppWebhook(id: Long): Boolean {
        return chatWebhooks.values.stream().filter { it != null }.anyMatch({ c -> c!!.getId() === id })
                || eventWebhooks.values.stream().filter { it != null }.anyMatch({ c -> c!!.getId() === id })
                || consoleWebhooks.values.stream().filter { it != null }.anyMatch({ c -> c!!.getId() === id })
                || utilityWebhooks.values.stream().filter { it != null }.anyMatch({ c -> c!!.getId() === id })
    }

    fun shutdown() {
        chatWebhooks.values.forEach { it?.close() }
        eventWebhooks.values.forEach { it?.close() }
        consoleWebhooks.values.forEach { it?.close() }
        utilityWebhooks.values.forEach { it?.close() }
    }

}