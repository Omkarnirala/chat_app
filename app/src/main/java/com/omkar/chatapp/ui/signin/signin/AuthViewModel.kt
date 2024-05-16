package com.omkar.chatapp.ui.signin.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import com.omkar.chatapp.ui.signin.signup.SignUpResult
import com.omkar.chatapp.ui.signin.signup.UserDetailsModel
import com.omkar.chatapp.utils.timberLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(
    private val repository: AuthRepository,
) : ViewModel() {

    val signInResult: LiveData<SignInResult> get() = _signInResult
    private val _signInResult = MutableLiveData<SignInResult>()

    fun signIn(email: String, password: String) {

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    repository.signIn(email, password) { isSuccess, exception, result ->
                        if (isSuccess) {
                            _signInResult.postValue(SignInResult.Success(result))
                        } else {
                            _signInResult.postValue(SignInResult.Failure(exception))
                        }
                    }
                } catch (exception: Exception) {
                    _signInResult.value = SignInResult.Failure(exception)
                }
            }
        }
    }

    private val _resetPasswordResult = MutableLiveData<ResetPasswordResult>()
    val resetPasswordResult: LiveData<ResetPasswordResult> get() = _resetPasswordResult

    fun resetPassword(email: String) {
        timberLog("AuthViewModel", email)
        repository.resetPassword(email) { isSuccess, exception, result ->
            if (isSuccess) {
                _resetPasswordResult.value = ResetPasswordResult.Success(result)
            } else {
                _resetPasswordResult.value = ResetPasswordResult.Failure(exception)
            }
        }
    }

    private val _signUpResult = MutableLiveData<SignUpResult>()
    val signUpResult: LiveData<SignUpResult> get() = _signUpResult

    fun signUp(email: String, password: String) {
        repository.signUp(email, password) { result ->
            _signUpResult.value = result
        }
    }

    fun addUserToFirestore(user: UserDetailsModel) {
        repository.addUserToFirestore(user)
    }

    /**
     * Validation
     */
    private val isValidForm = MutableLiveData<Boolean>()

    fun setValidation(email: String?, password: String?) {
        if (email?.isNotEmpty() == true && password?.isNotEmpty() == true) {
            isValidForm.postValue(true)
        } else {
            isValidForm.postValue(false)
        }
    }

    fun setSignUpValidation(userName: String?, email: String?, password: String?, cnfPassword: String?) {
        if (userName?.isNotEmpty() == true && email?.isNotEmpty() == true && password?.isNotEmpty() == true && cnfPassword?.isNotEmpty() == true && password ==
            cnfPassword) {
            isValidForm.postValue(true)
        } else {
            isValidForm.postValue(false)
        }
    }

    fun getValidation(): MutableLiveData<Boolean> {
        return isValidForm
    }

    /**
     * validation
     */
    private val emailAddress = MutableLiveData<String?>()

    fun setEmailAddress(email: String?) {
        emailAddress.postValue(email)
    }

    fun getEmailAddress(): MutableLiveData<String?> {
        return emailAddress
    }


}

sealed class SignInResult {
    data class Success(val authResult: AuthResult?) : SignInResult()
    data class Failure(val exception: Exception?) : SignInResult()
}

sealed class ResetPasswordResult {
    data class Success(val result: Void?) : ResetPasswordResult()
    data class Failure(val exception: Exception?) : ResetPasswordResult()
}
