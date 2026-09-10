package com.guardsim.callscreen

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var listAdapter: ArrayAdapter<String>

    private val roleRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        updateStatus()
    }

    private val contactsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(
                this,
                "Contacts permission is needed to detect unknown numbers",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        val enableButton: Button = findViewById(R.id.enableButton)
        val silenceSwitch: Switch = findViewById(R.id.silenceUnknownSwitch)
        val numberInput: EditText = findViewById(R.id.numberInput)
        val addButton: Button = findViewById(R.id.addButton)
        val blockedListView: ListView = findViewById(R.id.blockedList)

        listAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            BlocklistStore.getBlockedNumbers(this).toMutableList()
        )
        blockedListView.adapter = listAdapter

        blockedListView.setOnItemClickListener { _, _, position, _ ->
            val number = listAdapter.getItem(position) ?: return@setOnItemClickListener
            BlocklistStore.removeBlockedNumber(this, number)
            listAdapter.remove(number)
            Toast.makeText(this, "Removed $number", Toast.LENGTH_SHORT).show()
        }

        addButton.setOnClickListener {
            val number = numberInput.text.toString().trim()
            if (number.isNotEmpty()) {
                val normalized = BlocklistStore.normalize(number)
                BlocklistStore.addBlockedNumber(this, normalized)
                listAdapter.add(normalized)
                numberInput.text.clear()
            }
        }

        enableButton.setOnClickListener {
            requestCallScreeningRole()
        }

        silenceSwitch.isChecked = BlocklistStore.isSilenceUnknownEnabled(this)
        silenceSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
            BlocklistStore.setSilenceUnknown(this, isChecked)
        }

        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                    val intent = roleManager.createRequestRoleIntent(
                        RoleManager.ROLE_CALL_SCREENING
                    )
                    roleRequestLauncher.launch(intent)
                } else {
                    Toast.makeText(this, "Call screening already enabled", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Call screening role not available on this device",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(this, "This feature needs Android 10 or higher", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateStatus() {
        val enabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        } else {
            false
        }
        statusText.text = if (enabled) "Call screening: ON" else "Call screening: OFF"
    }
}
