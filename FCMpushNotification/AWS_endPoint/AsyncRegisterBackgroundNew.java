package com.thanthi.dtnext.dtnextapplication.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.GetEndpointAttributesRequest;
import com.amazonaws.services.sns.model.GetEndpointAttributesResult;
import com.amazonaws.services.sns.model.InvalidParameterException;
import com.amazonaws.services.sns.model.NotFoundException;
import com.amazonaws.services.sns.model.SetEndpointAttributesRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.thanthi.dtnext.dtnextapplication.utils.Constant;
import com.thanthi.dtnext.dtnextapplication.utils.SplashScreenUtils;
import com.thanthi.dtnext.dtnextapplication.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressLint("StaticFieldLeak")
public class AsyncRegisterBackgroundNew extends AsyncTask<String, String, Boolean> {

    private Context context;
    private String registerId;
    private AmazonSNSClient snsClient;
    private String pushStatus;
    private boolean updateNeeded = false;

    public AsyncRegisterBackgroundNew(Context context, String pushStatus) {
        this.context = context;
        this.pushStatus = pushStatus;
    }

    @Override
    protected Boolean doInBackground(String... arg0) {

        try {
            CognitoCachingCredentialsProvider cognitoProvider = new CognitoCachingCredentialsProvider(
                    context,
                    Constant.CC_ACCOUNT_ID,
                    Constant.CC_IDENTITY_POOL_ID,
                    Constant.CC_UN_AUTH_ROLE_ARN,
                    Constant.CC_AUTH_ROLE_ARN,
                    Regions.US_EAST_1
            );
            snsClient = new AmazonSNSClient(cognitoProvider.getCredentials()); //provide credentials here
            registerWithSNS();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Utils.putSharedPreference(context, Constant.DEVICE_ID_PREFERENCE, Utils.getAndroidId(context));
            context.sendBroadcast(new Intent(Constant.SPLASH_ACTIVITY_BROADCAST));
        } else {
            Toast.makeText(context, "Technical Problem, Please try after some time..!", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerWithSNS() {

        String endpointArn = retrieveEndpointArn();
        try {
            registerId = FirebaseInstanceId.getInstance().getToken();
        } catch (Exception e) {
            registerId = "abcd";
            e.printStackTrace();
        }
        String token = registerId;

        boolean createNeeded = (endpointArn == null);

        if (createNeeded) {
            endpointArn = createEndpoint(); // No endpoint ARN is stored; need to call CreateEndpoint
            createNeeded = false;
        }

        // Look up the endpoint and make sure the data in it is current, even if it was just created
        try {
            GetEndpointAttributesRequest geaReq = new GetEndpointAttributesRequest().withEndpointArn(endpointArn);
            GetEndpointAttributesResult geaRes = snsClient.getEndpointAttributes(geaReq);
            updateNeeded = !geaRes.getAttributes().get("Token").equals(token) || !geaRes.getAttributes().get("Enabled").equalsIgnoreCase(pushStatus);
        } catch (NotFoundException nfe) {
            createNeeded = true; // we had a stored ARN, but the endpoint associated with itdisappeared. Recreate it.
        }

        if (createNeeded) createEndpoint();

        if (updateNeeded) {
            Map<String, String> attrs = new HashMap<>(); // endpoint is out of sync with the current data; update the token and enable it.
            attrs.put("Token", token);
            attrs.put("Enabled", pushStatus);
            SetEndpointAttributesRequest saeReq = new SetEndpointAttributesRequest().withEndpointArn(endpointArn).withAttributes(attrs);
            snsClient.setEndpointAttributes(saeReq);
        }
    }

    private String createEndpoint() {
        try {
            registerId = FirebaseInstanceId.getInstance().getToken();
        } catch (Exception e) {
            registerId = "abcd";
            e.printStackTrace();
        }
        String endpointArn;
        try {
            String applicationArn = Constant.APPLICATION_ARN;
            CreatePlatformEndpointRequest cpeReq = new CreatePlatformEndpointRequest().withPlatformApplicationArn(applicationArn).withToken(registerId);
            CreatePlatformEndpointResult cpeRes = snsClient.createPlatformEndpoint(cpeReq);
            endpointArn = cpeRes.getEndpointArn();
        } catch (InvalidParameterException e) {
            e.printStackTrace();
            String message = e.getErrorMessage();
            Pattern p = Pattern.compile(".*Endpoint (arn:aws:sns[^ ]+) already exists " + "with the same Token.*");
            Matcher m = p.matcher(message);
            if (m.matches()) {
                // the endpoint already exists for this token, but with additional custom data that createEndpoint doesn't want to overwrite. Just use the existing endpoint.
                endpointArn = m.group(1);
            } else {
                throw e;
            }
        }

        String topicArn = Constant.TOPIC_ARN;
        SubscribeRequest subRequest = new SubscribeRequest(topicArn, "application", endpointArn);
        SubscribeResult g = snsClient.subscribe(subRequest);
        storeEndpointArn(endpointArn);
        return endpointArn;
    }

    private String retrieveEndpointArn() {
        // retrieve endpointArn from permanent storage, or return null if null stored
        if (Utils.getSharedPreference(context, Constant.ENDPOINT_PREFERENCE).equals(" ")) return null;
        else return Utils.getSharedPreference(context, Constant.ENDPOINT_PREFERENCE);
    }

    private void storeEndpointArn(String endpointArn) {
        // write endpoint arn to permanent storage
        Utils.putSharedPreference(context, Constant.ENDPOINT_PREFERENCE, endpointArn);
        SplashScreenUtils.setPushOnOffWithoutMainCalling(pushStatus, context);
    }
}
