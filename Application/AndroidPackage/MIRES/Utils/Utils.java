package com.amsavarthan.social.hify.MIRES.Utils;


//###############################################################################################
//
//                                      Utils
//
//                              Class with global names
//
//###############################################################################################

import android.content.Context;

import com.amsavarthan.social.hify.ui.activities.post.PostText;

import es.dmoral.toasty.Toasty;

public class Utils {

    public static String MIRES_users_file_flags= "MIRES_USERS_FILE_FLAGS";

    // MIRES users flags
    public static String MIRES_users_flags= "MIRES_USERS_FLAGS";

    // MIRES users recovery
    public static final String Mires_users_recovery ="MIRES_USERS_RECOVERY";

    // MIRES Logger module tag
    public static String MIRES_cloud_logger_todo = "MIRES_CLOUD_LOGGER_TODO";

    // MIRES users recovery
    public static final String Mires_users_tokens ="MIRES_USERS_TOKENS";

    // MIRES create request
    public static String CREATE="create";

    // MIRES update request
    public static String UPDATE="update";

    // MIRES delete request
    public static String DELETE="delete";

    // Action not allowed
    public static void ActionNotAllowed(Context context){
        Toasty.error(context, "Action not possible", Toasty.LENGTH_SHORT, true).show();
    }

}
