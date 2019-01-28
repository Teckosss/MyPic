package com.deguffroy.adrien.projetphoto.Controllers.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.deguffroy.adrien.projetphoto.R
import kotlinx.android.synthetic.main.activity_fullscreen.*

class FullscreenActivity : BaseActivity() {

  companion object {
      private const val EXTRA_IMAGE = "EXTRA_IMAGE"
      fun newInstance(context: Context, image:String)= Intent(context,FullscreenActivity::class.java).apply { putExtra(
          EXTRA_IMAGE, image) }
  }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)

        if (!(intent.getStringExtra(EXTRA_IMAGE).isNullOrEmpty())){
            this.loadImage(intent.getStringExtra(EXTRA_IMAGE))
        }
    }

    // -------------------
    // UI
    // -------------------

    private fun loadImage(url:String){
        Log.e("DetailFragment","ImageURL value : $url")
        Glide.with(this).load(url).apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)).into(fullscreen_image)
    }


}
