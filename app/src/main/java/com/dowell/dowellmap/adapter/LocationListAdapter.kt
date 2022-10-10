package com.dowell.dowellmap.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dowell.dowellmap.data.LocationModel
import com.dowell.dowellmap.databinding.LocationItemBinding


class LocationListAdapter() :
    ListAdapter<LocationModel.Prediction, LocationListAdapter.LocationModelViewHolder>(ItemGroupDiffUtill())  {

    var onItemClick: ((LocationModel.Prediction) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationModelViewHolder {
        return LocationModelViewHolder(
            LocationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }



    override fun getItemCount(): Int {
        return itemCount
    }
    override fun onBindViewHolder(holder: LocationModelViewHolder, position: Int) {
        holder.binding.address.text = getItem(position).description
    }

    class ItemGroupDiffUtill : DiffUtil.ItemCallback<LocationModel.Prediction>() {
        override fun areItemsTheSame(oldItem: LocationModel.Prediction, newItem: LocationModel.Prediction): Boolean {
            return oldItem.description === newItem.description
        }

        override fun areContentsTheSame(oldItem: LocationModel.Prediction, newItem: LocationModel.Prediction): Boolean {
            return oldItem == newItem
        }

    }

    inner class LocationModelViewHolder(val binding: LocationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(getItem(adapterPosition))
            }
        }

    }
}