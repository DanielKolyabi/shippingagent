package ru.relabs.kurjercontroller.presentation.base.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import ru.relabs.kurjercontroller.presentation.host.IFragmentHolder


abstract class BaseFragment : Fragment() {

    var exitAnimation = ExitAnimation.Default
    protected val supervisor = SupervisorJob()
    protected val uiScope = CoroutineScope(Dispatchers.Main + supervisor)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? IFragmentHolder)?.onFragmentAttached(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        supervisor.cancelChildren()
    }

    override fun onResume() {
        super.onResume()
    }

    /**
     * Back pressed handle
     * @return Should back pressed be intercepted
     */
    open fun interceptBackPressed(): Boolean {
        return false
    }

    enum class ExitAnimation {
        Default, Left, Right
    }
}