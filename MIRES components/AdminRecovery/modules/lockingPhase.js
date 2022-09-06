/**
 *    MIRES Recovery Service for mobile applications that use Firebase
 * 
 *    Thesis Project - Diogo Lopes Vaz, IST
 *  
 *    locking phase algorithm that analyses the log and identifies the corrupted documents, malicious transactions and dependencies
 */


//##############################################################################################
//
//                                        IMPORTs
//
//##############################################################################################

const auxiliarFunctions = require('./auxiliarFunctions');
const globalVariables = require('./globalVariables');


//##############################################################################################
//
//                                       VARIABLES
//
//##############################################################################################

// New documents recovered
var new_documents = {}
// Data corrupted on each document
var corrupted_data={};


//##############################################################################################
//
//                                        MAIN FUNCTION
//
//##############################################################################################



module.exports = {

    process : async function process(transaction_id,log){

    var log_lenght= log.length;
    for (var i = globalVariables.getTransactionBeggin(transaction_id); i < log_lenght; i++) {
      
      // Save the begin of each transaction
      if(!globalVariables.transactionBegginingAlreadyStored(log[i].data.transaction_id)){
        globalVariables.addBegginingOfTransaction(log[i].data.transaction_id,i);
      }
  
      // If the request is part of the transaction already analyzed
      if(transaction_id==log[i].data.transaction_id || globalVariables.maliciousTransactionAlreadyStored(log[i].data.transaction_id)){
   
          if(!globalVariables.blockedDocAlreadyStored(log[i].data.doc)){
            globalVariables.addDocToBlock(log[i].data.doc);
            globalVariables.addTimestampOfInfectedTransaction(log[i].data.doc,log[i].data.timestamp)
            await lockDocument(log[i].data.doc);
          }
                    
           // Update corrupted data
          updateCorruptedData(log[i],false);

          if(log[i].data.type=="delete") {
            if(new_documents.hasOwnProperty(log[i].data.doc)){
              if(new_documents[log[i].data.doc]==null){
                delete new_documents[log[i].data.doc];
              }
  
            }
            else{
              new_documents[log[i].data.doc]={};
            }
          }
  
          // If it is a create request, then the document should not exist and we must invert both read and write requests to this document
          else if(log[i].data.type=="create"){
            if(new_documents.hasOwnProperty(log[i].data.doc)){
              if(new_documents[log[i].data.doc]=={}){
                delete new_documents[log[i].data.doc];
              }
            }
            else{
              // Invert the request
              new_documents[log[i].data.doc]=null;
            }
          }
      }
      
      else{
        // Invert entire interaction if one request of it reads from an affected document
        if(readToInvert(log[i])){
          globalVariables.addTransactionToAnalyse(log[i].data.transaction_id);
          return;
        }
        else if(isFromSubcollection(log[i]) && maxFileDeletedOnRecovery(log[i]))
        { 
          globalVariables.addTransactionToAnalyse(log[i].data.transaction_id);
          return;
        }
        // Replay record
       else if(corrupted_data.hasOwnProperty(log[i].data.doc)){
  
         // If the document was malicious created 
         if(new_documents.hasOwnProperty(log[i].data.doc) && new_documents[log[i].data.doc]==null){
          globalVariables.addTransactionToAnalyse(log[i].data.transaction_id);
          return;
        }
        // If the document was malicious deleted (catch create requests)
        else if (new_documents.hasOwnProperty(log[i].data.doc) && Object.keys(new_documents[log[i].data.doc]).length==0){
          globalVariables.addTransactionToAnalyse(log[i].data.transaction_id);
          return;
        }
        // Normal blind writes 
        else{      
            updateCorruptedData(log[i],true);
            }     
       }
       
      }
    }
  },
  resetVariables : function resetVariables(){
      // New documents recovered
      new_documents = {}
      // Data corrupted on each document
      corrupted_data={};
    }
}


//##############################################################################################
//
//                                 AUXILIAR FUNCTIONS
//
//##############################################################################################

// Block the documents
async function lockDocument(doc_path){

    await APPAdmin.firestore().doc(doc_path).get().then(async doc =>{ 
       if(doc.exists){
           await APPAdmin.firestore().doc(doc_path).set({MIRES_locked:true,MIRES_ignore:true},{merge:true}).catch(err => {
             throw new Error("lockDocuments function: " + err);
           });
       }
       else{
         await APPAdmin.firestore().doc(doc_path).set({MIRES_locked:true,MIRES_ignore:true}).catch(err => {
           throw new Error("lockDocuments function: " + err);
           })
       }
     }).catch(err => {
       throw new Error("lockDocuments function: " + err);
     });
 }


 // Function that saves the corrupted versions of the documents
