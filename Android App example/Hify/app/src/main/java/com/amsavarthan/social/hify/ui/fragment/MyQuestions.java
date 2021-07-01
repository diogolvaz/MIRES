package com.amsavarthan.social.hify.ui.fragment;


import android.content.Context;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amsavarthan.social.hify.R;
import com.amsavarthan.social.hify.adapters.QuestionAdapter;
import com.amsavarthan.social.hify.models.AllQuestionsModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static android.content.Context.MODE_PRIVATE;

public class MyQuestions extends Fragment {


    private RecyclerView recyclerView;
    private Context context;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mCurrentUser;
    private QuestionAdapter adapter;
    private static String TAG = MyQuestions.class.getSimpleName();
    private List<AllQuestionsModel> allQuestionsModelList = new ArrayList<>();
    private TextView et0,et1,et2,et3,et4,et5,et6,et7,et8,et9,et10,et11,et12,et13;
    private String userId;
    private SwipeRefreshLayout refreshLayout;

    public MyQuestions() { }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      return inflater.inflate(R.layout.frag_my_answered, container, false);
    }

    public static MyQuestions newInstance(String user_id){

        Bundle args=new Bundle();
        args.putString("user_id",user_id);

        MyQuestions fragment=new MyQuestions();
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(getView(), savedInstanceState);

        context = getView().getContext();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        adapter = new QuestionAdapter(allQuestionsModelList);

        if (mCurrentUser != null) {

            mFirestore = FirebaseFirestore.getInstance();

            if(getArguments()!=null){
                userId=getArguments().getString("user_id");
            }else{
                userId=mCurrentUser.getUid();
            }

            et0=getView().findViewById(R.id.all);
            et1=getView().findViewById(R.id.accountancy);
            et2=getView().findViewById(R.id.astronomy);
            et3=getView().findViewById(R.id.biology);
            et4=getView().findViewById(R.id.business_maths);
            et5=getView().findViewById(R.id.computer_science);
            et6=getView().findViewById(R.id.commerce);
            et7=getView().findViewById(R.id.chemistry);
            et8=getView().findViewById(R.id.economics);
            et9=getView().findViewById(R.id.geography);
            et10=getView().findViewById(R.id.history);
            et11=getView().findViewById(R.id.physics);
            et12=getView().findViewById(R.id.p_science);
            et13=getView().findViewById(R.id.maths);

            refreshLayout=getView().findViewById(R.id.refreshLayout);

            recyclerView = getView().findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            allQuestionsModelList.clear();
            recyclerView.setAdapter(adapter);

            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    allQuestionsModelList.clear();
                    adapter.notifyDataSetChanged();
                    getQuestions();
                }
            });

            setUpOnClick();
            getQuestions();

        }
    }

    private void getQuestions() {

        refreshLayout.setRefreshing(true);
        getView().findViewById(R.id.default_item).setVisibility(View.GONE);
        mFirestore.collection("Questions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {

                        if (!documentSnapshots.isEmpty()) {

                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    if (doc.getDocument().getString("id").equals(userId)) {
                                        AllQuestionsModel question = doc.getDocument().toObject(AllQuestionsModel.class).withId(doc.getDocument().getId());
                                        allQuestionsModelList.add(question);
                                        adapter.notifyDataSetChanged();
                                        refreshLayout.setRefreshing(false);
                                    }

                                }
                            }


                            if(allQuestionsModelList.isEmpty()){
                                getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                                refreshLayout.setRefreshing(false);
                            }



                        } else {
                            getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                            refreshLayout.setRefreshing(false);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        refreshLayout.setRefreshing(false);
                        Toasty.error(context, "Some technical error occurred", Toasty.LENGTH_SHORT, true).show();
                        Log.w("Error","listen:error",e);

                    }
                });


    }

    public void filterResult(String subject){

        if(subject.equals("All")){
            getQuestions();
        }else{

            getView().findViewById(R.id.default_item).setVisibility(View.GONE);
            refreshLayout.setRefreshing(true);
            mFirestore.collection("Questions")
                    .whereEqualTo("subject",subject)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {

                            try {
                                if (!documentSnapshots.isEmpty()) {

                                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                        if (doc.getType() == DocumentChange.Type.ADDED) {

                                            if (doc.getDocument().getString("id").equals(userId)) {
                                                AllQuestionsModel question = doc.getDocument().toObject(AllQuestionsModel.class).withId(doc.getDocument().getId());
                                                allQuestionsModelList.add(question);
                                                adapter.notifyDataSetChanged();
                                                refreshLayout.setRefreshing(false);
                                            }

                                        }


                                    }

                                    if(allQuestionsModelList.isEmpty()){
                                        getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                                        refreshLayout.setRefreshing(false);
                                    }

                                } else {
                                    getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                                    refreshLayout.setRefreshing(false);
                                }
                            }catch (NullPointerException eee){
                                Toasty.error(context, "Some technical error occurred", Toasty.LENGTH_SHORT, true).show();
                                adapter.notifyDataSetChanged();
                                if(allQuestionsModelList.isEmpty()){
                                    getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                                    refreshLayout.setRefreshing(false);
                                }
                            } catch (Exception ee){
                                ee.printStackTrace();
                                Toasty.error(context, "Some technical error occurred", Toasty.LENGTH_SHORT, true).show();
                                if(allQuestionsModelList.isEmpty()){
                                    refreshLayout.setRefreshing(false);
                                    getView().findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                                }
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            refreshLayout.setRefreshing(false);
                            Toasty.error(context, "Some technical error occurred", Toasty.LENGTH_SHORT, true).show();
                            Log.w("Error","listen:error",e);

                        }
                    });


        }

    }

    public void setUpOnClick(){

        et0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                getQuestions();
                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        getQuestions();
                    }
                });
            }
        });
        et1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Accountancy");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Accountancy");
                    }
                });
            }
        });
        et2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Astronomy");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Astronomy");
                    }
                });
            }
        });
        et3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Biology");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Biology");
                    }
                });
            }
        });
        et4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Business Maths");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Business Maths");
                    }
                });
            }
        });
        et5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Computer Science");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Computer Science");
                    }
                });
            }
        });
        et6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Commerce");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Commerce");
                    }
                });
            }
        });
        et7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Chemistry");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Chemistry");
                    }
                });
            }
        });
        et8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Economics");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Economics");
                    }
                });
            }
        });
        et9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Geography");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Geography");
                    }
                });
            }
        });
        et10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("History");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("History");
                    }
                });
            }
        });
        et11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Physics");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Physics");
                    }
                });
            }
        });
        et12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Political Science");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Political Science");
                    }
                });
            }
        });
        et13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allQuestionsModelList.clear();
                adapter.notifyDataSetChanged();
                filterResult("Maths");

                refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        allQuestionsModelList.clear();
                        adapter.notifyDataSetChanged();
                        filterResult("Maths");
                    }
                });
            }
        });

    }
}

