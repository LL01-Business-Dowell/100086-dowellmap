package com.dowell.dowellmap.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.dowell.dowellmap.data.model.LocationModel
import com.dowell.dowellmap.databinding.LocationItemBinding


class SearchAdapter(context: Context, val predictions: ArrayList<LocationModel.Prediction?>) :
    ArrayAdapter<LocationModel.Prediction?>(context, 0, predictions) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val binding:LocationItemBinding
        var row = convertView

        if (row == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            binding = LocationItemBinding.inflate(inflater, parent, false)
            row = binding.root
        } else {
            binding = LocationItemBinding.bind(row)
        }

        binding.address.text = predictions[position]?.description
        binding.removeIcon.visibility=View.INVISIBLE
        return row
    }

}