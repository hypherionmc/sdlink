package com.hypherionmc.sdlinkrw.api.accounts

import com.google.gson.Gson
import com.hypherionmc.craterlib.api.game.authlib.CraterGameProfile
import com.hypherionmc.craterlib.core.event.CraterEventBus
import com.hypherionmc.sdlink.api.messaging.Result
import com.hypherionmc.sdlink.compat.rolesync.RoleSync
import com.hypherionmc.sdlink.core.config.SDLinkConfig
import com.hypherionmc.sdlink.core.managers.DatabaseManager
import com.hypherionmc.sdlinkrw.api.events.VerificationEvent
import com.hypherionmc.sdlinkrw.modules.cache.discord.SDLCache
import com.hypherionmc.sdlinkrw.modules.database.SDLinkAccount
import com.hypherionmc.sdlinkrw.modules.kotlin.discord_ext.getAllRoles
import com.hypherionmc.sdlinkrw.modules.kotlin.discord_ext.memberFromCache
import com.hypherionmc.sdlinkrw.modules.kotlin.java_ext.random
import com.hypherionmc.sdlinkrw.modules.kotlin.java_ext.translate
import com.hypherionmc.sdlinkrw.util.Debugger
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import org.apache.commons.lang3.ArrayUtils
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author HypherionSA
 *
 * Represents a Minecraft account stored either in the database or in the cache.
 */
class MinecraftAccount private constructor(val username: String, val uuid: UUID) {

    /**
     * Convert this account to a GameProfile.
     *
     * @return The GameProfile representation of this account.
     */
    fun toGameProfile(): CraterGameProfile = CraterGameProfile.fromGame(username, uuid)

    /**
     * Check if this account is verified.
     *
     * @return True if the account is verified, false otherwise.
     */
    fun isAccountVerified(): Boolean {
        val account = getStoredAccount()
        return !account.discordID.isNullOrBlank()
    }

    /**
     * Get the stored account from the database.
     *
     * @return The stored account or a blank account if none exists.
     */
    fun getStoredAccount(): SDLinkAccount {
        return DatabaseManager.INSTANCE.findById(uuid.toString(), SDLinkAccount::class.java) ?: newDBEntry()
    }

    /**
     * Create a new database entry for this account.
     *
     * @return The newly created account.
     */
    private fun newDBEntry(): SDLinkAccount {
        val account = SDLinkAccount(
            uuid.toString(),
            username,
            username,
            null,
            null,
            false
        )

        DatabaseManager.INSTANCE.updateEntry(account)

        return account
    }

    /**
     * Get the Discord name of this account.
     *
     * @return The Discord name of this account or "Unlinked" if not linked.
     */
    fun getDiscordName(): String {
        val account = getStoredAccount()
        if (account.discordID.isNullOrEmpty()) return "account.unlinked".translate()

        return SDLCache.getUserById(account.discordID!!)?.user?.name ?: "account.unlinked".translate()
    }

    /**
     * Get the Discord user of this account.
     *
     * @param guild The guild to get the user from.
     * @return The Discord user of this account or null if not linked.
     */
    fun getDiscordUser(guild: Guild): DiscordUser? {
        val account = getStoredAccount()
        if (account.discordID.isNullOrEmpty()) return null

        val member: Member = guild.memberFromCache(account.discordID!!) ?: return null
        return DiscordUser(member.effectiveName, member.effectiveAvatarUrl, member.idLong, member.asMention, member.colorRaw)
    }

    // TODO: Nuke this when nothing else needs to anymore
    @Deprecated("Use getDiscordUser(Guild)")
    fun getDiscordUser(): DiscordUser? {
        val account = getStoredAccount()
        if (account.discordID.isNullOrEmpty()) return null

        val member: Member = SDLCache.findMemberByDiscordID(account.discordID!!) ?: return null
        return DiscordUser(member.effectiveName, member.effectiveAvatarUrl, member.idLong, member.asMention, member.colorRaw)
    }

    /**
     * Verify this account.
     *
     * @param member The member to verify.
     * @return Result of the verification. Use {@link Result#isError()} to check for errors.
     */
    fun verifyAccount(member: Member): Result {
        val account = getStoredAccount()
        account.discordID = member.id
        account.verifyCode = null

        try {
            DatabaseManager.INSTANCE.updateEntry(account)
        } catch (_: Exception) {}

        SDLCache.updateVerifiedRoles(member, false)
        SDLCache.updateNickname(member, account.effectiveInGameName, true)

        CraterEventBus.INSTANCE.postEvent(VerificationEvent.PlayerVerified(this))
        return Result.success("account.verify_success".translate())
    }

    /**
     * Unverify this account.
     *
     * @param member The member to unverify.
     * @return Result of the unverification. Use {@link Result#isError()} to check for errors.
     */
    fun unverifyAccount(member: Member, guild: Guild): Result {
        val account = getStoredAccount()
        val oldAccount = this
        account.discordID = null
        account.verifyCode = null

        try {
            DatabaseManager.INSTANCE.updateEntry(account)
        } catch (_: Exception) {}

        SDLCache.updateVerifiedRoles(member, false)
        SDLCache.updateNickname(member, account.effectiveInGameName, false)

        try {
            val roles = member.getAllRoles()
            for (role: Role in roles) {
                // TODO: Multisync
                RoleSync.INSTANCE.roleRemovedFromMember(member, role, guild, oldAccount)
            }
        } catch (_: Exception) {}

        CraterEventBus.INSTANCE.postEvent(VerificationEvent.PlayerUnverified(this))
        return Result.success("account.unverify_success".translate())
    }

