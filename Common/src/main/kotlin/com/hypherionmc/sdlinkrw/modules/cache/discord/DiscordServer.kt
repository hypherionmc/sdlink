package com.hypherionmc.sdlinkrw.modules.cache.discord

import com.hypherionmc.sdlink.api.messaging.MessageType
import com.hypherionmc.sdlink.core.config.SDLinkConfig
import com.hypherionmc.sdlink.core.managers.DatabaseManager
import com.hypherionmc.sdlinkrw.SDLinkConstants
import com.hypherionmc.sdlinkrw.modules.database.SDLWebhooks
import com.hypherionmc.sdlinkrw.modules.kotlin.java_ext.decrypt
import com.hypherionmc.sdlinkrw.modules.kotlin.java_ext.encrypt
import com.hypherionmc.sdlinkrw.util.Debugger
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
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

/**
 * @author HypherionSA
 *
 * Per Server cache for Discord
 */
class DiscordServer(val guild: Guild, val jda: JDA) {

    //region Cache
    val channelMap: MutableMap<MessageType, List<GuildMessageChannel>> = mutableMapOf()
    val webhookCache: MutableMap<Long, Webhook> = mutableMapOf()

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

    /**
     * Initialize the entire cache for this server.
     */
    fun loadCache() {
        loadChannelCache()
        loadRoleCache()
        loadMemberCache()
        loadEmojiCache()
    }

