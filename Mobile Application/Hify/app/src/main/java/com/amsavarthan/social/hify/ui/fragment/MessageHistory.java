package com.amsavarthan.social.hify.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amsavarthan.social.hify.R;
import com.amsavarthan.social.hify.adapters.MessageImageAdapter;
import com.amsavarthan.social.hify.adapters.MessageImageReplyAdapter;
import com.amsavarthan.social.hify.adapters.MessageTextAdapter;
import com.amsavarthan.social.hify.adapters.MessageTextReplyAdapter;
import com.amsavarthan.social.hify.models.Message;
import com.amsavarthan.social.hify.models.MessageReply;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by amsavarthan on 29/3/18.
 */

public class MessageHistory extends Fragment {

    private List<Message> messages;
    private List<MessageReply> messageReplies;
    private MessageImageAdapter messageImageAdapter;
    private MessageImageReplyAdapter messageImageReplyAdapter;
    private MessageTextAdapter messageTextAdapter;
    private MessageTextReplyAdapter messageTextReplyAdapter;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private RecyclerView mRecyclerView;
    private TextView tab_1,tab_2,tab_3,tab_4;
    private SwipeRefreshLayout refreshLayout;
    private String selected;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.frag_messages_history, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        messages=new ArrayList<>();
        messageReplies=new ArrayList<>();
        messageTextAdapter=new MessageTextAdapter(messages,view.getContext());
        messageImageAdapter=new MessageImageAdapter(messages,view.getContext());
        messageImageReplyAdapter=new MessageImageReplyAdapter(messageReplies,view.getContext());
        messageTextReplyAdapter=new MessageTextReplyAdapter(messageReplies,view.getContext());

        mRecyclerView = view.findViewById(R.id.messageList);
        tab_1=view.findViewById(R.id.text);
        tab_2=view.findViewById(R.id.text_reply);
        tab_3=view.findViewById(R.id.image);
        tab_4=view.findViewById(R.id.image_reply);
        refreshLayout=view.findViewById(R.id.refreshLayout);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));
        selected="null";

        tab_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!selected.equals("text")) {
                    messages.clear();
                    messageTextAdapter.notifyDataSetChanged();
                    mRecyclerView.setAdapter(messageTextAdapter);
                    getTextMessage();

                    refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            messages.clear();
                            messageTextAdapter.notifyDataSetChanged();
                            getTextMessage();
                        }
                    });
                    selected="text";
                }
            }
        });

        tab_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!selected.equals("reply")) {
                    messageReplies.clear();
                    messageTextReplyAdapter.notifyDataSetChanged();
                    mRecyclerView.setAdapter(messageTextReplyAdapter);
                    getTextReplyMessage();

                    refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            messageReplies.clear();
                            messageTextReplyAdapter.notifyDataSetChanged();
                            getTextReplyMessage();
                        }
                    });
                    selected="reply";
                }

            }
        });

        tab_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!selected.equals("image")) {
                    messages.clear();
                    messageImageAdapter.notifyDataSetChanged();
                    mRecyclerView.setAdapter(messageImageAdapter);
                    getImageMessage();

                    refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            messages.clear();
                            messageImageAdapter.notifyDataSetChanged();
                            getImageMessage();
                        }
                    });
                    selected="image";
                }
            }
        });

        tab_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!selected.equals("img_reply")) {
                    messageReplies.clear();
                    messageImageReplyAdapter.notifyDataSetChanged();
                    mRecyclerView.setAdapter(messageImageReplyAdapter);
                    getImageReplyMessage();

                    refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            messageReplies.clear();
                            messageImageReplyAdapter.notifyDataSetChanged();
                            getImageReplyMessage();
                        }
                    });
                    selected="img_reply";
                }
            }
        });

        tab_1.performClick();

    }

    public void getTextMessage(){

        refreshLayout.setRefreshing(true);
        getView().findViewById(R.id.default_item).setVisibility(View.GONE);
        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("Notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                        if(e!=null){
                            refreshLayout.setRefreshing(false);
                            Toasty.error(getView().getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT, true).show();
                            Log.w("error","listen",e);
                            return;
                        }

                        if(!queryDocumentSnapshots.isEmpty()){

                            for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    Message message = doc.getDocument().toObject(Message.class).withId(doc.getDocument().getId());
                                    messages.add(message);
                                    messageTextAdapter.notifyDataSetChanged();
                                    refreshLayout.setRefreshing(false);

                                }

                            }

                            if(messages.isEmpty()){
                                refreshLayout.setRefreshing(false);
                                getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                            }

                        }else {
                            refreshLayout.setRefreshing(false);
                            getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                        }

                    }
                });

    }

    public void getTextReplyMessage(){

        refreshLayout.setRefreshing(true);
        getView().findViewById(R.id.default_item).setVisibility(View.GONE);
        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("Notifications_reply")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.isEmpty()){

                            for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()){

                                if(doc.getType()== DocumentChange.Type.ADDED){

                                    MessageReply messageReply=doc.getDocument().toObject(MessageReply.class).withId(doc.getDocument().getId());
                                    messageReplies.add(messageReply);
                                    messageTextReplyAdapter.notifyDataSetChanged();
                                    refreshLayout.setRefreshing(false);
                                }


                            }

                            if(messageReplies.isEmpty()){
                                refreshLayout.setRefreshing(false);
                                getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                            }

                        }else {
                            refreshLayout.setRefreshing(false);
                            getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                        }


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        refreshLayout.setRefreshing(false);
                        Toasty.error(getView().getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT, true).show();
                        Log.w("error","listen",e);
                    }
                });

    }

    public void getImageMessage(){

        refreshLayout.setRefreshing(true);
        getView().findViewById(R.id.default_item).setVisibility(View.GONE);
        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("Notifications_image")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.isEmpty()){

                            for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()){

                                if(doc.getType()== DocumentChange.Type.ADDED) {

                                    Message message = doc.getDocument().toObject(Message.class).withId(doc.getDocument().getId());
                                    messages.add(message);
                                    messageImageAdapter.notifyDataSetChanged();
                                    refreshLayout.setRefreshing(false);
                                }

                            }
                            if(messages.isEmpty()){
                                refreshLayout.setRefreshing(false);
                                getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                            }

                        }else {
                            refreshLayout.setRefreshing(false);
                            getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        refreshLayout.setRefreshing(false);
                        Toasty.error(getView().getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT, true).show();
                        Log.w("error","listen",e);
                    }
                });

    }

    public void getImageReplyMessage(){

        refreshLayout.setRefreshing(true);
        getView().findViewById(R.id.default_item).setVisibility(View.GONE);
        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("Notifications_reply_image")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.isEmpty()){

                            for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()){

                                if(doc.getType()== DocumentChange.Type.ADDED){

                                    MessageReply messageReply=doc.getDocument().toObject(MessageReply.class).withId(doc.getDocument().getId());
                                    messageReplies.add(messageReply);
                                    messageImageReplyAdapter.notifyDataSetChanged();
                                    refreshLayout.setRefreshing(false);
                                }


                            }
                            if(messageReplies.isEmpty()){
                                refreshLayout.setRefreshing(false);
                                getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                            }

                        }else {
                            refreshLayout.setRefreshing(false);
                            getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        refreshLayout.setRefreshing(false);
                        Toasty.error(getView().getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT, true).show();
                        Log.w("error","listen",e);

                    }
                });

    }


}
