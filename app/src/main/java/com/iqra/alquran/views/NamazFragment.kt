package com.iqra.alquran.views

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.switchmaterial.SwitchMaterial
import com.iqra.alquran.R
import com.iqra.alquran.network.models.NamazTimings.Data.Timings
import com.iqra.alquran.utils.Constants
import com.iqra.alquran.viewmodels.MainViewModel
import com.iqra.alquran.worker.AlarmWorker
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*


class NamazFragment : BaseFragment(), BaseFragment.Namaz
{

    lateinit var mainActivity: MainActivity
    lateinit var viewModel: MainViewModel
    lateinit var ivLeft: ImageView
    lateinit var ivRight: ImageView
    lateinit var tvDate: TextView
    lateinit var tvDuration: TextView
    lateinit var tvAddress: TextView
    lateinit var llFajr: LinearLayout
    lateinit var llDhuhr: LinearLayout
    lateinit var llAsr: LinearLayout
    lateinit var llMaghrib: LinearLayout
    lateinit var llIsha: LinearLayout
    lateinit var tvFajr: TextView
    lateinit var tvDhuhr: TextView
    lateinit var tvAsr: TextView
    lateinit var tvMaghrib: TextView
    lateinit var tvIsha: TextView
    lateinit var swFajrAlarm: SwitchMaterial
    lateinit var swDhuhrAlarm: SwitchMaterial
    lateinit var swAsrAlarm: SwitchMaterial
    lateinit var swMaghribAlarm: SwitchMaterial
    lateinit var swIshaAlarm: SwitchMaterial
    var countDownTimer: CountDownTimer? = null
    var namazAlarmList: MutableList<String>? = null
    var namazDate: Calendar = Calendar.getInstance()

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        mainActivity = activity as MainActivity

        val view = inflater.inflate(R.layout.fragment_namaz, container, false)

        llFajr = view.findViewById(R.id.llFajr)
        llDhuhr = view.findViewById(R.id.llDhuhr)
        llAsr = view.findViewById(R.id.llAsr)
        llMaghrib = view.findViewById(R.id.llMaghrib)
        llIsha = view.findViewById(R.id.llIsha)
        ivLeft = view.findViewById(R.id.ivLeft)
        ivRight = view.findViewById(R.id.ivRight)
        tvDate = view.findViewById(R.id.tvDate)
        tvDuration = view.findViewById(R.id.tvDuration)
        tvAddress = view.findViewById(R.id.tvAddress)
        tvFajr = view.findViewById(R.id.tvFajrTime)
        tvDhuhr = view.findViewById(R.id.tvDhuhrTime)
        tvAsr = view.findViewById(R.id.tvAsrTime)
        tvMaghrib = view.findViewById(R.id.tvMaghribTime)
        tvIsha = view.findViewById(R.id.tvIshaTime)
        swFajrAlarm = view.findViewById(R.id.swFajrAlarm)
        swDhuhrAlarm = view.findViewById(R.id.swDhuhrAlarm)
        swAsrAlarm = view.findViewById(R.id.swAsrAlarm)
        swMaghribAlarm = view.findViewById(R.id.swMaghribAlarm)
        swIshaAlarm = view.findViewById(R.id.swIshaAlarm)

        val sharedPreferences = mainActivity.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        namazAlarmList = sharedPreferences.getStringSet(Constants.KEY_NAMAZ_ALARMS, setOf<String>())
            ?.toMutableList()

        for (alarm in namazAlarmList!!)
        {
            when (alarm)
            {
                Constants.NAMAZ[0] -> swFajrAlarm.isChecked = true
                Constants.NAMAZ[1] -> swDhuhrAlarm.isChecked = true
                Constants.NAMAZ[2] -> swAsrAlarm.isChecked = true
                Constants.NAMAZ[3] -> swMaghribAlarm.isChecked = true
                else ->
                {
                    swIshaAlarm.isChecked = true
                }
            }
        }

        ivLeft.setOnClickListener {
            countDownTimer?.cancel()
            countDownTimer = null

            namazDate.add(Calendar.DAY_OF_MONTH, -1)
            getNamazTimings()
        }

        ivRight.setOnClickListener {
            countDownTimer?.cancel()
            countDownTimer = null

            namazDate.add(Calendar.DAY_OF_MONTH, 1)
            getNamazTimings()
        }

        swFajrAlarm.setOnCheckedChangeListener { button, isActive ->
            val editor = sharedPreferences.edit()
            if (isActive)
            {
                namazAlarmList!!.add(Constants.NAMAZ[0])
            } else
            {
                namazAlarmList!!.remove(Constants.NAMAZ[0])
            }
            editor.putStringSet(Constants.KEY_NAMAZ_ALARMS, namazAlarmList!!.toSet())
            editor.apply()
        }

