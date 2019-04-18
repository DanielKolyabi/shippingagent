package ru.relabs.kurjercontroller.ui.fragments.report.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.holder_report_appartament_button_group_addition.view.*
import kotlinx.android.synthetic.main.holder_report_appartament_button_group_main.view.*
import ru.relabs.kurjercontroller.R

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
            view.regular_button?.setOnClickListener {
                onStateChanged(1)
            }
            view.not_regular_button?.setOnClickListener {
                onStateChanged(2)
            }
            view.not_confirmed_button?.setOnClickListener {
                onStateChanged(4)
            }
            view.broken_button_main?.setOnClickListener {
                onStateChanged(8)
            }
        } else {
            view.yes_button?.setOnClickListener {
                onStateChanged(16)
            }
            view.no_button?.setOnClickListener {
                onStateChanged(32)
            }
            view.broken_button_addition?.setOnClickListener {
                onStateChanged(8)
            }
        }

        view.regular_button?.setSelectButtonActive(state and 1 > 0)
        view.not_regular_button?.setSelectButtonActive(state and 2 > 0)
        view.not_confirmed_button?.setSelectButtonActive(state and 4 > 0)
        view.broken_button_main?.setSelectButtonActive(state and 8 > 0)
        view.broken_button_addition?.setSelectButtonActive(state and 8 > 0)
        view.yes_button?.setSelectButtonActive(state and 16 > 0)
        view.no_button?.setSelectButtonActive(state and 32 > 0)

        return view
    }

    private fun Button.setSelectButtonActive(active: Boolean) {
        if (active) {
            this.setBackgroundResource(R.drawable.abc_btn_colored_material)
        } else {
            this.setBackgroundResource(R.drawable.abc_btn_default_mtrl_shape)
        }
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