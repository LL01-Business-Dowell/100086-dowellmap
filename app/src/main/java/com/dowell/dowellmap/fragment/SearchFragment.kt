package com.dowell.dowellmap.fragment

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
import com.dowell.dowellmap.R
import com.dowell.dowellmap.activity.MainActivityViewModel
import com.dowell.dowellmap.adapter.SearchAdapter
import com.dowell.dowellmap.data.LocationModel
import com.dowell.dowellmap.databinding.FragmentSearchBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: MainActivityViewModel by activityViewModels()
    //lateinit var searchAdapter:LocationListAdapter
    lateinit var searchAdapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        with(binding){

            autoCompleteTextView.addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if(!TextUtils.isEmpty(s)){
                        sharedViewModel.setQuery(s.toString())
                    }

                }

                override fun afterTextChanged(s: Editable?) {}

            })

            lifecycleScope.launch {
                sharedViewModel.results.asFlow()
                    .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                    .distinctUntilChanged()
                    .collect { data ->
                        //Log.i("data", data.toString())
                        data.predictions?.let { displayResult(it as ArrayList<LocationModel.Prediction?>) }
                    }
            }



            routeBtn.setOnClickListener {
                view?.findNavController()?.navigate(R.id.action_searchFragment_to_mapFragment)
            }
        }


        return  binding.root
    }

    private fun displayResult(data: ArrayList<LocationModel.Prediction?>) {
      /*  binding.autoCompleteTextView.setAdapter(searchAdapter)
        searchAdapter.submitList(data.locations)
*/

        Log.i("predictions", data.toString())
        searchAdapter = SearchAdapter(requireContext(),data)
        binding.autoCompleteTextView.setAdapter(searchAdapter)
        searchAdapter.notifyDataSetChanged()
    }
}