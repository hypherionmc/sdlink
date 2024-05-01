/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.database;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Document(collection = "verifiedaccounts", schemaVersion = "1.0")
public class SDLinkAccount {

    @Id
    private String uuid;
    private String username;
    private String discordID;
    private String verifyCode;
    private boolean isOffline;

}
