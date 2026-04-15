package com.hypherionmc.sdlinkrw.modules.cache.discord

import club.minnced.discord.webhook.WebhookClient
import com.hypherionmc.sdlink.api.messaging.MessageDestination
import com.hypherionmc.sdlink.core.config.SDLinkConfig
import com.hypherionmc.sdlink.core.discord.BotController
import com.hypherionmc.sdlink.core.managers.DatabaseManager
import com.hypherionmc.sdlink.util.Debugger
import com.hypherionmc.sdlinkrw.modules.database.SDLWebhooks
import com.hypherionmc.sdlinkrw.modules.kotlin.decrypt
import com.hypherionmc.sdlinkrw.modules.kotlin.encrypt
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji
import net.dv8tion.jda.internal.entities.WebhookImpl
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import kotlin.jvm.Throws

/**
 * @author HypherionSA
 * Per Server cache for Discord
 */
class DiscordServer(val guild: Guild, val jda: JDA) {

    //region Cache
    val channelMap: MutableMap<MessageDestination, List<GuildMessageChannel>> = mutableMapOf()
    val webhookMap: MutableMap<MessageDestination, WebhookClient> = mutableMapOf()

    val guildChannels: MutableSet<GuildChannel> = mutableSetOf()
    val guildRoles: MutableSet<Role> = mutableSetOf()
    val guildMembers: MutableSet<Member> = mutableSetOf()
    val guildEmojis: MutableSet<RichCustomEmoji> = mutableSetOf()
    //endregion

    //region Permissions
    // Base Permissions required by the bot to operate
    private val botPerms: MutableList<Permission> = mutableListOf(
        Permission.NICKNAME_CHANGE,
        Permission.MANAGE_WEBHOOKS,
        Permission.MESSAGE_SEND,
        Permission.MESSAGE_EMBED_LINKS,
        Permission.MESSAGE_HISTORY,
        Permission.MESSAGE_EXT_EMOJI,
        Permission.MANAGE_ROLES,
        Permission.MESSAGE_MANAGE,
        Permission.MESSAGE_SEND_IN_THREADS
    )

    // Basic Channel Permissions required by all channels
    private val baseChannelPerms: MutableList<Permission> = mutableListOf(
        Permission.VIEW_CHANNEL,
        Permission.MESSAGE_SEND,
        Permission.MESSAGE_EMBED_LINKS,
        Permission.MANAGE_WEBHOOKS
    )
    //endregion

    val webhookCache: MutableMap<Long, Webhook> = mutableMapOf()

    /**
     * Initialize the entire cache for this server.
     */
    fun loadCache() {
        loadChannelCache()
        loadRoleCache()
        loadMemberCache()
        loadEmojiCache()
    }

    @Throws(Exception::class)
    fun getWebhook(chan: GuildMessageChannel): Webhook {
        Debugger.INSTANCE.log("Getting webhook for $chan from cache")
        return webhookCache.computeIfAbsent(chan.idLong) {
            Debugger.INSTANCE.log("Creating webhook for $chan")
            Debugger.INSTANCE.log("ChannelType Matches: ${chan is StandardGuildMessageChannel}")
            val wh = (chan as StandardGuildMessageChannel)
                .createWebhook("SDL")
                .complete()

            Debugger.INSTANCE.log("Created webhook for $chan")
            val whCache = SDLWebhooks(
                UUID.randomUUID(),
                wh.id,
                wh.token!!.encrypt(),
                chan.id,
                guild.id
            )

            DatabaseManager.INSTANCE.updateEntry(whCache)
            wh
        }
    }

    /**
     * Load the channel cache for this server.
     */
    internal fun loadChannelCache() {
        guildChannels.clear()
        guildChannels.addAll(guild.getChannels(false).filter { it.type != ChannelType.CATEGORY }.toList())

        Debugger.INSTANCE.log("Loaded ${guildChannels.size} channels for ${guild.name}")

        DatabaseManager.INSTANCE.findAll(SDLWebhooks::class.java).filter { it.guildId == guild.id }.forEach { wh ->
            val channel = guildChannels.firstOrNull { it.id == wh.channelId } ?: return@forEach

            val webhook = WebhookImpl(channel as StandardGuildMessageChannel, wh.webhookId.toLong(), WebhookType.INCOMING)
            webhook.setToken(wh.webhookToken.decrypt())
            webhook.setOwner(guild.selfMember, jda.selfUser)
            webhookCache[channel.idLong] = webhook
        }
    }

    /**
     * Load the role cache for this server.
     */
    internal fun loadRoleCache() {
        guildRoles.clear()
        guildRoles.addAll(guild.roles.filter { !it.isManaged }.toList())
    }

    /**
     * Load the member cache for this server.
     */
    internal fun loadMemberCache() {
        guildMembers.clear()
        guildMembers.addAll(guild.members.toList())
    }

    /**
     * Load the emoji cache for this server.
     */
    internal fun loadEmojiCache() {
        guildEmojis.clear()
        guildEmojis.addAll(guild.emojis.toList())
    }

    /**
     * Update the verified roles for a member during verification.
     * @param member The member to update
     * @param added Whether the roles should be added or removed
     */
    fun updateVerifiedRoles(member: Member, added: Boolean) {
        if (SDLinkConfig.INSTANCE.accessControl.verifiedRole.isEmpty()) return
        val roles = guildRoles.filter { SDLinkConfig.INSTANCE.accessControl.verifiedRole.contains(it.id) }.toList()
        if (roles.isEmpty()) return

        for (role in roles) {
            try {
                if (added) {
                    guild.addRoleToMember(member, role).queue()
                } else {
                    guild.removeRoleFromMember(member, role).queue()
                }
            } catch (e: Exception) {
                BotController.INSTANCE.logger.error("Failed to update verified roles for ${member.effectiveName} in ${guild.name}", e.message)
            }
        }
    }

