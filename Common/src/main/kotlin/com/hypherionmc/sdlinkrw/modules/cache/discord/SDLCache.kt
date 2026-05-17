package com.hypherionmc.sdlinkrw.modules.cache.discord

import com.hypherionmc.sdlink.api.messaging.MessageDestination
import com.hypherionmc.sdlink.core.config.SDLinkCompatConfig
import com.hypherionmc.sdlink.core.config.SDLinkConfig
import com.hypherionmc.sdlink.core.discord.BotController
import com.hypherionmc.sdlinkrw.SDLinkConstants
import com.hypherionmc.sdlinkrw.util.Debugger
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import java.util.concurrent.atomic.AtomicInteger

object SDLCache {

    private val servers: MutableSet<DiscordServer> = mutableSetOf()

    // Invite URL for bot shown in server logs
    const val DISCORD_INVITE: String = "https://discord.com/api/oauth2/authorize?client_id={bot_id}&permissions=277965401108&scope=bot%20applications.commands"

    /**
     * Initialize the cache from the current available servers
     *
     * @param jda The JDA instance to use for getting servers
     */
    fun loadCache(jda: JDA) {
        servers.clear()
        servers.addAll(jda.guilds.map { DiscordServer(it, jda) })
        servers.forEach { it.loadCache() }
    }

    /**
     * Retrieve the server cache for a guild
     *
     * @param guild The guild to retrieve the cache for
     */
    fun getServer(guild: Guild): DiscordServer? = servers.find { it.guild.idLong == guild.idLong }

    /**
     * Reload the channel cache for a guild
     *
     * @param guild The guild to reload the cache for
     */
    fun reloadChannelCache(guild: Guild) = getServer(guild)?.loadChannelCache()

    /**
     * Reload the role cache for a guild
     *
     * @param guild The guild to reload the cache for
     */
    fun reloadRoleCache(guild: Guild) = getServer(guild)?.loadRoleCache()

    /**
     * Reload the member cache for a guild
     *
     * @param guild The guild to reload the cache for
     */
    fun reloadMemberCache(guild: Guild) = getServer(guild)?.loadMemberCache()

    /**
     * Reload the emoji cache for a guild
     *
     * @param guild The guild to reload the cache for
     */
    fun reloadEmojiCache(guild: Guild) = getServer(guild)?.loadEmojiCache()

    /**
     * Retrieve a member from the cache by their ID
     *
     * @param id The ID of the member to retrieve
     * @param guild The guild to retrieve the member from
     */
    fun getMemberById(id: String, guild: Guild): Member? = getServer(guild)?.guildMembers?.filter { it.id == id }?.let { return it.first() }

    /**
     * Retrieve a user from the cache by their ID
     *
     * @param id The ID of the user to retrieve
     */
    fun getUserById(id: String) = servers.flatMap { it.guildMembers }.firstOrNull { it.user.id == id }

    /**
     * Update the verified roles for a member
     *
     * @param member The member to update
     */
    fun updateVerifiedRoles(member: Member, added: Boolean) = servers.forEach { it.updateVerifiedRoles(member, added) }

    /**
     * Update the nickname for a member
     *
     * @param member The member to update
     */
    fun updateNickname(member: Member, accountName: String?, added: Boolean) = servers.forEach { it.updateNickname(member, accountName, added) }

    /**
     * Retrieve all roles for a member
     */
    fun getAllRolesForUser(user: User): Set<Role> =
        servers.flatMap { guild ->
            guild.guild.getMember(user)?.roles.orEmpty()
        }.toSet()

    /**
     * Find a member by their Discord ID
     *
     * @param id The ID of the member to find
     */
    fun findMemberByDiscordID(id: String) = servers.flatMap { it.guildMembers }.firstOrNull { it.id == id }

    /**
     * Ban a member from all servers
     *
     * @param userid The user ID of the member to ban
     */
    fun banMember(userid: String) = servers.forEach { it.banMember(userid) }

    /**
     * Retrieve all roles from all servers
     *
     * @return A list of all roles from all servers
     */
    fun getAllRoles() = servers.flatMap { it.guildRoles }

    /**
     * Retrieve all channels from all servers
     *
     * @return A list of all channels from all servers
     */
    fun getAllChannels() = servers.flatMap { it.guildChannels }

    /**
     * Retrieve all emojis from all servers
     *
     * @return A list of all emojis from all servers
     */
    fun getAllEmojis() = servers.flatMap { it.guildEmojis }

    /**
     * Retrieve all members from all servers
     *
     * @return A list of all members from all servers
     */
    fun getAllMembers() = servers.flatMap { it.guildMembers }

