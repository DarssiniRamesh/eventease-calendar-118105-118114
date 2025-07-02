package com.example.calendareventfrontend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calendareventfrontend.data.EventRepository
import com.example.calendareventfrontend.model.Event
import com.example.calendareventfrontend.databinding.FragmentSearchBinding
import androidx.appcompat.widget.SearchView

// PUBLIC_INTERFACE
class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: EventAdapter

    // PUBLIC_INTERFACE
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupSearch()
        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = EventAdapter(emptyList(), ::onEventClicked)
        binding.recyclerViewSearch.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSearch.adapter = adapter
    }

    private fun setupSearch() {
        binding.eventSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchEvents(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchEvents(newText)
                return true
            }
        })
    }

    private fun searchEvents(query: String) {
        val list = if (query.isNotBlank()) {
            EventRepository.searchEvents(query)
        } else {
            emptyList()
        }
        adapter.updateEvents(list)
    }

    private fun onEventClicked(event: Event) {
        val dialog = EventDialogFragment.newInstance(event)
        dialog.show(parentFragmentManager, "EventDialogFragment")
    }
}
