package com.hypherionmc.sdlink.client;

import com.hypherionmc.craterlib.nojang.resources.ResourceIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Based on <a href="https://github.com/SarahIsWeird/MoreChatSuggestions/blob/main/src/main/java/com/sarahisweird/morechatsuggestions/client/MoreChatSuggestions.java">...</a>
 */
public class MentionsController {

    private static final Map<ResourceIdentifier, Collection<String>> mentions = new LinkedHashMap<>();
    private static final Map<ResourceIdentifier, MentionCondition> mentionConditions = new LinkedHashMap<>();
    private static boolean lastMentionConditional = true;

    public static void registerMention(ResourceIdentifier mentionClass, Collection<String> suggestions, MentionCondition condition) {
        mentions.put(mentionClass, suggestions);
        mentionConditions.put(mentionClass, condition);
    }

    public static Collection<String> getMentions(String currentWord) {
        ArrayList<String> applicableMentions = new ArrayList<>();
        lastMentionConditional = false;

        mentionConditions.forEach((mention, condition) -> {
            boolean shouldSuggest = condition.shouldAddMention(currentWord);
            if (!shouldSuggest) return;

            if (!lastMentionConditional && condition != MentionCondition.ALWAYS) {
                lastMentionConditional = true;
            }

            applicableMentions.addAll(mentions.get(mention));
        });

        return applicableMentions;
    }

    public static boolean isLastMentionConditional() {
        return lastMentionConditional;
    }

}
