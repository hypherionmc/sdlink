/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.util;

import com.hypherionmc.sdlink.core.discord.BotController;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author HypherionSA
 * Util Class to handle Encryption/Decryption of Bot-Tokens and Webhook URLS
 * Since people DON'T READ THE COMMENTS and leave these in-tact,
 * they are now encrypted by default
 */
public final class EncryptionUtil {

    private final boolean canRun;
    public static EncryptionUtil INSTANCE = getInstance();
    // Instance of the Encryptor Used
    private final StandardPBEStringEncryptor encryptor;

    private EncryptionUtil() {
        String encCode = "";

        File storageDir = new File("sdlinkstorage");
        if (storageDir.exists())
            storageDir.mkdirs();

        // Try to read a saved encryption key, or try to save a new one
        try {
            File encKey = new File(storageDir.getAbsolutePath() + File.separator + "sdlink.enc");
            if (!encKey.exists()) {
                FileUtils.writeStringToFile(encKey, getSaltString(), StandardCharsets.UTF_8);
            }
            encCode = FileUtils.readFileToString(encKey, StandardCharsets.UTF_8);
        } catch (Exception e) {
            BotController.INSTANCE.getLogger().error("Failed to initialize Encryption", e);
        }

        canRun = !encCode.isEmpty();

        encryptor = new StandardPBEStringEncryptor();
        if (canRun)
            encryptor.setPassword(encCode);

        if (!canRun)
            BotController.INSTANCE.getLogger().error("Failed to initialize encryption system. Your config values will not be encrypted!");
    }

    private static EncryptionUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EncryptionUtil();
        }
        return INSTANCE;
    }

    /**
     * Will Encrypt the string passed into it, if it's not already encrypted
     *
     * @param input The string to be encrypted
     * @return The encrypted string
     */
    public String encrypt(String input) {
        if (!canRun)
            return input;

        if (isEncrypted(input)) {
            return input;
        }

        input = "enc:" + input;
        return encryptor.encrypt(input);
    }

    /**
     * Decrypts an encrypted string
     *
     * @param input The encrypted String
     * @return The Plain Text String
     */
    public String decrypt(String input) {
        if (!canRun)
            return input;

        if (!isEncrypted(input))
            return input;

        input = internalDecrypt(input);

        if (input.startsWith("enc:")) {
            input = input.replaceFirst("enc:", "");
        }
        return input;
    }

    // Used internally
    private String internalDecrypt(String input) {
        if (!canRun)
            return input;

        return encryptor.decrypt(input);
    }

    // Test if String is encrypted
    private boolean isEncrypted(String input) {
        try {
            String temp = internalDecrypt(input);
            return temp.startsWith("enc:");
        } catch (EncryptionOperationNotPossibleException ignored) {
            // String is likely not encrypted
        }
        return false;
    }

    // Generate Random codes for encryption/decryption
    private String getSaltString() {
        return RandomStringUtils.random(SDLinkUtils.intInRange(30, 100), true, true);
    }
}
