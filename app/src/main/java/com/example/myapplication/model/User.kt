package com.example.myapplication.model

class User {
     var userId:String?=null
     var userName:String?=null
     var phoneNumber:String?=null
     var profileImage:String?=null

    constructor(){}
    constructor(userId: String?, userName: String?, phoneNumber: String?, profileImage: String?) {
        this.userId = userId
        this.userName = userName
        this.phoneNumber = phoneNumber
        this.profileImage = profileImage
    }




}