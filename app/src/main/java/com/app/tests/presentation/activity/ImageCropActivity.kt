package com.app.tests.presentation.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import com.app.tests.R
import com.app.tests.databinding.ActivityImageCropBinding
import com.app.tests.presentation.base.BaseActivity
import com.app.tests.util.Constants.EXTRA_IMAGE_CROPPED_PATH
import com.app.tests.util.Constants.EXTRA_IMAGE_PATH
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageView

class ImageCropActivity : BaseActivity<ActivityImageCropBinding>(), CropImageView.OnCropImageCompleteListener {

    override fun createBinding(inflater: LayoutInflater) = ActivityImageCropBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_ImageView)
        super.onCreate(savedInstanceState)

        val imagePath = intent.extras?.getString(EXTRA_IMAGE_PATH).orEmpty()

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Crop Image"
        }

        binding.ivImage.setOnCropImageCompleteListener(this)

        if (imagePath.startsWith("http")) {
            Thread {
                val bitmap = loadBitmap(imagePath)
                binding.ivImage.setImageBitmap(bitmap)
            }.also {
                it.start()
                it.join()
            }
        } else {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            binding.ivImage.setImageBitmap(bitmap)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        with (binding.ivImage) {
            return when (item.itemId) {
                android.R.id.home -> {
                    finish()
                    true
                }
                R.id.main_action_crop -> {
                    croppedImageAsync()
                    true
                }
                R.id.main_action_rotate -> {
                    rotateImage(90)
                    true
                }
                R.id.main_action_flip_horizontally -> {
                    flipImageHorizontally()
                    true
                }
                R.id.main_action_flip_vertically -> {
                    flipImageVertically()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
        if (result.error != null) {
            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show()
            return
        }
        Intent().also {
            it.putExtra(EXTRA_IMAGE_CROPPED_PATH, result.getUriFilePath(view.context, true))
            setResult(RESULT_OK, it)
        }
        finish()
    }

    private fun loadBitmap(imagePath: String) =
        Glide.with(this)
            .asBitmap()
            .load(imagePath)
            .submit()
            .get()
}
