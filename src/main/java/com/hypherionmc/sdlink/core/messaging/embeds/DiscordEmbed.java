/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.messaging.embeds;

import java.util.ArrayList;

public class DiscordEmbed {

    public String color;
    public String title;
    public String url;
    public Author author;
    public String description;
    public Thumbnail thumbnail;
    public ArrayList<Field> fields;
    public Image image;
    public int timestamp;
    public Footer footer;

    public static class Author {
        public String name;
        public String icon_url;
        public String url;
    }

    public static class Field {
        public String name;
        public String value;
        public boolean inline;
    }

    public static class Footer {
        public String text;
        public String icon_url;
    }

    public static class Image {
        public String url;
    }

    public static class Thumbnail {
        public String url;
    }
}