/*
 * Copyright © 2019 The Android Password Authors. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-Only
 */
package me.msfjarvis.openpgpktx

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import java.util.Date

class AutocryptPeerUpdate() : Parcelable {
    private var keyData: ByteArray? = null
    private var effectiveDate: Date? = null
    private lateinit var preferEncrypt: PreferEncrypt

    private constructor(
        keyData: ByteArray?,
        effectiveDate: Date?,
        preferEncrypt: PreferEncrypt
    ) : this() {
        this.keyData = keyData
        this.effectiveDate = effectiveDate
        this.preferEncrypt = preferEncrypt
    }

    private constructor(source: Parcel, version: Int) : this() {
        keyData = source.createByteArray()
        effectiveDate = if (source.readInt() != 0) Date(source.readLong()) else null
        preferEncrypt = PreferEncrypt.values()[source.readInt()]
    }

    fun createAutocryptPeerUpdate(
        keyData: ByteArray?,
        timestamp: Date?
    ): AutocryptPeerUpdate {
        return AutocryptPeerUpdate(keyData, timestamp, PreferEncrypt.NOPREFERENCE)
    }

    fun getKeyData(): ByteArray? {
        return keyData
    }

    fun hasKeyData(): Boolean {
        return keyData != null
    }

    fun getEffectiveDate(): Date? {
        return effectiveDate
    }

    fun getPreferEncrypt(): PreferEncrypt? {
        return preferEncrypt
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
        dest.writeByteArray(keyData)
        if (effectiveDate != null) {
            dest.writeInt(1)
            dest.writeLong(effectiveDate!!.time)
        } else {
            dest.writeInt(0)
        }
        dest.writeInt(preferEncrypt.ordinal)
        // Go back and write the size
        val parcelableSize = dest.dataPosition() - startPosition
        dest.setDataPosition(sizePosition)
        dest.writeInt(parcelableSize)
        dest.setDataPosition(startPosition + parcelableSize)
    }

    companion object CREATOR : Creator<AutocryptPeerUpdate> {
        private const val PARCELABLE_VERSION = 1
        override fun createFromParcel(source: Parcel): AutocryptPeerUpdate? {
            val version = source.readInt() // parcelableVersion
            val parcelableSize = source.readInt()
            val startPosition = source.dataPosition()
            val vr = AutocryptPeerUpdate(source, version)
            // skip over all fields added in future versions of this parcel
            source.setDataPosition(startPosition + parcelableSize)
            return vr
        }

        override fun newArray(size: Int): Array<AutocryptPeerUpdate?>? {
            return arrayOfNulls(size)
        }
    }

    enum class PreferEncrypt {
        NOPREFERENCE, MUTUAL
    }
}