    /**
     * Check if this account can log in passing all Access Control checks.
     *
     * @return Result of the check. Use {@link Result#isError()} to check for errors.
     */
    fun canLogin(): Result {
        if (!SDLinkConfig.INSTANCE.accessControl.enabled && !SDLinkConfig.INSTANCE.accessControl.optionalVerification)
            return Result.success("")

        val account = getStoredAccount()

        if (!isAccountVerified() && SDLinkConfig.INSTANCE.accessControl.enabled) {
            if (account.verifyCode.isNullOrEmpty()) {
                val code = Int.random(1000, 9999)
                account.verifyCode = code.toString()
                DatabaseManager.INSTANCE.updateEntry(account)

                return Result.error(SDLinkConfig.INSTANCE.accessControl.verificationMessages.accountVerify.replace("{code}", code.toString()))
            } else {
                return Result.error(SDLinkConfig.INSTANCE.accessControl.verificationMessages.accountVerify.replace("{code}", account.verifyCode!!))
            }
        }

        val result = checkAccessControl()

        if (result.isError) {
            when (result.message) {
                "notFound" -> {
                    return Result.error("account.not_in_database".translate())
                }

                "noGuildFound" -> {
                    return Result.error("error.no_discord_server".translate())
                }

                "memberNotFound" -> {
                    return Result.error(SDLinkConfig.INSTANCE.accessControl.verificationMessages.nonMember)
                }

                "userCacheEmpty" -> {
                    return Result.error("error.empty_cache".translate())
                }

                "rolesNotLoaded" -> {
                    return Result.error("error.no_roles".translate())
                }

                "accessDeniedByRole" -> {
                    return Result.error(SDLinkConfig.INSTANCE.accessControl.verificationMessages.roleDenied)
                }

                "rolesNotFound" -> {
                    return Result.error(
                        SDLinkConfig.INSTANCE
                            .accessControl
                            .verificationMessages
                            .requireRoles
                            .replace(
                                "{roles}",
                                ArrayUtils.toString(SDLCache.getVerificationRoles().stream().map<String> { obj: Role -> obj.name }.toList())
                            )
                    )
                }
            }
        }

        return Result.success("")
    }

    /**
     * Check if this account passes all Access Control checks.
     *
     * @return Result of the check. Use {@link Result#isError()} to check for errors.
     */
    fun checkAccessControl(): Result {
        if (!SDLinkConfig.INSTANCE.accessControl.enabled && !SDLinkConfig.INSTANCE.accessControl.optionalVerification)
            return Result.success("")

        val account = getStoredAccount()
        if (account.discordID.isNullOrEmpty() && SDLinkConfig.INSTANCE.accessControl.enabled)
            return Result.error("notVerified")

        // TODO: Limit membership requirements to specific discord servers
        if (SDLinkConfig.INSTANCE.accessControl.requireDiscordMembership) {
            SDLCache.findMemberByDiscordID(account.discordID!!) ?: return Result.error("memberNotFound")
        }

        if (!SDLinkConfig.INSTANCE.accessControl.requiredRoles.isEmpty() || !SDLinkConfig.INSTANCE.accessControl.deniedRoles.isEmpty()) {
            val anyFound = AtomicBoolean(false)
            val deniedFound = AtomicBoolean(false)

            val member = SDLCache.findMemberByDiscordID(account.discordID!!) ?: return Result.success("pass")

            Debugger.log("${member.effectiveName} has roles: ${member.getAllRoles().stream().map { obj: Role -> obj.name }.toList()}")

            for (role in member.getAllRoles()) {
                if (SDLCache.getDeniedRoles().stream().anyMatch { it.idLong == role.idLong }) {
                    deniedFound.set(true)
                    break
                } else if (SDLCache.getVerificationRoles().stream().anyMatch { it.idLong == role.idLong }) {
                    anyFound.set(true)
                    break
                }
            }

            if (deniedFound.get() && !SDLCache.getDeniedRoles().isEmpty()) return Result.error("accessDeniedByRole")

            if (!anyFound.get() && !SDLCache.getVerificationRoles().isEmpty()) return Result.error("rolesNotFound")
        }

        return Result.success("pass")
    }

    /**
     * Ban this account from the Discord server.
     */
    fun banDiscordMember() {
        val account = getStoredAccount()
        if (account.discordID.isNullOrEmpty()) return
        SDLCache.banMember(account.discordID!!)
    }

    companion object {

        /**
         * Create a new MinecraftAccount from an SDLinkAccount.
         *
         * @param account The SDLinkAccount to convert.
         * @return The MinecraftAccount representation of the account.
         */
        @JvmStatic
        fun of(account: SDLinkAccount): MinecraftAccount {
            return MinecraftAccount(account.username, UUID.fromString(account.uuid))
        }

        /**
         * Create a new MinecraftAccount from a CraterGameProfile.
         *
         * @param profile The CraterGameProfile to convert.
         * @return The MinecraftAccount representation of the profile.
         */
        @JvmStatic
        fun of(profile: CraterGameProfile): MinecraftAccount {
            return MinecraftAccount(profile.name, profile.id)
        }

        /**
         * Get a MinecraftAccount from a Discord ID.
         *
         * @param discordId The Discord ID to search for.
         * @return The MinecraftAccount if found, null otherwise.
         */
        @JvmStatic
        fun fromDiscordId(discordId: String): MinecraftAccount? {
            val account: SDLinkAccount? = DatabaseManager.INSTANCE
                .getCollection(SDLinkAccount::class.java)
                .stream()
                .filter { !it.discordID.isNullOrBlank() && it.discordID == discordId }
                .findFirst()
                .orElse(null)

            if (account != null) return of(account)
            return null
        }
    }
}