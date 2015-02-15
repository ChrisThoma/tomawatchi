package com.christhoma.tomawatchi.activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.christhoma.tomawatchi.R;
import com.christhoma.tomawatchi.util.TypefaceSpan;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AboutActivity extends ActionBarActivity {

    @InjectView(R.id.about_contact_email)
    TextView contactEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SpannableString span = new SpannableString("About");
        span.setSpan(new TypefaceSpan(this, "HipsterishFontNormal.ttf"), 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(span);
        ButterKnife.inject(this);

        contactEmail.setText("Tomawatchi@gmail.com");
        Linkify.addLinks(contactEmail, Linkify.EMAIL_ADDRESSES);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
