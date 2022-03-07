package com.leobit.pizzadelivery.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ServiceViewModel  : ViewModel(){
   var isRunnin : MutableLiveData<Boolean> = MutableLiveData<Boolean>(true)
}