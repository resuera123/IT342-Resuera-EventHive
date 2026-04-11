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

class CreateEventActivity : AppCompatActivity() {

    private var startDateTime = ""
    private var endDateTime = ""

    private val categories = listOf("Music", "Sports", "Tech", "Arts", "Food & Drink", "Business", "Health")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        val etTitle = findViewById<EditText>(R.id.etEventTitle)
        val etDesc = findViewById<EditText>(R.id.etEventDesc)
        val btnStartDate = findViewById<Button>(R.id.btnStartDate)
        val btnEndDate = findViewById<Button>(R.id.btnEndDate)
        val etLocation = findViewById<EditText>(R.id.etEventLocation)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val etMax = findViewById<EditText>(R.id.etMaxParticipants)
        val btnCreate = findViewById<Button>(R.id.btnCreateEvent)
        val btnCancel = findViewById<Button>(R.id.btnCancelCreate)

        // Category spinner
        val spinAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinAdapter

        btnStartDate.setOnClickListener { pickDateTime { dt -> startDateTime = dt; btnStartDate.text = dt } }
        btnEndDate.setOnClickListener { pickDateTime { dt -> endDateTime = dt; btnEndDate.text = dt } }

        btnCancel.setOnClickListener { finish() }

        btnCreate.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()
            val maxStr = etMax.text.toString().trim()

            if (title.isEmpty() || desc.isEmpty() || startDateTime.isEmpty() || endDateTime.isEmpty() || location.isEmpty() || maxStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val textType = "text/plain".toMediaType()
            btnCreate.isEnabled = false

            RetrofitClient.instance.createEvent(
                title.toRequestBody(textType),
                desc.toRequestBody(textType),
                startDateTime.toRequestBody(textType),
                endDateTime.toRequestBody(textType),
                location.toRequestBody(textType),
                category.toRequestBody(textType),
                maxStr.toRequestBody(textType),
                null // no image for now on mobile
            ).enqueue(object : Callback<EventResponse> {
                override fun onResponse(call: Call<EventResponse>, response: Response<EventResponse>) {
                    btnCreate.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@CreateEventActivity, "Event created!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@CreateEventActivity, "Failed to create event", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<EventResponse>, t: Throwable) {
                    btnCreate.isEnabled = true
                    Toast.makeText(this@CreateEventActivity, "Connection error", Toast.LENGTH_SHORT).show()
                }
            })
        }
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