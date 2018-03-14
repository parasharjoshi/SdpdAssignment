package com.oracle.bits.parasjos.sdpdassignment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.oracle.bits.parasjos.sdpdassignment.helper.SupportedFeatures;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SmsActivity extends AppCompatActivity {

    public static final String activityKey  = "Sent SMS's";
    ListView smsListView;
    ArrayList<SmsDetails> smsList;
    static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        smsListView = findViewById(R.id.SmsListView);
        checkPermissionElseRequest();
        context = getApplicationContext();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void checkPermissionElseRequest(){
        if(SupportedFeatures.hasAccess(this,Manifest.permission.READ_SMS)){
            showSmsList();
        }else {
            SupportedFeatures.requestAccess(this,
                    Manifest.permission.READ_SMS,
                    new String[]{Manifest.permission.READ_SMS},
                    SupportedFeatures.PERMISSION_READ_SMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == SupportedFeatures.PERMISSION_READ_SMS){
            if(SupportedFeatures.checkPermissionGrantResult(grantResults)){

                showSmsList();
            }else{
                Toast.makeText(this, getString(R.string.sms_access_denied), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void showSmsList(){
        //Toast.makeText(this, "Accessing SMS details... ", Toast.LENGTH_LONG).show();
        try {
            Uri SMS_Sent = Uri.parse("content://sms/sent");
            smsList = new ArrayList<SmsDetails>();
            List smsStringList = new ArrayList<String>();
            ContentResolver cRes = getContentResolver();
            Cursor cur = cRes.query(SMS_Sent, null, null, null, null);

            while (cur.moveToNext()) {
                smsStringList.add("To : " + cur.getString(cur.getColumnIndexOrThrow("address")).toString() +
                        "\nBody: " + cur.getString(cur.getColumnIndexOrThrow("body")).toString()
                );

                smsList.add(new SmsDetails(cur.getString(cur.getColumnIndexOrThrow("address")).toString(),
                        cur.getString(cur.getColumnIndexOrThrow("body")).toString(),
                        cur.getString(cur.getColumnIndexOrThrow("date")).toString(),
                        cur.getString(cur.getColumnIndexOrThrow("status")).toString(),
                        cur.getString(cur.getColumnIndexOrThrow("read")).toString(),
                        cur.getString(cur.getColumnIndexOrThrow("type")).toString()));
            }
            cur.close();

            /*ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, smsStringList);
            smsListView.setAdapter(arrayAdapter);*/

            SentMessageDisplayAdapter sentMessageDisplayAdapter = new SentMessageDisplayAdapter(this,smsList);
            smsListView.setAdapter(sentMessageDisplayAdapter);

            smsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView parent, View view, int position, long id) {
                    showDialog(Html.fromHtml(smsList.get(position).toString()));
                }
            });
        }catch (Exception e){
            System.out.println("Exception : "+e.getMessage());
        }
    }

    private void showDialog(Spanned text) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("SMS Details");
        alert.setMessage(text);
        alert.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    public void goToHome(MenuItem item) {
        finish();
    }

    public class SmsDetails{
        public String address;
        public String body;
        public String date;
        public String status;
        public String read;
        public String type;


        public SmsDetails(String address, String body, String date, String status, String read, String type) {
            this.address = address.replaceAll("[^a-zA-Z0-9]","");
            this.body = body;
            this.date = SupportedFeatures.dateFormat.format(new Date(Long.parseLong(date)));;
            this.status = status;
            this.read = read;
            this.read = type;
            if(this.address.length()>10){
                this.address=this.address.substring(this.address.length()-10);
            }
        }

        @Override
        public String toString() {
            String returnString = new String();
            if(address!=null && !address.isEmpty()){
                returnString+="<br/><b>"+getString(R.string.sms_to)+"</b> : "+address;
            }if(date!=null && !date.isEmpty()){
                returnString+="<br/><b>"+getString(R.string.sms_date)+"</b> : "+date;
            }if(status!=null && !status.isEmpty()){
                returnString+="<br/><b>"+getString(R.string.sms_status)+"</b> : "+status;
            }if(read!=null && !read.isEmpty()){
                returnString+="<br/><b>"+getString(R.string.sms_read)+"</b> : "+read;
            }if(type!=null && !type.isEmpty()){
                returnString+="<br/><b>"+getString(R.string.sms_type)+"</b> : "+read;
            }if(body!=null && !body.isEmpty()){
                returnString+="<br/><b>"+getString(R.string.sms_body)+"</b> : "+body;
            }
            return returnString;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    class SentMessageDisplayAdapter extends ArrayAdapter<SmsDetails>{
        private Context context;
        private int layoutResource;
        //private List<SmsDetails> beanList;
        //List<SmsDetails> mStringFilterList;

        public SentMessageDisplayAdapter(Context context, ArrayList<SmsDetails> messageDetails) {
            super(context,R.layout.sms_list_view,messageDetails);
            layoutResource = R.layout.sms_list_view;
            this.context = context;
            this.layoutResource = layoutResource;
            //beanList = messageDetails;
            //mStringFilterList=messageDetails;
        }

        //This and next method make the adapter not reuse the views this is bad for performance it is suggested to avoid these 2 methods.
        //See : https://stackoverflow.com/questions/6921462/listview-reusing-views-when-i-dont-want-it-to
        @Override
        public int getViewTypeCount() {
            return getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final SmsDetails message = getItem(position);
            System.out.println("Get View for position "+position+":"+message.address);
            View view = convertView;
            if (view == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                view = layoutInflater.inflate(layoutResource, null);
            }


            if (message != null) {
                ImageView userProfileView = (ImageView) view.findViewById(R.id.sms_icon);
                TextView smsAddress = (TextView) view.findViewById(R.id.sms_from_to);
                TextView smsBody = (TextView) view.findViewById(R.id.sms_body);
                TextView smsDate = (TextView) view.findViewById(R.id.sms_date);

                if (smsAddress != null) {
                    final ContactDetailsActivity.ContactDetails contact = SupportedFeatures.contactLookUp(message.address);

                    if(contact!=null){
                        System.out.println("For position "+position+"- "+ message.address+":"+contact.getPhoneNumber()+":"+contact.getDisplayName());
                        smsAddress.setText(contact.getDisplayName());
                        if (userProfileView != null) {
                            userProfileView.setImageResource(R.drawable.ic_user_account);
                            userProfileView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //ContactDetailsActivity.ContactDetails contact = SupportedFeatures.contactLookUp(message.address);
                                    System.out.println("On click For position "+position+"- "+ message.address+":"+contact.getPhoneNumber()+":"+contact.getDisplayName());
                                    if(contact!=null ){
                                        startProfileIntent(contact);
                                    }
                                }
                            });
                        }
                    }else{
                        smsAddress.setText(message.address);
                    }
                }
                if (smsBody != null) {
                    smsBody.setText(message.body);
                }
                if (smsDate != null) {
                    smsDate.setText(message.date.substring(0,message.date.indexOf(',')));
                }
            }
            return view;
        }
    }

    private static void startProfileIntent(ContactDetailsActivity.ContactDetails contact){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contact.getId()));
        intent.setData(uri);
        context.startActivity(intent);
    }
}
