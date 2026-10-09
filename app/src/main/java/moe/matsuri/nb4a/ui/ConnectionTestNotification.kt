package moe.matsuri.nb4a.ui

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.core.app.NotificationCompat
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.ktx.Logs

class ConnectionTestNotification(val context: Context, val title: String) {
    private val channelId = "connection-test"
    private val notificationId = 1001

    fun updateNotification(progress: Int, max: Int, finished: Boolean) {
        try {
            if (finished) {
                SagerNet.notification.cancel(notificationId)
                return
            }
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_byteflow_notification)
                .setLargeIcon(AppCompatResources.getDrawable(context, R.drawable.ic_byteflow_brand)!!.toBitmap(128, 128))
                .setContentTitle(title)
                .setOnlyAlertOnce(true)
                .setContentText("$progress / $max").setProgress(max, progress, false)
            SagerNet.notification.notify(notificationId, builder.build())
        } catch (e: Exception) {
            Logs.w(e)
        }
    }
}
