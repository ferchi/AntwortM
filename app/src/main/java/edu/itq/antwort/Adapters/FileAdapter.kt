package edu.itq.antwort.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.itq.antwort.R
import edu.itq.antwort.databinding.ItemFilesBinding
import java.io.File

class FileAdapter (private val context: Context, private val dataset: List<String> ) :
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

        when(extension){

            "jpg","png" -> {
                holder.binding.ivItemFileType.setImageResource(R.drawable.ic_baseline_image_24)
            }
            "pdf","doc","docx","txt" -> {
                holder.binding.ivItemFileType.setImageResource(R.drawable.ic_baseline_file_present_24)
            }
        }
    }

}