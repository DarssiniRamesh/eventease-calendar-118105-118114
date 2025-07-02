package com.example.calendareventfrontend

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.calendareventfrontend.data.EventRepository
import com.example.calendareventfrontend.model.Event
import com.example.calendareventfrontend.databinding.DialogEventBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// PUBLIC_INTERFACE
class EventDialogFragment : DialogFragment() {
    interface EventChangedListener {
        fun onEventChanged()
    }
    private var eventChangedListener: EventChangedListener? = null
    private var event: Event? = null
    private var isEdit: Boolean = false
    private lateinit var binding: DialogEventBinding
    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getSerializable(ARG_EVENT)?.let {
            event = it as Event
            isEdit = true
        }
    }

    // PUBLIC_INTERFACE
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogEventBinding.inflate(LayoutInflater.from(context))

        if (isEdit && event != null) {
            populateFields(event!!)
        } else {
            defaultInitFields()
        }

        setupDateTimePickers()

        return AlertDialog.Builder(requireContext())
            .setTitle(if (isEdit) getString(R.string.edit_event) else getString(R.string.add_event))
            .setView(binding.root)
            .setPositiveButton(if (isEdit) getString(R.string.save) else getString(R.string.add)) { _, _ ->
                if (validate()) {
                    val newEvent = createEventFromFields()
                    if (isEdit) {
                        EventRepository.updateEvent(newEvent)
                    } else {
                        EventRepository.addEvent(newEvent)
                    }
                    eventChangedListener?.onEventChanged()
                    dismiss()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .apply {
                if (isEdit) {
                    setNeutralButton(getString(R.string.delete)) { _, _ ->
                        event?.let { EventRepository.deleteEvent(it.id) }
                        eventChangedListener?.onEventChanged()
                        dismiss()
                    }
                }
            }
            .create()
    }

    private fun setupDateTimePickers() {
        binding.eventStart.setOnClickListener {
            showDateTimePicker(true)
        }
        binding.eventEnd.setOnClickListener {
            showDateTimePicker(false)
        }
    }

    private fun populateFields(event: Event) {
        binding.eventTitle.setText(event.title)
        binding.eventDescription.setText(event.description)
        calendar.time = Date(event.startTime)
        binding.eventStart.setText(formatDateTime(calendar.time))
        calendar.time = Date(event.endTime)
        binding.eventEnd.setText(formatDateTime(calendar.time))
        binding.eventLocation.setText(event.location ?: "")
        binding.reminderMinutes.setText(event.reminderMinutes?.toString() ?: "")
    }

    private fun defaultInitFields() {
        calendar.timeInMillis = System.currentTimeMillis()
        binding.eventStart.setText(formatDateTime(calendar.time))
        calendar.add(Calendar.HOUR, 1)
        binding.eventEnd.setText(formatDateTime(calendar.time))
    }

    private fun showDateTimePicker(isStart: Boolean) {
        val cal = Calendar.getInstance().apply {
            if (isStart) time = parseDateTime(binding.eventStart.text.toString())
            else time = parseDateTime(binding.eventEnd.text.toString())
        }
        DatePickerDialog(requireContext(), { _, y, m, d ->
            cal.set(Calendar.YEAR, y)
            cal.set(Calendar.MONTH, m)
            cal.set(Calendar.DAY_OF_MONTH, d)
            TimePickerDialog(requireContext(), { _, h, min ->
                cal.set(Calendar.HOUR_OF_DAY, h)
                cal.set(Calendar.MINUTE, min)
                if (isStart) binding.eventStart.setText(formatDateTime(cal.time))
                else binding.eventEnd.setText(formatDateTime(cal.time))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun validate(): Boolean {
        if (binding.eventTitle.text.isNullOrBlank()) return false
        return true
    }

    private fun createEventFromFields(): Event {
        val startTime = parseDateTime(binding.eventStart.text.toString()).time
        val endTime = parseDateTime(binding.eventEnd.text.toString()).time
        return Event(
            id = event?.id ?: "",
            title = binding.eventTitle.text.toString(),
            description = binding.eventDescription.text.toString(),
            startTime = startTime,
            endTime = endTime,
            reminderMinutes = binding.reminderMinutes.text.toString().toIntOrNull(),
            location = binding.eventLocation.text.toString().ifBlank { null }
        )
    }

    private fun formatDateTime(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date)
    }

    private fun parseDateTime(str: String): Date {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(str) ?: Date()
    }

    companion object {
        private const val ARG_EVENT = "event"
        // PUBLIC_INTERFACE
        fun newInstance(event: Event?): EventDialogFragment {
            val fragment = EventDialogFragment()
            fragment.arguments = Bundle().apply {
                if (event != null) putSerializable(ARG_EVENT, event)
            }
            return fragment
        }
    }
    // PUBLIC_INTERFACE
    fun setEventChangedListener(listener: EventChangedListener) {
        eventChangedListener = listener
    }
}
