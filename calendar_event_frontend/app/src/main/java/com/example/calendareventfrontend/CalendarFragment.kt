package com.example.calendareventfrontend

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calendareventfrontend.data.EventRepository
import com.example.calendareventfrontend.model.CalendarViewMode
import com.example.calendareventfrontend.model.Event
import com.example.calendareventfrontend.databinding.FragmentCalendarBinding

// PUBLIC_INTERFACE
class CalendarFragment : Fragment() {
    private lateinit var binding: FragmentCalendarBinding
    private lateinit var adapter: EventAdapter
    private var viewMode: CalendarViewMode = CalendarViewMode.MONTH

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            viewMode = args.getSerializable(ARG_VIEW_MODE) as? CalendarViewMode ?: CalendarViewMode.MONTH
        }
    }

    // PUBLIC_INTERFACE
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)

        // --- BEGIN: Inject dummy data for diagnosis if event list is empty ---
        val existingEvents = EventRepository.getAllEvents()
        if (existingEvents.isEmpty()) {
            // These events are for debug/demo purposes; in real use this should not happen.
            EventRepository.addEvent(
                com.example.calendareventfrontend.model.Event(
                    title = "Sample Event",
                    description = "This is a sample event.",
                    startTime = System.currentTimeMillis(),
                    endTime = System.currentTimeMillis() + 60 * 60 * 1000,
                    reminderMinutes = 10,
                    location = "Conference Room"
                )
            )
        }
        // --- END: Dummy data injection ---

        setupRecyclerView()
        setupToolbarTitle()

        // DEBUG: Add log and temporary view highlighting for diagnostics
        binding.recyclerView.setBackgroundColor(0x44FF0000) // translucent red overlay to see if RV is visible
        android.util.Log.d("CalendarFragment", "onCreateView: RecyclerView visibility=" + binding.recyclerView.visibility + ", adapter=" + binding.recyclerView.adapter)
        android.widget.Toast.makeText(requireContext(), "CalendarFragment onCreateView reached", android.widget.Toast.LENGTH_SHORT).show()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refreshEvents()
    }

    private fun setupRecyclerView() {
        adapter = EventAdapter(emptyList(), ::onEventClicked)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        refreshEvents()
    }

    // PUBLIC_INTERFACE
    fun refreshEvents() {
        // For prototype, use all events. Filtering by visible range could be added.
        val events = EventRepository.getAllEvents()
        adapter.updateEvents(events)
    }

    private fun setupToolbarTitle() {
        val title = when (viewMode) {
            CalendarViewMode.MONTH -> getString(R.string.title_month)
            CalendarViewMode.WEEK -> getString(R.string.title_week)
            CalendarViewMode.DAY -> getString(R.string.title_day)
        }
        (activity as? MainActivity)?.supportActionBar?.title = title
    }

    private fun onEventClicked(event: Event) {
        val dialog = EventDialogFragment.newInstance(event)
        dialog.setEventChangedListener(object : EventDialogFragment.EventChangedListener {
            override fun onEventChanged() {
                refreshEvents()
            }
        })
        dialog.show(childFragmentManager, "EventDialogFragment")
    }

    companion object {
        private const val ARG_VIEW_MODE = "view_mode"
        // PUBLIC_INTERFACE
        fun newInstance(viewMode: CalendarViewMode): CalendarFragment {
            val fragment = CalendarFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(ARG_VIEW_MODE, viewMode)
            }
            return fragment
        }
    }
}
