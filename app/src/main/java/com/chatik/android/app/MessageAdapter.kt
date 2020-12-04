package com.chatik.android.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private val data = mutableListOf<String>()

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            // Define click listener for the ViewHolder's View.
            textView = view.findViewById(R.id.textView)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.text_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your data at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = data[position]
    }

    // Return the size of your data (invoked by the layout manager)
    override fun getItemCount() = data.size

    fun addNewMessage(message: String) {
        data.add(message)
        this.notifyItemInserted(data.size - 1)
    }

    fun setMessages(messages: List<String>) {
        data.clear()
        data.addAll(messages)
        this.notifyDataSetChanged()
    }
}
