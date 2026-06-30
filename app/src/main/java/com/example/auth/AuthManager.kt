package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class AuraUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val isSandbox: Boolean = false
)

class AuthManager(private val context: Context) {
    private val TAG = "AuthManager"
    private val prefs: SharedPreferences = context.getSharedPreferences("aura_local_auth", Context.MODE_PRIVATE)
    
    private val _currentUser = MutableStateFlow<AuraUser?>(null)
    val currentUser: StateFlow<AuraUser?> = _currentUser

    private var firebaseAuth: FirebaseAuth? = null
    var isFirebaseEnabled = false
        private set

    init {
        try {
            // Check if Firebase is configured / google-services.json is valid
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                val authInstance = FirebaseAuth.getInstance()
                firebaseAuth = authInstance
                isFirebaseEnabled = true
                Log.i(TAG, "Firebase Auth successfully initialized.")
                
                // Set initial user if Firebase has one
                authInstance.currentUser?.let { fbUser ->
                    _currentUser.value = AuraUser(
                        uid = fbUser.uid,
                        email = fbUser.email ?: "",
                        displayName = fbUser.displayName ?: fbUser.email?.substringBefore("@") ?: "User",
                        isSandbox = false
                    )
                }
            } else {
                Log.w(TAG, "Firebase App is not initialized (likely missing google-services.json). Using Local Sandbox.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase Auth, falling back to Local Sandbox.", e)
        }

        // If no Firebase user, check local sandbox storage
        if (_currentUser.value == null) {
            val localUid = prefs.getString("local_uid", null)
            val localEmail = prefs.getString("local_email", null)
            val localName = prefs.getString("local_name", null)
            if (localUid != null && localEmail != null) {
                _currentUser.value = AuraUser(
                    uid = localUid,
                    email = localEmail,
                    displayName = localName ?: "User",
                    isSandbox = true
                )
            }
        }
    }

    suspend fun loginWithEmail(email: String, password: String): Result<AuraUser> {
        val auth = firebaseAuth
        if (isFirebaseEnabled && auth != null) {
            return try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val fbUser = result.user
                if (fbUser != null) {
                    val user = AuraUser(
                        uid = fbUser.uid,
                        email = fbUser.email ?: "",
                        displayName = fbUser.displayName ?: fbUser.email?.substringBefore("@") ?: "User",
                        isSandbox = false
                    )
                    _currentUser.value = user
                    Result.success(user)
                } else {
                    Result.failure(Exception("Login returned null user"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Firebase login failed, trying local sandbox fallback", e)
                loginSandbox(email, password)
            }
        } else {
            return loginSandbox(email, password)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String): Result<AuraUser> {
        val auth = firebaseAuth
        if (isFirebaseEnabled && auth != null) {
            return try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val fbUser = result.user
                if (fbUser != null) {
                    // Real Firebase signup success!
                    val user = AuraUser(
                        uid = fbUser.uid,
                        email = fbUser.email ?: "",
                        displayName = name,
                        isSandbox = false
                    )
                    _currentUser.value = user
                    Result.success(user)
                } else {
                    Result.failure(Exception("Signup returned null user"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Firebase signup failed, creating local sandbox user instead", e)
                signUpSandbox(email, password, name)
            }
        } else {
            return signUpSandbox(email, password, name)
        }
    }

    fun loginWithGoogleSimulated(name: String, email: String): Result<AuraUser> {
        // Authenticating via Google is simulated if client ID or services are not fully provisioned
        val uid = "google_" + UUID.randomUUID().toString().take(8)
        val user = AuraUser(
            uid = uid,
            email = email,
            displayName = name,
            isSandbox = true
        )
        prefs.edit()
            .putString("local_uid", uid)
            .putString("local_email", email)
            .putString("local_name", name)
            .apply()
        _currentUser.value = user
        return Result.success(user)
    }

    fun logout() {
        val auth = firebaseAuth
        if (isFirebaseEnabled && auth != null) {
            auth.signOut()
        }
        prefs.edit().clear().apply()
        _currentUser.value = null
    }

    private fun loginSandbox(email: String, password: String): Result<AuraUser> {
        val storedPassword = prefs.getString("pwd_$email", null)
        return if (storedPassword != null && storedPassword == password) {
            val uid = prefs.getString("uid_$email", UUID.randomUUID().toString())!!
            val name = prefs.getString("name_$email", email.substringBefore("@"))!!
            
            val user = AuraUser(uid = uid, email = email, displayName = name, isSandbox = true)
            prefs.edit()
                .putString("local_uid", uid)
                .putString("local_email", email)
                .putString("local_name", name)
                .apply()
            
            _currentUser.value = user
            Result.success(user)
        } else if (storedPassword == null) {
            // Simple sandbox behavior: Auto-register user to avoid frustration!
            signUpSandbox(email, password, email.substringBefore("@"))
        } else {
            Result.failure(Exception("Incorrect sandbox password."))
        }
    }

    private fun signUpSandbox(email: String, password: String, name: String): Result<AuraUser> {
        val uid = "sb_" + UUID.randomUUID().toString().take(8)
        prefs.edit()
            .putString("pwd_$email", password)
            .putString("uid_$email", uid)
            .putString("name_$email", name)
            .putString("local_uid", uid)
            .putString("local_email", email)
            .putString("local_name", name)
            .apply()
        
        val user = AuraUser(uid = uid, email = email, displayName = name, isSandbox = true)
        _currentUser.value = user
        return Result.success(user)
    }
}
