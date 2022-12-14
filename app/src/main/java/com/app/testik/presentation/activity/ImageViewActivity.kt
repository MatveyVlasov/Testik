package com.app.testik.presentation.activity

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import com.app.testik.R
import com.app.testik.databinding.ActivityImageViewBinding
import com.app.testik.presentation.base.BaseActivity
import com.app.testik.util.Constants.EXTRA_IMAGE_PATH
import com.app.testik.util.Constants.EXTRA_IMAGE_TITLE
import com.app.testik.util.loadedFromServer
import com.bumptech.glide.Glide

class ImageViewActivity : BaseActivity<ActivityImageViewBinding>() {

    override fun createBinding(inflater: LayoutInflater) = ActivityImageViewBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_ImageView)
        super.onCreate(savedInstanceState)

        val imageTitle = intent.extras?.getString(EXTRA_IMAGE_TITLE).orEmpty()
        val imagePath = intent.extras?.getString(EXTRA_IMAGE_PATH).orEmpty()

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = imageTitle
        }

        if (imagePath.loadedFromServer()) {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadBitmap(imagePath: String) =
        Glide.with(this)
            .asBitmap()
            .load(imagePath)
            .submit()
            .get()
}
