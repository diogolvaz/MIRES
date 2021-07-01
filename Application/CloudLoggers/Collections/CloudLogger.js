const functions = require('firebase-functions');

const admin = require('firebase-admin');


// Init application database reference
const Appadmin = admin.initializeApp();

const serviceAccount = require("* path to the MIRES account key file *, e.g., ./MIRESServiceAccountKey.json");
const MIRESadmin = admin.initializeApp({credential: admin.credential.cert(serviceAccount),
                },"MIRES");

exports.CreateOperationsLogger = functions.region('* define the region to deploy *, e.g., europe-west2').firestore.document('* collection *').onCreate(async (snapshot, context) => { 
    
        // Actual doc
        const actual_doc =  snapshot.data();

        // To ignore the logging process
        if (toIgnore(actual_doc)){
            return null;
        }
        
        else{
            try{
           // Process Flag
           var flag = await Appadmin.firestore().collection("MIRES_USERS_FLAGS").doc(actual_doc.MIRES_operation_id).get();

                var log_record;

                if(flag.exists){
            
                    log_record=flag.data();
                    delete log_record.locked;
                    delete log_record.ignore;
    
                     // Delete blocked property (if exists)
                     if(log_record.hasOwnProperty("blocked")){
                        delete log_record.blocked;
                    }
    
                    
                    // If type operation is null
                    if(log_record.type==="MIRES_CLOUD_LOGGER_TODO"){
                            log_record["type"]="create"
                    }
                
                    // Reconstruct the data
                    let data_structure = log_record.data;
                    for (const key1 of Object.keys(data_structure)){
                        data_structure[key1]=actual_doc[key1];
                    }
            
                    log_record["data"]=data_structure;

                    // Reconstruct the read dependency
                    if(log_record.hasOwnProperty("read")){
                        let files_read_length = log_record.read.length;
                        for(let i=0;i<files_read_length;i++){
                            if(log_record.read[i].version==="MIRES_CLOUD_LOGGER_TODO"){
                                log_record.read[i].version=flag.id;
                            }
                            if(log_record.read[i].doc==="MIRES_CLOUD_LOGGER_TODO"){
                                log_record.read[i].doc="* collection *"+context.params.postId;
                            }
                        }
                    }
               
               await MIRESadmin.firestore().collection("LOG").doc(flag.id).set(log_record);

               await flag.delete();

               return null;

            }else{
                return null;
            }
        } catch (error){
            console.log("Error: "+error);
            return error;
        }

        }    
});

exports.UpdateOperationsLogger = functions.region('* define the region to deploy *, e.g., europe-west2').firestore.document('* collection *').onUpdate(async (change, context) => { 
    
    // Actual doc
    const actual_doc =  change.after.data();

    // To ignore the logging process
    if (toIgnore(actual_doc)){
        return null;
    }
    
    else{

        try{
        const previous_doc =  change.before.data();
   
        const flag = await Appadmin.firestore().collection("MIRES_USERS_FLAGS").doc(actual_doc.MIRES_operation_id).get();

            var log_record;

            if(flag.exists){
        
                log_record=flag.data();
                delete log_record.locked;
                delete log_record.ignore;

                 // Delete blocked property (if exists)
                 if(log_record.hasOwnProperty("blocked")){
                    delete log_record.blocked;
                }

                
                // If type operation is null
                if(log_record.type==="MIRES_CLOUD_LOGGER_TODO"){
                        log_record["type"]="update"
                }
            
                // Reconstruct the data
                let data_structure = log_record.data;
                for (const key1 of Object.keys(data_structure)){
                    if(actual_doc.hasOwnProperty(key1)){
                        data_structure[key1]=actual_doc[key1];
                    }
                    else{
                        data_structure[key1]="REMOVED";
                    }
                }
                for (const key2 of Object.keys(previous_doc)){
                        if(!actual_doc.hasOwnProperty(key2)){
                            data_structure[key2]="REMOVED";
                        }
                    }  

                log_record["data"]=data_structure;

                // Reconstruct the read dependency
                if(log_record.hasOwnProperty("read")){
                    let files_read_length = log_record.read.length;
                    for(let i=0;i<files_read_length;i++){
                        if(log_record.read[i].version==="MIRES_CLOUD_LOGGER_TODO"){
                            log_record.read[i].version=flag.id;
                        }
                        if(log_record.read[i].doc==="MIRES_CLOUD_LOGGER_TODO"){
                            log_record.read[i].doc="* collection *"+context.params.postId;
                        }
                    }
                }

                await MIRESadmin.firestore().collection("LOG").doc(flag.id).set(log_record);
               
                await flag.delete();

                return null;
            }
            else{
                return null;
            }
        } catch (error){
            console.log("Error: "+err);
            return error;
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
