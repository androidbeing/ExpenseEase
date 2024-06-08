package com.dolphin.expenseease.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dolphin.expenseease.data.db.Expense
import com.dolphin.expenseease.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        viewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        binding.btnAddExpense.setOnClickListener {
            addExpense()
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.allExpenses.observe(viewLifecycleOwner) {
            Log.i("AAA", it.toString())
        }
    }

    private fun addExpense() {
        // Create a new Expense object
        val expense = Expense(amount = 100.0, type = "Food", notes = "Lunch", date = "07/06/2024", createdAt = Date().time, updatedAt = Date().time)

        // Insert the expense into the database
        viewModel.insertExpense(expense)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}