package ru.relabs.kurjercontroller.presentation.taskDetails

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.domain.models.TaskState
import ru.relabs.kurjercontroller.presentation.base.recycler.DelegateAdapter
import ru.relabs.kurjercontroller.presentation.base.tea.renderT
import ru.relabs.kurjercontroller.presentation.helpers.TaskAddressSorter
import ru.relabs.kurjercontroller.utils.extensions.getColorCompat
import ru.relabs.kurjercontroller.utils.extensions.visible

/**
 * Created by Daniil Kurchanov on 06.04.2020.
 */
object TaskDetailsRenders {
    fun renderLoading(view: View): TaskDetailsRender = renderT(
        { it.loaders > 0 },
        { view.visible = it }
    )

    fun renderList(adapter: DelegateAdapter<TaskDetailsItem>): TaskDetailsRender = renderT(
        { it.task },
        { task ->
            adapter.items.clear()
            if (task != null) {
                adapter.items.add(TaskDetailsItem.PageHeader(task))
                if (task.filtered) {
                    adapter.items.add(TaskDetailsItem.ListFiltersHeader)
                    adapter.items.addAll(task.taskFilters.publishers.map {
                        TaskDetailsItem.FilterItem("Издатель", it)
                    })
                    adapter.items.addAll(task.taskFilters.brigades.map {
                        TaskDetailsItem.FilterItem("Бригада", it)
                    })
                    adapter.items.addAll(task.taskFilters.users.map {
                        TaskDetailsItem.FilterItem("Распространитель", it)
                    })
                    adapter.items.addAll(task.taskFilters.districts.map {
                        TaskDetailsItem.FilterItem("Округ", it)
                    })
                    adapter.items.addAll(task.taskFilters.regions.map {
                        TaskDetailsItem.FilterItem("Район", it)
                    })
                } else {
                    adapter.items.add(TaskDetailsItem.ListAddressesHeader)
                    adapter.items.addAll(TaskAddressSorter.sortInfoTaskItemsAlphabetic(task.taskItems).map {
                        TaskDetailsItem.AddressItem(it)
                    })
                }
            }
            adapter.notifyDataSetChanged()
        }
    )

    fun renderExamine(view: View): TaskDetailsRender = renderT(
        { it.task?.state?.state == TaskState.CREATED },
        { view.visible = it }
    )

    fun renderTargetTask(adapter: DelegateAdapter<TaskDetailsItem>, list: RecyclerView): TaskDetailsRender = renderT(
        { it.targetAddress },
        {
            it?.let { a ->
                adapter.items
                    .indexOfFirst { item -> item is TaskDetailsItem.AddressItem && item.taskItem.address.idnd == a.idnd }
                    .takeIf { idx -> idx > 0 }
                    ?.let { idx ->
                        list.scrollToPosition(idx)
                        list.post {
                            list?.findViewHolderForAdapterPosition(idx)?.itemView?.let {
                                flashSelectedColor(it)
                            }
                        }
                    }
            }
        }
    )

    private fun flashSelectedColor(itemView: View) {
        startFlashValueAnimator(itemView) {
            startFlashValueAnimator(itemView)
        }
    }

    private fun startFlashValueAnimator(view: View, onAnimationEnd: (() -> Unit)? = null) {
        val targetColor = view.resources.getColorCompat(R.color.colorAccent)
        val colorFrom = ColorUtils.setAlphaComponent(targetColor, 0)

        val colorAnimationTo = getValueAnimator(colorFrom, targetColor, view, 500)
        val colorAnimationFrom = getValueAnimator(targetColor, colorFrom, view, 500)
        onAnimationEnd?.let {
            colorAnimationFrom.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    it()
                }
            })
        }
        colorAnimationTo.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                colorAnimationFrom.start()
            }
        })

        colorAnimationTo.start()
    }

    private fun getValueAnimator(from: Int, to: Int, view: View?, duration: Int = 250): ValueAnimator {
        return ValueAnimator.ofObject(ArgbEvaluator(), from, to).apply {
            setDuration(duration.toLong())
            addUpdateListener { animator -> view?.setBackgroundColor(animator.animatedValue as Int) }
        }
    }
}