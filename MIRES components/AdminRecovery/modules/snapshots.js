
/**
 *    MIRES Recovery Service for mobile applications that use Firebase
 * 
 *    Thesis Project - Diogo Lopes Vaz, IST
 * 
 *    Module that contains function to load, create and delete snapshots
 */


//############################################################################################
//
//                                     IMPORTs
//
//############################################################################################

 // Import
const MIRESconfig = require('../../../Configuration/MIRESconfiguration.json');
const initializeApp = require('../../../Configuration/intializeAdmins');


// Admin SDKs
APPAdmin = initializeApp.APPAdmin;
MIRESAdmin = initializeApp.MIRESAdmin;


//############################################################################################
//
//                                     VARIABLES
//
//############################################################################################


//############################################################################################
//
//                                     SNAPSHOT FUNCTIONS
//
//############################################################################################

  module.exports ={
    createSnapshot : async function createSnapshot(actual_doc, doc_name,counter){
    
      await MIRESAdmin.firestore().collection(MIRESconfig.MIRES_snapshots).where("doc", "==",doc_name).get()
      .then(async first=>{
        if(first.empty){
          let first_doc= await MIRESAdmin.firestore().collection(MIRESconfig.MIRES_snapshots).add({doc:doc_name});
          actual_doc.MIRES_ignore=false;
          console.log("--> createSnapshot functions: snapshot created");
          await MIRESAdmin.firestore().collection(MIRESconfig.MIRES_snapshots).doc(first_doc.id).collection(MIRESconfig.MIRES_snapshots).doc(actual_doc.MIRES_operation_id).set({counter: counter, timestamp: actual_doc.MIRES_timestamp,snapshot:actual_doc});
          return;
        }
        else{
          var begin_snapshot = first.docs[0];
          actual_doc.MIRES_ignore=false;
          console.log("--> createSnapshot functions: snapshot created");
          return MIRESAdmin.firestore().collection(MIRESconfig.MIRES_snapshots).doc(begin_snapshot.id).collection(MIRESconfig.MIRES_snapshots).doc(actual_doc.MIRES_operation_id).set({counter: counter,timestamp:actual_doc.MIRES_timestamp,snapshot:actual_doc});
        } 
      }).catch(err => {
        throw new Error("createSnapshot function: " + err);
      });
    },

    // Delete snapshot
    deleteSnapshot: async function deleteSnapshot(request, counter){

    await MIRESAdmin.firestore().collection(MIRESconfig.MIRES_snapshots).where("doc", "==", request.data.doc).get()
    .then(async first=>{
      if(first.empty){
        console.log("deleteSnapshot function: no snapshot to delete.");
        return;
      }
      else{
        var begin_snapshot = first.docs[0];
        await MIRESAdmin.firestore().collection(MIRESconfig.MIRES_snapshots).doc(begin_snapshot.id).collection(MIRESconfig.MIRES_snapshots).where("counter","==",counter).get()
        .then( async snapshots=>{
          if(!snapshots.empty){
            var snapshot = snapshots.docs[0];
            console.log("deleteSnapshot functions: snapshot deleted.");
            await MIRESAdmin.firestore().collection(MIRESconfig.MIRES_snapshots).doc(begin_snapshot.id).collection(MIRESconfig.MIRES_snapshots).doc(snapshot.id).delete();

            // Delete the first document when the snapshots collection is empty
            await MIRESAdmin.firestore().collection(MIRESconfig.MIRES_snapshots).doc(begin_snapshot.id).collection(MIRESconfig.MIRES_snapshots).get().then(async collection=>{
              if(collection.empty){
                console.log("deleteSnapshot functions: snapshots empty.");
                await MIRESAdmin.firestore().collection(MIRESconfig.MIRES_snapshots).doc(begin_snapshot.id).delete();
              }
              else{
                return;
              }
            })
          }
          else{
            console.log("deleteSnapshot function: no snapshot to delete.");
            return;
          }
        }).catch(err => {
          throw new Error("deleteSnapshot function: " + err);
        });
      } 
    }).catch(err => {
      throw new Error("deleteSnapshot function: " + err);
    });
    },

     // Get document snapshot
     getSnapshot: function getSnapshot(doc, timestamp){
      return MIRESAdmin.firestore().collection(MIRESconfig.MIRES_snapshots).where("doc", "==", doc).get()
        .then(first=>{
          if(first.empty){
            // There are no snapshots - False
            return null;
          }
          else{
            var begin_snapshot = first.docs[0];
            return MIRESAdmin.firestore().collection(MIRESconfig.MIRES_snapshots).doc(begin_snapshot.id).collection(MIRESconfig.MIRES_snapshots).where("timestamp","<",timestamp).orderBy("timestamp","desc").limit(1).get()
            .then(snapshots=>{
              if(snapshots.empty){
                return null;
              }else{
                console.log("getSnapshot function: Snapshot loaded");
                return snapshots.docs[0].data().snapshot;
              }
            }).catch(err => {
              throw new Error("getSnapshot function: " + err);
            });
          } 
      }).catch(err => {
        throw new Error("getSnapshot function: " + err);
      });
    }
}