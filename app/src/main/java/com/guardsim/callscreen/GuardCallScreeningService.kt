package com.guardsim.callscreen

import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.CallScreeningService.CallResponse

/**
 * This is the heart of GuardSim. Android calls onScreenCall() for every
 * incoming call BEFORE the phone rings, because this app holds the
 * ROLE_CALL_SCREENING system role. We decide here whether to let it
 * ring normally, reject it outright, or let it ring silently.
 */
class GuardCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val rawNumber = callDetails.handle?.schemeSpecificPart

        if (rawNumber == null) {
            allowCall(callDetails)
            return
        }

        val number = BlocklistStore.normalize(rawNumber)

        if (BlocklistStore.isBlocked(this, number)) {
            respondToCall(
                callDetails,
                CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipNotification(true)
                    .build()
            )
            return
        }

        if (BlocklistStore.isSilenceUnknownEnabled(this) && !isInContacts(number)) {
            respondToCall(
                callDetails,
                CallResponse.Builder()
                    .setSilenceCall(true)
                    .build()
            )
            return
        }

        allowCall(callDetails)
    }

    private fun allowCall(callDetails: Call.Details) {
        respondToCall(callDetails, CallResponse.Builder().build())
    }

    private fun isInContacts(number: String): Boolean {
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            )
            contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup._ID),
                null, null, null
            )?.use { it.moveToFirst() } ?: false
        } catch (e: SecurityException) {
            // No contacts permission granted: treat as safe rather than
            // silencing calls we can't actually verify.
            true
        }
    }
}
