package com.example.dima.booxchange;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Dima on 2/27/2018.
 */

public class BackgroundConn extends AsyncTask<String, Void, String> {
    Context context;
    AlertDialog alertDialog;
    BackgroundConn (Context ctx) {
        context = ctx;
    }
    @Override
    protected String doInBackground(String... voids) {
        String type = voids[0];
        String add_book_url = "http://95.65.106.34/booxchange/add_book.php";
        if (type.equals("adding")) {
            try {
                String title = voids[1];
                String author = voids[2];
                String edition = voids[3];
                String isbn = voids[4];
                String info = voids[5];
                URL url = new URL(add_book_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data = URLEncoder.encode("title","UTF-8")+"="+URLEncoder.encode(title,"UTF-8")+"&"
                        + URLEncoder.encode("author","UTF-8")+"="+URLEncoder.encode(author,"UTF-8")+"&"
                        + URLEncoder.encode("edition","UTF-8")+"="+URLEncoder.encode(edition,"UTF-8")+"&"
                        + URLEncoder.encode("isbn","UTF-8")+"="+URLEncoder.encode(isbn,"UTF-8")+"&"
                        + URLEncoder.encode("info","UTF-8")+"="+URLEncoder.encode(info,"UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String result = "";
                String line = "";
                while ((line = bufferedReader.readLine())!= null) {
                    result += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return result;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Login Status");
    }

    @Override
    protected void onPostExecute(String result) {
        alertDialog.setMessage(result);
        alertDialog.show();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
