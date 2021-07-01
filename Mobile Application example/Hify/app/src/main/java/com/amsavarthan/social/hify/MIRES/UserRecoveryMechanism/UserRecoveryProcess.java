package com.amsavarthan.social.hify.MIRES.UserRecoveryMechanism;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static com.amsavarthan.social.hify.MIRES.Utils.Utils.Mires_users_recovery;


//#############################################################################
//
//                          Action Receiver
//
//        This class contains the action that will be done if
//          the user clicks on MIRES Client undo buttons
//
//#############################################################################


public class UserRecoveryProcess extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Begining blocking phase
        long begin = System.currentTimeMillis();

        Toast.makeText(context,"Trying to recover the last transaction...",Toast.LENGTH_LONG).show();

        // Remove the undo notification
        UserRecoveryNotification.deleteRecoveryNotification(context);

        // Get request id
        final String transaction_id = intent.getStringExtra("transaction_id");

        // Get documents to block
        final ArrayList<String> documents = (ArrayList<String>) intent.getSerializableExtra("documents");

        FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {

                    //Analyze the documents
                    for (String doc : documents) {

                        DocumentReference doc_ref = FirebaseFirestore.getInstance().document(doc);
                        DocumentSnapshot doc_to_block = transaction.get(doc_ref);

                        // Analyse the documents that exist
                        if (doc_to_block.exists()) {
                            if ((doc_to_block.get("MIRES_blocked").equals(false) || doc_to_block.get("MIRES_locked").equals(true)) && doc_to_block.get("MIRES_transaction_id").equals(transaction_id)) {
                                throw new FirebaseFirestoreException("Lock the document(s) failed!",
                                        FirebaseFirestoreException.Code.ABORTED);
                            }
                        }
                    }

                    // Block the documents
                    for (String doc : documents) {

                        // Block the document
                        DocumentReference doc_ref = FirebaseFirestore.getInstance().document(doc);
                        Map<String, Object> MIRES = new HashMap<>();
                        MIRES.put("MIRES_locked", true);
                        MIRES.put("MIRES_ignore", true);
                        transaction.set(doc_ref, MIRES, SetOptions.merge());
                    }

                // Save the undo request
                DocumentReference sfDocRef = FirebaseFirestore.getInstance().collection(Mires_users_recovery).document(transaction_id);
                Map<String, Object> data = new HashMap<>();
                data.put("from", FirebaseAuth.getInstance().getUid());
                data.put("documents",documents);
                data.put("timestamp", FieldValue.serverTimestamp());
                transaction.set(sfDocRef, data);

                return null;

            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toasty.success(context, "Recovery initiated", Toasty.LENGTH_SHORT, true).show();
                Log.d("USER RECOVER RESULT", " "+documents.size() +" documents blocked in "+ String.valueOf(System.currentTimeMillis()-begin) +" (ms)");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(context, "Recovery not possible", Toasty.LENGTH_SHORT, true).show();
                Log.d("USER RECOVER RESULT", " ERROR - "+e.getMessage());
            }});


        //This is used to close the notification tray
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }
}
