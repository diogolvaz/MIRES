
const functions = require('firebase-functions');

const admin = require('firebase-admin');


// Init application database reference
const Appadmin = admin.initializeApp();

const serviceAccount = require("* path to the MIRES account key file *, e.g., ./MIRESServiceAccountKey.json");
const MIRESadmin = admin.initializeApp({credential: admin.credential.cert(serviceAccount),
                },"MIRES");

exports.FlagsLogger = functions.region('* define the region to deploy *, e.g., europe-west2').firestore.document('MIRES_USERS_FLAGS/{flagID}').onCreate(async (snapshot, context) => { 

    // Doc after operation
    const actual_doc = snapshot.data();

    if(actual_doc.type==="delete"){
        try{
            
            const log_record = actual_doc;
            delete log_record.locked;
            delete log_record.ignore;
    
             // Delete blocked property (if exists)
             if(log_record.hasOwnProperty("blocked")){
                delete log_record.blocked;
            }

        await MIRESadmin.firestore().collection("LOG").doc(context.params.flagID).set(log_record);
               
        const promise2 =await Appadmin.firestore().collection("MIRES_USERS_FLAGS").doc(context.params.flagID).delete();

        return null;

        } catch (error){
            console.log("Error: "+error);
            return error;
        }   
    }
    else{
        return null;
    }   
});