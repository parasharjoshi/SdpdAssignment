package com.oracle.bits.parasjos.sdpdassignment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.oracle.bits.parasjos.sdpdassignment.helper.SupportedFeatures;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by parasjos on 26-02-2018.
 */

public class ContactDetailsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    public static final String activityKey = "Contact Details";
    private LoadContact mLoadTask = null;
    ListView contactListView;
    SearchView contactSearchView;
    List<ContactDetails> contactList= new ArrayList<>();
    private TextView mLoadMsg;
    private View mProgressView;
    private View mDisplayFormView;
    private ContactContactDisplayAdapter customContactAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        contactListView = findViewById(R.id.contactListView);
        mProgressView = findViewById(R.id.load_progress);
        mLoadMsg = findViewById(R.id.contact_loadMsg);
        mDisplayFormView=findViewById(R.id.display_form);
        contactSearchView = findViewById(R.id.contact_label);
        showProgress(true);
        if (contactList==null || contactList.isEmpty()){
            if(SupportedFeatures.globalContactList!=null && !SupportedFeatures.globalContactList.isEmpty()){
                contactList.addAll(SupportedFeatures.globalContactList);
                displayContacts();
                showProgress(false);
            }else{
                fetchAndDisplayContacts();
            }
        }
    }

    private void displayContacts(){
        //Contacts are available just display them
        customContactAdapter= new ContactContactDisplayAdapter(getApplicationContext(),R.layout.contact_list_view,contactList);
        contactListView.setAdapter(customContactAdapter);
        contactSearchView.setOnQueryTextListener(ContactDetailsActivity.this);
        /*contactSearchView.setIconifiedByDefault(false);
        contactSearchView.requestFocus();*/
    }

    public void checkPermissionElseRequest(){
        if(!SupportedFeatures.hasAccess(this,Manifest.permission.READ_CONTACTS)){
            SupportedFeatures.requestAccess(this,
                    Manifest.permission.READ_CONTACTS,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    SupportedFeatures.PERMISSION_READ_CONTACT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == SupportedFeatures.PERMISSION_READ_CONTACT){
            if(SupportedFeatures.checkPermissionGrantResult(grantResults)){
                fetchAndDisplayContacts();
            }else{
                Toast.makeText(this, getString(R.string.contact_access_denied), Toast.LENGTH_LONG).show();
            }
        }
    }
    public void fetchAndDisplayContacts(){
        checkPermissionElseRequest();
        mLoadTask = new LoadContact();
        mLoadTask.execute((Void) null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        /*MenuItem item = menu.getItem(0);
        item.setVisible(true);
        item.setEnabled(true);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showProgress(true);
                fetchAndDisplayContacts();
                showProgress(false);
                return false;
            }
        });*/
        return true;
    }

    private void showDialog(final ContactDetails contact) {
        View alertLayout = getLayoutInflater().inflate(R.layout.contact_alert, null);
        TextView contactIdView = (TextView)alertLayout.findViewById(R.id.contact_id_value);
        ImageView contactProfileImageView = (ImageView)alertLayout.findViewById(R.id.dialog_contact_profile_icon);
        TextView contactNameView = (TextView)alertLayout.findViewById(R.id.contact_name_value);
        TextView contactPhoneView = (TextView)alertLayout.findViewById(R.id.contact_phone_value);
        TextView contactTimesConView = (TextView)alertLayout.findViewById(R.id.contact_timescon_value);
        Button contactActionCallButton = (Button)alertLayout.findViewById(R.id.contact_action_call);
        Button contactActionSmsButton = (Button)alertLayout.findViewById(R.id.contact_action_sms);
        Button contactActionCancelButton = (Button)alertLayout.findViewById(R.id.contact_action_cancel);

        if (contactIdView!=null){
            contactIdView.setText(contact.id);
        }
        if (contactNameView!=null){
            contactNameView.setText(contact.displayName);
        }
        if (contactPhoneView!=null){
            contactPhoneView.setText(contact.phoneNumber);
        }
        if (contactTimesConView!=null){
            contactTimesConView.setText(""+contact.timesContacted+" "+getString(R.string.contact_times));
        }
        //Create alert dialog
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(contact.displayName);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        alert.setIcon(R.drawable.ic_user_account);

        final AlertDialog alertDialog = alert.create();

        //Set Listeners for the alert buttons
        if (contactActionCallButton!=null) {
            contactActionCallButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startCallIntent(contact.phoneNumber);
                    alertDialog.dismiss();
                }
            });
        }

        if (contactActionSmsButton!=null) {
            contactActionSmsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startSmsIntent(contact.phoneNumber);
                    alertDialog.dismiss();

                }
            });
        }

        if (contactActionCancelButton!=null) {
            contactActionCancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                }
            });
        }

        if (contactProfileImageView!=null){
            contactProfileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startProfileIntent(contact.id);
                    alertDialog.dismiss();
                }
            });
        }
        alertDialog.show();
    }

    private void startSmsIntent(String phoneNo){
        Uri uri = Uri.parse("smsto:"+phoneNo);
        Intent intent = new Intent(Intent.ACTION_SENDTO,uri);
        intent.putExtra("sms_body","Hi there!");
        startActivity(intent);
    }

    private void startCallIntent(String phoneNo){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:"+phoneNo));
        startActivity(intent);
    }

    private void startProfileIntent(String id){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(id));
        intent.setData(uri);
        getApplicationContext().startActivity(intent);
    }

    public void goToHome(MenuItem item) {
        finish();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        System.out.println("parasjos: onQueryTextSubmit : "+s);
        customContactAdapter.getFilter().filter(s);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        System.out.println("parasjos: onQueryTextChange : "+s);
        customContactAdapter.getFilter().filter(s);
        return false;
    }

    public class ContactDetails implements Comparable<ContactDetails>{
        protected String id;
        String profilePicFile;
        String displayName;
        String displayNamePrimary;
        String phoneNumber;
        String timesContacted;

        public ContactDetails(String id, String profilePicFile, String displayName, String displayNamePrimary, String phoneNumber, String timesContacted) {
            this.id = id;
            this.profilePicFile = profilePicFile;
            this.displayName = displayName;
            this.displayNamePrimary = displayNamePrimary;
            this.phoneNumber = phoneNumber.replaceAll("[^a-zA-Z0-9]","");
            if(this.phoneNumber.length()>10){
                this.phoneNumber=this.phoneNumber.substring(this.phoneNumber.length()-10);
            }
            //this.phoneNumber = phoneNumber;
            this.timesContacted = timesContacted;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        @Override
        public String toString() {
            String returnString = new String();
            if(id!=null && !id.isEmpty()){
                returnString+="\nID : "+id;
            } if(displayName!=null && !displayName.isEmpty()){
                returnString+="Display Name : "+displayName;
            } if(displayNamePrimary!=null && !displayNamePrimary.isEmpty()){
                returnString+="\nDisplay Name Primary : "+displayNamePrimary;
            } if(timesContacted!=null && !timesContacted.isEmpty()){
                returnString+="\nTimes Contacted : "+timesContacted;
            } if(phoneNumber!=null && !phoneNumber.isEmpty()){
                returnString+="\nPhone No : "+phoneNumber;
            }
            return returnString;
        }

        @Override
        public int compareTo(@NonNull ContactDetails contactDetails) {
            return  (this.displayName+this.phoneNumber).compareToIgnoreCase((contactDetails.displayName+contactDetails.phoneNumber));
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mDisplayFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mDisplayFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDisplayFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoadMsg.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mDisplayFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class LoadContact extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            String ID = ContactsContract.Contacts._ID;
            String DisplayName = ContactsContract.Contacts.DISPLAY_NAME;
            String DisplayNamePrimary = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY;
            String HasPhoneNumber = ContactsContract.Contacts.HAS_PHONE_NUMBER;
            String TimesContacted = ContactsContract.Contacts.TIMES_CONTACTED;
            String ProfilePic = ContactsContract.Contacts.PHOTO_THUMBNAIL_URI;
            try {
                Uri contacts = Uri.parse(ContactsContract.Contacts.CONTENT_URI.toString());
                Set<ContactDetails> tempContactSet = new TreeSet<>();
                ContentResolver cRes = getContentResolver();
                Cursor cur = cRes.query(contacts, null, null, null, DisplayName);

                while (cur.moveToNext()) {
                    String contactId = cur.getString(cur.getColumnIndex(ID));
                    String displayName = cur.getString(cur.getColumnIndex(DisplayName));
                    String displayNamePrimary = cur.getString(cur.getColumnIndex(DisplayNamePrimary));
                    String timesContacted = cur.getString(cur.getColumnIndex(TimesContacted));
                    String profilePic = cur.getString(cur.getColumnIndex(ProfilePic));
                    int hasPhone = cur.getInt(cur.getColumnIndex(HasPhoneNumber));
                    if (hasPhone>0) {
                        // You know it has a number so now query it
                        Cursor phones = getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId, null, null);
                        while (phones.moveToNext()) {
                            tempContactSet.add(new ContactDetails(contactId,profilePic,
                                    displayName,
                                    displayNamePrimary,
                                    phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
                                    timesContacted));
                        }
                        phones.close();
                    }
                }
                cur.close();

                //Use custom adapter
                contactList = new ArrayList<ContactDetails>();
                contactList.addAll(tempContactSet);
                /*customContactAdapter= new ContactContactDisplayAdapter(getApplicationContext(),R.layout.contact_list_view,contactList);
                contactListView.setAdapter(customContactAdapter);
                contactSearchView.setOnQueryTextListener(ContactDetailsActivity.this);*/
                displayContacts();

            }catch (Exception e){
                System.out.println("Exception : "+e.getMessage());
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mLoadTask = null;
            showProgress(false);
            if(contactList!=null && !contactList.isEmpty()){
                contactSearchView.setQueryHint("("+contactList.size()+") "+getString(R.string.contactActivityName));
            }else{
                contactSearchView.setQueryHint("(0) "+getString(R.string.contactActivityName));
            }

            if (!success) {
                Toast.makeText(ContactDetailsActivity.this, getString(R.string.contact_load_failed), Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(ContactDetailsActivity.this, getString(R.string.contact_cache_being_updated), Toast.LENGTH_SHORT).show();
                SupportedFeatures.updateGlobalContactList(contactList);
                SupportedFeatures.populateContactLookUp(contactList);
                Toast.makeText(ContactDetailsActivity.this, getString(R.string.contact_cache_updated), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mLoadTask = null;
            showProgress(false);
        }
    }


    class ContactContactDisplayAdapter extends BaseAdapter implements Filterable {
        private Context context;
        private int layoutResource;
        private List<ContactDetails> beanList;
        List<ContactDetails> mStringFilterList;
        ValueFilter valueFilter;

        public ContactContactDisplayAdapter(Context context, int layoutResource, List<ContactDetails> contactDetails) {
            this.context = context;
            this.layoutResource = layoutResource;
            beanList = contactDetails;
            mStringFilterList=contactDetails;
        }

        @Override
        public int getCount() {
            return beanList.size();
        }

        @Override
        public Object getItem(int i) {
            return (Object) beanList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            System.out.println("Get getViewTypeCount "+getViewTypeCount());
            System.out.println("Get getItemViewType for "+position+" is "+String.valueOf(getItemViewType(position)));
            View view = convertView;
            if (view == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                view = layoutInflater.inflate(layoutResource, null);
            }

            final ContactDetails contactDetail = beanList.get(position);

            if (contactDetail != null) {
                ImageView userProfileView = (ImageView) view.findViewById(R.id.contact_profile);
                TextView contactNameView = (TextView) view.findViewById(R.id.contact_name);
                TextView contactPhoneView = (TextView) view.findViewById(R.id.contact_phone);
                ImageButton callButtonView = (ImageButton) view.findViewById(R.id.contact_call_button);
                ImageButton smsButtonView = (ImageButton) view.findViewById(R.id.contact_SMS_button);
                TableRow contactLinearLayout = (TableRow)view.findViewById(R.id.contact_linear_layout);

                if (userProfileView != null) {
                    userProfileView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startProfileIntent(contactDetail.id);
                        }
                    });
                }
                if (contactNameView != null) {
                    contactNameView.setText(contactDetail.displayName);
                }
                if (contactPhoneView != null) {
                    contactPhoneView.setText(contactDetail.phoneNumber);
                }
                if (callButtonView != null) {
                    callButtonView.setImageResource(R.drawable.ic_green_call);
                    callButtonView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startCallIntent(contactDetail.phoneNumber);
                        }
                    });
                }
                if (smsButtonView != null) {
                    smsButtonView.setImageResource(R.drawable.ic_sms_action);
                    smsButtonView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startSmsIntent(contactDetail.phoneNumber);
                        }
                    });
                }

                if (contactLinearLayout != null) {
                    contactLinearLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showDialog(contactDetail);
                        }
                    });
                }
            }
            return view;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            if (valueFilter == null) {
                valueFilter = new ValueFilter();
            }
            return valueFilter;
        }

        private class ValueFilter extends Filter {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                System.out.println("parasjos: In publishResults! result size "+results.values);
                beanList = (List<ContactDetails>) results.values;
                System.out.println("result size "+beanList.size());
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                System.out.println("parasjos: In performFiltering! for text "+constraint);

                FilterResults results = new FilterResults();

                // perform the search here using the searchConstraint String.
                if (constraint != null && constraint.length() > 0) {
                    System.out.println("parasjos: Filtering Text!!!!");
                    ArrayList<ContactDetails> filterList = new ArrayList<ContactDetails>();
                    for (int i = 0; i < ContactDetailsActivity.this.contactList.size(); i++) {
                        ContactDetails contact = ContactDetailsActivity.this.contactList.get(i);
                        if (contact.displayName.toLowerCase().contains(constraint.toString())
                                || contact.phoneNumber.toLowerCase().contains(constraint.toString()))  {
                            filterList.add(contact);
                        }
                    }
                    results.count = filterList.size();
                    results.values = filterList;
                } else {
                    System.out.println("parasjos: No text to filter Text!!!!");
                    results.count = ContactDetailsActivity.this.contactList.size();
                    results.values = ContactDetailsActivity.this.contactList;
                }
                return results;
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
