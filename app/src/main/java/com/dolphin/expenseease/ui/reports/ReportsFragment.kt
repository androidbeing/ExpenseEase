package com.dolphin.expenseease.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dolphin.expenseease.databinding.FragmentReportsBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ReportsFragment : Fragment() {
    private val viewModel: ReportsViewModel by viewModels()
    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupCharts()
        observeData()

        return root
    }

    private fun setupCharts() {
        // Setup Bar Chart
        setupBarChart(binding.barChart)

        // Setup Pie Chart
        setupPieChart(binding.pieChart)
    }

    private fun observeData() {
        // Observe wallet balance
        viewModel.getLatestWallet().observe(viewLifecycleOwner) { wallet ->
            wallet?.let {
                viewModel.setCurrentBalance(it.balance)
                updatePieChart(it.balance)
            }
        }

        // Fetch monthly data
        viewModel.fetchMonthlyData()

        // Observe monthly expenses
        viewModel.monthlyExpenses.observe(viewLifecycleOwner) { expenses ->
            viewModel.currentBalance.value?.let { balance ->
                updatePieChart(balance)
            }
        }

        // Observe daily expenses
        viewModel.getDailyExpensesForCurrentMonth().observe(viewLifecycleOwner) { dailyExpenses ->
            updateBarChart(dailyExpenses)
        }
    }

    private fun setupBarChart(barChart: BarChart) {
        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setMaxVisibleValueCount(31)
            setPinchZoom(false)
            setDrawGridBackground(false)

            // X-axis configuration
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelRotationAngle = -45f
                textSize = 9f
            }

            // Y-axis configuration
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                textSize = 10f
            }

            axisRight.isEnabled = false
            legend.isEnabled = false
            animateY(1000)
        }
    }

    private fun setupPieChart(pieChart: PieChart) {
        pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(false)
            setDrawEntryLabels(true)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            centerText = "This Month"
            setCenterTextSize(16f)
            setHoleRadius(45f)
            setTransparentCircleRadius(50f)

            legend.isEnabled = true
            legend.textSize = 12f

            animateY(1000)
        }
    }

    private fun updateBarChart(dailyExpenses: List<com.dolphin.expenseease.data.db.expense.DailyExpense>) {
        if (dailyExpenses.isEmpty()) {
            binding.barChart.clear()
            binding.barChart.invalidate()
            return
        }

        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        dailyExpenses.forEachIndexed { index, expense ->
            entries.add(BarEntry(index.toFloat(), expense.totalAmount.toFloat()))
            // Extract day from date (assuming date format is YYYY-MM-DD)
            val day = try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(expense.date)
                val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
                dayFormat.format(date ?: Date())
            } catch (e: Exception) {
                expense.date.takeLast(2)
            }
            labels.add(day)
        }

        val dataSet = BarDataSet(entries, "Daily Expenses")
        dataSet.apply {
            color = Color.parseColor("#4CAF50")
            valueTextColor = Color.BLACK
            valueTextSize = 9f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value > 0) "₹${value.toInt()}" else ""
                }
            }
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.8f

        binding.barChart.apply {
            data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size.coerceAtMost(10)
            invalidate()
        }
    }

    private fun updatePieChart(balance: Double) {
        val spent = viewModel.monthlyExpenses.value ?: 0.0

        // Update text views
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        binding.textBalance.text = "Balance: ${numberFormat.format(balance)}"
        binding.textSpent.text = "Spent: ${numberFormat.format(spent)}"

        if (balance <= 0 && spent <= 0) {
            binding.pieChart.clear()
            binding.pieChart.invalidate()
            return
        }

        val entries = mutableListOf<PieEntry>()

        if (balance > 0) {
            entries.add(PieEntry(balance.toFloat(), "Balance"))
        }
        if (spent > 0) {
            entries.add(PieEntry(spent.toFloat(), "Spent"))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.apply {
            colors = listOf(
                Color.parseColor("#4CAF50"), // Green for balance
                Color.parseColor("#F44336")  // Red for spent
            )
            valueTextColor = Color.WHITE
            valueTextSize = 14f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "₹${value.toInt()}"
                }
            }
        }

        val pieData = PieData(dataSet)
        binding.pieChart.apply {
            data = pieData
            highlightValues(null)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}