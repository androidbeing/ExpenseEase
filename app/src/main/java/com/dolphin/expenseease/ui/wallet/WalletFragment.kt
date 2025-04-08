package com.dolphin.expenseease.ui.wallet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dolphin.expenseease.data.db.wallet.MyWallet
import com.dolphin.expenseease.databinding.FragmentWalletBinding
import com.dolphin.expenseease.listeners.AddBalanceListener
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WalletFragment : Fragment() {

    private val viewModel: WalletViewModel by viewModels()
    private var _binding: FragmentWalletBinding? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var balanceAdapter: BalanceAdapter
    private lateinit var balanceList: MutableList<MyWallet>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        val root: View = binding.root
        initViews()
        return root
    }

    private fun initViews() {
        balanceList = mutableListOf()
        balanceAdapter = BalanceAdapter(requireContext(), balanceList)
        binding.recyclerBalance.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerBalance.adapter = balanceAdapter

        binding.fab.setOnClickListener {
            addBalanceDialog()
        }
    }

    private fun addBalanceDialog() {
        val addBalanceBottomSheet = AddBalanceSheet(object : AddBalanceListener {
            override fun onBalanceAdd(addedAmount: Double, notes: String) {
                coroutineScope.launch {
                    val wallet = viewModel.getLatestBalance().value
                    val newBalanceValue = wallet?.balance?.plus(addedAmount)
                    val newBalance = MyWallet(
                        id = wallet?.id ?: 0,
                        balance = newBalanceValue ?: 0.0,
                        addedAmount = addedAmount,
                        notes = notes,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    viewModel.addBalance(newBalance)
                }
            }
        })
        addBalanceBottomSheet.show(childFragmentManager, AddBalanceSheet.TAG)
    }

    private fun initObservers() {
        viewModel.allBalances.observe(viewLifecycleOwner) { balances ->
            Log.i("AAA", "Balance Avl: ${Gson().toJson(balances)}")
            balanceList.clear()
            balanceList.addAll(balances)
            balanceAdapter.notifyDataSetChanged()
            setView(balanceList.isNotEmpty())
        }
    }

    private fun setView(hasItems: Boolean) {
        binding.txtNoItems.visibility = if (hasItems) View.GONE else View.VISIBLE
        binding.recyclerBalance.visibility = if (hasItems) View.VISIBLE else View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}