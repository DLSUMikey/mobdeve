package com.example.posystem2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.posystem2.databinding.ActivityAddItemBinding
import com.squareup.picasso.Picasso

class AddItemActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityAddItemBinding
    private var imageUri: Uri? = null
    private var isEditing = false
    private var itemId: Int = -1

    private val myActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            try {
                imageUri = result.data!!.data
                imageUri?.let { Picasso.get().load(it).into(viewBinding.tempImageIv) }
            } catch (exception: Exception) {
                Log.d("AddItemActivity", "Error loading image: ${exception.localizedMessage}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        intent?.let {
            if (it.hasExtra("itemId")) {
                isEditing = true
                itemId = it.getIntExtra("itemId", -1)
                viewBinding.editTextItemName.setText(it.getStringExtra("itemName"))
                viewBinding.editTextItemPrice.setText(it.getFloatExtra("itemPrice", 0f).toString())
                it.getStringExtra("imageUri")?.let { uri ->
                    imageUri = Uri.parse(uri)
                    Picasso.get().load(imageUri).into(viewBinding.tempImageIv)
                }
            }
        }

        viewBinding.selectBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            myActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"))
        }

        viewBinding.addBtn.setOnClickListener {
            if (areFieldsComplete()) {
                val newItem = ItemModel(
                    orderId = if (isEditing) itemId else 0,
                    imageUri = imageUri.toString(),
                    itemName = viewBinding.editTextItemName.text.toString(),
                    itemPrice = viewBinding.editTextItemPrice.text.toString().toFloat()
                )

                val dbHandler = DatabaseHandler(this)
                if (isEditing) {
                    dbHandler.updateItem(newItem)
                } else {
                    dbHandler.addNewItem(newItem)
                }

                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Please fill up all fields", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun areFieldsComplete(): Boolean {
        return viewBinding.editTextItemName.text.isNotEmpty() &&
                viewBinding.editTextItemPrice.text.isNotEmpty() &&
                imageUri != null
    }
}
