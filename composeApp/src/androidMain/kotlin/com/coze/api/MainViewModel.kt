package com.coze.api

import androidx.lifecycle.ViewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import com.coze.api.demo.ChatDemo
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {
    private val _greetingList = MutableStateFlow<List<String>>(listOf())
    val greetingList: StateFlow<List<String>> get() = _greetingList

    init {
        viewModelScope.launch {
            val chatDemo = ChatDemo()
            var response = ""
            _greetingList.update { list -> list + "[Stream Chat]" + response }

            chatDemo.streamTest().collect { phrase ->
                response += phrase
                // println(response)
                _greetingList.update { list -> list.dropLast(1) + response }
            }

            _greetingList.update { list -> list + "---" + "[Create Chat]" }
            chatDemo.noneStreamCreate().collect { phrase ->
                _greetingList.update { list -> list + phrase }
            }

            _greetingList.update { list -> list + "---" + "Create Chat and Poll." }
            chatDemo.noneStreamCreateAndPoll().collect { phrase ->
                _greetingList.update { list -> list + phrase }
            }
        }
    }
}
