package com.example.calendareventfrontend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.calendareventfrontend.model.Event
import com.example.calendareventfrontend.databinding.ItemDayCellBinding
import java.util.*

/**
 * Adapter representing day cells for a month-view calendar grid.
 * Each cell can display event indicator if at least one event is scheduled that day.
 */
class MonthCalendarAdapter(
    private val days: List<DayCell>,
    private val onDayClicked: (year: Int, month: Int, day: Int, List<Event>) -> Unit
) : RecyclerView.Adapter<MonthCalendarAdapter.DayViewHolder>() {

    data class DayCell(
        val year: Int,
        val month: Int,
        val day: Int,
        val inMonth: Boolean,
        val events: List<Event>
    )

    inner class DayViewHolder(val binding: ItemDayCellBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cell: DayCell) {
            binding.dayNumber.text = cell.day.toString()
            // Faded look for non-current-month days
            binding.dayNumber.alpha = if (cell.inMonth) 1.0f else 0.33f
            // Show event indicator if events exist
            if (cell.events.isNotEmpty()) {
                binding.eventIndicator.visibility = View.VISIBLE
            } else {
                binding.eventIndicator.visibility = View.GONE
            }
            binding.root.setOnClickListener {
                onDayClicked(cell.year, cell.month, cell.day, cell.events)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemDayCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Set cell widths/heights for a 7x6 grid (fill parent width)
        val metrics = parent.resources.displayMetrics
        val cellWidth = (metrics.widthPixels / 7.0).toInt()
        val params = binding.root.layoutParams ?: ViewGroup.LayoutParams(cellWidth, cellWidth)
        params.width = cellWidth
        params.height = cellWidth
        binding.root.layoutParams = params
        return DayViewHolder(binding)
    }

    override fun getItemCount(): Int = days.size

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(days[position])
    }
}
