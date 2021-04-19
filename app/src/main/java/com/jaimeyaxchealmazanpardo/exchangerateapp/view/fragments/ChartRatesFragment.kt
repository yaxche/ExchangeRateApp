package com.jaimeyaxchealmazanpardo.exchangerateapp.view.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Parcel
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.jaimeyaxchealmazanpardo.exchangerateapp.R
import com.jaimeyaxchealmazanpardo.exchangerateapp.databinding.FragmentChartRatesBinding
import com.jaimeyaxchealmazanpardo.exchangerateapp.utils.ConnectivityManangerLiveData
import com.jaimeyaxchealmazanpardo.exchangerateapp.utils.CurrencyLoadState
import com.jaimeyaxchealmazanpardo.exchangerateapp.viewmodels.MainViewModel
import com.yabu.livechart.model.DataPoint
import com.yabu.livechart.view.LiveChart
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class ChartRatesFragment : Fragment() {

    private val TAG = "ChartRatesFragment"
    private val START_DIALOG = "START_DATE_DIALOG"
    private val viewModel : MainViewModel by viewModels()
    private lateinit var binding : FragmentChartRatesBinding
    private val materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Pick a range of dates")
            .setCalendarConstraints(CalendarConstraints.Builder()
                    .setValidator(object :CalendarConstraints.DateValidator{
                        override fun describeContents() = 0
                        override fun writeToParcel(p0: Parcel?, p1: Int) {}
                        override fun isValid(date: Long) = date < Calendar.getInstance().timeInMillis
                    })
                    .setEnd(Calendar.getInstance().timeInMillis)
                    .build()
    ).build()
    private lateinit var connectivityLiveData : ConnectivityManangerLiveData
    private lateinit var snackbarNoIternetConnection : Snackbar


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_chart_rates,container,false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        connectivityLiveData = ConnectivityManangerLiveData(requireActivity().application)
        initView()
        initObservers()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        snackbarNoIternetConnection = Snackbar.make(requireView(),getString(R.string.error_network),Snackbar.LENGTH_INDEFINITE).setBackgroundTint(Color.RED)
    }

    private fun initView(){
        viewModel.currencyList = resources.getStringArray(R.array.rates)
        binding.chartRates.drawYBounds()
            .drawBaseline()
            .drawVerticalGuidelines(steps=1)
            .drawHorizontalGuidelines(steps=1)
            .drawFill()

        materialDatePicker.addOnPositiveButtonClickListener {
            viewModel.setDateRange(it.first,it.second)
        }

        binding.chartRates.setOnTouchCallbackListener(object : LiveChart.OnTouchCallback{
            override fun onTouchCallback(point: DataPoint) {
                viewModel.setCurrencyDateIntTime(point)
            }
            override fun onTouchFinished() {}
        })

    }

    private fun initObservers(){
        //for draw chart
        viewModel.exchangeRatesValues.observe(viewLifecycleOwner){dataSetPoint ->
            binding.chartRates.setDataset(dataSetPoint).drawDataset()
        }
        //for change currency textview
        viewModel.typeRate.observe(viewLifecycleOwner){
            binding.tvExchangeRate.text = resources.getStringArray(R.array.currency_exchange)[it]
        }
        //for show or hide chart
        viewModel.graphVisibility.observe(viewLifecycleOwner){visible ->
            binding.chartRates.visibility = View.VISIBLE.takeIf { visible } ?: View.GONE
            binding.tvInstructionsSingleDateCurrency.visibility = View.VISIBLE.takeIf { !visible } ?: View.GONE
        }
        //for single day selected in datepicker
        viewModel.singleDateExchangeCurrency.observe(viewLifecycleOwner){
            val currencyType = it.second.EUR.takeIf { viewModel.checkedItem == 1 } ?: it.second.USD
            binding.tvInstructionsSingleDateCurrency.text = String.format(resources.getString(R.string.singleDay_currency),it.first,currencyType)

        }
        //for show information per day on touch chart
        viewModel.currencyDataInTime.observe(viewLifecycleOwner){
            binding.tvAmount.text = getString(R.string.amount,viewModel.currencyList[viewModel.checkedItem])
            binding.tvDateInTime.text = getString(R.string.date_in_time,it.first)
            binding.tvExchangeRateInTime.text = String.format(getString(R.string.currency_in_time),it.second,resources.getStringArray(R.array.rates)[0].takeIf { viewModel.checkedItem==1 } ?: resources.getStringArray(R.array.rates)[1])
        }
        //for handle states of data(LOAD, LOADED, ERROR)
        viewModel.currencyLoadState.observe(viewLifecycleOwner){
            onCurrencyLoadState(it)
        }
        //for handle connectivity
        connectivityLiveData.observe(viewLifecycleOwner){ isAvailable ->
            Log.d(TAG,"$isAvailable")

            when(isAvailable){
                true -> {
                    snackbarNoIternetConnection.dismiss()
                }
                false -> {
                    snackbarNoIternetConnection.show()
                }
                else -> {}
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.add_range_dates -> {
                materialDatePicker.show(requireActivity().supportFragmentManager,START_DIALOG)
                true
            }
            R.id.exchange_rates -> {
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.select_exchange_rate))
                        .setSingleChoiceItems(resources.getStringArray(R.array.rates),viewModel.checkedItem){
                            dialog, item ->
                            viewModel.setTypeRate(item)
                            viewModel.refreshData()
                            dialog.dismiss()
                        }.show()
                true
            }
            else -> {super.onOptionsItemSelected(item)}
        }
    }

    private fun onCurrencyLoadState(state : CurrencyLoadState){
        when(state){
            CurrencyLoadState.LOADING -> {
                binding.chartRates.visibility = View.GONE
                binding.loadingDataProgressbar.visibility = View.VISIBLE
                binding.tvInstructionsSingleDateCurrency.visibility = View.GONE
            }
            CurrencyLoadState.LOAD -> {
                binding.loadingDataProgressbar.visibility = View.GONE
            }
            CurrencyLoadState.ERROR -> {
                binding.loadingDataProgressbar.visibility = View.GONE
                binding.tvInstructionsSingleDateCurrency.visibility = View.VISIBLE
                binding.tvInstructionsSingleDateCurrency.text = getString(R.string.error_occured)
                Snackbar.make(requireView(),getString(R.string.error_occured),Snackbar.LENGTH_SHORT).show()
            }
        }
    }

}