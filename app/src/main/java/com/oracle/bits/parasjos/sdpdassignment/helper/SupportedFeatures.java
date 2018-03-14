package com.oracle.bits.parasjos.sdpdassignment.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.oracle.bits.parasjos.sdpdassignment.ContactDetailsActivity;
import com.oracle.bits.parasjos.sdpdassignment.PhoneDetailsActivity;
import com.oracle.bits.parasjos.sdpdassignment.R;
import com.oracle.bits.parasjos.sdpdassignment.SlateActivity;
import com.oracle.bits.parasjos.sdpdassignment.SmsActivity;
import com.oracle.bits.parasjos.sdpdassignment.TabbedSmsActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by parasjos on 26-02-2018.
 */

public class SupportedFeatures {

    private static Map<String, ContactDetailsActivity.ContactDetails> contactMap = new HashMap<String,ContactDetailsActivity.ContactDetails>();
    private static Map<String, ActivityMeta> activityMetaMap = new HashMap<>();
    public static DateFormat dateFormat = new SimpleDateFormat("dd-MMM, yyyy HH:mm a");;
    public static List<ContactDetailsActivity.ContactDetails> globalContactList= new ArrayList<>();

    private static List<String> supportedActivities= new ArrayList();
    static{
        supportedActivities.add(ContactDetailsActivity.activityKey);
        supportedActivities.add(SmsActivity.activityKey);
        supportedActivities.add(TabbedSmsActivity.activityKey);
        supportedActivities.add(PhoneDetailsActivity.activityKey);
        supportedActivities.add(SlateActivity.activityKey);
        supportedActivities.add("Photo Viewer");

        //Create map of activity meta
        activityMetaMap.put(ContactDetailsActivity.activityKey,
                new ActivityMeta(ContactDetailsActivity.activityKey,
                        R.drawable.ic_home_contact_icon,
                        R.string.title_activity_contact,
                        R.string.contact_activity_desc,
                        ContactDetailsActivity.class));
        activityMetaMap.put(SmsActivity.activityKey,
                new ActivityMeta(SmsActivity.activityKey,
                        R.drawable.ic_home_sms_icon,
                        R.string.title_activity_sentsms,
                        R.string.sms_activity_desc,
                        SmsActivity.class));
        activityMetaMap.put(TabbedSmsActivity.activityKey,
                new ActivityMeta(TabbedSmsActivity.activityKey,
                        R.drawable.ic_home_all_sms_icon,
                        R.string.title_activity_allsms,
                        R.string.all_sms_activity_desc,
                        TabbedSmsActivity.class));
        activityMetaMap.put(SlateActivity.activityKey,
                new ActivityMeta(SlateActivity.activityKey,
                        R.drawable.ic_home_slate_icon,
                        R.string.title_activity_slate,
                        R.string.slate_activity_desc,
                        SlateActivity.class));
        activityMetaMap.put(PhoneDetailsActivity.activityKey,
                new ActivityMeta(PhoneDetailsActivity.activityKey,
                R.drawable.ic_home_phone_status_icon,
                R.string.title_activity_phone_details,
                R.string.phone_details_activity_desc,
                        PhoneDetailsActivity.class));
    }

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    public static final int PERMISSION_READ_STATE = 100;
    public static final int PERMISSION_READ_SMS = 200;
    public static final int PERMISSION_READ_CONTACT = 300;
    public static final int PERMISSION_ACCESS_GALLERY = 400;

    public  static ActivityMeta getActivityMetaForKey(String key){
        if(key !=null && !key.isEmpty()){
            return activityMetaMap.get(key);
        }
        return null;
    }



    public static List getSupportedActivities(){
        return supportedActivities;
    }

    public static boolean hasAccess(Activity activity, String permission) {
        int permissionCheck = ContextCompat.checkSelfPermission(activity,
                permission);

        if ((permissionCheck == PackageManager.PERMISSION_GRANTED)) {
            return true;
        }else{
            return false;
        }
    }

    public static void requestAccess(Activity activity, String permission, String[] permissions, int requestCode) {
        int permissionCheck = ContextCompat.checkSelfPermission(activity,
                permission);

        if (!(permissionCheck == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(activity,
                    permissions,
                    requestCode);
        }
    }

    public static boolean checkPermissionGrantResult(@NonNull int[] grantResults){
        if (grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }

    }

    public static class ActivityMeta{
        public String activityKey;
        public Class<? extends Activity> activityClass;
        public Integer activityIcon;
        public Integer activityLabel;
        public Integer activityDescription;

        public ActivityMeta(String activityKey, Integer activityIcon, Integer activityLabel, Integer activityDescription,Class<? extends Activity> activityClass) {
            this.activityKey = activityKey;
            this.activityIcon = activityIcon;
            this.activityLabel = activityLabel;
            this.activityDescription = activityDescription;
            this.activityClass = activityClass;
        }
    }

    public static synchronized ContactDetailsActivity.ContactDetails contactLookUp(String phNo){
        ContactDetailsActivity.ContactDetails contact = null;
        if(phNo!=null && !phNo.isEmpty()) {
            if (contactMap != null && !contactMap.isEmpty()) {
                contact = contactMap.get(phNo);
            }
        }
        return contact;
    }

    public static synchronized void populateContactLookUp(List<ContactDetailsActivity.ContactDetails> contactsList){
        if (contactMap==null){
            contactMap = new HashMap<>();
        }
        if (contactsList!=null && !contactsList.isEmpty()){
            for (ContactDetailsActivity.ContactDetails contact:
                 contactsList) {
                contactMap.put(contact.getPhoneNumber(),contact);
            }
        }

    }

    public static void updateGlobalContactList(List<ContactDetailsActivity.ContactDetails> contactList){
        if(contactList!=null && !contactList.isEmpty()){
            globalContactList.addAll(contactList);
        }

    }
}
