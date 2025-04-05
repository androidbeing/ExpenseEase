package com.dolphin.expenseease.ui.wallet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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

        binding.fab.setOnClickListener {
            addBalanceDialog()
        }

        return root
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
        viewModel.getLatestBalance().observe(viewLifecycleOwner) { wallet ->
            Log.i("AAA", "Balance Avl: ${Gson().toJson(wallet)}")
        }
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