        swDhuhrAlarm.setOnCheckedChangeListener { button, isActive ->
            val editor = sharedPreferences.edit()
            if (isActive)
            {
                namazAlarmList!!.add(Constants.NAMAZ[1])
            } else
            {
                namazAlarmList!!.remove(Constants.NAMAZ[1])
            }
            editor.putStringSet(Constants.KEY_NAMAZ_ALARMS, namazAlarmList!!.toSet())
            editor.apply()
        }

        swAsrAlarm.setOnCheckedChangeListener { button, isActive ->
            val editor = sharedPreferences.edit()
            if (isActive)
            {
                namazAlarmList!!.add(Constants.NAMAZ[2])
            } else
            {
                namazAlarmList!!.remove(Constants.NAMAZ[2])
            }
            editor.putStringSet(Constants.KEY_NAMAZ_ALARMS, namazAlarmList!!.toSet())
            editor.apply()
        }

        swMaghribAlarm.setOnCheckedChangeListener { button, isActive ->
            val editor = sharedPreferences.edit()
            if (isActive)
            {
                namazAlarmList!!.add(Constants.NAMAZ[3])
            } else
            {
                namazAlarmList!!.remove(Constants.NAMAZ[3])
            }
            editor.putStringSet(Constants.KEY_NAMAZ_ALARMS, namazAlarmList!!.toSet())
            editor.apply()
        }

        swIshaAlarm.setOnCheckedChangeListener { button, isActive ->
            val editor = sharedPreferences.edit()
            if (isActive)
            {
                namazAlarmList!!.add(Constants.NAMAZ[4])
            } else
            {
                namazAlarmList!!.remove(Constants.NAMAZ[4])
            }
            editor.putStringSet(Constants.KEY_NAMAZ_ALARMS, namazAlarmList!!.toSet())
            editor.apply()
        }

        viewModel.namazTimings.observe(viewLifecycleOwner) {
            if (it.data != null)
            {
                if (namazDate.get(Calendar.DAY_OF_MONTH) == Calendar.getInstance()
                        .get(Calendar.DAY_OF_MONTH)
                )
                {
                    startTimerForNextPrayer(it.data.data.timings)
                } else
                {
                    resetLayout(null)
                }

                tvFajr.text = it.data.data.timings.Fajr
                tvDhuhr.text = it.data.data.timings.Dhuhr
                tvAsr.text = it.data.data.timings.Asr
                tvMaghrib.text = it.data.data.timings.Maghrib
                tvIsha.text = it.data.data.timings.Isha

                val date = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(namazDate.time)
                viewModel.getHijriTime(date)

            } else if (it.message != null)
            {
                mainActivity.hideDialog()

                val message = when (it.message)
                {
                    Constants.MSG_TRY_LATER ->
                    {
                        getString(R.string.msg_try_later)
                    }
                    Constants.MSG_CONNECT_INTERNET ->
                    {
                        getString(R.string.msg_connect_internet)
                    }
                    else ->
                    {
                        it.message
                    }
                }

                mainActivity.showSnackBar(message, message == Constants.MSG_CONNECT_INTERNET)
            } else
            {
                mainActivity.showCustomDialog(R.layout.progress_dialog)
            }
        }

