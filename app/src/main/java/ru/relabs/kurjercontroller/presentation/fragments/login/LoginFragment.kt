package ru.relabs.kurjercontroller.presentation.fragments.login


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.relabs.kurjercontroller.BuildConfig
import ru.relabs.kurjercontroller.R
import ru.relabs.kurjercontroller.activity
import ru.relabs.kurjercontroller.application
import ru.relabs.kurjercontroller.utils.NetworkHelper
import ru.relabs.kurjercontroller.presentation.activities.ErrorButtonsListener
import ru.relabs.kurjercontroller.presentation.activities.showError
import ru.relabs.kurjercontroller.utils.extensions.setVisible


class LoginFragment : Fragment() {
    val presenter = LoginPresenter(this)
    var shouldResetRememberOnInput = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter.loadUserCredentials()

        bindListeners()
        presenter.setRememberPasswordEnabled(true)
        app_version.text = resources.getString(R.string.app_version_label, BuildConfig.VERSION_NAME)
    }

    fun bindListeners() {
        val myTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!shouldResetRememberOnInput) {
                    return
                }
                if (s?.length == 0) {
                    shouldResetRememberOnInput = false
                }
                presenter.resetAuthByToken()
            }
        }

        login_input.addTextChangedListener(myTextWatcher)
        password_input.addTextChangedListener(myTextWatcher)

        remember_password_text.setOnClickListener {
            presenter.onRememberPasswordClick()
        }
        login_button?.isEnabled = true
        login_button.setOnClickListener {
            if(application().lastRequiredAppVersion > BuildConfig.VERSION_CODE){
                activity()?.showError("Необходимо обновить приложение.", object: ErrorButtonsListener {
                    override fun positiveListener() {
                        activity()?.checkUpdates()
                    }
                }, "Обновить")
                return@setOnClickListener
            }
            if (!NetworkHelper.isNetworkEnabled(context)) {
                activity?.showError("Необходимо включить передачу данных")
                return@setOnClickListener
            }
            presenter.onLoginClick(login_input.text.toString(), password_input.text.toString())
        }
    }

    suspend fun setLoginButtonLoading(state: Boolean) = withContext(Dispatchers.Main) {
        login_button?.isEnabled = !state
        loading?.setVisible(state)
    }

    fun setRememberPasswordEnabled(enabled: Boolean) {
        val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, resources.displayMetrics).toInt()
        remember_password_text?.setCompoundDrawables(
            context?.getDrawable(
                if (enabled)
                    R.drawable.ic_checked_checkbox
                else
                    R.drawable.ic_unchecked_checkbox
            )?.apply {
                setBounds(0, 0, size, size)
            }, null, null, null
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.bgScope.terminate()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            LoginFragment()
    }
}
