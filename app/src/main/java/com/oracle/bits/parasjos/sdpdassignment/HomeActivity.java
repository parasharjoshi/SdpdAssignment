package com.oracle.bits.parasjos.sdpdassignment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.oracle.bits.parasjos.sdpdassignment.helper.SupportedFeatures;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("HomeActivity on create start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        listView = findViewById(R.id.mainListView);
        populateActivitiesForHome();

        System.out.println("HomeActivity on create start");
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
            SupportedFeatures.ActivityMeta activityMeta = SupportedFeatures.getActivityMetaForKey(SupportedFeatures.getSupportedActivities().get(position).toString());
            if(activityMeta!=null) {
                Class targetActivityClass = SupportedFeatures.getActivityMetaForKey(SupportedFeatures.getSupportedActivities().get(position).toString()).activityClass;
                System.out.println("Clicked an option " + position + " Id is " + id + " Need to invoke class is " + targetActivityClass);
                if (targetActivityClass != null) {
                    Intent intent = new Intent(HomeActivity.this, targetActivityClass);
                    startActivity(intent);
                }
            }else{
                Toast.makeText(HomeActivity.this, getString(R.string.feature_not_supported), Toast.LENGTH_LONG).show();
                view.setEnabled(false);
                view.setClickable(false);
                view.setActivated(false);
                TextView header = view.findViewById(R.id.home_feature_label);
                header.setTextColor(Color.LTGRAY);
                TextView desc = view.findViewById(R.id.home_feature_description);
                desc.setTextColor(Color.LTGRAY);
                ImageView featureIcon = view.findViewById(R.id.home_feature_icon);
                featureIcon.setImageResource(R.drawable.ic_home_default_disabled);
            }
            }
        });
        System.out.println("HomeActivity on create end");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.removeItem(1);
        return true;
    }

    public void populateActivitiesForHome(){
        FeatureDisplayAdapter featureDisplayAdapter = new FeatureDisplayAdapter(getApplicationContext(), R.layout.feature_list_view, SupportedFeatures.getSupportedActivities());
        listView.setAdapter(featureDisplayAdapter);
    }

    public void goToHome(MenuItem item) {
        //Do nothing as you are on home
    }

    class FeatureDisplayAdapter extends ArrayAdapter<String> {
        private int layoutResource;
        public FeatureDisplayAdapter(Context context, int layoutResource, List<String> features) {
            super(context, layoutResource, features);
            this.layoutResource = layoutResource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                view = layoutInflater.inflate(layoutResource, null);
            }

            final String activity = getItem(position);

            if (activity != null) {
                ImageView featureIcon = (ImageView) view.findViewById(R.id.home_feature_icon);
                TextView featureLabel = (TextView) view.findViewById(R.id.home_feature_label);
                TextView featureDesc = (TextView) view.findViewById(R.id.home_feature_description);
                TableRow contactLinearLayout = (TableRow)view.findViewById(R.id.home_linear_layout);

                System.out.println("Activity : "+activity);

                SupportedFeatures.ActivityMeta activityMeta = SupportedFeatures.getActivityMetaForKey(activity);
                if(activityMeta!=null) {
                    if (featureIcon != null) {
                        featureIcon.setImageResource(activityMeta.activityIcon);
                    }
                    if (featureLabel != null) {
                        featureLabel.setText(activityMeta.activityLabel);
                    }
                    if (featureDesc != null) {
                        featureDesc.setText(activityMeta.activityDescription);
                    }
                }else{
                    if (featureIcon != null) {
                        featureIcon.setImageResource(R.drawable.ic_home_default_icon);
                        featureIcon.setEnabled(false);
                    }
                    if (featureLabel != null) {
                        featureLabel.setText(activity);
                        featureLabel.setEnabled(false);
                        featureLabel.setClickable(false);
                    }
                    if (featureDesc != null) {
                        featureDesc.setText(activity);
                        featureDesc.setEnabled(false);
                    }
                }
            }
            return view;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void startActivityClearStack(Class activity){
        Context context = getApplicationContext();
        Intent intent = new Intent(context, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("exceededBiometricScans", true);
        context.startActivity(intent);
        ((Activity) context).finish();
    }
}
