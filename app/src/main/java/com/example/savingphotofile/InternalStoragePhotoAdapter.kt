package com.example.savingphotofile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.savingphotofile.databinding.ItemPhotoBinding

class InternalStoragePhotoAdapter(private val onPhotoClick : (InternalStoragePhoto) -> Unit)
    :ListAdapter<InternalStoragePhoto,InternalStoragePhotoAdapter.PhotoViewHolder>(this){

    companion object : DiffUtil.ItemCallback<InternalStoragePhoto>(){
        override fun areItemsTheSame(
            oldItem: InternalStoragePhoto,
            newItem: InternalStoragePhoto
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: InternalStoragePhoto,
            newItem: InternalStoragePhoto
        ): Boolean {
            return oldItem.name == newItem.name && oldItem.bmp.sameAs(newItem.bmp)
        }

    }

    inner class PhotoViewHolder(val binding : ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(
            ItemPhotoBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        )
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = currentList[position]
        holder.binding.apply {
            ivPhoto.setImageBitmap(photo.bmp)

            val aspectRation = photo.bmp.width.toFloat() / photo.bmp.height.toFloat()
            ConstraintSet().apply {
                clone(root)
                setDimensionRatio(ivPhoto.id,aspectRation.toString())
                applyTo(root)
            }
            ivPhoto.setOnLongClickListener{
                onPhotoClick(photo)
                true
            }
        }
    }
}