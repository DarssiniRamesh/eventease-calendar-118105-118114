package com.example.calendareventfrontend

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calendareventfrontend.data.EventRepository
import com.example.calendareventfrontend.model.CalendarViewMode
import com.example.calendareventfrontend.model.Event
import com.example.calendareventfrontend.databinding.FragmentCalendarBinding
import java.util.Calendar

// PUBLIC_INTERFACE
class CalendarFragment : Fragment() {
    private lateinit var binding: FragmentCalendarBinding
    private var viewMode: CalendarViewMode = CalendarViewMode.MONTH

    // Month grid adapter
    private var monthAdapter: MonthCalendarAdapter? = null

    // Only used in non-month view (retained for possible extension)
    private lateinit var eventListAdapter: EventAdapter

    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            viewMode = args.getSerializable(ARG_VIEW_MODE) as? CalendarViewMode ?: CalendarViewMode.MONTH
        }
        val today = Calendar.getInstance()
        selectedYear = today.get(Calendar.YEAR)
        selectedMonth = today.get(Calendar.MONTH)
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

        setupToolbarTitle()

        if (viewMode == CalendarViewMode.MONTH) {
            setupMonthGridRecyclerView()
        } else {
            setupEventListRecyclerView()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refreshEvents()
    }

    private fun setupMonthGridRecyclerView() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 7)
        binding.recyclerView.layoutManager = gridLayoutManager
        // Adapter will be (re-)created in refreshEvents() to account for data changes.
        refreshEvents()
    }

    private fun setupEventListRecyclerView() {
        eventListAdapter = EventAdapter(emptyList(), ::onEventClicked)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = eventListAdapter
        refreshEvents()
    }

    // PUBLIC_INTERFACE
    fun refreshEvents() {
        if (viewMode == CalendarViewMode.MONTH) {
            val dayCells = generateMonthCells(selectedYear, selectedMonth)
            monthAdapter = MonthCalendarAdapter(dayCells, ::onDayCellClicked)
            binding.recyclerView.adapter = monthAdapter
        } else {
            // For prototype, use all events. Filtering by visible range could be added for week/day.
            val events = EventRepository.getAllEvents()
            eventListAdapter.updateEvents(events)
        }
    }

    /**
     * Generates the cells for the month grid, populating with event list per day.
     * @return list with 42 DayCell objects (start from Sunday of week 1).
     */
    private fun generateMonthCells(year: Int, month: Int): List<MonthCalendarAdapter.DayCell> {
        val cells = mutableListOf<MonthCalendarAdapter.DayCell>()
        val events = EventRepository.getAllEvents()
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        // Find the day-of-week of first of the month (Calendar.SUNDAY==1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        // Determine which date to start cell 0
        cal.add(Calendar.DAY_OF_MONTH, -(firstDayOfWeek - Calendar.SUNDAY))

        for (cell in 0 until 42) {
            val cellYear = cal.get(Calendar.YEAR)
            val cellMonth = cal.get(Calendar.MONTH)
            val cellDay = cal.get(Calendar.DAY_OF_MONTH)
            val inMonth = (cellMonth == month)
            // Event match: events where day/month/year match cell
            val cellEvents = events.filter {
                val eventCal = Calendar.getInstance()
                eventCal.timeInMillis = it.startTime
                eventCal.get(Calendar.YEAR) == cellYear &&
                eventCal.get(Calendar.MONTH) == cellMonth &&
                eventCal.get(Calendar.DAY_OF_MONTH) == cellDay
            }
            cells.add(
                MonthCalendarAdapter.DayCell(
                    year = cellYear,
                    month = cellMonth,
                    day = cellDay,
                    inMonth = inMonth,
                    events = cellEvents
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return cells
    }

    private fun onDayCellClicked(year: Int, month: Int, day: Int, events: List<Event>) {
        if (events.isNotEmpty()) {
            // Show first event details (or show list if multiple - for now: open first)
            val dialog = EventDialogFragment.newInstance(events[0])
            dialog.setEventChangedListener(object : EventDialogFragment.EventChangedListener {
                override fun onEventChanged() {
                    refreshEvents()
                }
            })
            dialog.show(childFragmentManager, "EventDialogFragment")
        } else {
            // No event: prompt add dialog on this date
            // Pre-fill date in event creation dialog (extend if dialog supports it)
            val dialog = EventDialogFragment.newInstance(null)
            dialog.setEventChangedListener(object : EventDialogFragment.EventChangedListener {
                override fun onEventChanged() {
                    refreshEvents()
                }
            })
            dialog.show(childFragmentManager, "EventDialogFragment")
        }
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
