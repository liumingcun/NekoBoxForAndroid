package io.nekohasekai.sagernet.widget

import android.content.Context
import android.text.format.Formatter
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.TooltipCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import io.nekohasekai.sagernet.database.SagerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.material.card.MaterialCardView
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.bg.BaseService
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.ktx.*
import io.nekohasekai.sagernet.ui.MainActivity

/** Connection controls and live proxy rates, displayed above the server list. */
class StatsBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle,
) : MaterialCardView(context, attrs, defStyleAttr) {
    private lateinit var statusText: TextView
    private lateinit var nodeText: TextView
    private lateinit var hintText: TextView
    private lateinit var txText: TextView
    private lateinit var rxText: TextView
    private lateinit var connectionSwitch: SwitchCompat
    private var testing = false
    private var wasConnected = false

    override fun onFinishInflate() {
        super.onFinishInflate()
        statusText = findViewById(R.id.status)
        nodeText = findViewById(R.id.current_node)
        hintText = findViewById(R.id.connection_hint)
        txText = findViewById(R.id.tx)
        rxText = findViewById(R.id.rx)
        connectionSwitch = findViewById(R.id.connection_switch)
        updateSpeed(0, 0)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        refreshProfile()
    }

    fun refreshProfile() {
        val owner = findViewTreeLifecycleOwner() ?: return
        val profileId = DataStore.selectedProxy
        owner.lifecycleScope.launch {
            val name = withContext(Dispatchers.IO) {
                SagerDatabase.proxyDao.getById(profileId)?.displayName()
            }
            if (profileId == DataStore.selectedProxy) {
                nodeText.text = name ?: context.getString(R.string.profile_empty)
            }
        }
    }

    fun updateProfile(profileId: Long, name: String) {
        if (profileId == DataStore.selectedProxy) nodeText.text = name
    }

    private fun setStatus(text: CharSequence) {
        statusText.text = text
        TooltipCompat.setTooltipText(this, text)
    }

    fun changeState(state: BaseService.State) {
        connectionSwitch.isChecked = state.canStop
        connectionSwitch.isEnabled = state.canStop || state == BaseService.State.Stopped
        connectionSwitch.contentDescription = context.getText(
            if (state.canStop) R.string.stop else R.string.connect
        )
        setStatus(context.getText(when (state) {
            BaseService.State.Connected -> R.string.vpn_connected
            BaseService.State.Connecting -> R.string.connecting
            BaseService.State.Stopping -> R.string.stopping
            else -> R.string.not_connected
        }))
        if (!state.connected) {
            testing = false
            hintText.setText(R.string.nb_connect_hint)
            updateSpeed(0, 0)
        }
        if (state.connected && !wasConnected) hintText.setText(R.string.nb_test_hint)
        wasConnected = state.connected
        refreshProfile()
    }

    fun updateSpeed(txRate: Long, rxRate: Long) {
        txText.text = context.getString(R.string.speed, Formatter.formatFileSize(context, txRate))
        rxText.text = context.getString(R.string.speed, Formatter.formatFileSize(context, rxRate))
    }

    fun testConnection(activity: MainActivity) {
        if (testing || !DataStore.serviceState.connected) return
        testing = true
        hintText.setText(R.string.connection_test_testing)
        runOnDefaultDispatcher {
            try {
                val elapsed = activity.urlTest()
                onMainDispatcher {
                    testing = false
                    if (DataStore.serviceState.connected) {
                        hintText.text = context.getString(
                            if (DataStore.connectionTestURL.startsWith("https://")) {
                                R.string.connection_test_available
                            } else {
                                R.string.connection_test_available_http
                            }, elapsed
                        )
                    } else changeState(DataStore.serviceState)
                }
            } catch (e: Exception) {
                Logs.w(e.toString())
                onMainDispatcher {
                    testing = false
                    changeState(DataStore.serviceState)
                    hintText.setText(R.string.connection_test_fail)
                    activity.snackbar(context.getString(
                        R.string.connection_test_error, e.readableMessage
                    )).show()
                }
            }
        }
    }
}
