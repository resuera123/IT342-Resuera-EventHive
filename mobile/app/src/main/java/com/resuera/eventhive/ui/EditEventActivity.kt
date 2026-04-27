package com.resuera.eventhive.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.resuera.eventhive.R
import com.resuera.eventhive.api.RetrofitClient
import com.resuera.eventhive.model.EventResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class EditEventActivity : AppCompatActivity() {

    private var startDateTime = ""
    private var endDateTime = ""
    private val categories = listOf("Music", "Sports", "Tech", "Arts", "Food & Drink", "Business", "Health")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_event)

        val eventId = intent.getLongExtra("EVENT_ID", 0)
        val title = intent.getStringExtra("EVENT_TITLE") ?: ""
        val desc = intent.getStringExtra("EVENT_DESC") ?: ""
        val start = intent.getStringExtra("EVENT_START") ?: ""
        val end = intent.getStringExtra("EVENT_END") ?: ""
        val location = intent.getStringExtra("EVENT_LOCATION") ?: ""
        val category = intent.getStringExtra("EVENT_CATEGORY") ?: "Music"
        val max = intent.getIntExtra("EVENT_MAX", 0)

        startDateTime = start.take(16) // yyyy-MM-ddTHH:mm
        endDateTime = end.take(16)

        val etTitle = findViewById<EditText>(R.id.etEditTitle)
        val etDesc = findViewById<EditText>(R.id.etEditDesc)
        val btnStart = findViewById<Button>(R.id.btnEditStartDate)
        val btnEnd = findViewById<Button>(R.id.btnEditEndDate)
        val etLocation = findViewById<EditText>(R.id.etEditLocation)
        val spinnerCat = findViewById<Spinner>(R.id.spinnerEditCategory)
        val etMax = findViewById<EditText>(R.id.etEditMaxParticipants)
        val btnSave = findViewById<Button>(R.id.btnSaveEdit)
        val btnCancel = findViewById<Button>(R.id.btnCancelEdit)

        // Pre-fill
        etTitle.setText(title)
        etDesc.setText(desc)
        btnStart.text = startDateTime.replace("T", " ")
        btnEnd.text = endDateTime.replace("T", " ")
        etLocation.setText(location)
        etMax.setText(if (max > 0) max.toString() else "")

        val spinAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCat.adapter = spinAdapter
        spinnerCat.setSelection(categories.indexOf(category).coerceAtLeast(0))

        btnStart.setOnClickListener { pickDateTime { dt -> startDateTime = dt; btnStart.text = dt.replace("T", " ") } }
        btnEnd.setOnClickListener { pickDateTime { dt -> endDateTime = dt; btnEnd.text = dt.replace("T", " ") } }
        btnCancel.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val t = etTitle.text.toString().trim()
            val d = etDesc.text.toString().trim()
            val l = etLocation.text.toString().trim()
            val c = spinnerCat.selectedItem.toString()
            val m = etMax.text.toString().trim()

            if (t.isEmpty() || d.isEmpty() || startDateTime.isEmpty() || endDateTime.isEmpty() || l.isEmpty() || m.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            val textType = "text/plain".toMediaType()

            RetrofitClient.instance.updateEvent(
                eventId,
                t.toRequestBody(textType),
                d.toRequestBody(textType),
                startDateTime.toRequestBody(textType),
                endDateTime.toRequestBody(textType),
                l.toRequestBody(textType),
                c.toRequestBody(textType),
                m.toRequestBody(textType),
                null
            ).enqueue(object : Callback<EventResponse> {
                override fun onResponse(call: Call<EventResponse>, response: Response<EventResponse>) {
                    btnSave.isEnabled = true
                    if (response.isSuccessful) {
                        DialogHelper.showSuccess(this@EditEventActivity, "Event Updated", "Your event has been updated successfully.") { finish() }
                    } else {
                        Toast.makeText(this@EditEventActivity, "Failed to update event", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<EventResponse>, t: Throwable) {
                    btnSave.isEnabled = true
                    Toast.makeText(this@EditEventActivity, "Connection error", Toast.LENGTH_SHORT).show()
                }
            })
        }

        findViewById<ImageButton>(R.id.btnBackFromEdit).setOnClickListener { finish() }
    }

    private fun pickDateTime(callback: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            TimePickerDialog(this, { _, h, min ->
                callback(String.format("%04d-%02d-%02dT%02d:%02d", y, m + 1, d, h, min))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }
}