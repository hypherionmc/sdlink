package com.hypherionmc.sdlink.api.messaging;

import com.hypherionmc.craterlib.api.game.text.Text;
import com.hypherionmc.craterlib.libs.kyori.adventure.text.TextComponent;
import com.hypherionmc.craterlib.libs.kyori.adventure.text.event.ClickEvent;
import com.hypherionmc.craterlib.libs.kyori.adventure.text.event.HoverEvent;
import com.hypherionmc.craterlib.libs.kyori.adventure.text.format.NamedTextColor;
import com.hypherionmc.craterlib.libs.kyori.adventure.text.format.Style;
import com.hypherionmc.craterlib.libs.kyori.adventure.text.format.TextColor;
import com.hypherionmc.sdlinkrw.SDLinkConstants;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.discord.SDLWebhookServerMember;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlink.util.SDLinkChatUtils;
import com.hypherionmc.sdlinkrw.modules.database.SDLinkAccount;
import com.hypherionmc.sdlinkrw.modules.translations.SDText;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.Role;
import net.fellbaum.jemoji.EmojiManager;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HypherionSA
 * A Helper class to convert Discord Messages, to Minecraft Messages
 */
@RequiredArgsConstructor(staticName = "of")
@Getter
public final class MessageContext {

    private final Member originalSender;
    private final Message originalMessage;

    @Nullable private String formattedMessage;
    @Nullable private String formattedReply;
    @Nullable private Member replyMember;
    private Member sender;

    final Pattern patternStart = Pattern.compile("%(.*?)(?:\\|(.*?))?%", Pattern.CASE_INSENSITIVE);

    /**
     * Parse a Discord Message into individual, usable components
     */
    private void parseMessage() {
        // Sender
        sender = originalMessage.isWebhookMessage()
                ? SDLWebhookServerMember.of(originalMessage.getAuthor(), originalMessage.getGuild(), originalMessage.getJDA()) : originalSender;

        // Message Content
        String message = originalMessage.getContentDisplay();
        MessageReference messageReference = originalMessage.getMessageReference();
        Message replyReference = originalMessage.getReferencedMessage();

        // Message was forwarded
        if (messageReference != null && messageReference.getType() == MessageReference.MessageReferenceType.FORWARD) {
            message = originalMessage.getMessageSnapshots().get(0).getContentRaw();
        }

        // Check for attachments
        if (!originalMessage.getAttachments().isEmpty()) {
            String attachmentText = String.valueOf(SDText.translate("message.attachments", (long) originalMessage.getAttachments().size()));
            message = message.isEmpty() ? String.format("%s attachments", originalMessage.getAttachments().size()) : String.format("%s %s", message, attachmentText);
        }

        // The output message is empty, so we ignore
        if (message.isEmpty()) return;

        // Message is a reply message
        if (replyReference != null) {
            try {
                replyMember = replyReference.isWebhookMessage()
                        ? SDLWebhookServerMember.of(replyReference.getAuthor(), replyReference.getGuild(), replyReference.getJDA()) : replyReference.getMember();

                formattedReply = replyReference.getContentDisplay();

                if (!replyReference.getAttachments().isEmpty()) {
                    String attachmentText = String.valueOf(SDText.translate("message.attachments", (long) replyReference.getAttachments().size()));
                    formattedReply = formattedReply.isEmpty() ? String.format("%s attachments", replyReference.getAttachments().size()) : String.format("%s %s", formattedReply, attachmentText);
                }

                formattedReply = EmojiManager.replaceAllEmojis(formattedReply, emoji -> !emoji.getDiscordAliases().isEmpty() ? emoji.getDiscordAliases().get(0) : emoji.getEmoji());
            } catch (Exception e) {
                if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                    SDLinkConstants.LOGGER.error("Failed to process reply formatting: {}", e.getMessage());
                }
            }
        }

        message = message.replace("§k", "#k");