        viewModel.hijriTime.observe(viewLifecycleOwner) {
            if (it.data != null)
            {
                mainActivity.hideDialog()

                val hijriDate = it.data.data.hijri
                tvDate.text = " " + hijriDate.day +
                        " " + hijriDate.month.en +
                        " " + hijriDate.year +
                        " " + hijriDate.designation.abbreviated

            } else if (it.message != null)
            {
                mainActivity.hideDialog()

                val message = when (it.message)
                {
                    Constants.MSG_TRY_LATER ->
                    {
                        getString(R.string.msg_try_later)
                    }
                    Constants.MSG_CONNECT_INTERNET ->
                    {
                        getString(R.string.msg_connect_internet)
                    }
                    else ->
                    {
                        it.message
                    }
                }

                mainActivity.showSnackBar(message, message == Constants.MSG_CONNECT_INTERNET)
            } else
            {
                mainActivity.showCustomDialog(R.layout.progress_dialog)
            }
        }
        return view
    }

    @SuppressLint("SetTextI18n")
    fun startTimerForNextPrayer(timings: Timings)
    {
        val prayer = nextPrayer(timings)
        countDownTimer = object : CountDownTimer(prayer.second, 1000)
        {
            override fun onTick(millisUntilFinished: Long)
            {
                if (countDownTimer == null)
                {
                    return
                }
                var seconds = millisUntilFinished / 1000
                var minutes = seconds / 60
                var hours = minutes / 60
                seconds %= 60;
                minutes %= 60;
                hours %= 24;
                tvDuration.text = "${prayer.first} ${getString(R.string.`in`)} ${
                    hours.toString().padStart(2, '0')
                }:" +
                        "${minutes.toString().padStart(2, '0')}:" +
                        seconds.toString().padStart(2, '0')

                if (namazDate.get(Calendar.DAY_OF_MONTH) < Calendar.getInstance()
                        .get(Calendar.DAY_OF_MONTH)
                )
                {
                    countDownTimer?.cancel()
                    countDownTimer = null
                    ivRight.performClick()
                }
            }

            override fun onFinish()
            {
                countDownTimer?.cancel()
                countDownTimer = null
                startTimerForNextPrayer(timings)
            }
        }
        countDownTimer?.start()
        highlightPrayer(prayer.first)
    }

    fun nextPrayer(timings: Timings): Pair<String, Long>
    {
        val prayerTimingMap = mapOf(
            getString(R.string.fajr) to timings.Fajr,
            getString(R.string.dhuhr) to timings.Dhuhr,
            getString(R.string.asr) to timings.Asr,
            getString(R.string.maghrib) to timings.Maghrib,
            getString(R.string.isha) to timings.Isha
        )
        val currentTime = Calendar.getInstance()
        val alarmTime = Calendar.getInstance()
        for (prayerTiming in prayerTimingMap)
        {
            alarmTime.set(Calendar.HOUR_OF_DAY, prayerTiming.value.substring(0, 2).toInt())
            alarmTime.set(Calendar.MINUTE, prayerTiming.value.substring(3).toInt())
            alarmTime.set(Calendar.SECOND, 0)
            if (currentTime.before(alarmTime))
            {
                return prayerTiming.key to alarmTime.timeInMillis - currentTime.timeInMillis
            }
        }
        val fajrTime = prayerTimingMap.entries.first()
        alarmTime.set(Calendar.HOUR_OF_DAY, fajrTime.value.substring(0, 2).toInt())
        alarmTime.set(Calendar.MINUTE, fajrTime.value.substring(3).toInt())
        alarmTime.set(Calendar.SECOND, 0)
        alarmTime.add(Calendar.DAY_OF_MONTH, 1)
        return fajrTime.key to alarmTime.timeInMillis - currentTime.timeInMillis
    }

    fun highlightPrayer(prayer: String)
    {
        when (prayer)
        {
            getString(R.string.fajr) -> resetLayout(llIsha)
            getString(R.string.dhuhr) -> resetLayout(llFajr)
            getString(R.string.asr) -> resetLayout(llDhuhr)
            getString(R.string.maghrib) -> resetLayout(llAsr)
            getString(R.string.isha) -> resetLayout(llMaghrib)
        }
    }

    fun resetLayout(v: LinearLayout?)
    {
        llFajr.setBackgroundColor(mainActivity.getColor(android.R.color.transparent))
        llDhuhr.setBackgroundColor(mainActivity.getColor(android.R.color.transparent))
        llAsr.setBackgroundColor(mainActivity.getColor(android.R.color.transparent))
        llMaghrib.setBackgroundColor(mainActivity.getColor(android.R.color.transparent))
        llIsha.setBackgroundColor(mainActivity.getColor(android.R.color.transparent))

        if (v != null)
        {
            tvDuration.visibility = View.VISIBLE
            v.setBackgroundColor(mainActivity.getColor(R.color.green))
        } else
        {
            tvDuration.visibility = View.GONE
        }
    }

    override fun onStop()
    {
        super.onStop()
        AlarmWorker.updateAlarms(mainActivity, false)
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        countDownTimer?.cancel()
        countDownTimer = null
    }

    companion object
    {
        @JvmStatic
        fun newInstance() = NamazFragment()
    }

    override fun getNamazTimings()
    {
        val geocoder = Geocoder(mainActivity, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            geocoder.getFromLocation(lat, long, 1) { addressList ->
                if (addressList.size > 0)
                {
                    val locality = addressList.first().locality
                    val country = addressList.first().countryName
                    tvAddress.text = " $locality, $country"
                }
            }
        }else{
            val addressList = geocoder.getFromLocation(lat,long,1)
            if (addressList != null && addressList.size > 0)
            {
                val locality = addressList.first().locality
                val country = addressList.first().countryName
                tvAddress.text = " $locality, $country"
            }
        }

        viewModel.getNamazTimings(
            (namazDate.timeInMillis / 1000).toString(),
            lat.toString(),
            long.toString()
        )
    }
}