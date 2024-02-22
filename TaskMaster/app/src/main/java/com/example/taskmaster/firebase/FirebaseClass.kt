package com.example.taskmaster.firebase

import android.app.Activity
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.taskmaster.activities.DetailsOfCard
import com.example.taskmaster.activities.CreateBoardActivity
import com.example.taskmaster.activities.MainActivity
import com.example.taskmaster.activities.ActivityForFriends
import com.example.taskmaster.activities.ProfileActivity
import com.example.taskmaster.activities.SignInActivity
import com.example.taskmaster.activities.SingUpActivity
import com.example.taskmaster.activities.ListOfTasksActivity
import com.example.taskmaster.models.TaskBoardModel
import com.example.taskmaster.models.UserModel
import com.example.taskmaster.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

class FirebaseClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SingUpActivity, userInfo: UserModel) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId()).set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener {
                Log.e(activity.javaClass.simpleName, "Error")
            }
    }


    fun removeBoardBackgroundImage(activity: ListOfTasksActivity, boardDocumentId: String) {
        val boardHashMap = HashMap<String, Any>()
        boardHashMap[Constants.BOARD_BACKGROUND_IMAGE] = ""

        mFireStore.collection(Constants.BOARDS)
            .document(boardDocumentId)
            .update(boardHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Board background image removed successfully")

                activity.hideProgressDialog()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error removing board background image", e)
            }
    }

    fun getBoardsList(activity: MainActivity) {
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val boardList: ArrayList<TaskBoardModel> = ArrayList()
                for (i in document.documents) {
                    val board = i.toObject(TaskBoardModel::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                }

                activity.populateBoardsListToUI(boardList)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board", e)
            }
    }

    fun updateProfileDataOfUser(
        activity: Activity,
        userHashMap: HashMap<String, Any>
    ) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile data updated successfully")
                when (activity){
                    is MainActivity ->{
                        activity.tokenUpdateSuccess()
                    }
                    is ProfileActivity ->{
                        activity.profileUpdateSuccess()
                    }
                }
            }.addOnFailureListener {

            when (activity){
                    is MainActivity ->{
                        activity.hideProgressDialog()
                    }
                    is ProfileActivity ->{
                        activity.hideProgressDialog()
                    }
                }
                Log.w(
                    activity.javaClass.simpleName,
                    "Error while creating a board"
                )
                Toast.makeText(
                    activity,
                    "Error while creating profile",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun getBoardDetails(activity: ListOfTasksActivity, documentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())

                val board = document.toObject(TaskBoardModel::class.java)!!
                board.documentId = document.id

                activity.boardDetails(board)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board", e)
            }
    }

    fun deleteBoard(activity: ListOfTasksActivity, documentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Board deleted successfully")
                Toast.makeText(activity, "Successfully deleted board", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while deleting board", e)
                Toast.makeText(activity, "Error while deleting board", Toast.LENGTH_LONG).show()
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: TaskBoardModel) {
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Board created successfully")

                Toast.makeText(activity, "Board created successfully", Toast.LENGTH_LONG).show()
                activity.boardCreatedSuccessfully()

            }.addOnFailureListener { exception ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating the board", exception)
            }
        }

    fun UpdateTaskList(activity: Activity, board: TaskBoardModel) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "ListOfTask updated successfully")
                if (activity is ListOfTasksActivity){
                    activity.addUpdateTaskListSuccess()
                } else if(activity is DetailsOfCard){
                    activity.addUpdateTaskListSuccess()
                }
            }.addOnFailureListener { exception ->
                if (activity is ListOfTasksActivity) {
                    activity.hideProgressDialog()
                } else if (activity is DetailsOfCard) {
                    activity.hideProgressDialog()
                }
                    Log.e(activity.javaClass.simpleName, "Error while creating list", exception)
            }

    }


    fun loadUserData(activity: Activity, readBoardsList: Boolean = false) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(UserModel::class.java)

                when (activity) {
                    is SignInActivity -> {
                        if (loggedInUser != null) {
                            activity.signInSuccess()
                        }
                    }

                    is MainActivity -> {
                        if (loggedInUser != null) {
                            activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                        }
                    }

                    is ProfileActivity -> {
                        if (loggedInUser != null) {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }
            }.addOnFailureListener {
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }

                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e("SignInUser", "Error")
            }
    }

    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if (currentUser != null) {
            currentUserId = currentUser.uid
        }
        return currentUserId
    }

    fun getFriendsAssigne(activity: Activity, assignedTo: ArrayList<String>) {

        mFireStore.collection(Constants.USERS)
            .whereIn(
                Constants.ID,
                assignedTo
            )
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val usersList: ArrayList<UserModel> = ArrayList()

                for (i in document.documents) {
                    val user = i.toObject(UserModel::class.java)!!
                    usersList.add(user)
                }
                if (activity is ActivityForFriends){
                    activity.setupOfFriendsList(usersList)
                } else if(activity is ListOfTasksActivity){
                    activity.boardMembersDetailList(usersList)
                }
            }
            .addOnFailureListener { e ->
                if (activity is ActivityForFriends){
                    activity.hideProgressDialog()
                } else if(activity is ListOfTasksActivity){
                    activity.hideProgressDialog()
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    e
                )
            }
    }
    fun getDetailsOFFriends(activity: ActivityForFriends, email: String) {

        mFireStore.collection(Constants.USERS)

            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                if (document.documents.size > 0) {
                    val user = document.documents[0].toObject(UserModel::class.java)!!
                    activity.detailsOfFriend(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found.")
                }

            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user details",
                    e
                )
            }
    }

    fun assignFriendToBoard(activity: ActivityForFriends, board: TaskBoardModel, user: UserModel) {

        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully")
                activity.friendsAssignSuccess(user)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board", e)
            }
    }
    fun uploadBoardBackgroundImage(activity: ListOfTasksActivity, boardDocumentId: String, uri: Uri) {
        val storageReference = FirebaseStorage.getInstance().reference
        val filePath = storageReference.child(Constants.BOARD_BACKGROUND_IMAGES_DIR).child("$boardDocumentId.jpg")

        filePath.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                Log.i(activity.javaClass.simpleName, "Image uploaded successfully")

                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()
                    updateBoardBackgroundImage(activity, boardDocumentId, imageUrl)
                }
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error uploading image", e)
            }
    }

    fun updateBoardBackgroundImage(activity: ListOfTasksActivity, boardDocumentId: String, imageUrl: String) {
        val boardHashMap = HashMap<String, Any>()
        boardHashMap[Constants.BOARD_BACKGROUND_IMAGE] = imageUrl

        mFireStore.collection(Constants.BOARDS)
            .document(boardDocumentId)
            .update(boardHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Board background image updated successfully")

                activity.showSelectedImage(Uri.parse(imageUrl))
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error updating board background image", e)
            }
    }
}