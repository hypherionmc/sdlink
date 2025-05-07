package com.hypherionmc.sdlink.api.messaging;

import com.hypherionmc.craterlib.utils.ChatUtils;
import com.hypherionmc.sdlink.SDLinkConstants;
import com.hypherionmc.sdlink.core.config.SDLinkConfig;
import com.hypherionmc.sdlink.core.config.impl.MessageIgnoreConfig;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import com.hypherionmc.sdlink.core.discord.BotController;
import com.hypherionmc.sdlink.core.discord.SDLWebhookServerMember;
import com.hypherionmc.sdlink.core.managers.DatabaseManager;
import com.hypherionmc.sdlink.util.SDLinkChatUtils;
import com.hypherionmc.sdlink.util.translations.Text;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.Role;
import net.fellbaum.jemoji.EmojiManager;
import org.jetbrains.annotations.Nullable;
import shadow.kyori.adventure.text.Component;
import shadow.kyori.adventure.text.TextComponent;
import shadow.kyori.adventure.text.event.ClickEvent;
import shadow.kyori.adventure.text.event.HoverEvent;
import shadow.kyori.adventure.text.format.NamedTextColor;
import shadow.kyori.adventure.text.format.Style;
import shadow.kyori.adventure.text.format.TextColor;

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
            String attachmentText = String.valueOf(Text.translate("message.attachments", (long) originalMessage.getAttachments().size()));
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
                    String attachmentText = String.valueOf(Text.translate("message.attachments", (long) replyReference.getAttachments().size()));
                    formattedReply = formattedReply.isEmpty() ? String.format("%s attachments", replyReference.getAttachments().size()) : String.format("%s %s", formattedReply, attachmentText);
                }

                formattedReply = EmojiManager.replaceAllEmojis(formattedReply, emoji -> !emoji.getDiscordAliases().isEmpty() ? emoji.getDiscordAliases().get(0) : emoji.getEmoji());
            } catch (Exception e) {
                if (SDLinkConfig.INSTANCE.generalConfig.debugging) {
                    BotController.INSTANCE.getLogger().error("Failed to process reply formatting: {}", e.getMessage());
                }
            }
        }

        // Format emojis
        formattedMessage = EmojiManager.replaceAllEmojis(message, emoji -> !emoji.getDiscordAliases().isEmpty() ? emoji.getDiscordAliases().get(0) : emoji.getEmoji());
    }

    /**
     * Get the formatted message, to be sent to Minecraft
     *
     * @return The formatted {@link Component} that will be sent to Minecraft
     */
    public Component getFormattedMessageComponent() {
        // Parse Discord Content
        parseMessage();

        if (SDLinkConfig.INSTANCE.generalConfig.debugging) SDLinkConstants.LOGGER.info("Got message {} from {}", formattedMessage, sender.getEffectiveName());

        // Checked linked names, if any
        AtomicReference<String> user = new AtomicReference<>(sender.getEffectiveName());
        try {
            if (SDLinkConfig.INSTANCE.chatConfig.useLinkedNames) {
                List<SDLinkAccount> accounts = DatabaseManager.INSTANCE.getCollection(SDLinkAccount.class);
                accounts.stream().filter(a -> a.getDiscordID() != null && a.getDiscordID().equals(sender.getId())).findFirst().ifPresent(u -> user.set(u.getInGameName()));
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

        String prefix = SDLinkChatUtils.applyFiltering(mainPrefix,
                (i) -> (i.target == MessageIgnoreConfig.FilterTarget.USERNAME || i.target == MessageIgnoreConfig.FilterTarget.BOTH)
                        && i.appliesTo == MessageIgnoreConfig.AppliesTo.MINECRAFT);

        if (prefix.isEmpty())
            prefix = mainPrefix;

        Style baseStyle = Style.empty();
        Component component = parsePlaceholders(ChatUtils.format(prefix), baseStyle, sender);

        // Apply messaging filters
        formattedMessage = SDLinkChatUtils.applyFiltering(formattedMessage, (i) -> (i.target == MessageIgnoreConfig.FilterTarget.CHAT || i.target == MessageIgnoreConfig.FilterTarget.BOTH) && i.appliesTo == MessageIgnoreConfig.AppliesTo.MINECRAFT);
        if (formattedMessage.isEmpty())
            return null;

        Component finalComponent = component.append(SDLinkChatUtils.parseChatLinks(formattedMessage));

        // Handle Replies
        if (formattedReply != null && !formattedReply.isEmpty() && replyMember != null) {
            String newReply = SDLinkChatUtils.applyFiltering(formattedReply, (i) -> (i.target == MessageIgnoreConfig.FilterTarget.CHAT || i.target == MessageIgnoreConfig.FilterTarget.BOTH) && i.appliesTo == MessageIgnoreConfig.AppliesTo.MINECRAFT);

            if (newReply != null && !newReply.isEmpty()) {
                formattedReply = newReply;
            }

            finalComponent = parsePlaceholders(
                    ChatUtils.format(SDLinkConfig.INSTANCE.messageFormatting.mcReplyFormatting),
                    Style.style().build(),
                    replyMember,
                    formattedReply
            ).append(finalComponent);

            if (formattedReply.length() > 30) {
                finalComponent = finalComponent.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, SDLinkChatUtils.parseChatLinks(formattedReply)));
            }

            try {
                finalComponent = finalComponent.clickEvent(ClickEvent.openUrl(
                        new URL(getOriginalMessage().getReferencedMessage().getJumpUrl())
                ));
            } catch (Exception ignored) {}
        }

        if ((formattedReply == null || formattedReply.isEmpty()) && SDLinkConfig.INSTANCE.chatConfig.showDiscordInfo) {
            finalComponent = appendDiscordInfo(sender, finalComponent);
        }

        return finalComponent;
    }

    /**
     * Parse Placeholders in strings, into their values
     *
     * @param inComponent The {@link Component} to handle
     * @param baseStyle The base {@link Style} to build on
     * @param member The Discord {@link Member} that sent the message
     * @return The formatted Component
     */
    private Component parsePlaceholders(Component inComponent, Style baseStyle, Member member) {
        return parsePlaceholders(inComponent, baseStyle, member, null);
    }

    /**
     * Parse Placeholders in strings, into their values
     *
     * @param component The {@link Component} to handle
     * @param baseStyle The base {@link Style} to build on
     * @param member The Discord {@link Member} that sent the message
     * @param message The optional message that will replace the %message_summary% placeholder
     * @return The formatted Component
     */
    private Component parsePlaceholders(Component component, Style baseStyle, Member member, @Nullable String message) {
        Component result = Component.empty();

        if (component instanceof TextComponent textComponent) {
            String content = textComponent.content();
            int lastIndex = 0;
            Matcher matcher = patternStart.matcher(content);

            while (matcher.find()) {
                if (matcher.start() > lastIndex) {
                    result = result.append(
                            Component.text(content.substring(lastIndex, matcher.start()))
                                    .style(baseStyle.merge(textComponent.style()))
                    );
                }

                String var = matcher.group(1);
                if (var != null) {
                    switch (var) {
                        case "color" -> baseStyle = baseStyle.color(TextColor.color(member.getColorRaw()));
                        case "end_color" -> baseStyle = baseStyle.color(NamedTextColor.WHITE);
                        case "replier_name" -> result = result.append(
                                Component.text(member.getEffectiveName())
                                        .style(baseStyle.merge(textComponent.style()))
                        );
                        case "message_summary" -> {
                            if (message != null) {
                                Component msgComponent = SDLinkChatUtils.parseChatLinks(message);

                                if (message.length() > 30) {
                                    msgComponent = Component.text(ChatUtils.resolve(msgComponent, false).substring(0, 30) + "...").style(msgComponent.style());
                                }

                                result = result.append(
                                        msgComponent.applyFallbackStyle(baseStyle.merge(textComponent.style()))
                                );
                            }
                        }
                        default -> result = result.append(
                                Component.text("%" + var + "%")
                                        .style(baseStyle.merge(textComponent.style()))
                        );
                    }
                }

                lastIndex = matcher.end();
            }

            if (lastIndex < content.length()) {
                result = result.append(Component.text(content.substring(lastIndex))
                                .style(baseStyle.merge(textComponent.style()))
                );
            }
        }

        for (Component child : component.children()) {
            result = result.append(parsePlaceholders(child, baseStyle.merge(component.style()), member, message));
        }

        return result;
    }

    /**
     * Append Discord User Info to the hover tooltip
     *
     * @param member The Discord Member that sent the message
     * @param currentComponent The {@link Component} that will be sent to minecraft
     * @return The formatted component with tooltip added
     */
    private Component appendDiscordInfo(Member member, Component currentComponent) {
        Component memberDetails = Component.empty();

        memberDetails = memberDetails
                .append(Component.text(
                        Text.translate("tooltip.display_name") + ": ")
                        .style(Style.style().color(NamedTextColor.YELLOW).build())
                        .append(Component.text(member.getEffectiveName()).style(Style.style()
                                .color(NamedTextColor.WHITE).build()))
                        .appendNewline()
                )
                .append(Component.text(
                        Text.translate("tooltip.username") + ": ")
                        .style(Style.style().color(NamedTextColor.YELLOW).build())
                        .append(Component.text(member.getUser().getName())
                                .style(Style.style().color(NamedTextColor.WHITE).build()))
                        .appendNewline()
                )
                .append(Component.text(
                        Text.translate("tooltip.roles") + ": ")
                        .style(Style.style().color(NamedTextColor.YELLOW).build())
                        .append(Component.text(String.join(", ", member.getRoles().stream().map(Role::getName).toList()))
                                .style(Style.style().color(NamedTextColor.WHITE).build()))
                );

        currentComponent = currentComponent.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, memberDetails));
        return currentComponent;
    }

}
