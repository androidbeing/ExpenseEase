package com.dolphin.expenseease.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dolphin.expenseease.data.db.expense.Expense
import com.dolphin.expenseease.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var expenseList: MutableList<Expense>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        expenseList = mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        initViews()
        return root
    }

    private fun initViews() {
        expenseAdapter = ExpenseAdapter(requireContext(), expenseList)
        binding.recyclerExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerExpenses.adapter = expenseAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.allExpenses.observe(viewLifecycleOwner) {
            expenseList.clear()
            expenseList.addAll(it)
            expenseAdapter.notifyDataSetChanged()
            setView(expenseList.isNotEmpty())
        }
        setView(expenseList.isNotEmpty())
    }

    private fun setView(hasItems: Boolean) {
        binding.textNoItems.visibility = if (hasItems) View.GONE else View.VISIBLE
        binding.recyclerExpenses.visibility = if (hasItems) View.VISIBLE else View.GONE
    }

    private fun addExpense() {
        // Create a new Expense object
        val expense = Expense(
            amount = 100.0,
            type = "Food",
            notes = "Lunch",
            date = "07/06/2024",
            createdAt = Date().time,
            updatedAt = Date().time
        )

        // Insert the expense into the database
        viewModel.addExpense(expense)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}