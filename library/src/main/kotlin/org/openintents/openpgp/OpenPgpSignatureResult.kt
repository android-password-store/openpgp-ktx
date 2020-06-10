/*
 * Copyright © 2019 The Android Password Authors. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-Only
 */
@file:JvmName("OpenPgpSignatureResult")

package org.openintents.openpgp

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import me.msfjarvis.openpgpktx.util.OpenPgpUtils
import java.util.Collections
import java.util.Date

class OpenPgpSignatureResult : Parcelable {
    private val result: Int
    private val keyId: Long
    private val primaryUserId: String?
    private val userIds: ArrayList<String>?
    private val confirmedUserIds: ArrayList<String>?
    private val senderStatusResult: SenderStatusResult?
    private val signatureTimestamp: Date?
    private val autocryptPeerentityResult: AutocryptPeerResult?

    private constructor(
        signatureStatus: Int,
        signatureUserId: String?,
        keyId: Long,
        userIds: ArrayList<String>?,
        confirmedUserIds: ArrayList<String>?,
        senderStatusResult: SenderStatusResult?,
        signatureOnly: Boolean?,
        signatureTimestamp: Date?,
        autocryptPeerentityResult: AutocryptPeerResult?
    ) {
        result = signatureStatus
        primaryUserId = signatureUserId
        this.keyId = keyId
        this.userIds = userIds
        this.confirmedUserIds = confirmedUserIds
        this.senderStatusResult = senderStatusResult
        this.signatureTimestamp = signatureTimestamp
        this.autocryptPeerentityResult = autocryptPeerentityResult
    }

    private constructor(source: Parcel, version: Int) {
        result = source.readInt()
        // we dropped support for signatureOnly, but need to skip the value for compatibility
        source.readByte()
        primaryUserId = source.readString()
        keyId = source.readLong()
        userIds = if (version > 1) {
            source.createStringArrayList()
        } else {
            null
        }
        // backward compatibility for this exact version
        if (version > 2) {
            senderStatusResult = readEnumWithNullAndFallback(
                source,
                SenderStatusResult.values,
                SenderStatusResult.UNKNOWN
            )
            confirmedUserIds = source.createStringArrayList()
        } else {
            senderStatusResult = SenderStatusResult.UNKNOWN
            confirmedUserIds = null
        }
        signatureTimestamp = if (version > 3) {
            if (source.readInt() > 0) Date(source.readLong()) else null
        } else {
            null
        }
        autocryptPeerentityResult = if (version > 4) {
            readEnumWithNullAndFallback(
                source,
                AutocryptPeerResult.values,
                null
            )
        } else {
            null
        }
    }

    fun getUserIds(): List<String> {
        return Collections.unmodifiableList(userIds)
    }

    fun getConfirmedUserIds(): List<String> {
        return Collections.unmodifiableList(confirmedUserIds)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        /**
         * NOTE: When adding fields in the process of updating this API, make sure to bump
         * [.PARCELABLE_VERSION].
         */
        dest.writeInt(PARCELABLE_VERSION)
        // Inject a placeholder that will store the parcel size from this point on
        // (not including the size itself).
        val sizePosition = dest.dataPosition()
        dest.writeInt(0)
        val startPosition = dest.dataPosition()
        // version 1
        dest.writeInt(result)
        // signatureOnly is deprecated since version 3. we pass a dummy value for compatibility
        dest.writeByte(0.toByte())
        dest.writeString(primaryUserId)
        dest.writeLong(keyId)
        // version 2
        dest.writeStringList(userIds)
        // version 3
        writeEnumWithNull(dest, senderStatusResult)
        dest.writeStringList(confirmedUserIds)
        // version 4
        if (signatureTimestamp != null) {
            dest.writeInt(1)
            dest.writeLong(signatureTimestamp.time)
        } else {
            dest.writeInt(0)
        }
        // version 5
        writeEnumWithNull(dest, autocryptPeerentityResult)
        // Go back and write the size
        val parcelableSize = dest.dataPosition() - startPosition
        dest.setDataPosition(sizePosition)
        dest.writeInt(parcelableSize)
        dest.setDataPosition(startPosition + parcelableSize)
    }

    override fun toString(): String {
        var out = "\nresult: $result"
        out += "\nprimaryUserId: $primaryUserId"
        out += "\nuserIds: $userIds"
        out += "\nkeyId: " + OpenPgpUtils.convertKeyIdToHex(keyId)
        return out
    }

    @Deprecated("")
    fun withSignatureOnlyFlag(signatureOnly: Boolean): OpenPgpSignatureResult {
        return OpenPgpSignatureResult(
            result, primaryUserId, keyId, userIds, confirmedUserIds,
            senderStatusResult, signatureOnly, signatureTimestamp, autocryptPeerentityResult
        )
    }

