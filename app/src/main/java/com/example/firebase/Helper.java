package com.example.firebase;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Helper extends AsyncTask<Void,Void, Boolean> {

    interface Consumer{
        void  accept(boolean internet);
    }

    public Helper(Consumer consumer) {
        this.consumer = consumer;
        execute();
    }

    Consumer consumer;




    @Override
    protected Boolean doInBackground(Void... voids) {
        try{
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com",80),1500);
            socket.close();
            return true;
        } catch (IOException e){
            return false;
        }

    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        consumer.accept(aBoolean);

    }
}
