//Copyright 2013-2015 Shravan Jambukesan and Akash Kakkilaya
package com.pixel.nfcsettings;

import java.io.IOException;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class WriteSmsActivity extends Activity {
	
	private NfcAdapter mNfcAdapter;
	private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private String urlAddress="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.writesms);

        final EditText smsNumberEditText = (EditText)this.findViewById(R.id.phonenumbereditText1);
        final EditText smsBodyEditText = (EditText)this.findViewById(R.id.smseditText2);

        Button writeSmsButton = (Button)this.findViewById(R.id.writesmsbutton1);
        writeSmsButton.setOnClickListener(new android.view.View.OnClickListener() 
        {
        	public void onClick(View view) {
        		String smsNumber = smsNumberEditText.getText().toString();
        		String smsBody = smsBodyEditText.getText().toString();
        		urlAddress = "sms:"+smsNumber+"?body="+smsBody;
        		TextView messageText = (TextView)findViewById(R.id.phonenumberView1);
        		messageText.setText("Touch NFC Tag to share SMS\n"+
        				"SMS adress: "+smsNumber+"\nSMS text: "+smsBody);  		        		
        	}
        });
    
       
     	
     	 mNfcAdapter = NfcAdapter.getDefaultAdapter(this); 
     	 mPendingIntent = PendingIntent.getActivity(this, 0,
                 new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    	 	IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
    	 	mFilters = new IntentFilter[] {
              ndef,
    	 	};
           mTechLists = new String[][] { new String[] { Ndef.class.getName() },
        		   new String[] { NdefFormatable.class.getName() }};
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mNfcAdapter != null) mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
                mTechLists);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);  
        String externalType = "nfclab.com:smsService";
		NdefRecord extRecord = new NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE, externalType.getBytes(), new byte[0], urlAddress.getBytes());
        NdefMessage newMessage = new NdefMessage(new NdefRecord[] { extRecord});
        writeNdefMessageToTag(newMessage, tag);   

    }

    @Override
    public void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }
    
    boolean writeNdefMessageToTag(NdefMessage message, Tag detectedTag) {
        int size = message.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(detectedTag);
            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                	Toast.makeText(this, "Tag is read-only.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                	Toast.makeText(this, "The data cannot written to tag, Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size + " bytes.", Toast.LENGTH_SHORT).show();
                    return false;
                }

                ndef.writeNdefMessage(message);
                ndef.close();                
                Toast.makeText(this, "Message is written tag.", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                NdefFormatable ndefFormat = NdefFormatable.get(detectedTag);
                if (ndefFormat != null) {
                    try {
                    	ndefFormat.connect();
                    	ndefFormat.format(message);
                    	ndefFormat.close();
                        Toast.makeText(this, "The data is written to the tag ", Toast.LENGTH_SHORT).show();
                        return true;
                    } catch (IOException e) {
                    	 Toast.makeText(this, "Failed to format tag", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } else {
                	 Toast.makeText(this, "NDEF is not supported", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        } catch (Exception e) {
        	Toast.makeText(this, "Write opreation has failed", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
