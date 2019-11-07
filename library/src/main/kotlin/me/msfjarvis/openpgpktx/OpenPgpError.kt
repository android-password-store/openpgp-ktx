/*
 * Copyright © 2019 Harsh Shandilya <msfjarvis@gmail.com>. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-Only
 */
package me.msfjarvis.openpgpktx

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

class OpenPgpError() : Parcelable {
    var errorId = 0
    var message: String? = null

    constructor(parcel: Parcel) : this() {
        errorId = parcel.readInt()
        message = parcel.readString()
    }

    constructor(errorId: Int, message: String?) : this() {
        this.errorId = errorId
        this.message = message
    }

    constructor(b: OpenPgpError) : this() {
        errorId = b.errorId
        message = b.message
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
        dest.writeInt(errorId)
        dest.writeString(message)
        // Go back and write the size
        val parcelableSize = dest.dataPosition() - startPosition
        dest.setDataPosition(sizePosition)
        dest.writeInt(parcelableSize)
        dest.setDataPosition(startPosition + parcelableSize)
    }

    companion object CREATOR : Creator<OpenPgpError> {
        /**
         * Since there might be a case where new versions of the client using the library getting
         * old versions of the protocol (and thus old versions of this class), we need a versioning
         * system for the parcels sent between the clients and the providers.
         */
        const val PARCELABLE_VERSION = 1

        // possible values for errorId
        const val CLIENT_SIDE_ERROR = -1
        const val GENERIC_ERROR = 0
        const val INCOMPATIBLE_API_VERSIONS = 1
        const val NO_OR_WRONG_PASSPHRASE = 2
        const val NO_USER_IDS = 3
        const val OPPORTUNISTIC_MISSING_KEYS = 4

        override fun createFromParcel(source: Parcel): OpenPgpError? {
            source.readInt() // parcelableVersion
            val parcelableSize = source.readInt()
            val startPosition = source.dataPosition()
            val error = OpenPgpError()
            error.errorId = source.readInt()
            error.message = source.readString()
            // skip over all fields added in future versions of this parcel
            source.setDataPosition(startPosition + parcelableSize)
            return error
        }

        override fun newArray(size: Int): Array<OpenPgpError?> {
            return arrayOfNulls(size)
        }
    }
}
