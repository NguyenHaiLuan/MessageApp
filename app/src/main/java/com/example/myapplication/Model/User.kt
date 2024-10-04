package com.example.myapplication.Model

class User {
    private var userId:String?=null
    private var userName:String?=null
    private var phoneNumber:String?=null
    private var profileImage:String?=null

    constructor(){}
    constructor(userId: String?, userName: String?, phoneNumber: String?, profileImage: String?) {
        this.userId = userId
        this.userName = userName
        this.phoneNumber = phoneNumber
        this.profileImage = profileImage
    }


}