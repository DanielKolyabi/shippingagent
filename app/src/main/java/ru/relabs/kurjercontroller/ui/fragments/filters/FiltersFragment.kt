package ru.relabs.kurjercontroller.ui.fragments.filters

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.hootsuite.nachos.ChipConfiguration
import com.hootsuite.nachos.NachoTextView
import com.hootsuite.nachos.chip.Chip
import com.hootsuite.nachos.chip.ChipCreator
import com.hootsuite.nachos.chip.ChipSpan
import com.hootsuite.nachos.chip.ChipSpanChipCreator
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import com.hootsuite.nachos.tokenizer.SpanChipTokenizer
import kotlinx.android.synthetic.main.fragment_filters.*
import kotlinx.android.synthetic.main.fragment_filters.view.*
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.models.Filter
import ru.relabs.kurjercontroller.models.TaskFiltersModel


/**
 * Created by ProOrange on 18.03.2019.
 */

const val FILTERS_REQUEST_CODE = 1

class FiltersFragment() : Fragment() {

    lateinit var filters: TaskFiltersModel
    val presenter = FiltersPresenter(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.publisher_filters.add(Filter(1, "Test Edition", true))
        view.publisher_filters.add(Filter(2, "Test Edition Delete", false))
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filters, container, false)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            filters = it.getParcelable("filters") ?: TaskFiltersModel.blank()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(filters: TaskFiltersModel?) =
            FiltersFragment().apply {
                arguments = Bundle().apply {
                    if (filters == null) {
                        putParcelable("filters", filters)
                    }
                }
            }
    }
}