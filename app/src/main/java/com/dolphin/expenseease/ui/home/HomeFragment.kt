package com.dolphin.expenseease.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dolphin.expenseease.R
import com.dolphin.expenseease.data.db.expense.Expense
import com.dolphin.expenseease.data.model.Alert
import com.dolphin.expenseease.databinding.FragmentHomeBinding
import com.dolphin.expenseease.listeners.AddExpenseListener
import com.dolphin.expenseease.listeners.ExpenseEditListener
import com.dolphin.expenseease.listeners.OnClickAlertListener
import com.dolphin.expenseease.ui.main.MainViewModel
import com.dolphin.expenseease.utils.DialogUtils.showAlertDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var expenseList: MutableList<Expense>
    private var updateIndex: Int = -1

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

    private fun initObservers() {
        viewModel.allExpenses.observe(viewLifecycleOwner) {
            expenseList.clear()
            expenseList.addAll(it.sortedByDescending { expense -> expense.createdAt })
            expenseAdapter.notifyDataSetChanged()
            setView(expenseList.isNotEmpty())
        }

        binding.fab.setOnClickListener {
            addExpenseDialog()
        }
    }

    private fun addExpenseDialog(expenseToUpdate: Expense? = null) {
        val addExpenseBottomSheet = AddExpenseSheet(expenseToUpdate, object : AddExpenseListener {
            override fun onExpenseAdd(expense: Expense) {
                if(expenseToUpdate != null) {
                    requireActivity().runOnUiThread {
                        expenseList[updateIndex] = expense
                        expenseAdapter.notifyItemChanged(updateIndex)
                        viewModel.updateExpense(expense)
                    }
                } else {
                    viewModel.addExpense(expense)
                }
            }
        })
        addExpenseBottomSheet.show(childFragmentManager, AddExpenseSheet.TAG)
    }

    private fun initViews() {
        expenseAdapter = ExpenseAdapter(requireContext(), expenseList, object: ExpenseEditListener {
            override fun onExpenseEdit(expense: Expense, index: Int) {
                updateIndex = index
                addExpenseDialog(expense)
            }

            override fun onExpenseRemove(expense: Expense, index: Int) {
                val alert = Alert(getString(R.string.delete), getString(R.string.del_msg))
                showAlertDialog(requireContext(), alert, object: OnClickAlertListener {
                    override fun onAcknowledge(isOkay: Boolean) {
                        if(isOkay) {
                            requireActivity().runOnUiThread {
                                expenseList.remove(expense)
                                expenseAdapter.notifyItemRemoved(index)
                                viewModel.deleteExpense(expense)
                            }
                        }
                    }
                })
            }
        })
        binding.recyclerExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerExpenses.adapter = expenseAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
    }

    private fun setView(hasItems: Boolean) {
        binding.textNoItems.visibility = if (hasItems) View.GONE else View.VISIBLE
        binding.recyclerExpenses.visibility = if (hasItems) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}