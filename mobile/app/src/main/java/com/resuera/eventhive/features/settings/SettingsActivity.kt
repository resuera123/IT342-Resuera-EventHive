package com.resuera.eventhive.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.resuera.eventhive.R
import com.resuera.eventhive.shared.network.RetrofitClient
import com.resuera.eventhive.shared.ui.DialogHelper
import com.resuera.eventhive.shared.ui.DialogIcon
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("EventHivePrefs", MODE_PRIVATE)
        val btnProfile = findViewById<Button>(R.id.btnTabProfile)
        val btnNotifs = findViewById<Button>(R.id.btnTabNotifications)
        val btnSecurity = findViewById<Button>(R.id.btnTabSecurity)
        val layoutProfile = findViewById<LinearLayout>(R.id.layoutProfile)
        val layoutNotifs = findViewById<LinearLayout>(R.id.layoutNotifications)
        val layoutSecurity = findViewById<LinearLayout>(R.id.layoutSecurity)

        fun selectTab(tab: String) {
            mapOf("profile" to btnProfile, "notifications" to btnNotifs, "security" to btnSecurity).forEach { (k, btn) ->
                btn.setBackgroundColor(if (k == tab) 0xFF212529.toInt() else 0xFFFFFFFF.toInt())
                btn.setTextColor(if (k == tab) 0xFFFFFFFF.toInt() else 0xFF212529.toInt())
            }
            layoutProfile.visibility = if (tab == "profile") View.VISIBLE else View.GONE
            layoutNotifs.visibility = if (tab == "notifications") View.VISIBLE else View.GONE
            layoutSecurity.visibility = if (tab == "security") View.VISIBLE else View.GONE
        }
        btnProfile.setOnClickListener { selectTab("profile") }
        btnNotifs.setOnClickListener { selectTab("notifications") }
        btnSecurity.setOnClickListener { selectTab("security") }

        // ═══ Profile ═══
        val etFirst = findViewById<TextInputEditText>(R.id.etSettingsFirstName)
        val etLast = findViewById<TextInputEditText>(R.id.etSettingsLastName)
        val etEmail = findViewById<TextInputEditText>(R.id.etSettingsEmail)
        val tvMsg = findViewById<TextView>(R.id.tvProfileMsg)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)

        etFirst.setText(prefs.getString("KEY_FIRSTNAME", ""))
        etLast.setText(prefs.getString("KEY_LASTNAME", ""))
        etEmail.setText(prefs.getString("KEY_EMAIL", ""))

        btnSave.setOnClickListener {
            btnSave.isEnabled = false; tvMsg.visibility = View.GONE
            RetrofitClient.instance.updateProfile(mapOf(
                "firstname" to (etFirst.text?.toString()?.trim() ?: ""),
                "lastname" to (etLast.text?.toString()?.trim() ?: ""),
                "email" to (etEmail.text?.toString()?.trim() ?: "")
            )).enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, r: Response<Map<String, Any>>) {
                    btnSave.isEnabled = true
                    if (r.isSuccessful) {
                        val d = r.body() ?: emptyMap()
                        prefs.edit().putString("KEY_FIRSTNAME", d["firstname"]?.toString()).putString("KEY_LASTNAME", d["lastname"]?.toString()).putString("KEY_EMAIL", d["email"]?.toString()).apply()
                        DialogHelper.showSuccess(this@SettingsActivity, "Profile Updated", "Your profile information has been saved successfully.")
                    } else { tvMsg.text = "Failed to update profile."; tvMsg.setTextColor(0xFFDC3545.toInt()); tvMsg.visibility = View.VISIBLE }
                }
                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) { btnSave.isEnabled = true; tvMsg.text = "Connection error."; tvMsg.setTextColor(0xFFDC3545.toInt()); tvMsg.visibility = View.VISIBLE }
            })
        }

        // ═══ Notifications ═══
        val np = getSharedPreferences("EventHiveNotifPrefs", MODE_PRIVATE)
        val swReg = findViewById<Switch>(R.id.swRegistration); swReg.isChecked = np.getBoolean("registration", true)
        val swUpd = findViewById<Switch>(R.id.swEventUpdates); swUpd.isChecked = np.getBoolean("eventUpdates", true)
        val swCan = findViewById<Switch>(R.id.swCancellation); swCan.isChecked = np.getBoolean("cancellation", true)
        val swNew = findViewById<Switch>(R.id.swNewParticipant); swNew.isChecked = np.getBoolean("newParticipant", true)

        findViewById<Button>(R.id.btnSaveNotifs).setOnClickListener {
            np.edit().putBoolean("registration", swReg.isChecked).putBoolean("eventUpdates", swUpd.isChecked).putBoolean("cancellation", swCan.isChecked).putBoolean("newParticipant", swNew.isChecked).apply()
            DialogHelper.showSuccess(this, "Preferences Saved", "Your notification preferences have been updated.")
        }

        // ═══ Security ═══
        val etCur = findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNew = findViewById<TextInputEditText>(R.id.etNewPassword)
        val etCon = findViewById<TextInputEditText>(R.id.etConfirmNewPassword)
        val tvSec = findViewById<TextView>(R.id.tvSecurityMsg)
        val btnPw = findViewById<Button>(R.id.btnChangePassword)

        btnPw.setOnClickListener {
            tvSec.visibility = View.GONE
            val newPw = etNew.text?.toString() ?: ""; val conPw = etCon.text?.toString() ?: ""
            if (newPw != conPw) { tvSec.text = "Passwords do not match."; tvSec.setTextColor(0xFFDC3545.toInt()); tvSec.visibility = View.VISIBLE; return@setOnClickListener }
            if (newPw.length < 8) { tvSec.text = "Minimum 8 characters."; tvSec.setTextColor(0xFFDC3545.toInt()); tvSec.visibility = View.VISIBLE; return@setOnClickListener }

            btnPw.isEnabled = false
            RetrofitClient.instance.changePassword(mapOf("currentPassword" to (etCur.text?.toString() ?: ""), "newPassword" to newPw)).enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(call: Call<Map<String, String>>, r: Response<Map<String, String>>) {
                    btnPw.isEnabled = true
                    if (r.isSuccessful) {
                        DialogHelper.showSuccess(this@SettingsActivity, "Password Changed", "Your password has been updated successfully.")
                        etCur.text?.clear(); etNew.text?.clear(); etCon.text?.clear()
                    } else { tvSec.text = "Current password is incorrect."; tvSec.setTextColor(0xFFDC3545.toInt()); tvSec.visibility = View.VISIBLE }
                }
                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) { btnPw.isEnabled = true; tvSec.text = "Connection error."; tvSec.setTextColor(0xFFDC3545.toInt()); tvSec.visibility = View.VISIBLE }
            })
        }

        // ═══ Danger Zone ═══
        findViewById<Button>(R.id.btnDeleteAccount).setOnClickListener {
            DialogHelper.showConfirm(this, DialogIcon.DANGER,
                "Delete Account",
                "Are you sure you want to permanently delete your account and all associated data?\n\nThis action cannot be undone.",
                "Delete Account", "#DC3545"
            ) {
                Toast.makeText(this, "Account deletion is not yet available.", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageButton>(R.id.btnBackFromSettings).setOnClickListener { finish() }
        selectTab("profile")
    }
}