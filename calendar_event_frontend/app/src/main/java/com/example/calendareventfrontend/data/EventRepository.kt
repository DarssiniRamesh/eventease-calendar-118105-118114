package com.example.calendareventfrontend.data

import com.example.calendareventfrontend.model.Event
import java.util.concurrent.CopyOnWriteArrayList

// PUBLIC_INTERFACE
object EventRepository {
    private val events = CopyOnWriteArrayList<Event>()

    /**
     * Returns a copy of all events.
     */
    // PUBLIC_INTERFACE
    fun getAllEvents(): List<Event> = events.toList()

    /**
     * Adds a new event.
     */
    // PUBLIC_INTERFACE
    fun addEvent(event: Event) {
        events.add(event)
    }

    /**
     * Updates an existing event.
     */
    // PUBLIC_INTERFACE
    fun updateEvent(updated: Event) {
        events.replaceAll { existing ->
            if (existing.id == updated.id) updated else existing
        }
    }

    /**
     * Deletes an event by ID.
     */
    // PUBLIC_INTERFACE
    fun deleteEvent(eventId: String) {
        events.removeIf { it.id == eventId }
    }

    /**
     * Searches for events by title/query.
     */
    // PUBLIC_INTERFACE
    fun searchEvents(query: String): List<Event> {
        return events.filter { it.title.contains(query, ignoreCase = true) }
    }

    /**
     * Gets event by ID.
     */
    // PUBLIC_INTERFACE
    fun getEvent(eventId: String): Event? {
        return events.find { it.id == eventId }
    }
}
