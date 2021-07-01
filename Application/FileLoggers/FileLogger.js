const functions = require('firebase-functions');

const admin = require('firebase-admin');


const serviceAccount = require("TO COMPLETE");
const MIRESadmin = admin.initializeApp({credential: admin.credential.cert(serviceAccount),
                },"MIRES");


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
