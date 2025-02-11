package com.platforming.autonomy.clazz;

import static com.platforming.autonomy.clazz.User.user;

import android.util.Log;

import androidx.annotation.NonNull;

import com.platforming.autonomy.interfaze.ListenerInterface;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreManager {
    private static FirebaseAuth firebaseAuth = null;

    public static FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public static void setFirebaseAuth(FirebaseAuth firebaseAuth) {
        FirestoreManager.firebaseAuth = firebaseAuth;
    }

    public void readUserData(ListenerInterface interfaze){
        FirebaseFirestore.getInstance().collection("users").document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentSnapshot documentSnapshot = task.getResult();

                if(documentSnapshot.exists()){
                    Log.w("readUserData", "Document exist",task.getException());
                    Map<String, Object> data = documentSnapshot.getData();
                    user = new User(firebaseAuth.getCurrentUser().getUid(), firebaseAuth.getCurrentUser().getEmail(), data);
                }
                else{
                    Log.w("readUserData", "Document doesn't exist");
                }

                interfaze.onSuccess();
            }else{
                Log.w("readUserData", "Failed with: ",task.getException());
                interfaze.onFail();
            }
        });
    }

    public void writeUserData(Map<String, Object> data, ListenerInterface interfaze){
        FirebaseFirestore.getInstance().collection("users").document(firebaseAuth.getCurrentUser().getUid()).set(data).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                interfaze.onSuccess();
            }
            else{
                interfaze.onFail();
            }
        });
    }

    public void updateUserData(Map<String, Object> data, ListenerInterface interfaze){
        FirebaseFirestore.getInstance().collection("users").document(firebaseAuth.getCurrentUser().getUid()).update(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    interfaze.onSuccess();
                }
            }
        });
    }

    public void updateUserData(Map<String, Object> data, long timeInMillis, ListenerInterface interfaze){
        FirebaseFirestore.getInstance().collection("users").document(firebaseAuth.getCurrentUser().getUid()).update(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    interfaze.onSuccess(timeInMillis);
                }
            }
        });
    }

    //BulletinBoard
    public void getBulletinBoardIds(ListenerInterface interfaze){
        FirebaseFirestore.getInstance().collection("posts").document("settings").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentSnapshot documentSnapshot = task.getResult();

                if(documentSnapshot.exists()){
                    Log.w("getBulletinBoardIds", "Document exist",task.getException());
                    Map<String, Object> data = documentSnapshot.getData();
                    BulletinBoard.Manager.ids = (ArrayList<String>) data.get("bulletinIds");
                }
                else{
                    Log.w("getBulletinBoardIds", "Document doesn't exist");
                }

                interfaze.onSuccess();
            }else{
                Log.w("getBulletinBoardIds", "Failed with: ",task.getException());
                interfaze.onFail();
            }
        });
    }

    public void readRecentPostData(ListenerInterface interfaze){
        FirebaseFirestore.getInstance().collection("posts").orderBy("date", Query.Direction.DESCENDING).limit(10).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    ArrayList<BulletinBoard.Post> posts = BulletinBoard.Manager.bulletinBoards.get("_RECENT").getPosts();
                    posts.clear();

                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        posts.add(new BulletinBoard.Post(documentSnapshot.getId(), documentSnapshot.getData()));
                    }
                    interfaze.onSuccess();
                }
            }
        });
    }

    private static QueryDocumentSnapshot lastDocument_post;
    public void readPostData(String bulletinId, ListenerInterface listenerInterface){
        FirebaseFirestore.getInstance().collection("posts").whereEqualTo("bulletinId", bulletinId).orderBy("date", Query.Direction.DESCENDING).limit(15).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    ArrayList<BulletinBoard.Post> posts = BulletinBoard.Manager.bulletinBoards.get(bulletinId).getPosts();
                    posts.clear();

                    int size = task.getResult().size();
                    if(size == 0){
                        listenerInterface.onSuccess();
                    }
                    else{
                        int i = 0;
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            BulletinBoard.Post post = new BulletinBoard.Post(documentSnapshot.getId(), documentSnapshot.getData());
                            posts.add(post);
                            listenerInterface.onSuccess();
                            readCommentSize(post, posts.size(), listenerInterface);
                            if (++i == size)
                                lastDocument_post = documentSnapshot;
                        }
                    }
                }
            }
        });
    }

    public void readExtraPostData(String bulletinId, ListenerInterface listenerInterface){
        FirebaseFirestore.getInstance().collection("posts").whereEqualTo("bulletinId", bulletinId).orderBy("date", Query.Direction.DESCENDING).limit(15).startAfter(lastDocument_post).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    ArrayList<BulletinBoard.Post> posts = BulletinBoard.Manager.bulletinBoards.get(bulletinId).getPosts();

                    int size = task.getResult().size();
                    if(size > 0){
                        int i = 0;
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            BulletinBoard.Post post = new BulletinBoard.Post(documentSnapshot.getId(), documentSnapshot.getData());
                            posts.add(post);
                            listenerInterface.onSuccess();
                            readCommentSize(post, posts.size(), listenerInterface);
                            if (++i == size)
                                lastDocument_post = documentSnapshot;
                        }
                    }
                }
            }
        });
    }

    private static QueryDocumentSnapshot lastDocument_myPost;
    public void readMyPostData(ListenerInterface listenerInterface){
        FirebaseFirestore.getInstance().collection("posts").whereEqualTo("uid", user.getUid()).orderBy("date", Query.Direction.DESCENDING).limit(15).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    ArrayList<BulletinBoard.Post> posts = BulletinBoard.Manager.bulletinBoards.get("_MY").getPosts();

                    int size = task.getResult().size();
                    int i = 0;
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        BulletinBoard.Post post = new BulletinBoard.Post(documentSnapshot.getId(), documentSnapshot.getData());
                        posts.add(post);
                        listenerInterface.onSuccess();
                        readCommentSize(post, posts.size(), listenerInterface);
                        if (++i == size)
                            lastDocument_myPost = documentSnapshot;
                    }
                }
            }
        });
    }

    public void readExtraMyPostData(ListenerInterface listenerInterface){
        FirebaseFirestore.getInstance().collection("posts").whereEqualTo("uid", user.getUid()).orderBy("date", Query.Direction.DESCENDING).limit(15).startAfter(lastDocument_myPost).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    ArrayList<BulletinBoard.Post> posts = BulletinBoard.Manager.bulletinBoards.get("_MY").getPosts();

                    int size = task.getResult().size();
                    if (size > 0) {
                        int i = 0;
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            BulletinBoard.Post post = new BulletinBoard.Post(documentSnapshot.getId(), documentSnapshot.getData());
                            posts.add(post);
                            listenerInterface.onSuccess();
                            readCommentSize(post, posts.size(), listenerInterface);
                            if (++i == size)
                                lastDocument_myPost = documentSnapshot;
                        }
                    }
                }
            }
        });
    }

    public void writePostData(Map<String, Object> data, ListenerInterface interfaze){
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("posts").document();
        documentReference.set(data).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                List<String> postIds = user.getMyPostIds();
                postIds.add(documentReference.getId());
                updateUserData(new HashMap<String, Object>() {{
                    put("myPostIds", postIds);
                }}, new ListenerInterface() {});

                interfaze.onSuccess();
            }
            else{
                interfaze.onFail();
            }
        });
    }

    public void updatePostData(String id, Map<String, Object> data, ListenerInterface listenerInterface) {
        FirebaseFirestore.getInstance().collection("posts").document(id).update(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                    listenerInterface.onSuccess();
                else
                    listenerInterface.onFail();
            }
        });
    }

    //Comment
    public void readCommentSize(BulletinBoard.Post post, ListenerInterface listenerInterface){
        FirebaseFirestore.getInstance().collection("posts").document(post.getId()).collection("comments").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                post.setCommentSize(task.getResult().size());
                listenerInterface.onSuccess();
            }
        });
    }

    public void readCommentSize(BulletinBoard.Post post, int postion, ListenerInterface listenerInterface){
        FirebaseFirestore.getInstance().collection("posts").document(post.getId()).collection("comments").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                post.setCommentSize(task.getResult().size());
                listenerInterface.onSuccess(postion);
            }
        });
    }

    private static QueryDocumentSnapshot lastDocument_comment;
    public void readCommentData(BulletinBoard.Post post, ListenerInterface listenerInterface){
        FirebaseFirestore.getInstance().collection("posts").document(post.getId()).collection("comments").orderBy("date", Query.Direction.DESCENDING).limit(5).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    ArrayList<BulletinBoard.Comment> comments = post.getComments();
                    comments.clear();

                    int size = task.getResult().size();
                    if(size > 0){
                        int i = 0;
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            comments.add(new BulletinBoard.Comment(documentSnapshot.getId(), documentSnapshot.getData()));
                            if(++i == size)
                                lastDocument_comment = documentSnapshot;
                        }
                        listenerInterface.onSuccess();
                    }
                }
            }
        });
    }

    public void readExtraCommentData(BulletinBoard.Post post, ListenerInterface listenerInterface){
        FirebaseFirestore.getInstance().collection("posts").document(post.getId()).collection("comments").orderBy("date", Query.Direction.DESCENDING).limit(5).startAfter(lastDocument_comment).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    ArrayList<BulletinBoard.Comment> comments = post.getComments();

                    int size = task.getResult().size();
                    if(size > 0){
                        int i = 0;
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            comments.add(new BulletinBoard.Comment(documentSnapshot.getId(), documentSnapshot.getData()));
                            if(++i == size)
                                lastDocument_comment = documentSnapshot;
                        }
                        listenerInterface.onSuccess();
                    }
                }
            }
        });
    }

    public void writeCommentData(String id, Map<String, Object> data, ListenerInterface listenerInterface){
        FirebaseFirestore.getInstance().collection("posts").document(id).collection("comments").document().set(data).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                listenerInterface.onSuccess();
            }
            else{
                listenerInterface.onFail();
            }
        });
    }

    public void deleteComment(String postId, String commentId, int pos, ListenerInterface listenerInterface){
        FirebaseFirestore.getInstance().collection("posts").document(postId).collection("comments").document(commentId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listenerInterface.onSuccess(pos);
            }
        });
    }
}