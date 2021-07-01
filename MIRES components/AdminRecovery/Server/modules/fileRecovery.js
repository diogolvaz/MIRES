/*
 *    MIRES Recovery Service for mobile applications that use Firebase
 *  
 *    File Recovery module : module that recovers files
 */

//############################################################################################
//
//                                     IMPORTs
//
//############################################################################################

const MIRESconfig = require('../../../Configuration/MIRESconfiguration.json');
const initializeApp = require('../../../Configuration/intializeAdmins');
const globalVariables = require('./globalVariables');
const {Storage} = require('@google-cloud/storage');

// Admin SDKs
APPAdmin = initializeApp.APPAdmin;
MIRESAdmin = initializeApp.MIRESAdmin;


//##############################################################################################
//
//                                        MAIN FUNCTION
//
//##############################################################################################

module.exports = {

    getLog : async function getLog(){
        log = await getFilesLog()
        return log
    },

    // Block files function
    blockFiles : async function blockFiles(malicious_transaction, log){
      await blockFile(malicious_transaction,log);
        
    },

    // Reconstruct files
    reconstructFiles: async function reconstructFiles(files, malicious_transactions){
        for(const file_path of files){
            await reconstructFile(file_path, malicious_transactions); 
        }
    }

}

//##############################################################################################
//
//                                 AUXILIAR FUNCTIONS
//
//##############################################################################################

function getFilesLog(){

    return MIRESAdmin.firestore().collection(MIRESconfig.MIRES_storage_log).get()
        .then(log =>{
            files_log={}
                log.forEach(log_row=>{
                    if(!files_log.hasOwnProperty(log_row.data().transaction_id)){
                        files_log[log_row.data().transaction_id] = {}
                    }
                    if(!files_log[log_row.data().transaction_id].hasOwnProperty(log_row.id)){
                        files_log[log_row.data().transaction_id][log_row.id] = {}
                    }
                    files_log[log_row.data().transaction_id][log_row.id]={
                        file_path:log_row.data().file_path,
                        generation: log_row.data().generation,
                        type: log_row.data().type} 
                })
                return files_log
        })
}



// block files (if existing)
async function blockFile(malicious_transaction, log){

    // get the files related with the transaction
    files = log[malicious_transaction]

    // block each file
    for (const file in files){
        if(!globalVariables.blockedFileAlreadyStored(files[file]["file_path"])){

            var metadata = {
                metadata: {
                'MIRES_locked':'true',
                'MIRES_ignore':'true'
                }
            }

            await APPAdmin.storage().bucket().file(files[file]["file_path"]).setMetadata(metadata).then(function() {
                globalVariables.addFileToBlock(files[file]["file_path"]);
            }).catch(function(error) {
                console.log("File not deleted or not existing.")
            });
        }
    }

}


// function that reconstructs the File
async function reconstructFile(file_path, malicious_transactions){
    
    file={}

    log = await getFileLog(file_path);

    for(const log_row of log){
        if(malicious_transactions.includes(log_row.transaction_id)){
            await deleteFileLogRow(log_row.id)
        }
        else{
            if(log_row.type=="upload"){
                file=log_row
            }
            else if(log_row.type=="delete"){
                // If the file is already empty
                if(Object.keys(file).length === 0){
                    await deleteFileLogRow(log_row.id)
                }
                else{
                    file={}
                }
            }
            
        }
    }
    // Replace file
    await updateFile(file, file_path);
}

// Function that returns the file log
function getFileLog(file){
    file_log=[]
    return MIRESAdmin.firestore().collection(MIRESconfig.MIRES_storage_log).where('file_path','==',file).orderBy("timestamp","asc").get()
    .then(log => {
        if(log.empty){
            console.log("getFileLog function: there are no logs.")
            return file_log;
        }
        else{
            log.forEach(row =>{
                file_log.push({
                    id:row.id,
                    file_path:row.data().file_path,
                    generation: row.data().generation,
                    type: row.data().type,
                    transaction_id : row.data().transaction_id,
                });
            })
            return file_log;
        }
    }) .catch(err => {
        console.log("getFileLog function: "+ err);
        });  
}

// Delete log row
async function deleteFileLogRow(id){
    await MIRESAdmin.firestore().collection(MIRESconfig.MIRES_storage_log).doc(id).delete().then(function(){
    })
    .catch(err => { 
        console.log("deleteFileLogRow function:" + err);
      })
}


async function updateFile(file, file_path){
    
    // If the file is empty, then it must be deleted
    if(Object.keys(file).length === 0){

        const storage = APPAdmin.storage();

        await storage.bucket().file(file_path).delete().then(function() {
          }).catch(function(error) {
            console.log("File not deleted or not existing.")
          });
    }
    // Else, get the file and replace on the database
    else{
        const cloud_storage = new Storage();

        srcBucketName = file.bucket_name;
        destBucketName = file.bucket_name;
        generation = file.generation;
        srcFilename = file_path;
        destFilename = file_path;
        await cloud_storage.bucket().file(srcFilename, 
            {
            generation,
            })
            .catch(err=>{
                console.log("updateFile function: " + err);
            })
            .copy(storage.bucket(destBucketName).file(destFilename));
        }

}