        // Format emojis
        formattedMessage = EmojiManager.replaceAllEmojis(message, emoji -> !emoji.getDiscordAliases().isEmpty() ? emoji.getDiscordAliases().get(0) : emoji.getEmoji());
    }

    /**
     * Get the formatted message, to be sent to Minecraft
     *
     * @return The formatted {@link Text} that will be sent to Minecraft
     */
    public Text getFormattedMessageComponent() {
        // Parse Discord Content
        parseMessage();

        if (SDLinkConfig.INSTANCE.generalConfig.debugging) SDLinkConstants.LOGGER.info("Got message {} from {}", formattedMessage, sender.getEffectiveName());

        // Checked linked names, if any
        AtomicReference<String> user = new AtomicReference<>(sender.getEffectiveName());
        try {
            if (SDLinkConfig.INSTANCE.chatConfig.useLinkedNames) {
                List<SDLinkAccount> accounts = DatabaseManager.INSTANCE.getCollection(SDLinkAccount.class);
                accounts.stream().filter(a -> a.getDiscordId() != null && a.getDiscordId().equals(sender.getId())).findFirst().ifPresent(u -> user.set(u.getInGameName()));
            }
        } catch (Exception e) {
            if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                SDLinkConstants.LOGGER.error("Failed to load account database: {}", e.getMessage());
            }
        }

        // Parse Prefix
        String mainPrefix = SDLinkConfig.INSTANCE.messageFormatting.mcPrefix
                .replace("%user%", user.get()).replace("%role%", sender.getRoles().isEmpty()
                        ? "No Role" : sender.getRoles().get(0).getName());

        String prefix = SDLinkChatUtils.applyFiltering(mainPrefix, (i) -> i.appliesTo.isMinecraft() && i.appliesTo.appliesToUsername(i));

        if (prefix.isEmpty())
            prefix = mainPrefix;

        Style baseStyle = Style.empty();
        Text component = parsePlaceholders(Text.formatted(prefix), baseStyle, sender);

        // Apply messaging filters
        formattedMessage = SDLinkChatUtils.applyFiltering(formattedMessage, (i) -> i.appliesTo.isMinecraft() && i.appliesTo.appliesToChat(i));
        if (formattedMessage.isEmpty())
            return null;

        Text finalComponent = component.append(SDLinkChatUtils.parseChatLinks(formattedMessage));

        // Handle Replies
        if (formattedReply != null && !formattedReply.isEmpty() && replyMember != null) {
            String newReply = SDLinkChatUtils.applyFiltering(formattedReply, (i) -> i.appliesTo.isMinecraft() && i.appliesTo.appliesToChat(i));

            if (newReply != null && !newReply.isEmpty()) {
                formattedReply = newReply;
            }

            finalComponent = parsePlaceholders(
                    Text.formatted(SDLinkConfig.INSTANCE.messageFormatting.mcReplyFormatting),
                    Style.style().build(),
                    replyMember,
                    formattedReply
            ).append(finalComponent);

            if (formattedReply.length() > 30) {
                finalComponent.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, SDLinkChatUtils.parseChatLinks(formattedReply).getComponent()));
            }

            try {
                finalComponent.clickEvent(ClickEvent.openUrl(
                        new URL(getOriginalMessage().getReferencedMessage().getJumpUrl())
                ));
            } catch (Exception ignored) {}
        }

        if ((formattedReply == null || formattedReply.isEmpty()) && SDLinkConfig.INSTANCE.chatConfig.showDiscordInfo) {
            appendDiscordInfo(sender, finalComponent);
        }

        return finalComponent;
    }

    /**
     * Parse Placeholders in strings, into their values
     *
     * @param inComponent The {@link Text} to handle
     * @param baseStyle The base {@link Style} to build on
     * @param member The Discord {@link Member} that sent the message
     * @return The formatted Component
     */
    private Text parsePlaceholders(Text inComponent, Style baseStyle, Member member) {
        return parsePlaceholders(inComponent, baseStyle, member, null);
    }

    /**
     * Parse Placeholders in strings, into their values
     *
     * @param component The {@link Text} to handle
     * @param baseStyle The base {@link Style} to build on
     * @param member The Discord {@link Member} that sent the message
     * @param message The optional message that will replace the %message_summary% placeholder
     * @return The formatted Component
     */
    private Text parsePlaceholders(Text component, Style baseStyle, Member member, @Nullable String message) {
        Text result = Text.empty();

        if (component.getComponent() instanceof TextComponent textComponent) {
            String content = textComponent.content();
            int lastIndex = 0;
            Matcher matcher = patternStart.matcher(content);

            while (matcher.find()) {
                if (matcher.start() > lastIndex) {
                    result.append(
                            Text.literal(content.substring(lastIndex, matcher.start()))
                                    .style(baseStyle.merge(textComponent.style()))
                    );
                }

                String var = matcher.group(1);
                if (var != null) {
                    switch (var) {
                        case "color" -> baseStyle = baseStyle.color(TextColor.color(member.getColorRaw()));
                        case "end_color" -> baseStyle = baseStyle.color(NamedTextColor.WHITE);
                        case "replier_name" -> result.append(
                                Text.literal(member.getEffectiveName())
                                        .style(baseStyle.merge(textComponent.style()))
                        );
                        case "message_summary" -> {
                            if (message != null) {
                                Text msgComponent = SDLinkChatUtils.parseChatLinks(message);

                                if (message.length() > 30) {
                                    msgComponent = Text.literal(msgComponent.asString().substring(0, 30) + "...").style(msgComponent.style());
                                }

                                result.append(
                                        msgComponent.applyFallbackStyle(baseStyle.merge(textComponent.style()))
                                );
                            }
                        }
                        default -> result.append(
                                Text.literal("%" + var + "%")
                                        .style(baseStyle.merge(textComponent.style()))
                        );
                    }
                }

                lastIndex = matcher.end();
            }

            if (lastIndex < content.length()) {
                result.append(Text.literal(content.substring(lastIndex))
                                .style(baseStyle.merge(textComponent.style()))
                );
            }
        }

        for (Text child : component.children()) {
            result.append(parsePlaceholders(child, baseStyle.merge(component.style()), member, message));
        }

        return result;
    }

    /**
     * Append Discord User Info to the hover tooltip
     *
     * @param member The Discord Member that sent the message
     * @param currentComponent The {@link Text} that will be sent to minecraft
     * @return The formatted component with tooltip added
     */
    private Text appendDiscordInfo(Member member, Text currentComponent) {
        Text memberDetails = Text.empty();

        memberDetails
                .append(Text.literal(
                        SDText.translate("tooltip.display_name") + ": ")
                        .style(Style.style().color(NamedTextColor.YELLOW).build())
                        .append(Text.literal(member.getEffectiveName()).style(Style.style()
                                .color(NamedTextColor.WHITE).build()))
                        .appendNewline()
                )
                .append(Text.literal(
                        SDText.translate("tooltip.username") + ": ")
                        .style(Style.style().color(NamedTextColor.YELLOW).build())
                        .append(Text.literal(member.getUser().getName())
                                .style(Style.style().color(NamedTextColor.WHITE).build()))
                        .appendNewline()
                )
                .append(Text.literal(
                        SDText.translate("tooltip.roles") + ": ")
                        .style(Style.style().color(NamedTextColor.YELLOW).build())
                        .append(Text.literal(String.join(", ", member.getRoles().stream().map(Role::getName).toList()))
                                .style(Style.style().color(NamedTextColor.WHITE).build()))
                );

        currentComponent.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, memberDetails.getComponent()));
        return currentComponent;
    }

}
