package com.dowell.dowellmap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dowell.dowellmap.data.model.LocationModel
import com.dowell.dowellmap.data.model.PlaceDetail
import com.dowell.dowellmap.databinding.LocationItemBinding


class LocationListAdapter(var removeListener:RemoveListener) : ListAdapter<PlaceDetail, LocationListAdapter.LocationModelViewHolder>(ItemGroupDiffUtill())  {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationModelViewHolder {
        return LocationModelViewHolder(
            LocationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }


    override fun onBindViewHolder(holder: LocationModelViewHolder, position: Int) {
        holder.binding.address.text = getItem(position).result?.formatted_address
        holder.binding.removeIcon.setOnClickListener {
            removeListener.removeLocation(position,getItem(position))
        }

    }

    interface RemoveListener{
        fun removeLocation(position:Int,prediction: PlaceDetail)
    }
    class ItemGroupDiffUtill : DiffUtil.ItemCallback<PlaceDetail>() {
        override fun areItemsTheSame(oldItem: PlaceDetail, newItem: PlaceDetail): Boolean {
            return oldItem.result?.place_id === newItem.result?.place_id
        }

        override fun areContentsTheSame(oldItem: PlaceDetail, newItem: PlaceDetail): Boolean {
            return oldItem == newItem
        }

    }

    inner class LocationModelViewHolder(val binding: LocationItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}