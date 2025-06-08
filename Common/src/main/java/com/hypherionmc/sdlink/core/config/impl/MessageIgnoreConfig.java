/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.config.impl;

import shadow.hypherionmc.moonconfig.core.conversion.Path;
import shadow.hypherionmc.moonconfig.core.conversion.SpecComment;

import java.util.ArrayList;
import java.util.List;

public final class MessageIgnoreConfig {

    @Path("enabled")
    @SpecComment("Enable the filter system")
    public boolean enabled = true;

    @Path("entries")
    @SpecComment("List of entries to process")
    public List<Ignore> entries = new ArrayList<>();

    @Path("ignoredThreads")
    @SpecComment("Ignore messages sent from certain threads. Enable debug logging to see what thread the message is from")
    public List<String> ignoredThread = new ArrayList<>();

    public enum FilterMode {
        STARTS_WITH,
        MATCHES,
        CONTAINS,
        REGEX
    }

    public enum ActionMode {
        REPLACE,
        IGNORE
    }

    public enum FilterTarget {
        CHAT,
        USERNAME,
        BOTH,
        CONSOLE
    }

    public enum AppliesTo {
        DISCORD,
        MINECRAFT
    }

    public static class Ignore {
        @Path("target")
        @SpecComment("Should this filter target CHAT, USERNAME, BOTH, or CONSOLE.")
        public FilterTarget target = FilterTarget.CHAT;

        @Path("ignoreConsole")
        @SpecComment("Ignore this filter in Console Messages")
        public boolean ignoreConsole = false;

        @Path("appliesTo")
        @SpecComment("What way of relay does this filter apply to. DISCORD or MINECRAFT")
        public AppliesTo appliesTo = AppliesTo.DISCORD;

        @Path("search")
        @SpecComment("The text to search for in the message")
        public String search;

        @Path("replace")
        @SpecComment("Text to replace `search` with, if it's found. Leave empty to ignore")
        public String replace;

        @Path("searchMode")
        @SpecComment("How should `search` be found in the text. Valid entries are STARTS_WITH, MATCHES and CONTAINS, REGEX")
        public FilterMode searchMode = FilterMode.CONTAINS;

        @Path("action")
        @SpecComment("How should `replace` be treated, when `search` is found using `searchMode`")
        public ActionMode action = ActionMode.REPLACE;
    }

}
