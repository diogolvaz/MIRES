package com.amsavarthan.social.hify.MIRES.Tokens;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static com.amsavarthan.social.hify.MIRES.Utils.Utils.Mires_users_tokens;


//###############################################################################################
//
//                                  SendToken
//
//                     This class allows to send the token of user
//                       to MIRES users tokens
//
//###############################################################################################

public class SendToken {

    public static void sendToken(String userID,String token) {

        // Save the token
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);

        FirebaseFirestore.getInstance().collection(Mires_users_tokens).document(userID).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("SEND TOKEN RESULT ", "SUCCESS");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("SEND TOKEN RESULT ", "ERROR - "+e.getMessage());
            }
        });
    }
}
