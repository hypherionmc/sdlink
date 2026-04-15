///*
// * This file is part of sdlink-core, licensed under the MIT License (MIT).
// * Copyright HypherionSA and Contributors
// */
//package com.hypherionmc.sdlink.core.database;
//
//import com.hypherionmc.sdlink.core.jsondb.annotations.Document;
//import com.hypherionmc.sdlink.core.jsondb.annotations.Id;
//import lombok.Getter;
//import lombok.Setter;
//
//@Setter
//@Getter
//@Document(collection = "verifiedaccounts")
//public final class SDLinkAccount {
//
//    @Id
//    private String uuid;
//    private String username;
//    private String inGameName;
//    private String discordID;
//    private String verifyCode;
//    private boolean isOffline;
//
//    public String getInGameName() {
//        return inGameName == null || inGameName.isEmpty() ? username : inGameName;
//    }
//}
