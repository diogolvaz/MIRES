const functions = require('firebase-functions');

const admin = require('firebase-admin');


// Init application database reference
const Appadmin = admin.initializeApp();

const serviceAccount = require("./MIRESAccountKey.json");
const MIRESadmin = admin.initializeApp({credential: admin.credential.cert(serviceAccount),
                },"MIRES");


exports.UsernamesCreateLogger = functions.region('europe-west2').firestore.document('Usernames/{usernameId}').onCreate(async (snapshot, context) => { 
    
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
            
                // Reconstrcut the read dependency
                if(log_record.hasOwnProperty("read")){
                    let files_read_length = log_record.read.length;
                    for(let i=0;i<files_read_length;i++){
                        if(log_record.read[i].version==="MIRES_CLOUD_LOGGER_TODO"){
                            log_record.read[i].version=flag.id;
                        }
                        if(log_record.read[i].doc==="MIRES_CLOUD_LOGGER_TODO"){
                            log_record.read[i].doc="Usernames/"+context.params.usernameId;
                        }
                    }
                }
                           
                await MIRESadmin.firestore().collection("LOG").doc(flag.id).set(log_record);
                
                await Appadmin.firestore().collection("MIRES_USERS_FLAGS").doc(flag.id).delete();
                
                return null;
                
                }
            else{
                return null;
            }
        } catch (error){
            console.log(error);
            return error;
        }
            
    }    
});

exports.UsernamesUpdateLogger = functions.region('europe-west2').firestore.document('Usernames/{usernameId}').onUpdate(async (change, context) => { 
    
    // Actual doc
    const actual_doc = change.after.data();

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

                    // Reconstrcut the read dependency
                    if(log_record.hasOwnProperty("read")){
                        let files_read_length = log_record.read.length;
                        for(let i=0;i<files_read_length;i++){
                            if(log_record.read[i].version==="MIRES_CLOUD_LOGGER_TODO"){
                                log_record.read[i].version=flag.id;
                            }
                            if(log_record.read[i].doc==="MIRES_CLOUD_LOGGER_TODO"){
                                log_record.read[i].doc="Usernames/"+context.params.usernameId;
                            }
                        }
                    }

                    await MIRESadmin.firestore().collection("LOG").doc(flag.id).set(log_record);

    
                    await Appadmin.firestore().collection("MIRES_USERS_FLAGS").doc(flag.id).delete();
 
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


exports.UsersCreateLogger = functions.region('europe-west2').firestore.document('Users/{userId}').onCreate(async (snapshot, context) => { 
    
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
            
                // Reconstrcut the read dependency
                if(log_record.hasOwnProperty("read")){
                    let files_read_length = log_record.read.length;
                    for(let i=0;i<files_read_length;i++){
                        if(log_record.read[i].version==="MIRES_CLOUD_LOGGER_TODO"){
                            log_record.read[i].version=flag.id;
                        }
                        if(log_record.read[i].doc==="MIRES_CLOUD_LOGGER_TODO"){
                            log_record.read[i].doc="Users/"+context.params.userId;
                        }
                    }
                }
                           
                await MIRESadmin.firestore().collection("LOG").doc(flag.id).set(log_record);
                
                await Appadmin.firestore().collection("MIRES_USERS_FLAGS").doc(flag.id).delete();
                
                return null;
                
                }
            else{
                return null;
            }
        } catch (error){
            console.log(error);
            return error;
        }
            
    }    
});

exports.UsersUpdateLogger = functions.region('europe-west2').firestore.document('Users/{userId}').onUpdate(async (change, context) => { 
    
    // Actual doc
    const actual_doc = change.after.data();

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

                    // Reconstrcut the read dependency
                    if(log_record.hasOwnProperty("read")){
                        let files_read_length = log_record.read.length;
                        for(let i=0;i<files_read_length;i++){
                            if(log_record.read[i].version==="MIRES_CLOUD_LOGGER_TODO"){
                                log_record.read[i].version=flag.id;
                            }
                            if(log_record.read[i].doc==="MIRES_CLOUD_LOGGER_TODO"){
                                log_record.read[i].doc="Users/"+context.params.userId;
                            }
                        }
                    }

                    await MIRESadmin.firestore().collection("LOG").doc(flag.id).set(log_record);

    
                    await Appadmin.firestore().collection("MIRES_USERS_FLAGS").doc(flag.id).delete();
 
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


exports.PostsCreateLogger = functions.region('europe-west2').firestore.document('Posts/{postId}').onCreate(async (snapshot, context) => { 
    
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

                    // Reconstrcut the read dependency
                    if(log_record.hasOwnProperty("read")){
                        let files_read_length = log_record.read.length;
                        for(let i=0;i<files_read_length;i++){
                            if(log_record.read[i].version==="MIRES_CLOUD_LOGGER_TODO"){
                                log_record.read[i].version=flag.id;
                            }
                            if(log_record.read[i].doc==="MIRES_CLOUD_LOGGER_TODO"){
                                log_record.read[i].doc="Posts/"+context.params.postId;
                            }
                        }
                    }
               
                    await MIRESadmin.firestore().collection("LOG").doc(flag.id).set(log_record);

                    await Appadmin.firestore().collection("MIRES_USERS_FLAGS").doc(flag.id).delete();

               return null;

            }else{
                return null;
            }
        } catch (error){
            console.log(error);
            return error;
        }

        }    
});

