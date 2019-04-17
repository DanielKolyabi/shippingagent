package ru.relabs.kurjercontroller.ui.fragments.report.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import ru.relabs.kurjercontroller.R

/**
 * Created by ProOrange on 16.04.2019.
 */

class ApartmentButtonsPagerAdapter(val context: Context?) : PagerAdapter(){

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = if(position == 0)
            R.layout.holder_report_appartament_button_group_main
        else
            R.layout.holder_report_appartament_button_group_addition

        val view = LayoutInflater.from(context).inflate(layout, container, false)
        container.addView(view)
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