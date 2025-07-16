package com.example.trat.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 모든 ViewModel의 기본 클래스
 * 
 * ## 제공 기능
 * - 공통 UI 상태 관리 (로딩, 에러)
 * - 표준화된 에러 처리 패턴
 * - 코루틴 기반 안전한 비동기 작업 실행
 * - 자동 로딩 상태 관리
 * 
 * ## 사용법
 * ```kotlin
 * class MyViewModel : BaseViewModel<MyUiState>() {
 *     fun doSomething() {
 *         launchSafely { 
 *             // 비즈니스 로직
 *         }
 *     }
 * }
 * ```
 * 
 * @param T UI 상태 타입 (BaseUiState를 구현해야 함)
 */
abstract class BaseViewModel<T : BaseUiState> : ViewModel() {
    
    // 추상 프로퍼티 - 각 ViewModel에서 구현
    protected abstract val _uiState: MutableStateFlow<T>
    abstract val uiState: StateFlow<T>
    
    /**
     * 에러 메시지 클리어
     */
    fun clearError() {
        updateUiState { clearErrorMessage() as T }
    }
    
    /**
     * 로딩 상태 설정
     */
    protected fun setLoading(isLoading: Boolean) {
        updateUiState { setLoadingState(isLoading) as T }
    }
    
    /**
     * 에러 메시지 설정
     */
    protected fun setError(message: String) {
        updateUiState { setErrorMessage(message) as T }
    }
    
    /**
     * UI 상태 업데이트 헬퍼 메서드
     */
    protected inline fun updateUiState(crossinline update: T.() -> T) {
        _uiState.value = _uiState.value.update()
    }
    
    /**
     * 안전한 비동기 작업 실행
     * 자동으로 로딩 상태와 에러 처리를 관리
     */
    protected fun launchSafely(
        onStart: (() -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null,
        action: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                onStart?.invoke() ?: setLoading(true)
                action()
                onComplete?.invoke() ?: setLoading(false)
            } catch (e: Exception) {
                val errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다"
                onError?.invoke(errorMessage) ?: setError(errorMessage)
                setLoading(false)
            }
        }
    }
    
    /**
     * 간단한 비동기 작업 (로딩 상태 없이)
     */
    protected fun launchSimple(
        onError: ((String) -> Unit)? = null,
        action: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                action()
            } catch (e: Exception) {
                val errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다"
                onError?.invoke(errorMessage) ?: setError(errorMessage)
            }
        }
    }
}

/**
 * 모든 UI 상태가 공통으로 가져야 할 인터페이스
 */
interface BaseUiState {
    val errorMessage: String?
    
    /**
     * 에러 메시지를 클리어한 새로운 상태 반환
     */
    fun clearErrorMessage(): BaseUiState
    
    /**
     * 로딩 상태를 설정한 새로운 상태 반환 (기본 구현은 자기 자신 반환)
     */
    fun setLoadingState(isLoading: Boolean): BaseUiState = this
    
    /**
     * 에러 메시지를 설정한 새로운 상태 반환
     */
    fun setErrorMessage(message: String): BaseUiState
} 