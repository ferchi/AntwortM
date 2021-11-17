package edu.itq.antwort.Adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.StorageReference
import edu.itq.antwort.Activities.GifViewerActivity
import edu.itq.antwort.Activities.ImageViewerActivity
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ItemFilesBinding
import java.io.File


class FileAdapter (private val context: Context, private val dataset: List<String>, private var references:List<StorageReference> = listOf()) :
RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    class ViewHolder (val binding: ItemFilesBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFilesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = dataset.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileName = dataset[position]

        holder.binding.tvItemFileName.text = fileName

        val extension = File(fileName).extension
        Log.d("extension", "extension: $fileName")

        when(extension){

            "jpg","png", "webp" -> {
                holder.binding.ivItemFileType.setImageResource(R.drawable.ic_baseline_image_24)
            }
            "pdf","doc","docx","txt" -> {
                holder.binding.ivItemFileType.setImageResource(R.drawable.ic_baseline_file_present_24)
            }
            "gif" -> {
                holder.binding.ivItemFileType.setImageResource(R.drawable.ic_baseline_gif_box_24)

            }
        }

        holder.binding.cardItemFile.setOnClickListener {

            if (!references.isNullOrEmpty()) {
                    references[position].downloadUrl.addOnCompleteListener { uri ->
                        val uriFile = uri.result.toString()

                        when(extension) {
                            "jpg","png", "webp" -> {
                                val intent = Intent(context, ImageViewerActivity::class.java).apply {
                                    putExtra("URL_IMAGE", uriFile) }
                                context.startActivity(intent)
                            }
                            "pdf","doc","docx","txt" -> {
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse(uriFile)
                                context.startActivity(intent)
                            }
                            "gif" -> {
                                val intent = Intent(context, GifViewerActivity::class.java).apply {
                                    putExtra("URL_IMAGE", uriFile) }
                                context.startActivity(intent)

                            }
                        }


                    }
            }
        }
    }

}