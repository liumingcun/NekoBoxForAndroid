package io.nekohasekai.sagernet.widget

import android.content.Context
import android.text.format.Formatter
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.TooltipCompat
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
    private lateinit var txText: TextView
    private lateinit var rxText: TextView
    private lateinit var connectionSwitch: SwitchCompat
    private var testing = false

    override fun onFinishInflate() {
        super.onFinishInflate()
        statusText = findViewById(R.id.status)
        txText = findViewById(R.id.tx)
        rxText = findViewById(R.id.rx)
        connectionSwitch = findViewById(R.id.connection_switch)
        updateSpeed(0, 0)
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
        if (!testing || !state.connected) {
            setStatus(context.getText(when (state) {
                BaseService.State.Connected -> R.string.vpn_connected
                BaseService.State.Connecting -> R.string.connecting
                BaseService.State.Stopping -> R.string.stopping
                else -> R.string.not_connected
            }))
        }
        if (!state.connected) updateSpeed(0, 0)
    }

    fun updateSpeed(txRate: Long, rxRate: Long) {
        txText.text = context.getString(R.string.speed, Formatter.formatFileSize(context, txRate))
        rxText.text = context.getString(R.string.speed, Formatter.formatFileSize(context, rxRate))
    }

    fun testConnection(activity: MainActivity) {
        if (testing || !DataStore.serviceState.connected) return
        testing = true
        setStatus(context.getText(R.string.connection_test_testing))
        runOnDefaultDispatcher {
            try {
                val elapsed = activity.urlTest()
                onMainDispatcher {
                    testing = false
                    if (DataStore.serviceState.connected) {
                        setStatus(context.getString(
                            if (DataStore.connectionTestURL.startsWith("https://")) {
                                R.string.connection_test_available
                            } else {
                                R.string.connection_test_available_http
                            }, elapsed
                        ))
                    } else changeState(DataStore.serviceState)
                }
            } catch (e: Exception) {
                Logs.w(e.toString())
                onMainDispatcher {
                    testing = false
                    changeState(DataStore.serviceState)
                    activity.snackbar(context.getString(
                        R.string.connection_test_error, e.readableMessage
                    )).show()
                }
            }
        }
    }
}
