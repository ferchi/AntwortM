package edu.itq.antwort.Activities

import android.content.ContentResolver
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import edu.itq.antwort.databinding.ActivityGifViewerBinding
import edu.itq.antwort.databinding.ActivityImageViewerBinding
import pl.droidsonroids.gif.GifDrawable

class GifViewerActivity : AppCompatActivity() {

    lateinit var binding: ActivityGifViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGifViewerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        val bundle = intent.extras
        val uriImage = bundle?.getString("URL_IMAGE").toString()

        Glide.with(this).asGif().load(uriImage).into(binding.givGifViewer)
    }
}