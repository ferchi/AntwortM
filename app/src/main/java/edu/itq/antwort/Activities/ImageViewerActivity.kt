package edu.itq.antwort.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.squareup.picasso.Picasso
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ActivityImageViewerBinding

class ImageViewerActivity : AppCompatActivity() {
    lateinit var binding: ActivityImageViewerBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        val bundle = intent.extras
        val uriImage = bundle?.getString("URL_IMAGE").toString()
        Picasso.get().load(uriImage).into(binding.ivImageViewer)

    }
}