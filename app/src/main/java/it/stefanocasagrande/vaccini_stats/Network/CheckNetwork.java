package it.stefanocasagrande.vaccini_stats.Network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;

import it.stefanocasagrande.vaccini_stats.GlobalVariables;

public class CheckNetwork {


    Context context;

    public CheckNetwork(Context context) {
        this.context = context;
    }

    // Network Check
    public void registerNetworkCallback()
    {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkRequest.Builder builder = new NetworkRequest.Builder();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback(){
                                                                       @Override
                                                                       public void onAvailable(Network network) {
                                                                           GlobalVariables.isNetworkConnected = true; // Global Static Variable
                                                                       }
                                                                       @Override
                                                                       public void onLost(Network network) {
                                                                           GlobalVariables.isNetworkConnected = false; // Global Static Variable
                                                                       }
                                                                   }

                );

                GlobalVariables.isNetworkConnected = false;
            }
            else
            {
                ConnectivityManager cm =
                        (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                GlobalVariables.isNetworkConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
            }

        }catch (Exception e){
            GlobalVariables.isNetworkConnected = false;
        }
    }
}
