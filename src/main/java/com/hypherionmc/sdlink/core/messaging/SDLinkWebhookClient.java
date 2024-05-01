/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.messaging;

import club.minnced.discord.webhook.WebhookClientBuilder;

/**
 * @author HypherionSA
 * Wrapped {@link WebhookClientBuilder} for our webhooks
 */
public class SDLinkWebhookClient extends WebhookClientBuilder {

    public SDLinkWebhookClient(String name, String url) {
        super(url);

        this.setThreadFactory((job) -> {
            Thread thread = new Thread(job);
            thread.setName(name + " Webhook Thread");
            thread.setDaemon(true);
            return thread;
        });
        this.setWait(false);
    }

    public SDLinkWebhookClient setThreadChannelID(String id) {
        this.setThreadId(Long.parseLong(id));
        return this;
    }

}
