package com.example.calendareventfrontend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.calendareventfrontend.model.Event
import com.example.calendareventfrontend.databinding.ItemEventBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// PUBLIC_INTERFACE
class EventAdapter(
    private var events: List<Event>,
    private val onEventClicked: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun getItemCount(): Int = events.size

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.binding.eventTitle.text = event.title
        holder.binding.eventTime.text = eventTimeString(event.startTime, event.endTime)
        holder.binding.root.setOnClickListener { onEventClicked(event) }
    }

    fun updateEvents(newEvents: List<Event>) {
        this.events = newEvents
        notifyDataSetChanged()
    }

    private fun eventTimeString(start: Long, end: Long): String {
        val fmt = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        return "${fmt.format(Date(start))} - ${fmt.format(Date(end))}"
    }
}
