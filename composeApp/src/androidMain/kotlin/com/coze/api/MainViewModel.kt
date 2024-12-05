//package com.coze.api
//
//import com.coze.api.demo.ChatDemo
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.update
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import androidx.lifecycle.viewModelScope
//
//class MainViewModel : ViewModel() {
//    private val _greetingList = MutableStateFlow<List<String>>(listOf())
//    val greetingList: StateFlow<List<String>> get() = _greetingList
//    private val chatDemo = ChatDemo()
//
//    fun generateStream(prompt: String) {
//        viewModelScope.launch {
//            var response = ""
//            _greetingList.update { listOf() }
//            _greetingList.update { list -> list + "[Stream Chat]" + response }
//
//            chatDemo.streamTest(prompt).collect { phrase ->
//                response += phrase
//                _greetingList.update { list -> list.dropLast(1) + response }
//            }
////
////            _greetingList.update { list -> list + "---" + "[Create Chat]" }
////            chatDemo.noneStreamCreate().collect { phrase ->
////                _greetingList.update { list -> list + phrase }
////            }
////
////            _greetingList.update { list -> list + "---" + "Create Chat and Poll." }
////            chatDemo.noneStreamCreateAndPoll().collect { phrase ->
////                _greetingList.update { list -> list + phrase }
////            }
//        }
//    }
//
//    fun generateNonStream(prompt: String) {
//        viewModelScope.launch {
//            _greetingList.update { listOf() }
//            _greetingList.update { list -> list + "[None Stream Chat]" }
//
////            _greetingList.update { list -> list + "---" + "[Create Chat]" }
////            chatDemo.noneStreamCreate().collect { phrase ->
////                _greetingList.update { list -> list + phrase }
////            }
//
//            _greetingList.update { list -> list + "---" + "Create Chat and Poll." }
//            chatDemo.noneStreamCreateAndPoll(prompt).collect { phrase ->
//                _greetingList.update { list -> list + phrase }
//            }
//        }
//    }
//}