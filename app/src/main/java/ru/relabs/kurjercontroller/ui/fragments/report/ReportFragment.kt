package ru.relabs.kurjercontroller.ui.fragments.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.relabs.kurjercontroller.R

/**
 * Created by ProOrange on 18.03.2019.
 */
class ReportFragment : Fragment() {

    val presenter = ReportPresenter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report, container, false)
    }
}