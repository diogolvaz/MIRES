/**
 *    MIRES Recovery Service for mobile applications that use Firebase
 * 
 *    Thesis Project - Diogo Lopes Vaz, IST
 * 
 *    Reconstruction phase algorithm that reconstructs all the documents corrupted
 */

 //##############################################################################################
//
//                                        IMPORTS
//
//##############################################################################################

const MIRESconfig = require('../../../Configuration/MIRESconfiguration.json');
const snapshots = require('./snapshots');
const auxiliarFunctions = require('./auxiliarFunctions');

//##############################################################################################
//
//                                        VARIABLES
//
//##############################################################################################




//##############################################################################################
//
//                                        MAIN FUNCTION
//
//##############################################################################################


module.exports = {

process : async function reconstructDocuments(blocked_docs, infected_timestamp, malicious_transactions){

    var blocked_docs_lenght= blocked_docs.length;
    
    // Snapshot counters
    var corrupted_snapshot=0;
    var legitimate_snapshot=0;
  
    for (var i = 0; i < blocked_docs_lenght; i++) {
     
      // Get document snapshot
      var new_document = await snapshots.getSnapshot(blocked_docs[i],infected_timestamp[blocked_docs[i]]);
      var doc_log;
  
      if(new_document==null){
        // Get all operations that affect the document
        new_document={}
        doc_log= await getDocumentLogRecordsByDoc(blocked_docs[i],null);
      }
      else{
        // Get all operations that affect the document 
        doc_log= await getDocumentLogRecordsByDoc(blocked_docs[i],new_document.MIRES_timestamp);
        corrupted_snapshot=new_document.MIRES_snapshot;
        legitimate_snapshot=new_document.MIRES_snapshot;
      }
 
      var doc_log_lenght= doc_log.length;
      
      //Reconstruct the document
      for( var l=0;l<doc_log_lenght;l++){
        // Corrupted counter
        corrupted_snapshot++;
  
        // Delete snapshot (if activated and identified as malicious)
        if(MIRESconfig.snapshot_flow && (corrupted_snapshot % MIRESconfig.snapshot_versions)==0 && infected_timestamp[doc_log[l].data.doc]<=doc_log[l].data.timestamp){
          // Delete snapshot
          await snapshots.deleteSnapshot(doc_log[l], corrupted_snapshot);
        }
  
        if (!malicious_transactions.includes(doc_log[l].data.transaction_id)){
          // Legitimate counter
          legitimate_snapshot++;
  
          var previous_document=new_document;
          new_document = await replayLogRecord(new_document,doc_log[l]);
  
           // Create snapshot (if activated and identified as malicious)
          if(MIRESconfig.snapshot_flow && (legitimate_snapshot % MIRESconfig.snapshot_versions)==0 && infected_timestamp[doc_log[l].data.doc]<=doc_log[l].data.timestamp){
            if(new_document.length==0){
              // Delete operations
              previous_document.MIRES_snapshot++;
              await snapshots.createSnapshot(previous_document,doc_log[l].data.doc, legitimate_snapshot);
            }
            else{
              // Create/Update operations
              await snapshots.createSnapshot(new_document,doc_log[l].data.doc,legitimate_snapshot);
            }
          }
        }
        else{
          // Update malicious operation on log
          await MIRESAdmin.firestore().collection(MIRESconfig.MIRES_log).doc(doc_log[l].id).delete();
        }
      }
      // Update the document on the DB
      await replaceDocumentOnDB(new_document,blocked_docs[i])
    }
  }
}


//##############################################################################################
//
//                                        AUXILIAR FUNCTIONS
//
//##############################################################################################




// Get the log records by document name
function getDocumentLogRecordsByDoc(doc,timestamp){

    var log=[]
  
    if(timestamp==null){
      // Get all te log to reconstruct the document
      return MIRESAdmin.firestore().collection(MIRESconfig.MIRES_log).orderBy("timestamp","asc").where("doc", "==", doc).get()
      .then(snapshot => {
        if(snapshot.empty){
          console.log("getDocumentLogRecordsByDoc function: log is empty.")
          return log;
        }
        else{
          // Get the existing log
          snapshot.forEach(doc => {
            const logRow ={
              id:doc.id,
              data:doc.data(),
            }
              log.push(logRow);
          });
          return log; 
        }
        }).catch(err => {
          throw new Error("getDocumentLogRecordsByDoc function: " + err);
        });
    }
    else{
       // Get all te log to reconstruct the document
       return MIRESAdmin.firestore().collection(MIRESconfig.MIRES_log).where("doc", "==", doc).where("timestamp",">",timestamp).orderBy("timestamp","asc").get()
       .then(snapshot => {
         if(snapshot.empty){
           console.log("getDocumentLogRecordsByDoc function: log is empty.")
           return log;
         }
         else{
           // Get the existing log
           snapshot.forEach(doc => {
             const logRow ={
               id:doc.id,
               data:doc.data(),
             }
               log.push(logRow);
           });
           return log; 
         }
         }).catch(err => {
           throw new Error("getDocumentLogRecordsByDoc function: " + err);
         });
    }
  }


  // Replay record
async function replayLogRecord(new_document,request){

    // Replay record
    var new_doc={};
  
    // Create requests : we simple reexecute them
    if(request.data.type=="create"){
      new_doc=request.data.data;
      new_doc["MIRES_operation_id"] = request.data.operation_id;
      new_doc["MIRES_locked"]=false;
      if(MIRESconfig.snapshot_flow){
        new_doc["MIRES_snapshot"]=1;
        new_doc["MIRES_timestamp"]=request.data.timestamp;
      }
    }
    else if(request.data.type=="delete"){
      new_doc={};
    }
    else if(request.data.type=="update"){
      new_doc= auxiliarFunctions.merge(new_document,request.data.data);
      new_doc["MIRES_operation_id"] = request.data.operation_id;
      new_doc["MIRES_locked"]=false;
      if(MIRESconfig.snapshot_flow){
        new_doc["MIRES_snapshot"]=new_doc["MIRES_snapshot"]+1;
        new_doc["MIRES_timestamp"]=request.data.timestamp;
      }
    }
    return new_doc;
  }

  // Replace original DB for recovered DB
async function replaceDocumentOnDB(document_data,document_name){
  
    //Remove the document
    if(Object.keys(document_data).length==0){
        await APPAdmin.firestore().doc(document_name).delete()
        .catch(err => { 
          throw new Error("replaceOriginalDB function:" + err);
        })
    }
    else{
      // Replace the document
      document_data["MIRES_ignore"]=true;
      await APPAdmin.firestore().doc(document_name).set(document_data)
      .catch(err => { 
        throw new Error("replaceOriginalDB function:" + err);
      })
    }
}