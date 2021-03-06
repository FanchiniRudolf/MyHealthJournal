package mx.lifehealthsolutions.myhealthjournal.controllers

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_condition.*
import mx.lifehealthsolutions.myhealthjournal.R
import mx.lifehealthsolutions.myhealthjournal.interfaces.ListenerRecycler
import mx.lifehealthsolutions.myhealthjournal.models.AdapterViewEntry
import mx.lifehealthsolutions.myhealthjournal.models.Entry
import com.github.mikephil.charting.data.Entry as ChartEntry
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.collections.ArrayList

class ConditionActivity : AppCompatActivity(), ListenerRecycler {

    private var entries =  ArrayList<Entry>()
    private var chartEntries =  ArrayList<ChartEntry>()
    private var chartEntryMinimum = Float.MAX_VALUE
    private var chartEntryMaximum = Float.MIN_VALUE
    private var adapterEntries: AdapterViewEntry = AdapterViewEntry(entries)
    private lateinit var recyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_condition)

        val condition_name = intent.getStringExtra("CONDITION")
        val condition_description = intent.getStringExtra("DESC")
        tvConditionName.text = condition_name
        tvDetailCondition.text = condition_description

        btn_back2.setOnClickListener{
            finish()
        }

        downloadEntries(condition_name)
        createRecycler()



    }

    private fun createChart() {
        val  chart: LineChart = lcGraph

        if (!entries.isEmpty()) {
            reduceData()
            Log.w("chartmax", (chartEntryMaximum - chartEntryMinimum).toString())
            Log.w("chartmax", (chartEntryMaximum).toString())
            Log.w("chartmini", (chartEntryMinimum).toString())
            Log.w("chart", chartEntries.toString())

            val data = LineDataSet(chartEntries, "Histórico")
            data.setDrawValues(false)
            data.color = Color.GREEN
            data.fillColor = Color.GREEN
            data.setDrawFilled(true)
            data.cubicIntensity = 1f

            chart.data = LineData(data)
            chart.description.text = "Severidad"
            chart.animateX(1000, Easing.EaseInCubic)
            chart.xAxis.axisMaximum = chartEntryMaximum + 100
            chart.xAxis.axisMinimum = chartEntryMinimum - 100
            chart.xAxis.setDrawLabels(false)
            chart.axisRight.axisMaximum = 3.5f
            chart.axisRight.axisMinimum = -0.1f
            chart.axisLeft.axisMaximum = 3.5f
            chart.axisLeft.axisMinimum = -0.1f
            chart.axisLeft.setDrawLabels(false)
        }

    }

    private fun reduceData() {
        val todaySeconds = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).toEpochSecond(ZoneOffset.MIN)
        val lastMonthSeconds = LocalDateTime.now().minusMonths(1).toEpochSecond(ZoneOffset.MIN)
        val month = ArrayList<Long>()
        val secondsInADay = 86400 //60*60*24
        var currentDay = lastMonthSeconds
        var chartEntriesMonth =  ArrayList<ChartEntry>()
        val averageSeverityArray = ArrayList<ArrayList<Float>>()

        while (currentDay < todaySeconds){
            month.add(currentDay)
            averageSeverityArray.add(ArrayList<Float>())
            chartEntriesMonth.add(ChartEntry(currentDay +0f, 0f))
            currentDay += secondsInADay
        }


        for (chartEntry in chartEntries){
            val seconds = chartEntry.x
            val severity = chartEntry.y
            var day = 0
            if (seconds > lastMonthSeconds){
                while (seconds > month[day]){
                        day++
                }
                averageSeverityArray[day-1].add(severity)
            }
        }
        chartEntries.removeAll(chartEntries)

        for (dayIndex in 0..month.size-1){
            val average: Float
            if (averageSeverityArray[dayIndex].size!=0){
                average = averageSeverityArray[dayIndex].average().toFloat()
            }else{
                average = 0f
            }
            val chartEntry = ChartEntry(month[dayIndex]+0f, average)
            chartEntries.add(chartEntry)
        }
    }

    private fun createRecycler() {

        adapterEntries?.listener = this


        val layout = LinearLayoutManager(this)
        layout.orientation = LinearLayoutManager.VERTICAL

        val lineDivisor = DividerItemDecoration(this, layout.orientation)

        recyclerView = recyclerEntries
        recyclerView.layoutManager = layout
        recyclerView.adapter = adapterEntries
        recyclerView.addItemDecoration(lineDivisor)

    }

    fun downloadEntries(condition_name: String){
        val db = FirebaseFirestore.getInstance()
        var userStr =  FirebaseAuth.getInstance().getCurrentUser()?.email
        val conditionRef =  db.collection("Users/$userStr/Conditions/").document("$condition_name")

        conditionRef.get()
            .addOnSuccessListener { document ->
                if (document != null){
                    //print(typeOf(document.data))


                    db.collection("Users/$userStr/Conditions/$condition_name/Entries")
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                print("-------")
                                print("$document.data")
                                entries.add(Entry(condition_name, document.data.get("date") as String,
                                    document.data.get("time") as String,
                                    document.data.get("severity") as Long,
                                    document.data.get("description") as String,
                                    document.data.get("event-time") as Long
                                ))
                                addEntry(document.data.get("severity").toString().toFloat(), document.data.get("date")  as String, document.data.get("time") as String)
                            }
                            createChart()
                            print("--------------")
                            Log.d("entries", "TAMAÑO: ${entries.size}")
                            print(entries.toString())
                            updateRecycler()
                        }

                }
                else{
                    //Empty Recycler
                    print("none")
                }
            }

    }


    private fun addEntry(severity: Float, date: String, time: String) {
        val dateArr = date.split("-").toMutableList()
        val timeArr = time.split(":").toMutableList()
        if (dateArr[1].length == 1) dateArr[1] = "0${dateArr[1]}"
        if (dateArr[2].length == 1) dateArr[2] = "0${dateArr[2]}"
        if (timeArr[0].length == 1) timeArr[0] = "0${timeArr[0]}"
        if (timeArr[1].length == 1) timeArr[1] = "0${timeArr[1]}"
        val dateTime = "${dateArr[0]}-${dateArr[1]}-${dateArr[2]}T${timeArr[0]}:${timeArr[1]}:00" //concatenation
        val date = LocalDateTime.parse(dateTime)
        val seconds = date.toEpochSecond(ZoneOffset.MIN).toString().toFloat()
        if (seconds < chartEntryMinimum) chartEntryMinimum = seconds
        if (seconds > chartEntryMaximum) chartEntryMaximum = seconds
        var entry = ChartEntry(seconds, severity)
        chartEntries.add(entry)
    }

    private fun updateRecycler() {
        adapterEntries?.arrEntradas = entries
        adapterEntries?.notifyDataSetChanged()
    }

    override fun itemClicked(position: Int) {
        val intentConditionActivity = Intent(this, EntryDataActiv::class.java)
        val conditionDate = adapterEntries?.arrEntradas?.get(position)?.date
        val conditionTime = adapterEntries?.arrEntradas?.get(position)?.time

        intentConditionActivity.putExtra("CONDITION", intent.getStringExtra("CONDITION"))
        intentConditionActivity.putExtra("ENTRY", "${conditionDate}-${conditionTime}")

        startActivity(intentConditionActivity)
    }
}
