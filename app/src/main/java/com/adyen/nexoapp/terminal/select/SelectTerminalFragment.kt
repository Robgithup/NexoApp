package com.adyen.nexoapp.terminal.select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.adyen.nexoapp.R
import com.adyen.nexoapp.terminal.TerminalItem
import kotlinx.android.synthetic.main.fragment_select_terminal.selectButton
import kotlinx.android.synthetic.main.fragment_select_terminal.terminalModelRecyclerView
import kotlinx.android.synthetic.main.fragment_select_terminal.terminalModelTextView
import kotlinx.android.synthetic.main.view_terminal_model.view.terminalModelImageView


class SelectTerminalFragment : Fragment() {
    private val snapHelper by lazy { LinearSnapHelper() }

    var listener: Listener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_select_terminal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val screenWidth = resources.displayMetrics.widthPixels
        val itemWidth = (screenWidth * 0.5).toInt()
        val padding = (screenWidth * 0.25).toInt()
        val layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
        terminalModelRecyclerView.layoutManager = layoutManager
        terminalModelRecyclerView.adapter = Adapter(itemWidth)
        terminalModelRecyclerView.setPadding(padding, terminalModelRecyclerView.paddingTop, padding, terminalModelRecyclerView.paddingBottom)
        terminalModelRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    terminalModelTextView.text = getSnappedItem()?.nameRes?.let { getText(it) }
                }
            }
        })
        terminalModelRecyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                terminalModelTextView.text = getSnappedItem()?.nameRes?.let { getText(it) }
                terminalModelRecyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        snapHelper.attachToRecyclerView(terminalModelRecyclerView)

        selectButton.setOnClickListener {
            val snappedItem = getSnappedItem()

            if (snappedItem != null) {
                listener?.onTerminalItemSelected(snappedItem)
            }
        }
    }

    private fun getSnappedView(): View? {
        return snapHelper.findSnapView(terminalModelRecyclerView.layoutManager)
    }

    private fun getSnappedItem(): TerminalItem? {
        val snappedView = getSnappedView()
        return if (snappedView != null) {
            val adapterPosition = terminalModelRecyclerView.getChildAdapterPosition(snappedView)
            (terminalModelRecyclerView.adapter as Adapter).items.getOrNull(adapterPosition)
        } else {
            null
        }
    }

    private fun scrollToItem(view: View) {
        val adapterPosition = terminalModelRecyclerView.getChildAdapterPosition(view)

        if (adapterPosition != RecyclerView.NO_POSITION) {
            terminalModelRecyclerView.smoothScrollToPosition(adapterPosition)
        }
    }

    companion object {
        const val TAG = "SelectTerminalFragment"
    }

    interface Listener {
        fun onTerminalItemSelected(terminalItem: TerminalItem)
    }

    private inner class Adapter(private val itemWidth: Int) : RecyclerView.Adapter<ViewHolder>() {
        val items = TerminalItem.values()

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.view_terminal_model, parent, false)
            val viewHolder = ViewHolder(itemView)
            val layoutParams = RecyclerView.LayoutParams(itemWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            itemView.layoutParams = layoutParams
            itemView.setOnClickListener {
                if (it != getSnappedView()) {
                    scrollToItem(it)
                } else {
                    val item = items.getOrNull(viewHolder.adapterPosition)
                    if (item != null) {
                        listener?.onTerminalItemSelected(item)
                    }
                }
            }
            return viewHolder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.itemView.terminalModelImageView.setImageResource(item.drawableRes)
        }
    }

    private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
