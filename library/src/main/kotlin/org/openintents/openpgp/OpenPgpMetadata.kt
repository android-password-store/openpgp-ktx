/*
 * Copyright © 2019 Harsh Shandilya <msfjarvis@gmail.com>. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-Only
 */
@file:JvmName("OpenPgpMetadata")
package org.openintents.openpgp

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

class OpenPgpMetadata() : Parcelable {

    var filename: String? = null
    var mimeType: String? = null
    var charset: String? = null
    var modificationTime: Long = 0
    var originalSize: Long = 0

    constructor(
        filename: String?,
        mimeType: String?,
        modificationTime: Long,
        originalSize: Long,
        charset: String?
    ) : this() {
        this.filename = filename
        this.mimeType = mimeType
        this.modificationTime = modificationTime
        this.originalSize = originalSize
        this.charset = charset
    }

    constructor(
        filename: String?,
        mimeType: String?,
        modificationTime: Long,
        originalSize: Long
    ) : this() {
        this.filename = filename
        this.mimeType = mimeType
        this.modificationTime = modificationTime
        this.originalSize = originalSize
    }

    constructor(b: OpenPgpMetadata) : this() {
        filename = b.filename
        mimeType = b.mimeType
        modificationTime = b.modificationTime
        originalSize = b.originalSize
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
        dest.writeString(filename)
        dest.writeString(mimeType)
        dest.writeLong(modificationTime)
        dest.writeLong(originalSize)
        // version 2
        dest.writeString(charset)
        // Go back and write the size
        val parcelableSize = dest.dataPosition() - startPosition
        dest.setDataPosition(sizePosition)
        dest.writeInt(parcelableSize)
        dest.setDataPosition(startPosition + parcelableSize)
    }

    companion object CREATOR : Creator<OpenPgpMetadata> {
        /**
         * Since there might be a case where new versions of the client using the library getting
         * old versions of the protocol (and thus old versions of this class), we need a versioning
         * system for the parcels sent between the clients and the providers.
         */
        const val PARCELABLE_VERSION = 2
        override fun createFromParcel(source: Parcel): OpenPgpMetadata? {
            val version = source.readInt() // parcelableVersion
            val parcelableSize = source.readInt()
            val startPosition = source.dataPosition()
            val vr = OpenPgpMetadata()
            vr.filename = source.readString()
            vr.mimeType = source.readString()
            vr.modificationTime = source.readLong()
            vr.originalSize = source.readLong()
            if (version >= 2) {
                vr.charset = source.readString()
            }
            // skip over all fields added in future versions of this parcel
            source.setDataPosition(startPosition + parcelableSize)
            return vr
        }

        override fun newArray(size: Int): Array<OpenPgpMetadata?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        var out = "\nfilename: $filename"
        out += "\nmimeType: $mimeType"
        out += "\nmodificationTime: $modificationTime"
        out += "\noriginalSize: $originalSize"
        out += "\ncharset: $charset"
        return out
    }
}
