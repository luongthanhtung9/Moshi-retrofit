/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.android.marsrealestate.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.marsrealestate.network.MarsApi
import com.example.android.marsrealestate.network.MarsApiFilter
import com.example.android.marsrealestate.network.MarsProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*


enum class MarsApiStatus { LOADING, ERROR, DONE }

/**
 * The [ViewModel] that is attached to the [OverviewFragment].
 */
class OverviewViewModel : ViewModel() {

    private val _response = MutableLiveData<List<MarsProperty>>()
    private val _status = MutableLiveData<MarsApiStatus>()

    val response: LiveData<List<MarsProperty>>
        get() = _response

    val status: LiveData<MarsApiStatus>
        get() = _status

    private var viewModelJob = Job()

    private var uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _navigateToSelectProperty = MutableLiveData<MarsProperty>()

    val navigateToSelectProperty: LiveData<MarsProperty>
        get() = _navigateToSelectProperty

    init {
        getMarsRealEstateProperties(MarsApiFilter.SHOW_ALL)
    }

    private fun getMarsRealEstateProperties(filter: MarsApiFilter) {

        uiScope.launch {
            val getPropertiesDeffred = MarsApi.retrofitService.getProperties(filter.value)
            try {
                _status.value = MarsApiStatus.LOADING
                val listResult = getPropertiesDeffred.await()
                if (listResult.isNotEmpty()) {
                    _response.value = listResult
                    _status.value = MarsApiStatus.DONE
                }
            } catch (t: Throwable) {
                _status.value = MarsApiStatus.ERROR
                _response.value = ArrayList()
            }

        }

    }

    fun displayPropertyDetail(marsProperty: MarsProperty) {
        _navigateToSelectProperty.value = marsProperty
    }

    fun displayPropertyDetailComplete() {
        _navigateToSelectProperty.value = null
    }

    fun updateFilter(filter: MarsApiFilter) {
        getMarsRealEstateProperties(filter)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