    /**
     * Update the nickname for a member during verification.
     * @param member The member to update
     * @param accountName The account name to set the nickname to
     * @param added Whether the nickname should be added or removed
     */
    fun updateNickname(member: Member, accountName: String?, added: Boolean) {
        if (!SDLinkConfig.INSTANCE.accessControl.changeDiscordNickname) return

        try {
            if (added) {
                guild.modifyNickname(member, member.effectiveName).queue()
            } else {
                if (member.nickname == null || member.nickname!! != accountName) return
                guild.modifyNickname(member, null).queue()
            }
        } catch (e: Exception) {
            BotController.INSTANCE.logger.error("Failed to update nickname for ${member.effectiveName} in ${guild.name}", e.message)
        }
    }

    /**
     * Ban a member from the server when a ban occurs in Minecraft.
     * @param userid The user ID of the member to ban
     */
    fun banMember(userid: String) {
        if (!SDLinkConfig.INSTANCE.accessControl.banMemberOnMinecraftBan) return
        if (guildMembers.firstOrNull { it.id == userid } == null) return

        try {
            guild.ban(UserSnowflake.fromId(userid), 7, TimeUnit.DAYS).reason("Banned on Minecraft Server").queue()
        } catch (e: Exception) {
            BotController.INSTANCE.logger.error("Failed to ban member in ${guild.name}", e.message)
        }
    }

    //region Permission Checker

    fun checkBotSetup(builder: StringBuilder, errCount: AtomicInteger) {
        Debugger.INSTANCE.log("Checking bot setup for ${guild.name}")
        if (SDLinkConfig.INSTANCE.accessControl.banMemberOnMinecraftBan)
            botPerms.add(Permission.BAN_MEMBERS)

        if (SDLinkConfig.INSTANCE.accessControl.changeDiscordNickname)
            botPerms.add(Permission.NICKNAME_MANAGE)

        val bot = guild.selfMember
        val currentBotPerms = bot.permissionsExplicit
        if (currentBotPerms.contains(Permission.ADMINISTRATOR)) return

        checkBotPerms(errCount, builder, currentBotPerms)

        val chatChannels = fetchChannels(SDLinkConfig.INSTANCE.channelsAndWebhooks.channels.chatChannelID)
        val eventChannels = fetchChannels(SDLinkConfig.INSTANCE.channelsAndWebhooks.channels.eventsChannelID)
        val consoleChannel = fetchChannels(SDLinkConfig.INSTANCE.channelsAndWebhooks.channels.consoleChannelID)

        for (channel in chatChannels) {
            checkChannelPerms(channel.id, "Chat Channel", errCount, builder, bot, true)
        }

        for (channel in eventChannels) {
            checkChannelPerms(channel.id, "Events Channel", errCount, builder, bot, false)

        }

        for (channel in consoleChannel) {
            checkChannelPerms(channel.id, "Console Channel", errCount, builder, bot, false)
        }

        channelMap[MessageDestination.CHAT] = chatChannels
        channelMap[MessageDestination.EVENT] = eventChannels
        channelMap[MessageDestination.CONSOLE] = consoleChannel

        if (eventChannels.isEmpty() && !chatChannels.isEmpty()) {
            channelMap[MessageDestination.EVENT] = chatChannels
            BotController.INSTANCE.logger.warn("No Events Channels set for ${guild.name}. Defaulting to Chat Channels")
        }

        if (consoleChannel.isEmpty() && !chatChannels.isEmpty()) {
            channelMap[MessageDestination.CONSOLE] = chatChannels
            BotController.INSTANCE.logger.warn("No Console Channels set for ${guild.name}. Defaulting to Chat Channels")
        }

        if (eventChannels.isEmpty() && consoleChannel.isEmpty() && chatChannels.isEmpty()) {
            errCount.incrementAndGet()
            builder.append("${errCount.get()}) No Channels set for ${guild.name}. No messages will be relayed to or from it\r\n")
        }
    }

    private fun fetchChannels(filter: List<String>): List<StandardGuildMessageChannel> {
        return guildChannels
            .filter { it.id in filter && it.type == ChannelType.TEXT }
            .map { it as StandardGuildMessageChannel }
            .toList()
    }

    private fun checkBotPerms(errCount: AtomicInteger, builder: StringBuilder, permissions: EnumSet<Permission>) {
        botPerms.forEach(Consumer { perm: Permission ->
            if (!permissions.contains(perm)) {
                errCount.incrementAndGet()
                builder.append("${errCount.get()}) Missing Bot Permission in ${guild.name}: ${perm.name}\r\n")
            }
        })
    }

    private fun checkChannelPerms(channelID: String, channelName: String, errCount: AtomicInteger, builder: StringBuilder, bot: Member, isChatChannel: Boolean) {
        if (channelID == "" || channelID == "0") return
        val channel = guild.getChannelById(GuildMessageChannel::class.java, channelID) ?: return

        val permissions = bot.getPermissions(channel)

        baseChannelPerms.forEach(Consumer { perm: Permission ->
            if (!permissions.contains(perm)) {
                errCount.incrementAndGet()
                builder.append("${errCount.get()}) Missing $channelName Permission in ${guild.name}: ${perm.name}\r\n")
            }
        })

        if (isChatChannel) {
            if (SDLinkConfig.INSTANCE.botConfig.channelTopic.doTopicUpdates && !permissions.contains(Permission.MANAGE_CHANNEL)) {
                errCount.incrementAndGet()
                builder.append("${errCount.get()}) Missing Chat Channel Permission in ${guild.name}: Manage Channel. Topic updates will not work\r\n")
            }
        }
    }
    //endregion
}