/*
 * Copyright © 2019 The Android Password Authors. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-Only
 */
package me.msfjarvis.openpgpktx.util

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import java.io.Serializable
import java.util.Locale
import java.util.regex.Pattern

object OpenPgpUtils {
    private val PGP_MESSAGE: Pattern = Pattern.compile(
        ".*?(-----BEGIN PGP MESSAGE-----.*?-----END PGP MESSAGE-----).*",
        Pattern.DOTALL
    )
    private val PGP_SIGNED_MESSAGE: Pattern = Pattern.compile(
        ".*?(-----BEGIN PGP SIGNED MESSAGE-----.*?-----BEGIN PGP SIGNATURE-----.*?-----END PGP SIGNATURE-----).*",
        Pattern.DOTALL
    )
    const val PARSE_RESULT_NO_PGP = -1
    const val PARSE_RESULT_MESSAGE = 0
    const val PARSE_RESULT_SIGNED_MESSAGE = 1

    fun parseMessage(message: String): Int {
        val matcherSigned = PGP_SIGNED_MESSAGE.matcher(message)
        val matcherMessage = PGP_MESSAGE.matcher(message)
        return if (matcherMessage.matches()) {
            PARSE_RESULT_MESSAGE
        } else if (matcherSigned.matches()) {
            PARSE_RESULT_SIGNED_MESSAGE
        } else {
            PARSE_RESULT_NO_PGP
        }
    }

    fun isAvailable(context: Context): Boolean {
        val intent = Intent(OpenPgpApi.SERVICE_INTENT_2)
        val resInfo =
            context.packageManager.queryIntentServices(intent, 0)
        return resInfo.isNotEmpty()
    }

    fun convertKeyIdToHex(keyId: Long): String {
        return "0x" + convertKeyIdToHex32bit(keyId shr 32) + convertKeyIdToHex32bit(
            keyId
        )
    }

    private fun convertKeyIdToHex32bit(keyId: Long): String {
        var hexString =
            java.lang.Long.toHexString(keyId and 0xffffffffL).toLowerCase(Locale.ENGLISH)
        while (hexString.length < 8) {
            hexString = "0$hexString"
        }
        return hexString
    }

    private val USER_ID_PATTERN = Pattern.compile("^(.*?)(?: \\((.*)\\))?(?: <(.*)>)?$")
    private val EMAIL_PATTERN =
        Pattern.compile("^<?\"?([^<>\"]*@[^<>\"]*\\.[^<>\"]*)\"?>?$")

    /**
     * Splits userId string into naming part, email part, and comment part.
     * See SplitUserIdTest for examples.
     */
    fun splitUserId(userId: String): UserId {
        if (!TextUtils.isEmpty(userId)) {
            val matcher = USER_ID_PATTERN.matcher(userId)
            if (matcher.matches()) {
                var name = if (matcher.group(1).isEmpty()) null else matcher.group(1)
                val comment = matcher.group(2)
                var email = matcher.group(3)
                if (email != null && name != null) {
                    val emailMatcher = EMAIL_PATTERN.matcher(name)
                    if (emailMatcher.matches() && email == emailMatcher.group(1)) {
                        email = emailMatcher.group(1)
                        name = null
                    }
                }
                if (email == null && name != null) {
                    val emailMatcher = EMAIL_PATTERN.matcher(name)
                    if (emailMatcher.matches()) {
                        email = emailMatcher.group(1)
                        name = null
                    }
                }
                return UserId(name, email, comment)
            }
        }
        return UserId(null, null, null)
    }

    /**
     * Returns a composed user id. Returns null if name, email and comment are empty.
     */
    fun createUserId(userId: UserId): String? {
        val userIdBuilder = StringBuilder()
        if (!TextUtils.isEmpty(userId.name)) {
            userIdBuilder.append(userId.name)
        }
        if (!TextUtils.isEmpty(userId.comment)) {
            userIdBuilder.append(" (")
            userIdBuilder.append(userId.comment)
            userIdBuilder.append(")")
        }
        if (!TextUtils.isEmpty(userId.email)) {
            userIdBuilder.append(" <")
            userIdBuilder.append(userId.email)
            userIdBuilder.append(">")
        }
        return if (userIdBuilder.isEmpty()) null else userIdBuilder.toString()
    }

    class UserId(val name: String?, val email: String?, val comment: String?) :
        Serializable
}
