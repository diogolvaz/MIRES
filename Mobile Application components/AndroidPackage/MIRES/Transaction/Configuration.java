package com.amsavarthan.social.hify.MIRES.Transaction;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.firebase.client.Firebase;
import com.google.common.primitives.Bytes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageMetadata;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.amsavarthan.social.hify.MIRES.UserRecoveryMechanism.UserRecoveryNotification.deleteRecoveryNotification;
import static com.amsavarthan.social.hify.MIRES.Utils.Utils.MIRES_cloud_logger_todo;
import static com.amsavarthan.social.hify.MIRES.Utils.Utils.MIRES_users_flags;


//###############################################################################################
//
//                                      Configuration
//
//                       This class allow to configure the mobile application
//                          on the different operations performed
//
//###############################################################################################

public class Configuration {

    //###########################################################################################
    //
    //
    //                                                     MAIN RECOVERY
    //
    //
    //###########################################################################################

    // Initiate interaction
    public static Map<String, Object> transactionInitiated(String userID, Context context) {

        // Delete undo
        if(context!=null) {
            deleteRecoveryNotification(context);
        }
        // Create state
        Map<String, Object> transaction_state = new HashMap<>();
        // User ID
        transaction_state.put("user_id",userID);
        // Locked document
        transaction_state.put("documents",new ArrayList<HashMap<String,String>>());
        // Transaction ID
        transaction_state.put("transaction_id",generateRandomID());

        return transaction_state;
    }


    //Get flag
    public static Map<String, Object> getFlag(Map<String,Object> data, String doc, String type,List<Map<String, Object>> read_data, Map<String,Object> transaction_state){

        Map<String, Object> flag = new HashMap<>();

        // Data structure
        Map<String, Object> data_structure = new HashMap<>();
        for(Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            if(!key.startsWith("MIRES")) {
                data_structure.put(key, MIRES_cloud_logger_todo);
            }
            else{
                flag.put(key.substring(6), entry.getValue());

            }
        }

        //Log information
        if(read_data!=null) {
            flag.put("read", read_data);
        }
        if(!data_structure.isEmpty()) {
            flag.put("data", data_structure);
        }

        // Transaction ID
        flag.put("transaction_id", transaction_state.get("transaction_id"));

        // User ID
        if(!flag.containsKey("user_id")){
            flag.put("user_id",transaction_state.get("user_id"));
        }

        // Timestamp
        if(!flag.containsKey("timestamp")){
            flag.put("timestamp", FieldValue.serverTimestamp());
        }

        // Doc name
        flag.put("doc",doc);

        // Request type
        flag.put("type",type);

        return  flag;
    }

    // Configure request data
    public static void activateRecovery(Map<String, Object> data, String request_id, Map<String,Object> transaction_state){

        // Define transaction ID
        //setTransactionID(transaction_state,request_id);

        // Generate MIRES configuration data
        data.put("MIRES_locked", false);
        data.put("MIRES_ignore", false);
        data.put("MIRES_operation_id", request_id);
    }

    // Generate Random ID
    public static String generateRandomID(){
        return FirebaseFirestore.getInstance().collection(MIRES_users_flags).document().getId();
    }


    //###################################################################################################################################
    //
    //
    //                                                     READ DEPENDENCY
    //
    //
    //####################################################################################################################################

    // Get data read
    public static Map<String,Object> setDataRead(String version, String doc, List<String> fields){

        Map<String, Object> read = new HashMap<>();
        // Version
        read.put("version", version);
        // Document
        read.put("doc", doc);
        // Fields read
        read.put("data",fields);

        return read;
    }

    //###################################################################################################################################
    //
    //
    //                                                     RECOVERABLE TRANSACTION
    //
    //
    //####################################################################################################################################

    // Activate recovery
    public static void activateUserRecovery(Map<String, Object> data,String document, Map<String,Object> transaction_state){

        // Activate operation
        data.put("MIRES_blocked",true);
        data.put("MIRES_user_id", transaction_state.get("user_id"));

        // Store document
        addDocument(document,transaction_state);
    }

    // Add document to block
    public static void addDocument(String document, Map<String, Object> transation_state){
        ArrayList docs = (ArrayList) transation_state.get("documents");
        docs.add(document);
        transation_state.put("documents",docs);
    }

    //###################################################################################################################################
    //
    //
    //                                                          SNAPSHOTS
    //
    //
    //####################################################################################################################################

    // Activate snapshots
    public static void activateSnapshot(Map<String, Object> data){
        data.put("MIRES_snapshot",FieldValue.increment(1));
        data.put("MIRES_timestamp",FieldValue.serverTimestamp());
    }


    //###################################################################################################################################
    //
    //
    //                                                          FILE RECOVERY
    //
    //
    //####################################################################################################################################

    // Get file metadata
    public static StorageMetadata getMIRESMetadata(String transaction_id){
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("transaction_id", transaction_id)
                .setCustomMetadata("MIRES_locked", String.valueOf(false))
                .setCustomMetadata("MIRES_ignore", String.valueOf(false))
                .build();
        return metadata;
    }

    //###################################################################################################################################
    //
    //
    //                                                          READ OPERATIONS - FILTER
    //
    //
    //####################################################################################################################################


    // Document not locked
    public static boolean documentNotLocked(DocumentSnapshot documentSnapshot){
        if(documentSnapshot.contains("MIRES_locked")) {
            return !documentSnapshot.getBoolean("MIRES_locked");
        }
        else{
            return true;
        }
    }

    // Document not blocked
    public static boolean documentNotBlocked(DocumentSnapshot documentSnapshot, String user_id){
        if(documentSnapshot.contains("MIRES_blocked") && documentSnapshot.contains("MIRES_user_id")){
            if (documentSnapshot.get("MIRES_blocked").equals(false) || documentSnapshot.get("MIRES_user_id").equals(user_id)) {
                return true;
            } else {
                return false;
            }
        }
        else{
            return true;
        }
    }

    // Get Document version
    public static String getVersionFromDocument(DocumentSnapshot doc){
        return doc.get("MIRES_operation_id").toString();
    }


}
