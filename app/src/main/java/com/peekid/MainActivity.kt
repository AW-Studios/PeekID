package com.peekid

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView

class MainActivity : AppCompatActivity() {

    private lateinit var statusIndicator: ImageView
    private lateinit var statusText: MaterialTextView
    private lateinit var grantButton: MaterialButton
    private lateinit var whatsappLogo: ImageView
    private lateinit var whitelistChipGroup: ChipGroup
    private lateinit var emptyWhitelistText: MaterialTextView
    private lateinit var addContactButton: MaterialButton
    private lateinit var batteryWarningCard: MaterialCardView
    private lateinit var fixBatteryButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
        loadProtectedApps()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        updateBatteryOptimizationStatus()
        updateWhitelistUI()
    }

    private fun initViews() {
        statusIndicator = findViewById(R.id.statusIndicator)
        statusText = findViewById(R.id.statusText)
        grantButton = findViewById(R.id.grantButton)
        whatsappLogo = findViewById(R.id.whatsappLogo)
        whitelistChipGroup = findViewById(R.id.whitelistChipGroup)
        emptyWhitelistText = findViewById(R.id.emptyWhitelistText)
        addContactButton = findViewById(R.id.addContactButton)
        batteryWarningCard = findViewById(R.id.batteryWarningCard)
        fixBatteryButton = findViewById(R.id.fixBatteryButton)
    }

    private fun setupListeners() {
        grantButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        addContactButton.setOnClickListener {
            showAddContactDialog()
        }

        fixBatteryButton.setOnClickListener {
            requestIgnoreBatteryOptimisation()
        }
    }

    private fun isNotificationAccessGranted(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        return enabledListeners?.contains(packageName) == true
    }

    private fun updatePermissionStatus() {
        val isGranted = isNotificationAccessGranted()

        if (isGranted) {
            statusText.text = "PeekID is Active"
            statusIndicator.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.status_active)
            )
            grantButton.visibility = View.GONE
        } else {
            statusText.text = "Permission Required"
            statusIndicator.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.status_inactive)
            )
            grantButton.visibility = View.VISIBLE
            grantButton.text = "Grant Notification Access"
        }
    }

    private fun loadProtectedApps() {
        try {
            val icon = packageManager.getApplicationIcon("com.whatsapp")
            whatsappLogo.setImageDrawable(icon)
        } catch (e: Exception) {
            whatsappLogo.setImageResource(R.drawable.ic_notification)
        }
    }

    private fun updateWhitelistUI() {
        val contacts = WhitelistManager.getWhitelistedContacts(this)
        whitelistChipGroup.removeAllViews()

        if (contacts.isEmpty()) {
            emptyWhitelistText.visibility = View.VISIBLE
            whitelistChipGroup.visibility = View.GONE
        } else {
            emptyWhitelistText.visibility = View.GONE
            whitelistChipGroup.visibility = View.VISIBLE

            for (contact in contacts) {
                val chip = Chip(this).apply {
                    text = contact
                    isCloseIconVisible = true
                    setCloseIconResource(R.drawable.ic_close)
                    setOnCloseIconClickListener {
                        WhitelistManager.removeContact(this@MainActivity, contact)
                        updateWhitelistUI()
                    }
                }
                whitelistChipGroup.addView(chip)
            }
        }
    }

    private fun showAddContactDialog() {
        val input = TextInputEditText(this).apply {
            hint = "Contact or Group name"
            setSingleLine(true)
            setPadding(48, 36, 48, 36)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Whitelisted Contact")
            .setMessage("Messages from this contact will appear normally with preview.")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text?.toString()?.trim() ?: ""
                if (name.isNotEmpty()) {
                    WhitelistManager.addContact(this, name)
                    updateWhitelistUI()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateBatteryOptimizationStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
                batteryWarningCard.visibility = View.GONE
            } else {
                batteryWarningCard.visibility = View.VISIBLE
            }
        } else {
            batteryWarningCard.visibility = View.GONE
        }
    }

    private fun requestIgnoreBatteryOptimisation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }
    }
}