function updateCorruptedData(request,replay){

    // Create corrupted document
    if(!corrupted_data.hasOwnProperty(request.data.doc)){
      corrupted_data[request.data.doc]={};
    }

    // If it is a delete request
    if(request.data.type=="delete"){
      corrupted_data[request.data.doc][request.id] = {};
    }
    else{

      // Create corrupted document version
        if(!corrupted_data[request.data.doc].hasOwnProperty(request.id)){
          corrupted_data[request.data.doc][request.id]={};
        }

        // Merge corrupted data
        if(!replay){
          if(corrupted_data[request.data.doc].hasOwnProperty("last")){
            corrupted_data[request.data.doc][request.id] =  auxiliarFunctions.merge(corrupted_data[request.data.doc][corrupted_data[request.data.doc]["last"]],request.data.data);
          }
          else{
            corrupted_data[request.data.doc][request.id] = request.data.data;
          }
        }
        // Blind write
        else{
          corrupted_data[request.data.doc][request.id] = diff(corrupted_data[request.data.doc][corrupted_data[request.data.doc]["last"]],request.data.data);
          }
  }
  corrupted_data[request.data.doc]["last"]=request.id; 
}


// Check if the read must be inverted
function readToInvert(request){
    // If the operation was affected
    if(request.data.hasOwnProperty("read")){
      var read_length = request.data.read.length;
      // For each file readed
      for( var f=0; f<read_length ;f++){
        // We see if that file is corrupted
        if(Object.keys(corrupted_data).includes(request.data.read[f].doc)){
          // We see if there is a structural dependency
          if(request.data.read[f].version==null && new_documents.hasOwnProperty(request.data.read[f].doc) && new_documents[request.data.read[f].doc]==null){
            return true;
          }
          // We see if, from the corrupted file, I have readed an corrupted version
          else if(Object.keys(corrupted_data[request.data.read[f].doc]).includes(request.data.read[f].version)){
            for(var d=0; d<request.data.read[f].data.length ;d++){
              var fieldPath = request.data.read[f].data[d].split('.');
              // Then we see if, from the corrupted version, we have read corrupted data 
              if(matchDataRead(corrupted_data[request.data.read[f].doc][request.data.read[f].version],fieldPath)){
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }

  // See if file is in a subcollection
function isFromSubcollection(request){
    var doc_name = request.data.doc.split("/");
    if((doc_name.length-2)>1){
      return true;
    }
    else{
      return false;
    }
  }
  
  
  // Check if the file thar contains the subcollection exists
  function maxFileDeletedOnRecovery(request){
    var doc_name = request.data.doc.split("/");
    doc_name.pop();
    doc_name.pop();
    var max_doc = doc_name.join("/");
    if(new_documents.hasOwnProperty(max_doc) && new_documents[max_doc]==null){
      return true;
    }
    else{
      return false;
    }
  }

  // Match the data readed
function matchDataRead(corrupted, keys_readed) {
  
    //If the file that we read does not exist, we must invert this interaction
    if(corrupted == null){
      return true;
    }
    
    //If the corrupted version is empty, the request has not readed corrupted data
    if(Object.keys(corrupted).length==0){
      return false;
    }
  
    else{
      var field= keys_readed.shift();
      //If the corrupted contains this key
      if(corrupted.hasOwnProperty(field))
      {
        // Return true if is the last key of the path
        if(keys_readed.length==0)
          {
          return true;
          }
        // If not, we continue to analyze
        else
          {
          return matchDataRead(corrupted[field],keys_readed);
          }
      }
      else 
      {
        return false
      }
    }
  }

  
// Diff between two json objects
function diff(corrupted, new_data){

    const result={}; 
  
    // If the corrupted data is already empty, return an empty version
    if(Object.keys(corrupted).length==0)
    { return result; }
  
    Object.keys(corrupted).forEach(key => {
      // If the field has not been modified
      if (!new_data.hasOwnProperty(key) && corrupted.hasOwnProperty(key)){ 
        result[key] = corrupted[key];
      }   
    });
    return result;
  }

