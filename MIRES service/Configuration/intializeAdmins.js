/*
                      MIRES project

    Definition: file that creates the admins sdks from both
    MIRES and Application conteiners

*/

const admin = require('firebase-admin');

/* MIRES CONTAINER PROJECT */
var MIRESAdmin = admin.initializeApp({
    credential: admin.credential.cert(__dirname + "/MIRESAccountKey.json"), /* TODO */
  },"MIRES");

/* APPLICATION CONTAINER PROJECT */
var APPAdmin =admin.initializeApp({
    credential: admin.credential.cert(__dirname + "/APPAccountKey.json"),  /* TODO */
    storageBucket : "TODO", 
  },"APP");

  module.exports= {
    MIRESAdmin,
    APPAdmin
}
