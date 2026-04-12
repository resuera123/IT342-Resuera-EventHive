package com.resuera.eventhive.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.resuera.eventhive.R
import com.resuera.eventhive.api.RetrofitClient
import com.resuera.eventhive.model.EventResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class CreateEventActivity : AppCompatActivity() {

    private var startDateTime = ""
    private var endDateTime = ""
    private var selectedImageUri: Uri? = null
    private val categories = listOf("Music", "Sports", "Tech", "Arts", "Food & Drink", "Business", "Health")

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            val ivPreview = findViewById<ImageView>(R.id.ivEventImagePreview)
            val tvImageHint = findViewById<TextView>(R.id.tvImageHint)
            if (selectedImageUri != null) {
                Glide.with(this).load(selectedImageUri).centerCrop().into(ivPreview)
                ivPreview.visibility = android.view.View.VISIBLE
                tvImageHint.text = "Tap to change image"
            }
        }
    }

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
        val btnPickImage = findViewById<LinearLayout>(R.id.layoutImagePicker)

        val spinAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinAdapter

        btnStartDate.setOnClickListener { pickDateTime { dt -> startDateTime = dt; btnStartDate.text = dt.replace("T", " ") } }
        btnEndDate.setOnClickListener { pickDateTime { dt -> endDateTime = dt; btnEndDate.text = dt.replace("T", " ") } }

        btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            pickImageLauncher.launch(intent)
        }

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

            btnCreate.isEnabled = false
            val textType = "text/plain".toMediaType()

            // Prepare image part
            var imagePart: MultipartBody.Part? = null
            if (selectedImageUri != null) {
                try {
                    val inputStream = contentResolver.openInputStream(selectedImageUri!!)
                    val file = File(cacheDir, "event_upload.jpg")
                    FileOutputStream(file).use { out -> inputStream?.copyTo(out) }
                    val requestBody = file.asRequestBody("image/*".toMediaType())
                    imagePart = MultipartBody.Part.createFormData("image", file.name, requestBody)
                } catch (e: Exception) {
                    // Continue without image
                }
            }

            RetrofitClient.instance.createEvent(
                title.toRequestBody(textType),
                desc.toRequestBody(textType),
                startDateTime.toRequestBody(textType),
                endDateTime.toRequestBody(textType),
                location.toRequestBody(textType),
                category.toRequestBody(textType),
                maxStr.toRequestBody(textType),
                imagePart
            ).enqueue(object : Callback<EventResponse> {
                override fun onResponse(call: Call<EventResponse>, response: Response<EventResponse>) {
                    btnCreate.isEnabled = true
                    if (response.isSuccessful) {
                        DialogHelper.showSuccess(
                            this@CreateEventActivity,
                            "Event Created!",
                            "\"$title\" has been created successfully and is now visible to participants."
                        ) { finish() }
                    } else {
                        Toast.makeText(this@CreateEventActivity, "Failed to create event", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<EventResponse>, t: Throwable) {
                    btnCreate.isEnabled = true
                    Toast.makeText(this@CreateEventActivity, "Connection error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        findViewById<ImageButton>(R.id.btnBackFromCreate).setOnClickListener { finish() }
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