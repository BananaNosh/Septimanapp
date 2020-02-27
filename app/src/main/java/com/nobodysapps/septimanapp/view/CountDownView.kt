package com.nobodysapps.septimanapp.view

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.widget.TextView
import com.nobodysapps.septimanapp.R
import java.util.*

class CountDownView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {
    var endTime: Calendar? = null
        private set
    private var timer: CountDownTimer? = null
    private var listener: Listener? = null
    var started: Boolean = false
        private set

    fun setEndTime(endTime: Calendar) {
        this.endTime = endTime
        stopTimer()
    }

    fun startTimer() {
        if (started) return
        endTime?.let {
            started = true
            val millisToGo = it.timeInMillis - Calendar.getInstance().timeInMillis
            timer = object: CountDownTimer(millisToGo, 1000) {
                override fun onFinish() {
                    setTextAccordingToMillis(0)
                    listener?.onFinished()
                }

                override fun onTick(millisUntilFinished: Long) {
                    setTextAccordingToMillis(millisUntilFinished)
                }

                private fun setTextAccordingToMillis(millisUntilFinished: Long) {
                    val (days, hours, minutes) = millisToComponents((millisUntilFinished / 1000).toInt())
                    val countDownText =
                        String.format(Locale.getDefault(), context.getString(R.string.nav_drawer_count_down_pattern), days, hours, minutes)
                    if (countDownText != text) {
                        text = countDownText
                    }
                }

            }
            timer?.start()
        }
    }

    private fun millisToComponents(timeInSeconds: Int): Triple<Int, Int, Int> {
//        val seconds = (timeInMillis / 1000) % 60
        val minutes = (timeInSeconds / 60) % 60
        val hours = (timeInSeconds / 3600) % 24
        val days = timeInSeconds / (24 * 3600)

        return Triple(days, hours, minutes)
    }

    fun stopTimer() {
        timer?.cancel()
        timer = null
        started = false
    }

    interface Listener {
        fun onFinished()
    }
}