    fun withAutocryptPeerResult(autocryptPeerentityResult: AutocryptPeerResult?): OpenPgpSignatureResult {
        return OpenPgpSignatureResult(
            result, primaryUserId, keyId, userIds, confirmedUserIds,
            senderStatusResult, null, signatureTimestamp, autocryptPeerentityResult
        )
    }

    enum class SenderStatusResult {
        UNKNOWN, USER_ID_CONFIRMED, USER_ID_UNCONFIRMED, USER_ID_MISSING;

        companion object {
            val values =
                values()
        }
    }

    enum class AutocryptPeerResult {
        OK, NEW, MISMATCH;

        companion object {
            val values =
                values()
        }
    }

    companion object CREATOR : Creator<OpenPgpSignatureResult> {
        /**
         * Since there might be a case where new versions of the client using the library getting
         * old versions of the protocol (and thus old versions of this class), we need a versioning
         * system for the parcels sent between the clients and the providers.
         */
        private const val PARCELABLE_VERSION = 5

        // content not signed
        const val RESULT_NO_SIGNATURE = -1

        // invalid signature!
        const val RESULT_INVALID_SIGNATURE = 0

        // successfully verified signature, with confirmed key
        const val RESULT_VALID_KEY_CONFIRMED = 1

        // no key was found for this signature verification
        const val RESULT_KEY_MISSING = 2

        // successfully verified signature, but with unconfirmed key
        const val RESULT_VALID_KEY_UNCONFIRMED = 3

        // key has been revoked -> invalid signature!
        const val RESULT_INVALID_KEY_REVOKED = 4

        // key is expired -> invalid signature!
        const val RESULT_INVALID_KEY_EXPIRED = 5

        // insecure cryptographic algorithms/protocol -> invalid signature!
        const val RESULT_INVALID_KEY_INSECURE = 6

        override fun createFromParcel(source: Parcel): OpenPgpSignatureResult? {
            val version = source.readInt() // parcelableVersion
            val parcelableSize = source.readInt()
            val startPosition = source.dataPosition()
            val vr = OpenPgpSignatureResult(source, version)
            // skip over all fields added in future versions of this parcel
            source.setDataPosition(startPosition + parcelableSize)
            return vr
        }

        override fun newArray(size: Int): Array<OpenPgpSignatureResult?> {
            return arrayOfNulls(size)
        }

        fun createWithValidSignature(
            signatureStatus: Int,
            primaryUserId: String?,
            keyId: Long,
            userIds: ArrayList<String>?,
            confirmedUserIds: ArrayList<String>?,
            senderStatusResult: SenderStatusResult?,
            signatureTimestamp: Date?
        ): OpenPgpSignatureResult {
            require(!(signatureStatus == RESULT_NO_SIGNATURE || signatureStatus == RESULT_KEY_MISSING || signatureStatus == RESULT_INVALID_SIGNATURE)) { "can only use this method for valid types of signatures" }
            return OpenPgpSignatureResult(
                signatureStatus, primaryUserId, keyId, userIds, confirmedUserIds,
                senderStatusResult, null, signatureTimestamp, null
            )
        }

        fun createWithNoSignature(): OpenPgpSignatureResult {
            return OpenPgpSignatureResult(
                RESULT_NO_SIGNATURE,
                null,
                0L,
                null,
                null,
                null,
                null,
                null,
                null
            )
        }

        fun createWithKeyMissing(keyId: Long, signatureTimestamp: Date?): OpenPgpSignatureResult {
            return OpenPgpSignatureResult(
                RESULT_KEY_MISSING,
                null,
                keyId,
                null,
                null,
                null,
                null,
                signatureTimestamp,
                null
            )
        }

        fun createWithInvalidSignature(): OpenPgpSignatureResult {
            return OpenPgpSignatureResult(
                RESULT_INVALID_SIGNATURE,
                null,
                0L,
                null,
                null,
                null,
                null,
                null,
                null
            )
        }

        private fun <T : Enum<T>?> readEnumWithNullAndFallback(
            source: Parcel,
            enumValues: Array<T>,
            fallback: T?
        ): T? {
            val valueOrdinal = source.readInt()
            if (valueOrdinal == -1) {
                return null
            }
            return if (valueOrdinal >= enumValues.size) {
                fallback
            } else enumValues[valueOrdinal]
        }

        private fun writeEnumWithNull(dest: Parcel, enumValue: Enum<*>?) {
            if (enumValue == null) {
                dest.writeInt(-1)
                return
            }
            dest.writeInt(enumValue.ordinal)
        }
    }
}
