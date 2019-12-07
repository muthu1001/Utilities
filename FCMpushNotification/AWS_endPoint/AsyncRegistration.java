package com.thanthi.dtnext.dtnextapplication.async;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

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
public class AsyncRegistration extends AsyncTask<String, String, Boolean> {
    private Context context;
    private Activity activity;
    private String registerId;
    private AmazonSNSClient client;

    private String pushStatus;

    public AsyncRegistration(Context context, String pushStatus, Activity activity) {
        this.context = context;
        this.activity = activity;
        this.pushStatus = pushStatus;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        try {
            CognitoCachingCredentialsProvider cognitoProvider = new CognitoCachingCredentialsProvider(
                    context,
                    Constant.CC_ACCOUNT_ID,
                    Constant.CC_IDENTITY_POOL_ID,
                    Constant.CC_UN_AUTH_ROLE_ARN,
                    Constant.CC_AUTH_ROLE_ARN,
                    Regions.US_EAST_1
            );
            client = new AmazonSNSClient(cognitoProvider.getCredentials()); //provide credentials here
            registerWithSNS();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private void registerWithSNS() {

        String endpointArn = retrieveEndpointArn();
        try {
            registerId = FirebaseInstanceId.getInstance().getToken();
        } catch (Exception e) {
            registerId ="abcd";
            e.printStackTrace();
        }
        String token = registerId;

        boolean updateNeeded = false;
        boolean createNeeded = (null == endpointArn);

        if (createNeeded) {
            // No endpoint ARN is stored; need to call CreateEndpoint
            endpointArn = createEndpoint();
            createNeeded = false;
        }
        // Look up the endpoint and make sure the data in it is current, even ifit was just created
        try {
            GetEndpointAttributesRequest getEndpointAttributesRequest = new GetEndpointAttributesRequest().withEndpointArn(endpointArn);
            GetEndpointAttributesResult getEndpointAttributesResult = client.getEndpointAttributes(getEndpointAttributesRequest);
            updateNeeded = !getEndpointAttributesResult.getAttributes().get("Token").equals(token) || !getEndpointAttributesResult.getAttributes().get("Enabled").equalsIgnoreCase(pushStatus);
        } catch (NotFoundException nfe) {
            // we had a stored ARN, but the endpoint associated with it disappeared. Recreate it.
            createNeeded = true;
        }

        if (createNeeded) createEndpoint();

        if (updateNeeded) {
            // endpoint is out of sync with the current data update the token and enable it.
            Map<String, String> attribs = new HashMap<>();
            attribs.put("Token", token);
            attribs.put("Enabled", pushStatus);
            SetEndpointAttributesRequest setEndpointAttributesRequest = new SetEndpointAttributesRequest().withEndpointArn(endpointArn).withAttributes(attribs);
            client.setEndpointAttributes(setEndpointAttributesRequest);
        }
    }

    private String createEndpoint() {
        try {
            registerId = FirebaseInstanceId.getInstance().getToken();
        } catch (Exception e) {
            registerId ="abcd";
            e.printStackTrace();
        }
        String endpointArn;
        try {
            String applicationArn = Constant.APPLICATION_ARN;

            CreatePlatformEndpointRequest createPlatformEndpointRequest = new CreatePlatformEndpointRequest().withPlatformApplicationArn(applicationArn).withToken(registerId);
            CreatePlatformEndpointResult createPlatformEndpointResult = client.createPlatformEndpoint(createPlatformEndpointRequest);
            endpointArn = createPlatformEndpointResult.getEndpointArn();
        } catch (InvalidParameterException e) {
            e.printStackTrace();
            String errorMessage = e.getErrorMessage();
            Pattern p = Pattern.compile(".*Endpoint (arn:aws:sns[^ ]+) already exists " + "with the same Token.*");
            Matcher m = p.matcher(errorMessage);
            if (m.matches()) {
                // the endpoint already exists for this token, but with additional custom data that CreateEndpoint doesn't want to overwrite. Just use the existing endpoint.
                endpointArn = m.group(1);
            } else {
                throw e; // rethrow exception, the input is actually bad
            }
        }

        String topicArn = Constant.TOPIC_ARN;

        SubscribeRequest subRequest = new SubscribeRequest(topicArn, "application", endpointArn);
        SubscribeResult g = client.subscribe(subRequest);
        storeEndpointArn(endpointArn);
        return endpointArn;
    }

    /*
     * @return the arn the app was registered under previously, or null if no endpoint arn is stored
     */
    private String retrieveEndpointArn() {
        // retrieve endpointArn from permanent storage, or return null if null stored
        if(Utils.getSharedPreference(context, Constant.ENDPOINT_PREFERENCE).equals(" ")) return null;
        else return Utils.getSharedPreference(context, Constant.ENDPOINT_PREFERENCE);
    }

    /*
     * Stores the endpoint arn in permanent storage for look up next time
     * */
    private void storeEndpointArn(String endpointArn) {
        // write endpoint arn to permanent storage
        Utils.putSharedPreference(context, Constant.ENDPOINT_PREFERENCE, endpointArn);
        SplashScreenUtils.setPushOnOff(pushStatus, context, activity);
    }


}

