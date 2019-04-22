package ru.relabs.kurjercontroller.ui.fragments.report.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.holder_report_appartament_button_group_addition.view.*
import kotlinx.android.synthetic.main.holder_report_appartament_button_group_main.view.*
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.ui.extensions.setSelectButtonActive

/**
 * Created by ProOrange on 16.04.2019.
 */

class ApartmentButtonsPagerAdapter(
    val context: Context?,
    var state: Int,
    val onStateChanged: (change: Int) -> Unit
) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = if (position == 0)
            R.layout.holder_report_appartament_button_group_main
        else
            R.layout.holder_report_appartament_button_group_addition

        val view = LayoutInflater.from(context).inflate(layout, container, false)
        container.addView(view)

        if (position == 0) {
            view.yes_button_main?.setOnClickListener {
                state = state xor 1
                if(state and 4 > 0){
                    state = state xor 4
                }
                onStateChanged(state)
            }
            view.not_regular_button_main?.setOnClickListener {
                state = state xor 2
                onStateChanged(state)
            }
            view.no_button_main?.setOnClickListener {
                state = state xor 4
                if(state and 1 > 0){
                    state = state xor 1
                }
                onStateChanged(state)
            }
            view.broken_button_main?.setOnClickListener {
                state = state xor 8
                onStateChanged(state)
            }
        } else {
            view.yes_button_addition?.setOnClickListener {
                state = state xor 16
                if(state and 32 > 0){
                    state = state xor 32
                }
                onStateChanged(state)
            }
            view.no_button_addition?.setOnClickListener {
                state = state xor 32
                if(state and 16 > 0){
                    state = state xor 16
                }
                onStateChanged(state)
            }
            view.broken_button_addition?.setOnClickListener {
                state = state xor 8
                onStateChanged(state)
            }
        }

        view.yes_button_main?.setSelectButtonActive(state and 1 > 0)
        view.not_regular_button_main?.setSelectButtonActive(state and 2 > 0)
        view.no_button_main?.setSelectButtonActive(state and 4 > 0)
        view.broken_button_main?.setSelectButtonActive(state and 8 > 0)
        view.broken_button_addition?.setSelectButtonActive(state and 8 > 0)
        view.yes_button_addition?.setSelectButtonActive(state and 16 > 0)
        view.no_button_addition?.setSelectButtonActive(state and 32 > 0)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount(): Int {
        return 2
    }

}