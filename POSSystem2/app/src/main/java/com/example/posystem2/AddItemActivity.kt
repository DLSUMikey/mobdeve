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
        ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            try {
                if (result.data != null) {
                    imageUri = result.data!!.data
                    Picasso.get().load(imageUri).into(viewBinding.tempImageIv)
                }
            } catch (exception: Exception) {
                Log.d("TAG", "" + exception.localizedMessage)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Check if we're editing an existing item
        intent?.let {
            if (it.hasExtra("itemId")) {
                isEditing = true
                itemId = it.getIntExtra("itemId", -1)
                viewBinding.editTextItemName.setText(it.getStringExtra("itemName"))
                viewBinding.editTextItemPrice.setText(it.getIntExtra("itemPrice", 0).toString())
                val imageId = it.getIntExtra("imageId", 0)
                if (imageId != 0) {
                    viewBinding.tempImageIv.setImageResource(imageId)
                }
            }
        }

        viewBinding.selectBtn.setOnClickListener {
            val i = Intent()
            i.type = "image/*"
            i.action = Intent.ACTION_OPEN_DOCUMENT
            myActivityResultLauncher.launch(Intent.createChooser(i, "Select Picture"))
        }

        viewBinding.addBtn.setOnClickListener {
            if (areFieldsComplete()) {
                val newItem = ItemModel(
                    orderId = 0,
                    imageId = 0, // Set your default drawable resource id or handle it according to your implementation
                    itemName = viewBinding.editTextItemName.text.toString(),
                    itemPrice = viewBinding.editTextItemPrice.text.toString().toInt()
                )

                val dbHandler = DatabaseHandler(this)
                if (isEditing) {
                    dbHandler.updateItem(newItem)
                } else {
                    dbHandler.addNewItem(newItem)
                }

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
