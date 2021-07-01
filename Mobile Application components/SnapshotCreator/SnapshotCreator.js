
/********************************************************************************************
 * 
 * 
 *                                  IMPORTS
 * 
 * 
 *******************************************************************************************/


const functions = require('firebase-functions');

const admin = require('firebase-admin');

// Init application database reference
const Appadmin = admin.initializeApp();

const serviceAccount = require("* path to the MIRES account key file *, e.g., ./MIRESServiceAccountKey.json");
const MIRESadmin = admin.initializeApp({credential: admin.credential.cert(serviceAccount),
                },"MIRES");


 /********************************************************************************************
 * 
 * 
 *                                  VARIABLES
 * 
 * 
 *******************************************************************************************/

// Number of versions
const versions=1000;

// Snapshots collection name
const snapshots_collection="SNAPSHOTS"

/********************************************************************************************
 * 
 * 
 *                                  MAIN FUNCTION
 * 
 * 
 *******************************************************************************************/

exports.SnapshotCreator = functions.region('europe-west2').firestore.document('COLLECTION').onWrite(async (change, context) => { 

                    const actual_doc= change.after.data();
                    var snapshot_name;
                    var timestamp;
                   
                           if(actual_doc!==undefined){
               
                               // Ignore
                               if (toIgnore(actual_doc)){
                                   return null;
                               }
               
                               else{
               
                                   if(actual_doc.hasOwnProperty("MIRES_snapshot")){
                                       if((actual_doc.MIRES_snapshot % versions)===0){
               
                                        snapshot_name= actual_doc.MIRES_operation_id;
                                        timestamp= actual_doc.MIRES_timestamp;
                                        counter = actual_doc.MIRES_snapshot;
                                        actual_doc.MIRES_ignore=false;
             
                                        // Delete users recovery data
                                        if(actual_doc.hasOwnProperty("MIRES_blocked")){
                                             delete actual_doc.MIRES_blocked;
                                             delete actual_doc.MIRES_user_id;
                                         }
                                       
                                           // Store snapshot
                                           return MIRESadmin.firestore().collection(snapshots_collection).where("doc","==","DOCUMENT").get().then(async doc_snapshot=>{
                                               if(doc_snapshot.empty){
                                                   let first_doc= await MIRESadmin.firestore().collection(snapshots_collection).add({doc:"DOCUMENT"});
                                                   return MIRESadmin.firestore().collection(snapshots_collection).doc(first_doc.id).collection(snapshots_collection).doc(snapshot_name).set({counter:counter,timestamp:timestamp,snapshot:actual_doc});
                                               }
                                               else{
                                                   return MIRESadmin.firestore().collection(snapshots_collection).doc(doc_snapshot.docs[0].id).collection(snapshots_collection).doc(snapshot_name).set({counter:counter,timestamp:timestamp,snapshot:actual_doc});
                                               }
                                           })
                                       }
                                       else {
                                           return null;
                                       }
                                   }
                                   else{
                                       return null;
                                   }
                               }
                           }
                           else{
                                   // Ignore requests on locked documents
                                   const previous_doc= change.before.data();
               
                                   if(previous_doc.hasOwnProperty("MIRES_locked") && previous_doc.MIRES_locked){
                                       return null;
                                   }
               
                                   else{
               
                                       if(previous_doc.hasOwnProperty("MIRES_snapshot")){
                                           if(((previous_doc.MIRES_snapshot+1) % versions)===0){
               
                                            previous_doc.MIRES_snapshot++;
                                            snapshot_name=previous_doc.MIRES_operation_id;
                                            timestamp= previous_doc.MIRES_timestamp;
                                            counter = previous_doc.MIRES_snapshot;
                                            previous_doc.MIRES_ignore=false;
             
                                            // Delete users recovery data
                                            if(previous_doc.hasOwnProperty("MIRES_blocked")){
                                                delete previous_doc.MIRES_blocked;
                                                delete previous_doc.MIRES_user_id;
                                            }
                                               
                                               // Store snapshot
                                               return MIRESadmin.firestore().collection(snapshots_collection).where("doc","==","DOCUMENT").get().then(async doc_snapshot=>{
                                                   if(doc_snapshot.empty){
                                                       let first_doc=await MIRESadmin.firestore().collection(snapshots_collection).add({doc:"DOCUMENT"});
                                                       return MIRESadmin.firestore().collection(snapshots_collection).doc(first_doc.id).collection(snapshots_collection).doc(snapshot_name).set({counter:counter,timestamp:timestamp,snapshot:previous_doc});
                                                   }
                                                   else{
                                                       return MIRESadmin.firestore().collection(snapshots_collection).doc(doc_snapshot.docs[0].id).collection(snapshots_collection).doc(snapshot_name).set({counter:counter,timestamp:timestamp,snapshot:previous_doc});
                                                   }
                                                   
                                               })
                                           }
                                           else{
                                               return null;
                                           }
                                       }
                                       else{
                                           return null;
                                       }
                               }
                           }
               });
               
               // Log to ignore
               function toIgnore(doc_data){
                   if(doc_data.hasOwnProperty("MIRES_ignore") && doc_data["MIRES_ignore"]===true){
                       return true;
                   }
                   else{
                       return false;
                   }
               }