package com.hypherionmc.sdlink.client;

/**
 * Based on <a href="https://github.com/SarahIsWeird/MoreChatSuggestions/blob/main/src/main/java/com/sarahisweird/morechatsuggestions/SuggestionCondition.java">...</a>
 */
@FunctionalInterface
public interface MentionCondition {

    boolean shouldAddMention(String currentWord);

    MentionCondition ALWAYS = currentWord -> true;

}
