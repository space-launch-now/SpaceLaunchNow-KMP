package me.calebjones.spacelaunchnow

import androidx.lifecycle.ViewModel
import me.calebjones.spacelaunchnow.data.UserRepository

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    fun sayHello(name : String) : String{
        val foundUser = repository.findUser(name)
        val platform = getPlatform()
        return foundUser?.let { "Hello '$it' from ${platform.name}" } ?: "User '$name' not found!"
    }
}