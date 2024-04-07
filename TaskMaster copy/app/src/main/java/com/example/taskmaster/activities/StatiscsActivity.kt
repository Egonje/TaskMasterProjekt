package com.example.taskmaster.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.taskmaster.R
import com.example.taskmaster.firebase.FirebaseClass
import com.example.taskmaster.models.UserModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class StatiscsActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var firebaseClass: FirebaseClass
    private lateinit var animationView: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statiscs)
        setupActionBar()

        pieChart = findViewById(R.id.chart)
        firebaseClass = FirebaseClass()

        fetchDataAndUpdateChart()
    }

    private fun setupActionBar() {
        val toolbarMA: Toolbar = findViewById(R.id.toolbar_stat_activity)
        setSupportActionBar(toolbarMA)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_white_ios_24)
            actionBar.title = "Statistics"
        }
        toolbarMA.setNavigationOnClickListener {
            finish()
        }
    }

    private fun fetchDataAndUpdateChart() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val userModel = documentSnapshot.toObject<UserModel>()
                    if (userModel != null) {
                        val createdTasks = userModel.createdTasks
                        val completedTasks = userModel.completedTasks
                        updateChart(createdTasks, completedTasks)
                    }
                }
                .addOnFailureListener { e ->
                    // Failed to fetch data
                }
        }
    }

    private fun updateChart(createdTasks: Int, completedTasks: Int) {
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(createdTasks.toFloat(), "Tasks"))
        entries.add(PieEntry(completedTasks.toFloat(), "Completed"))

        val colors = ArrayList<Int>()
        colors.add(ContextCompat.getColor(this, R.color.colorPrimaryLight2)) // Boja za "Created Tasks"
        colors.add(ContextCompat.getColor(this, R.color.colorPrimary)) // Boja za "Completed Tasks"

        val dataSet = PieDataSet(entries, "") // Prazan string za tumač
        dataSet.colors = colors
        dataSet.sliceSpace = 3f // Postavljanje razmaka između vrijednosti
        dataSet.valueTextColor = ContextCompat.getColor(this, R.color.divider_color) // Postavljanje boje teksta tumača
        dataSet.valueTextSize = 12f // Postavljanje velicine teksta tumaca
        dataSet.valueFormatter = PercentFormatter(pieChart)// Postavljanje razmaka između vrijednosti

        val data = PieData(dataSet)

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.animateY(1000)
        pieChart.isDrawHoleEnabled = false // Postavljanje da ne bude rupa u sredini

        val params = pieChart.layoutParams
        params.width = 1000
        params.height = 1000
        pieChart.layoutParams = params
        pieChart.invalidate()
        val difference = createdTasks - completedTasks

        if (difference <= 3 && completedTasks != 0 && completedTasks != createdTasks) {
            dialogDoingGreat()
        }
        else {
            dialogCanDoBetter()
        }
        if (completedTasks == createdTasks) {
            val intent = Intent(this, AllCompletedTasks::class.java)
            intent.putExtra("floatValue1", createdTasks.toFloat())
            intent.putExtra("floatValue2", completedTasks.toFloat())
            startActivity(intent)
            finish()
        }
    }

    private fun dialogCanDoBetter() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.can_do_better_dialog)

        val animationView = dialog.findViewById<LottieAnimationView>(R.id.animateViewCanDoBetter)
        val close = dialog.findViewById<TextView>(R.id.btn_close_can_do_better)

        animationView.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                animationView.postDelayed({
                    animationView.playAnimation()
                }, 500)
            }
        })

        close.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        animationView.playAnimation()
    }

    private fun dialogDoingGreat() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.doing_great_dialog)

        val animationView1 = dialog.findViewById<LottieAnimationView>(R.id.animateView)
        val close = dialog.findViewById<TextView>(R.id.btn_close)

        animationView1.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                animationView1.postDelayed({
                    animationView1.playAnimation()
                }, 500)
            }
        })

        close.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        animationView1.playAnimation()
    }
}