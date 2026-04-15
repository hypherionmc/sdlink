package com.hypherionmc.sdlinkrw.api.accounts

import com.hypherionmc.craterlib.api.game.authlib.CraterGameProfile
import com.hypherionmc.craterlib.core.event.CraterEventBus
import com.hypherionmc.sdlink.api.messaging.Result
import com.hypherionmc.sdlink.compat.rolesync.RoleSync
import com.hypherionmc.sdlink.core.config.SDLinkConfig
import com.hypherionmc.sdlink.core.managers.DatabaseManager
import com.hypherionmc.sdlink.util.Debugger
import com.hypherionmc.sdlinkrw.api.events.VerificationEvent
import com.hypherionmc.sdlinkrw.modules.cache.discord.SDLCache
import com.hypherionmc.sdlinkrw.modules.database.SDLinkAccount
import com.hypherionmc.sdlinkrw.modules.kotlin.discord_ext.getAllRoles
import com.hypherionmc.sdlinkrw.modules.kotlin.discord_ext.memberFromCache
import com.hypherionmc.sdlinkrw.modules.kotlin.random
import com.hypherionmc.sdlinkrw.modules.kotlin.translate
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import org.apache.commons.lang3.ArrayUtils
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class MinecraftAccount private constructor(val username: String, val uuid: UUID) {

    fun toGameProfile(): CraterGameProfile = CraterGameProfile.fromGame(username, uuid)

    fun isAccountVerified(): Boolean {
        val account = getStoredAccount()
        return !account.discordId.isNullOrBlank()
    }

    fun getStoredAccount(): SDLinkAccount {
        return DatabaseManager.INSTANCE.findById(uuid.toString(), SDLinkAccount::class.java) ?: newDBEntry()
    }

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

    fun getDiscordName(): String {
        val account = getStoredAccount()
        if (account.discordId.isNullOrEmpty()) return "account.unlinked".translate()

        return SDLCache.getUserById(account.discordId!!)?.user?.name ?: "account.unlinked".translate()
    }

    fun getDiscordUser(guild: Guild): DiscordUser? {
        val account = getStoredAccount()
        if (account.discordId.isNullOrEmpty()) return null

        val member: Member = guild.memberFromCache(account.discordId!!) ?: return null
        return DiscordUser(member.effectiveName, member.effectiveAvatarUrl, member.idLong, member.asMention, member.colorRaw)
    }

    // TODO: Nuke this when nothing else needs to anymore
    @Deprecated("Use getDiscordUser(Guild)")
    fun getDiscordUser(): DiscordUser? {
        val account = getStoredAccount()
        if (account.discordId.isNullOrEmpty()) return null

        val member: Member = SDLCache.findMemberByDiscordID(account.discordId!!) ?: return null
        return DiscordUser(member.effectiveName, member.effectiveAvatarUrl, member.idLong, member.asMention, member.colorRaw)
    }

    fun verifyAccount(member: Member): Result {
        val account = getStoredAccount()
        account.discordId = member.id
        account.verifyCode = null

        try {
            DatabaseManager.INSTANCE.updateEntry(account)
        } catch (_: Exception) {}

        SDLCache.updateVerifiedRoles(member, false)
        SDLCache.updateNickname(member, account.effectiveInGameName, true)

        CraterEventBus.INSTANCE.postEvent(VerificationEvent.PlayerVerified(this))
        return Result.success("account.verify_success".translate())
    }

    fun unverifyAccount(member: Member, guild: Guild): Result {
        val account = getStoredAccount()
        val oldAccount = this
        account.discordId = null
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

    fun checkAccessControl(): Result {
        if (!SDLinkConfig.INSTANCE.accessControl.enabled && !SDLinkConfig.INSTANCE.accessControl.optionalVerification)
            return Result.success("")

        val account = getStoredAccount()
        if (account.discordId.isNullOrEmpty() && SDLinkConfig.INSTANCE.accessControl.enabled)
            return Result.error("notVerified")

        // TODO: Limit membership requirements to specific discord servers
        if (SDLinkConfig.INSTANCE.accessControl.requireDiscordMembership) {
            SDLCache.findMemberByDiscordID(account.discordId!!) ?: return Result.error("memberNotFound")
        }

        if (!SDLinkConfig.INSTANCE.accessControl.requiredRoles.isEmpty() || !SDLinkConfig.INSTANCE.accessControl.deniedRoles.isEmpty()) {
            val anyFound = AtomicBoolean(false)
            val deniedFound = AtomicBoolean(false)

            val member = SDLCache.findMemberByDiscordID(account.discordId!!) ?: return Result.success("pass")

            Debugger.INSTANCE.log("${member.effectiveName} has roles: ${member.getAllRoles().stream().map { obj: Role -> obj.name }.toList()}")

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

    fun banDiscordMember() {
        val account = getStoredAccount()
        if (account.discordId.isNullOrEmpty()) return
        SDLCache.banMember(account.discordId!!)
    }

    companion object {
        @JvmStatic
        fun of(account: SDLinkAccount): MinecraftAccount {
            return MinecraftAccount(account.username, UUID.fromString(account.uuid))
        }

        @JvmStatic
        fun of(profile: CraterGameProfile): MinecraftAccount {
            return MinecraftAccount(profile.name, profile.id)
        }

        @JvmStatic
        fun fromDiscordId(discordId: String): MinecraftAccount? {
            val account: SDLinkAccount? = DatabaseManager.INSTANCE
                .getCollection(SDLinkAccount::class.java)
                .stream()
                .filter { !it.discordId.isNullOrBlank() && it.discordId == discordId }
                .findFirst()
                .orElse(null)

            if (account != null) return of(account)
            return null
        }
    }
}