exports.PostsUpdateLogger = functions.region('europe-west2').firestore.document('Posts/{postId}').onUpdate(async (change, context) => { 
    
    // Actual doc
    const actual_doc = change.after.data();

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

                    // Reconstrcut the read dependency
                    if(log_record.hasOwnProperty("read")){
                        let files_read_length = log_record.read.length;
                        for(let i=0;i<files_read_length;i++){
                            if(log_record.read[i].version==="MIRES_CLOUD_LOGGER_TODO"){
                                log_record.read[i].version=flag.id;
                            }
                            if(log_record.read[i].doc==="MIRES_CLOUD_LOGGER_TODO"){
                                log_record.read[i].doc="Posts/"+context.params.postId;
                            }
                        }
                    }

                    await MIRESadmin.firestore().collection("LOG").doc(flag.id).set(log_record);

    
                    await Appadmin.firestore().collection("MIRES_USERS_FLAGS").doc(flag.id).delete();
 
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


exports.FlagsLogger = functions.region('europe-west2').firestore.document('MIRES_USERS_FLAGS/{flagID}').onCreate(async (snapshot, context) => { 

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

exports.SnapshotCreator = functions.region('europe-west2').firestore.document('Users/{userId}').onWrite(async (change, context) => { 

     // Number of versions
     const versions=5;

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

                           // Set blocked property to true (if exists)
                           if(actual_doc.hasOwnProperty("MIRES_blocked")){
                                delete actual_doc.MIRES_blocked;
                                delete actual_doc.MIRES_user_id;
                            }
                        
                            // Store snapshot
                            return MIRESadmin.firestore().collection("SNAPSHOTS").where("doc","==","Users/"+context.params.userId).get().then(async doc_snapshot=>{
                                if(doc_snapshot.empty){
                                    let first_doc= await MIRESadmin.firestore().collection("SNAPSHOTS").add({doc:"USers/"+context.params.userId});
                                    return MIRESadmin.firestore().collection("SNAPSHOTS").doc(first_doc.id).collection("snapshots").doc(snapshot_name).set({counter:counter,timestamp:timestamp,snapshot:actual_doc});
                                }
                                else{
                                    return MIRESadmin.firestore().collection("SNAPSHOTS").doc(doc_snapshot.docs[0].id).collection("snapshots").doc(snapshot_name).set({counter:counter,timestamp:timestamp,snapshot:actual_doc});
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

                               // Set blocked property to true (if exists)
                               if(previous_doc.hasOwnProperty("MIRES_blocked")){
                                   delete previous_doc.MIRES_blocked;
                                   delete previous_doc.MIRES_user_id;
                               }
                                
                                // Store snapshot
                                return MIRESadmin.firestore().collection("SNAPSHOTS").where("doc","==","Users/"+context.params.userId).get().then(async doc_snapshot=>{
                                    if(doc_snapshot.empty){
                                        let first_doc=await MIRESadmin.firestore().collection("SNAPSHOTS").add({doc:"Users/"+context.params.userId});
                                        return MIRESadmin.firestore().collection("SNAPSHOTS").doc(first_doc.id).collection("snapshots").doc(snapshot_name).set({counter:counter,timestamp:timestamp,snapshot:previous_doc});
                                    }
                                    else{
                                        return MIRESadmin.firestore().collection("SNAPSHOTS").doc(doc_snapshot.docs[0].id).collection("snapshots").doc(snapshot_name).set({counter:counter,timestamp:timestamp,snapshot:previous_doc});
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


exports.logFiles = functions.region('europe-west2').storage.object().onFinalize(async object => {
    try{
        const file_name = object.name;
        const metadata = object.metadata;
        const generation = object.generation;
        const bucket = object.bucket
        const timestamp = object.timeCreated;
        const transaction_id = metadata["transaction_id"];
        const ignore = metadata['MIRES_ignore']

        if(ignore==="false"){
            // Store file data
            return MIRESadmin.firestore().collection("STORAGE").add({transaction_id: transaction_id,file_path:file_name, generation: generation, timestamp: timestamp, bucket_name:bucket, type:"upload"})
                .then(function(){
                    return null;
            })
            .catch(function(error) {
                console.error("File data: "+error);
            })
        }
        else{
            return null;
        }
    }catch(error) {
        console.error("Error when logging an upload operation: "+error);
    }
})


exports.logDeletedFiles = functions.region('europe-west2').storage.object().onArchive(async object => {
    try{
        const file_name = object.name;
        const timestamp = object.timeCreated;
        const bucket = object.bucket;
        const metadata = object.metadata;
        const ignore = metadata['MIRES_ignore']
        
        if(ignore==="false"){
            // Store file data
            return MIRESadmin.firestore().collection("STORAGE").add({bucket_name:bucket,file_path:file_name,timestamp:timestamp, type:"delete"})
                .then(function(){
                    return null;
            })
            .catch(function(error) {
                console.error("File data: "+error);
            })
        }
        else{
            return null;
        }
    }
    catch(error) {
        console.error("Error when logging a delete operation: "+error);
    }
})
