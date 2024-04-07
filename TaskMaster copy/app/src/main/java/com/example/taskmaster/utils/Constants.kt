package com.example.taskmaster.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants{

    const val USERS: String = "users"

    const val BOARDS: String = "boards"

    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"
    const val ASSIGNED_TO : String = "assignedTo"
    const val IS_TITLE_BOLD = "isTitleBold"
    const val READ_STORAGE_PERMISION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2
    const val DOCUMENT_ID: String = "documentId"
    const val BOARD_POSITION = "board_position"
    const val TASK_LIST: String = "taskList"
    const val BOARD_DETAIL: String = "board_detail"
    const val ID: String = "id"
    const val TOOLBAR_HEIGHT = "toolbar_height"
    const val EMAIL: String = "email"
    const val BOARD_MEMBERS_LIST: String = "board_members_list"

    const val TASK_LIST_ITEM_POSITION: String = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION: String = "card_list_item_position"
    const val SELECT: String = "Select"
    const val UN_SELECT: String = "UnSelect"
    const val BOARD_BACKGROUND_URI = "board_background_uri"
    const val TASKMASTER_PREFERENCES = "TaskMasterPrefs"
    const val CARDS: String = "cards"
    const val TASKS: String = "tasks"
    const val BOARD_BACKGROUND_IMAGE: String = "backgroundImage"
    const val BOARD_BACKGROUND_IMAGES_DIR = "board_background_images"

    const val FCM_KEY_MESSAGE:String = "message"
    const val FCM_KEY_DATA:String = "data"
    const val FCM_KEY_TO:String = "to"
    const val FCM_TOKEN_UPDATED = "fcmTokenUpdated"
    const val FCM_TOKEN = "fcmToken"
    const val FCM_BASE_URL:String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String = "authorization"
    const val FCM_KEY:String = "key"
    const val FCM_SERVER_KEY:String = "AAAAeIdPtck:APA91bHZW4RmEcQJW4N9rAfYFXZMmI9P5fj3ksyaOiLBFIGD6gY84pOedkO2SmjKl3Kk3IDSrAiUpOdf-panPPwDr5XKq_8Ee5tzfvxVUi-50BLEAGxf54rFMd5lCH27qVyunRxIq2o7"
    const val FCM_KEY_TITLE:String = "title"
    fun showImageChooser(activity: Activity) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExstension(activity: Activity,uri: Uri?): String?{
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}