    /**
     * Retrieve a role by its ID
     *
     * @param id The ID of the role to retrieve
     */
    fun retrieveRoleById(id: String) = getAllRoles().firstOrNull { it.id == id }

    /**
     * Retrieve a webhook by its ID
     *
     * @param id The ID of the webhook to retrieve
     * @return The webhook if found, null otherwise
     */
    fun getWebhook(id: Long): Webhook? {
        val channel = getAllChannels().firstOrNull { c -> c.idLong == id } ?: return null
        return getServer(channel.guild)?.getWebhook(channel as GuildMessageChannel)
    }

    /**
     * Retrieve a channel by its ID
     *
     * @param id The ID of the channel to retrieve
     * @return The channel if found, null otherwise
     */
    fun getChannel(id: Long) = getAllChannels().firstOrNull { c -> c.idLong == id }

    /**
     * Get the verification roles from the config
     *
     * @return A set of roles that are handed out when the user is verified
     */
    fun getVerificationRoles(): Set<Role> {
        return servers.flatMap { it.guildRoles }.filter { it.id in SDLinkConfig.INSTANCE.accessControl.requiredRoles }.toSet()
    }

    /**
     * Get the denied roles from the config
     *
     * @return A set of roles that the user cannot have in order to join the server
     */
    fun getDeniedRoles(): Set<Role> {
        return servers.flatMap { it.guildRoles }.filter { it.id in SDLinkConfig.INSTANCE.accessControl.deniedRoles }.toSet()
    }

    /**
     * Get the luckperms roles from the config
     *
     * @return A set of roles that are used to sync LuckPerms groups between discord and Minecraft
     */
    fun getLuckPermsRoles(): Set<Role> {
        val lproles = SDLinkCompatConfig.INSTANCE.luckpermsCompat.syncs.map { it.role }.toList()
        return servers.flatMap { it.guildRoles }.filter { it.id in lproles }.toSet()
    }

    /**
     * Get the ftb ranks roles from the config
     *
     * @return A set of roles that are used to sync FTB Ranks groups between discord and Minecraft
     */
    fun getFtbRanksRoles(): Set<Role> {
        val ftbroles = SDLinkCompatConfig.INSTANCE.ftbRanksCompat.syncs.map { it.role }.toList()
        return servers.flatMap { it.guildRoles }.filter { it.id in ftbroles }.toSet()
    }

    /**
     * Get the player roles roles from the config
     *
     * @return A set of roles that are used to sync Player Roles groups between discord and Minecraft
     */
    fun getPlayerRoleRoles(): Set<Role> {
        val prroles = SDLinkCompatConfig.INSTANCE.playerroles.syncs.map { it.role }.toList()
        return servers.flatMap { it.guildRoles }.filter { it.id in prroles }.toSet()
    }

    /**
     * Check the bot setup for errors
     */
    fun checkBotSetup() {
        val builder = StringBuilder()
        builder.append("\r\n").append("******************* Simple Discord Link Errors *******************").append("\r\n")
        val errCount = AtomicInteger()
        val controller = BotController.INSTANCE

        Debugger.log("Checking bot setup")
        if (!controller.isBotReady()) return

        val botLink = DISCORD_INVITE.replace("{bot_id}", controller.jda.selfUser.id)

        if (SDLinkConfig.INSTANCE.botConfig.printInviteLink)
            SDLinkConstants.LOGGER.info("Discord Invite Link for Bot: {}", botLink)

        Debugger.log("Checking Guilds")
        if (controller.jda.guilds.isEmpty()) {
            errCount.incrementAndGet()
            builder.append(errCount.get())
                .append(") ")
                .append("Bot does not appear to be in any servers. You need to invite the bot to your discord server before chat relays will work. Use link [")
                .append(botLink)
                .append("] to invite the bot.")
                .append("\r\n")
        } else {
            Debugger.log("Check Guild Limits")
            if (controller.jda.guilds.size > 5) {
                errCount.incrementAndGet()
                builder.append(errCount.get())
                    .append(") ")
                    .append("Bot is in more than 5 servers. This is not supported.")
                    .append("\r\n")
            } else {
                Debugger.log("Checking individual servers")
                servers.forEach { it.checkBotSetup(builder, errCount) }
            }
        }

        if (errCount.get() > 0) {
            builder.append("\r\n").append("******************* Simple Discord Link Errors *******************").append("\r\n")
            SDLinkConstants.LOGGER.error(builder.toString())
        }
    }

    /**
     * Get all channels that are destinations for a specific type of message
     *
     * @param type The type of message destination to get
     * @return A list of channels that are destinations for the specified type of message
     */
    fun getChannelDestinations(type: MessageDestination): List<GuildMessageChannel> {
        return servers.flatMap { it.channelMap[type] ?: emptyList() }
    }
}