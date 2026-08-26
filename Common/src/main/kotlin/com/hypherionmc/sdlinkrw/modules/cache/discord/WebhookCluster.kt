package com.hypherionmc.sdlinkrw.modules.cache.discord

import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.external.JDAWebhookClient
import com.hypherionmc.sdlink.api.messaging.MessageType
import com.hypherionmc.sdlinkrw.util.Debugger
import net.dv8tion.jda.api.entities.Webhook
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel

/**
 * @author HypherionSA
 *
 * A Cluster of Webhook Clients for use in SDLink.
 */
object WebhookCluster {

    val webhookCache: MutableMap<Long, JDAWebhookClient?> = mutableMapOf()
    val utilityWebhooks: MutableMap<Long, JDAWebhookClient?> = mutableMapOf()

    /**
     * Get or create a webhook client for a channel.
     *
     * @param type The type of channel
     * @param id The ID of the channel
     * @return The webhook client
     */
    fun getClient(type: MessageType, id: Long): JDAWebhookClient? {
        Debugger.log("Getting webhook client for $type $id")

        if (type == MessageType.RELAY) {
            return utilityWebhooks.computeIfAbsent(id) {
                val client = createWebhookClient(id, "Utility")
                return@computeIfAbsent client
            }
        } else {
            return webhookCache.computeIfAbsent(id) {
                val client = createWebhookClient(id, type.name)
                Debugger.log("Created webhook client for $type $id")
                return@computeIfAbsent client
            }
        }
    }

    /**
     * Create a webhook client for a channel.
     *
     * @param id The ID of the channel
     * @param name The name of the webhook client
     * @return The webhook client
     */
    private fun createWebhookClient(id: Long, name: String): JDAWebhookClient? {
        Debugger.log("Creating webhook client for $name $id")
        val channel = SDLCache.getChannel(id) ?: return null

        Debugger.log("Webhook channel: $channel")
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

        Debugger.log("Webhook client created")
        return whc.buildJDA()
    }

    /**
     * Check if a webhook ID is an app webhook from this bot
     *
     * @param id The ID of the webhook
     * @return True if the webhook is an app webhook, false otherwise
     */
    fun isAppWebhook(id: Long): Boolean {
        return webhookCache.values.stream().filter { it != null }.anyMatch({ c -> c!!.getId() === id })
                || utilityWebhooks.values.stream().filter { it != null }.anyMatch({ c -> c!!.getId() === id })
    }

    /**
     * Shutdown all webhook clients.
     */
    fun shutdown() {
        webhookCache.values.forEach { it?.close() }
        utilityWebhooks.values.forEach { it?.close() }
    }

}