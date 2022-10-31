package com.dowell.dowellmap.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asFlow
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.dowell.dowellmap.activity.MainActivityViewModel
import com.dowell.dowellmap.adapter.LocationListAdapter
import com.dowell.dowellmap.adapter.SearchAdapter
import com.dowell.dowellmap.data.SearchRepository
import com.dowell.dowellmap.data.model.LocationModel
import com.dowell.dowellmap.data.model.PlaceDetail
import com.dowell.dowellmap.data.network.ApiService
import com.dowell.dowellmap.data.network.Resource
import com.dowell.dowellmap.databinding.FragmentSearchBinding
import com.dowell.dowellmap.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment(), LocationListAdapter.RemoveListener {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainActivityViewModel by activityViewModels()
    lateinit var selectedAdapter: LocationListAdapter
    lateinit var searchAdapter: SearchAdapter
    lateinit var selectedPlace: LocationModel.Prediction
    lateinit var searchRepository: SearchRepository


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        with(binding) {
            selectedAdapter = LocationListAdapter(this@SearchFragment)
            autoCompleteTextView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!TextUtils.isEmpty(s)) {
                        viewModel.setQuery(s.toString())
                    }

                }

                override fun afterTextChanged(s: Editable?) {}

            })

            lifecycleScope.launch {
                viewModel.searchResults.asFlow()
                    .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                    .distinctUntilChanged()
                    .collect { data ->
                        when (data) {
                            is Resource.Success -> {
                                if (data.value.predictions?.isNotEmpty() == true) {
                                    Log.i("DataSize", data.value.predictions?.size.toString())
                                    data.value.predictions?.let { displayResult(it as ArrayList<LocationModel.Prediction?>) }
                                }
                            }
                            is Resource.Failure -> {
                                toast("Request Failed", requireContext())
                            }

                        }

                    }
            }

        }




        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun displayResult(data: ArrayList<LocationModel.Prediction?>) {

        searchAdapter = SearchAdapter(requireContext(), data)
        binding.autoCompleteTextView.setAdapter(searchAdapter)
        searchAdapter.notifyDataSetChanged()

        binding.autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->

            selectedPlace = searchAdapter.getItem(position)!!
            binding.autoCompleteTextView.setText("")

            //call place detail api
            if (viewModel.selectedPredictions.size <= 4) {
                selectedPlace.place_id?.let { viewModel.getPlaceDetail(it) }
            }else{
                toast("Maximum location reached", requireContext())
            }

            viewModel.locationDetailResults.observe(viewLifecycleOwner) {
                lifecycleScope.launch {
                    when (it) {
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.INVISIBLE
                            if (it.value.result != null) {

                                //pass object to viewModel for addition
                                // to selected location
                                viewModel.setSelectedPrediction(it.value)
                                selectedAdapter.submitList(viewModel.selectedPredictions)
                                binding.recyclerView.adapter = selectedAdapter
                                selectedAdapter.notifyDataSetChanged()

                            } else {
                                toast("No result", requireContext())
                            }
                        }
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is Resource.Failure -> {
                            binding.progressBar.visibility = View.INVISIBLE
                            toast("Request Failed", requireContext())
                        }

                    }
                }
            }
        }

        binding.routeBtn.setOnClickListener {
            val action = SearchFragmentDirections.actionSearchFragmentToMapFragment(viewModel.selectedPredictions.toTypedArray())
            view?.findNavController()?.navigate(action)

        }



    }



    override fun removeLocation(position: Int, prediction: PlaceDetail) {
        Log.i("ItemPosition", position.toString())
        viewModel.removeSelectedPrediction(position, prediction)
        selectedAdapter.notifyDataSetChanged()
    }
}