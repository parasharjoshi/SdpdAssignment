package com.oracle.bits.parasjos.sdpdassignment;

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
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.oracle.bits.parasjos.sdpdassignment.helper.SupportedFeatures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TabbedSmsActivity extends AppCompatActivity {
    public static final String activityKey  = "All SMS's";
    ArrayList<Message> smsInbox, smsSent, smsDraft;

    private static Context mContext;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        populateSmsLists();
        setContentView(R.layout.activity_tabbed_sms);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),smsInbox,smsSent,smsDraft);


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    private void populateSmsLists() {
        smsInbox= getSmsList("content://sms/inbox");
        smsSent=getSmsList("content://sms/sent");
        smsDraft=getSmsList("content://sms/draft");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == SupportedFeatures.PERMISSION_READ_CONTACT){
            if(SupportedFeatures.checkPermissionGrantResult(grantResults)){

            }else{
                Toast.makeText(this, getString(R.string.sms_access_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public ArrayList<Message> getSmsList(String providerUrl){
        //Toast.makeText(this, "Accessing SMS details... ", Toast.LENGTH_SHORT).show();
        try {
            ArrayList<Message> smsList = new ArrayList<>();
            Uri SMS_Sent = Uri.parse(providerUrl);
            ContentResolver cRes = getContentResolver();
            Cursor cur = cRes.query(SMS_Sent, null, null, null, null);

            while (cur.moveToNext()) {
                smsList.add(new Message(cur.getString(cur.getColumnIndexOrThrow("address")).toString(),
                        cur.getString(cur.getColumnIndexOrThrow("body")).toString(),
                        cur.getString(cur.getColumnIndexOrThrow("date")).toString()));
            }
            cur.close();
            return smsList;

        }catch (Exception e){
            System.out.println("Exception : "+e.getMessage());
        }
        return null;
    }

    public void goToHome(MenuItem item) {
        finish();
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_SMS_LIST = "sms_list";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber,ArrayList smsList) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putStringArrayList(ARG_SMS_LIST,smsList);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tabbed_sms, container, false);

            ListView listView = (ListView) rootView.findViewById(R.id.listSection);
            /*ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_expandable_list_item_1, getArguments().getStringArrayList(ARG_SMS_LIST));
            listView.setAdapter(arrayAdapter);*/

            MessageDisplayAdapter messageDisplayAdapter = new MessageDisplayAdapter(getContext(), (ArrayList<Message>) getArguments().get(ARG_SMS_LIST));
            listView.setAdapter(messageDisplayAdapter);
            if(getArguments().getStringArrayList(ARG_SMS_LIST)!=null){
                System.out.println("Size is "+getArguments().getStringArrayList(ARG_SMS_LIST).size());
            }else{
                System.out.println("Size is null");
            }

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView parent, View view, int position, long id) {
                    showDialog(((ArrayList<Message>)getArguments().get(ARG_SMS_LIST)).get(position).toString());
                }
            });

            return rootView;
        }

        private void showDialog(String text) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle(R.string.sms_details);
            alert.setMessage(Html.fromHtml(text));

            alert.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.show();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        ArrayList<Message> smsInbox, smsSent, smsDraft;
        String tab;

        /*public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }*/

        public SectionsPagerAdapter(FragmentManager fm, ArrayList<Message> smsInbox, ArrayList<Message> smsSent, ArrayList<Message> smsDraft) {
            super(fm);
            this.smsInbox = smsInbox;
            this.smsSent = smsSent;
            this.smsDraft = smsDraft;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            ArrayList<Message> list ;
            if(position==0){
                list=smsInbox;
            }else if (position==1){
                list=smsSent;
            }else if (position==2){
                list=smsDraft;
            }else{
                list = new ArrayList();
            }
            return PlaceholderFragment.newInstance(position + 1, list);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        /*private ArrayList<Message> getStringFromSMsList(List<SmsDetails> list){
            ArrayList<Message> retList = new ArrayList<>();
            if(list!=null) {
                for (SmsDetails det : list) {
                    retList.add(new Message(det.address,det.body,det.date,det.body.length()));
                    *//*retList.add("From : " + det.address +
                            "\nBody: " + det.body +
                            "\nDate: " + new Date(Long.parseLong(det.date)).toString() +
                            "\nTot Chars : " + det.body.length());*//*
                }
            }
            return retList;
        }*/
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    static class MessageDisplayAdapter extends BaseAdapter{
        private Context context;
        private int layoutResource;
        final private List<Message> beanList;

        public MessageDisplayAdapter(Context context, ArrayList<Message> messageDetails) {
            layoutResource = R.layout.sms_list_view;
            this.context = context;
            this.layoutResource = layoutResource;
            beanList = messageDetails;
            if(beanList!=null){
                System.out.println("BeanList size : "+beanList.size());
            }
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

        //This and next method make the adapter not reuse the views this is bad for performance it is suggested to avoid these 2 methods.
        //See : https://stackoverflow.com/questions/6921462/listview-reusing-views-when-i-dont-want-it-to
        @Override
        public int getViewTypeCount() {
            int returnVal = getCount();
            if(returnVal==0){
                return 1;
            }
            return getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            System.out.println("Get View for position "+position);
            View view = convertView;
            if (view == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                view = layoutInflater.inflate(layoutResource, null);
            }

            final Message message = beanList.get(position);

            if (message != null) {
                ImageView userProfileView = (ImageView) view.findViewById(R.id.sms_icon);
                TextView smsAddress = (TextView) view.findViewById(R.id.sms_from_to);
                TextView smsBody = (TextView) view.findViewById(R.id.sms_body);
                TextView smsDate = (TextView) view.findViewById(R.id.sms_date);

                if (smsAddress != null) {
                    final ContactDetailsActivity.ContactDetails contact = SupportedFeatures.contactLookUp(message.fromTo);
                    if(contact!=null){
                        System.out.println("Looking up for "+message.fromTo+" Received "+contact);
                        userProfileView.setImageResource(R.drawable.ic_user_account);
                        smsAddress.setText(contact.getDisplayName());
                        if (userProfileView != null) {
                            /*userProfileView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(contact!=null ){
                                        startProfileIntent(contact);
                                    }
                                }
                            });*/
                        }
                    }else{
                        smsAddress.setText(message.fromTo);
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

    public class Message {
        String fromTo;
        String body;
        String date;
        Integer totChars;

        public Message(String fromTo, String body, String date) {
            this.fromTo = fromTo.replaceAll("[^a-zA-Z0-9]","");
            this.body = body;
            this.date = SupportedFeatures.dateFormat.format(new Date(Long.parseLong(date)));
            this.totChars = totChars;
            if(this.fromTo.length()>10){
                this.fromTo=this.fromTo.substring(this.fromTo.length()-10);
            }
            if(this.body!=null){
                this.totChars = this.body.length();
            }
        }

        @Override
        public String toString() {
            String returnString = new String();
            if(fromTo!=null && !fromTo.isEmpty()){
                returnString+="<b>"+getString(R.string.sms_address)+"</b> : "+fromTo;
                ContactDetailsActivity.ContactDetails contact = SupportedFeatures.contactLookUp(this.fromTo);
                if(contact!=null){
                    returnString+="<br/><b>"+getString(R.string.sms_contact)+"</b> : "+contact.getDisplayName();
                }
            } if(date!=null && !date.isEmpty()){
                returnString+="<br/><b>"+getString(R.string.sms_date)+"</b> : "+date;
            } if(totChars!=null && totChars>0){
                returnString+="<br/><b>"+getString(R.string.sms_tot_char)+"</b> : "+totChars;
            }if(body!=null && !body.isEmpty()){
                returnString+="<br/><b>"+getString(R.string.sms_body)+"</b> : "+body;
            }
            return returnString;
        }
    }

    private static void startProfileIntent(ContactDetailsActivity.ContactDetails contact){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contact.getId()));
        intent.setData(uri);
        mContext.startActivity(intent);
    }

}
