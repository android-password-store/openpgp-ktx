/*
 * Copyright © 2019 The Android Password Authors. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-Only
 */
@file:JvmName("OpenPgpKeyPreference")
@file:Suppress("Unused")

package me.msfjarvis.openpgpktx.preference

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import androidx.preference.Preference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.msfjarvis.openpgpktx.R
import me.msfjarvis.openpgpktx.util.OpenPgpApi
import me.msfjarvis.openpgpktx.util.OpenPgpServiceConnection
import me.msfjarvis.openpgpktx.util.OpenPgpServiceConnection.OnBound
import me.msfjarvis.openpgpktx.util.getAttr
import org.openintents.openpgp.IOpenPgpService2
import org.openintents.openpgp.OpenPgpError

public class OpenPgpKeyPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = getAttr(context, R.attr.preferenceStyle, android.R.attr.preferenceStyle),
    defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes) {

    private var keyId: Long = 0
    public var openPgpProvider: String? = null
        set(value) {
            field = value
            updateEnabled()
        }
    public var defaultUserId: String? = null
    public var intentRequestCode: Int = 9999
    private var serviceConnection: OpenPgpServiceConnection? = null

    override fun getSummary(): CharSequence? {
        return if (keyId == NO_KEY) context.getString(R.string.openpgp_no_key_selected) else context.getString(
            R.string.openpgp_key_selected
        )
    }

    private fun updateEnabled() {
        isEnabled = !TextUtils.isEmpty(openPgpProvider)
        notifyChanged()
    }

    override fun onClick() {
        // bind to service
        serviceConnection = OpenPgpServiceConnection(
            context.applicationContext,
            openPgpProvider,
            object : OnBound {
                override fun onBound(service: IOpenPgpService2) {
                    getSignKeyId(Intent())
                }

                override fun onError(e: Exception) {
                    Log.e(TAG, "exception on binding!", e)
                }
            }
        )
        serviceConnection?.bindToService()
    }

    private fun getSignKeyId(data: Intent?) {
        val intent = data ?: Intent()
        intent.action = OpenPgpApi.ACTION_GET_SIGN_KEY_ID
        intent.putExtra(OpenPgpApi.EXTRA_USER_ID, defaultUserId)
        GlobalScope.launch {
            OpenPgpApi(
                context,
                serviceConnection!!.service!!
            ).executeApiAsync(data, null, null) { result ->
                if (result == null) return@executeApiAsync
                when (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR)) {
                    OpenPgpApi.RESULT_CODE_SUCCESS -> {
                        val keyId =
                            result.getLongExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, NO_KEY)
                        save(keyId)
                    }
                    OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED -> {
                        val pi =
                            result.getParcelableExtra<PendingIntent>(OpenPgpApi.RESULT_INTENT)
                        try {
                            val act = context as Activity
                            act.startIntentSenderFromChild(
                                act, pi?.intentSender,
                                intentRequestCode, null, 0, 0, 0
                            )
                        } catch (e: SendIntentException) {
                            Log.e(TAG, "SendIntentException", e)
                        }
                    }
                    OpenPgpApi.RESULT_CODE_ERROR -> {
                        val error: OpenPgpError? =
                            result.getParcelableExtra(OpenPgpApi.RESULT_ERROR)
                        Log.e(TAG, "RESULT_CODE_ERROR: " + error?.message)
                    }
                }
            }
        }
    }

    private fun save(newValue: Long) {
        // Give the client a chance to ignore this change if they deem it // invalid
        if (!callChangeListener(newValue)) {
            // They don't want the value to be set
            return
        }
        setAndPersist(newValue)
    }

    public fun setValue(keyId: Long) {
        setAndPersist(keyId)
    }

    public fun getValue(): Long {
        return keyId
    }

    private fun setAndPersist(newValue: Long) {
        keyId = newValue
        // Save to persistent storage (this method will make sure this
        // preference should be persistent, along with other useful checks)
        persistLong(keyId)
        // Data has changed, notify so UI can be refreshed!
        notifyChanged()
    }

    override fun onGetDefaultValue(
        a: TypedArray,
        index: Int
    ): Any? {
        // This preference type's value type is Long, so we read the default
        // value from the attributes as an Integer.
        return a.getInteger(index, NO_KEY.toInt()).toLong()
    }

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any) {
        if (restoreValue) { // Restore state
            keyId = getPersistedLong(keyId)
        } else { // Set state
            val value = defaultValue as Long
            setAndPersist(value)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        /*
         * Suppose a client uses this preference type without persisting. We
         * must save the instance state so it is able to, for example, survive
         * orientation changes.
         */
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            // No need to save instance state since it's persistent
            return superState
        }
        // Save the instance state
        val myState = SavedState(superState)
        myState.keyId = keyId
        myState.openPgpProvider = openPgpProvider
        myState.defaultUserId = defaultUserId
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state.javaClass != SavedState::class.java) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state)
            return
        }
        // Restore the instance state
        val myState = state as SavedState
        super.onRestoreInstanceState(myState.superState)
        keyId = myState.keyId
        openPgpProvider = myState.openPgpProvider
        defaultUserId = myState.defaultUserId
        notifyChanged()
    }

    public fun handleOnActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return if (requestCode == intentRequestCode && resultCode == Activity.RESULT_OK) {
            getSignKeyId(data)
            true
        } else {
            false
        }
    }

    private companion object {

        private const val NO_KEY = 0L
        private const val TAG = "OpenPgpKeyPreference"
    }

    /**
     * SavedState, a subclass of [androidx.preference.Preference.BaseSavedState], will store the state
     * of MyPreference, a subclass of Preference.
     *
     *
     * It is important to always call through to super methods.
     */
    internal class SavedState : BaseSavedState {

        internal var keyId: Long = 0
        internal var openPgpProvider: String? = null
        internal var defaultUserId: String? = null

        constructor(source: Parcel) : super(source) {
            keyId = source.readInt().toLong()
            openPgpProvider = source.readString()
            defaultUserId = source.readString()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeLong(keyId)
            dest.writeString(openPgpProvider)
            dest.writeString(defaultUserId)
        }

        constructor(superState: Parcelable?) : super(superState)

        companion object CREATOR : Creator<SavedState> {

            override fun createFromParcel(`in`: Parcel): SavedState? {
                return SavedState(`in`)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}
