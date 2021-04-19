package com.jaimeyaxchealmazanpardo.exchangerateapp.viewmodels



import android.util.Log
import androidx.lifecycle.*
import com.jaimeyaxchealmazanpardo.exchangerateapp.api.responses.Rates
import com.jaimeyaxchealmazanpardo.exchangerateapp.repositories.CurrencyRepository
import com.jaimeyaxchealmazanpardo.exchangerateapp.utils.CurrencyLoadState
import com.yabu.livechart.model.DataPoint
import com.yabu.livechart.model.Dataset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository : CurrencyRepository) : ViewModel() {

    private val TAG = "MainViewModel"
    private val DATE_PATTERN = "yyyy-MM-dd"
    private val TIME_ZONE = "GMT"
    private val _exchangeRatesLiveData = MutableLiveData<Dataset>()
    val exchangeRatesValues : LiveData<Dataset>
    get() = _exchangeRatesLiveData
    private val _starDateLiveData = MutableLiveData<String>()
    val starDateLiveData : LiveData<String>
    get() = _starDateLiveData
    private val _endDateLiveData = MutableLiveData<String>()
    val endDateLiveData : LiveData<String>
    get() = _endDateLiveData
    private val _typeRate = MutableLiveData<Int>()
    val typeRate : LiveData<Int>
    get() = _typeRate
    private val _currencyDataInTime = MutableLiveData<Pair<String,Double>>()
    val currencyDataInTime : LiveData<Pair<String,Double>>
    get() = _currencyDataInTime
    private val _singleDateExchangeCurrency = MutableLiveData<Pair<String,Rates>>()
    val singleDateExchangeCurrency : LiveData<Pair<String,Rates>>
    get() = _singleDateExchangeCurrency
    private val _graphVisibility = MutableLiveData<Boolean>()
    val graphVisibility : LiveData<Boolean>
    get() = _graphVisibility
    private val _currencyLoadState = MutableLiveData<CurrencyLoadState>()
    val currencyLoadState : LiveData<CurrencyLoadState>
    get() = _currencyLoadState

    private val simpleDateFormat = SimpleDateFormat(DATE_PATTERN).apply {
        timeZone =  TimeZone.getTimeZone(TIME_ZONE)
    }
    private lateinit var rates : Map<String,Rates>
    var checkedItem = -1
    private set
    lateinit var currencyList : Array<String>

    init {
        //Select EUR-USD for default exchange rate currency
        setTypeRate(0)
    }

    //Consume API function
    private fun getCurrency(rangesDates : String,base :Int) = viewModelScope.launch(Dispatchers.IO) {
        try{
            _currencyLoadState.postValue(CurrencyLoadState.LOADING)
            val data = repository.getCurrency(date = rangesDates, base = currencyList[base], target = currencyList[0].takeIf { currencyList[1] == currencyList[base] } ?: currencyList[1])
            _currencyLoadState.postValue(CurrencyLoadState.LOAD)
            data?.rates?.let {
                setExchangeRates(it)
                _currencyDataInTime.postValue(Pair(it.entries.first().key,it.entries.first().value.EUR.takeIf { checkedItem == 1 } ?: it.entries.first().value.USD))
            }
        }catch (e : Exception){
            e.printStackTrace()
            _currencyLoadState.postValue(CurrencyLoadState.ERROR)
        }
    }

    //Get start date and end date for consume service
    fun setDateRange(startDate : Long?, endDate : Long?){
        val cal = Calendar.getInstance()
        cal.timeInMillis = startDate ?: 0L
        if(startDate != endDate){
            cal.add(Calendar.DAY_OF_MONTH,-1)
        }
        val formattedStartDate = simpleDateFormat.format(cal.timeInMillis) ?: ""
        val formattedEndDate = simpleDateFormat.format(endDate) ?: ""
        if(formattedStartDate.isNotEmpty() && formattedEndDate.isNotEmpty()){
            _starDateLiveData.value = formattedStartDate
            _endDateLiveData.value = formattedEndDate
            getCurrency(formatDateForService(formattedStartDate,formattedEndDate),checkedItem)
        }
    }

    //Set data for draw points in chart
    private fun setExchangeRates(rates : Map<String,Rates>){
        if (rates.size == 1){
            _graphVisibility.postValue(false)
            _singleDateExchangeCurrency.postValue(Pair(rates.entries.first().key,rates.entries.first().value))
            return
        }
        _graphVisibility.postValue(true)
        this.rates = rates
        val dataPoints = mutableListOf<DataPoint>()
        var x = 0
        rates.forEach {
            dataPoints.add(DataPoint(x++.toFloat(),it.value.EUR.toFloat().takeIf { checkedItem == 1 } ?: it.value.USD.toFloat()))
        }
        _exchangeRatesLiveData.postValue(Dataset(dataPoints))
    }

    fun setTypeRate(item : Int){
        _typeRate.value = item
        checkedItem = item
    }

    //Get data from points to show data per point in chart
    fun setCurrencyDateIntTime(point : DataPoint){
        rates.onEachIndexed { index, entry ->
            if(index == point.x.toInt()){
                _currencyDataInTime.value = Pair(entry.key,entry.value.EUR.takeIf { checkedItem == 1 } ?: entry.value.USD)

            }
        }
    }

    private fun formatDateForService(startDate: String,endDate: String) = "$startDate..$endDate"

    fun refreshData(){
        _starDateLiveData.value?.let { startDate ->
            _endDateLiveData.value?.let { endDate ->
                if(startDate.isNotEmpty() && endDate.isNotEmpty()){
                    getCurrency(formatDateForService(startDate,endDate),checkedItem)
                }
            }
        }
    }
}