    /**
     * Retrieve a webhook for a channel. This will either be cached or created.
     *
     * @param chan The channel to retrieve the webhook for
     * @return The webhook for the channel
     */
    @Throws(Exception::class)
    fun getWebhook(chan: GuildMessageChannel): Webhook {
        Debugger.log("Getting webhook for $chan from cache")
        return webhookCache.computeIfAbsent(chan.idLong) {
            Debugger.log("Creating webhook for $chan")
            Debugger.log("ChannelType Matches: ${chan is StandardGuildMessageChannel}")
            val wh = (chan as StandardGuildMessageChannel)
                .createWebhook("SDL")
                .complete()

            Debugger.log("Created webhook for $chan")
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

        Debugger.log("Loaded ${guildChannels.size} channels for ${guild.name}")

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
     *
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
                    Debugger.log("Adding verified role ${role.name} to ${member.effectiveName} in ${guild.name}")
                    guild.addRoleToMember(member, role).queue()
                } else {
                    Debugger.log("Removing verified role ${role.name} to ${member.effectiveName} in ${guild.name}")
                    guild.removeRoleFromMember(member, role).queue()
                }
            } catch (e: Exception) {
                SDLinkConstants.LOGGER.error("Failed to update verified roles for ${member.effectiveName} in ${guild.name}", e.message)
            }
        }
    }

    /**
     * Update the nickname for a member during verification.
     *
     * @param member The member to update
     * @param accountName The account name to set the nickname to
     * @param added Whether the nickname should be added or removed
     */
    fun updateNickname(member: Member, accountName: String?, added: Boolean) {
        if (!SDLinkConfig.INSTANCE.accessControl.changeDiscordNickname) return

        try {
            if (added) {
                Debugger.log("Updating nickname for ${member.effectiveName} in ${guild.name}")
                guild.modifyNickname(member, accountName).queue()
            } else {
                if (member.nickname == null || member.nickname!! != accountName) return
                Debugger.log("Removing nickname for ${member.effectiveName} in ${guild.name}")
                guild.modifyNickname(member, null).queue()
            }
        } catch (e: Exception) {
            SDLinkConstants.LOGGER.error("Failed to update nickname for ${member.effectiveName} in ${guild.name}", e.message)
        }
    }

    /**
     * Ban a member from the server when a ban occurs in Minecraft.
     *
     * @param userid The user ID of the member to ban
     */
    fun banMember(userid: String) {
        if (!SDLinkConfig.INSTANCE.accessControl.banMemberOnMinecraftBan) return
        if (guildMembers.firstOrNull { it.id == userid } == null) return

        try {
            guild.ban(UserSnowflake.fromId(userid), 7, TimeUnit.DAYS).reason("Banned on Minecraft Server").queue()
        } catch (e: Exception) {
            SDLinkConstants.LOGGER.error("Failed to ban member in ${guild.name}", e.message)
        }
    }

    //region Permission Checker

    /**
     * Check the bot setup for errors and missing permission.
     *
     * @param builder The StringBuilder to append errors to
     * @param errCount The AtomicInteger to increment the error count by
     */
    fun checkBotSetup(builder: StringBuilder, errCount: AtomicInteger) {
        Debugger.log("Checking bot setup for ${guild.name}")
        if (SDLinkConfig.INSTANCE.accessControl.banMemberOnMinecraftBan)
            botPerms.add(Permission.BAN_MEMBERS)

        if (SDLinkConfig.INSTANCE.accessControl.changeDiscordNickname)
            botPerms.add(Permission.NICKNAME_MANAGE)

        val bot = guild.selfMember
        val currentBotPerms = bot.permissionsExplicit
        if (!currentBotPerms.contains(Permission.ADMINISTRATOR)) {
            checkBotPerms(errCount, builder, currentBotPerms)
        }

        val fallbackChat = fetchChannels(SDLinkConfig.INSTANCE.channels.chatMessages.channels)

        for (channel in fallbackChat) {
            checkChannelPerms(channel.id, "Chat Channel", errCount, builder, bot, true)
        }

        if (fallbackChat.isNotEmpty()) {
            channelMap[MessageType.CHAT] = fallbackChat
        }

        for (channel in SDLCache.messageDestinations) {
            if (channel.key == MessageType.CHAT) continue

            val channels = fetchChannels(channel.value.channelsRaw())

            if (channels.isEmpty()) {
                if (channel.key != MessageType.CONSOLE && !fallbackChat.isEmpty()) {
                    SDLinkConstants.LOGGER.warn("No ${channel.key.name} Channels set for ${guild.name}. Defaulting to Chat Channels")
                    channelMap[channel.key] = fallbackChat
                }
            } else {
                channelMap[channel.key] = channels

                for (chan in channelMap[channel.key]!!) {
                    checkChannelPerms(chan.id, "${channel.key.name} Channel", errCount, builder, bot, false)
                }
            }
        }

        if (channelMap.isEmpty()) {
            errCount.incrementAndGet()
            builder.append("${errCount.get()}) No Channels set for ${guild.name}. No messages will be relayed to or from it\r\n")
        }
    }

    /**
     * Fetch channels from the cache by ID
     *
     * @param filter The list of channel IDs to fetch
     * @return The list of channels that were fetched
     */
    private fun fetchChannels(filter: MutableList<String>): List<StandardGuildMessageChannel> {
        if (filter.contains("default_chat")) {
            filter.addAll(SDLinkConfig.INSTANCE.botConfig.defaultChannels.default_chat)
            filter.remove("default_chat")
        }

        if (filter.contains("default_event")) {
            filter.addAll(SDLinkConfig.INSTANCE.botConfig.defaultChannels.default_event)
            filter.remove("default_event")
        }

        if (filter.contains("default_console")) {
            filter.addAll(SDLinkConfig.INSTANCE.botConfig.defaultChannels.default_console)
            filter.remove("default_console")
        }

        return guildChannels
            .filter { it.id in filter && it.type == ChannelType.TEXT }
            .map { it as StandardGuildMessageChannel }
            .toList()
    }

    /**
     * Check the permissions of the bot in the current server
     *
     * @param errCount The AtomicInteger to increment the error count by
     * @param builder The StringBuilder to append errors to
     * @param permissions The permissions of the bot in the channel
     */
    private fun checkBotPerms(errCount: AtomicInteger, builder: StringBuilder, permissions: EnumSet<Permission>) {
        botPerms.forEach(Consumer { perm: Permission ->
            if (!permissions.contains(perm)) {
                errCount.incrementAndGet()
                builder.append("${errCount.get()}) Missing Bot Permission in ${guild.name}: ${perm.name}\r\n")
            }
        })
    }

    /**
     * Check the permissions of a channel
     *
     * @param channelID The ID of the channel to check
     * @param channelName The name of the channel to display in the error message
     * @param errCount The AtomicInteger to increment the error count by
     * @param builder The StringBuilder to append errors to
     * @param bot The bot member to check the permissions of
     * @param isChatChannel Whether the channel is a chat channel or not
     */
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