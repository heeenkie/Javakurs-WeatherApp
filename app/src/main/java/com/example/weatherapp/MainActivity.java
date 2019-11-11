package com.example.weatherapp;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{



    private Button updateButtonClick;
    private TextView temperature, windSpeed, cloudiness, precipitation;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateButtonClick = (Button) findViewById(R.id.updateButton);
        updateButtonClick.setOnClickListener(this);

        temperature = (TextView) findViewById(R.id.temperatureTextView);
        windSpeed = (TextView) findViewById(R.id.windSpeedTextView);
        cloudiness = (TextView) findViewById(R.id.cloudinessTextView);
        precipitation = (TextView) findViewById(R.id.precipitationTextView);

        image = (ImageView) findViewById(R.id.weatherImageView);
    }

    @Override
    public void onClick(View v) {
        GetWeather getWeather = new GetWeather();
        getWeather.execute();
    }


    private class GetWeather extends AsyncTask<Void, Void, Void> {
        private String apiUrl = "https://api.met.no/weatherapi/locationforecast/1.9/?lat=62.3908;lon=17.3069";
        private String imageUrl;
        private String temperatureText;
        private String windSpeedText;
        private String cloudinessText;
        private String precipitationText;
        private String strXml;
        private Bitmap draw;


        /**
         * Gets necessary data in the background without disturbing main thread.
         * @param voids
         * @return
         */
        @Override
        protected Void doInBackground(Void... voids) {
            getData(apiUrl);
            draw = getImage(imageUrl);
            return null;
        }

        /**
         * Updates element on display.
         * @param aVoid
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            temperature.setText(temperatureText);
            windSpeed.setText(windSpeedText);
            cloudiness.setText(cloudinessText);
            precipitation.setText(precipitationText);
            image.setImageBitmap(draw);
        }


        /**
         * Gets the xml from rest api as a string.
         * @param strUrl
         * @return
         */
        private void getData(String strUrl){
            try {
                URL url = new URL(strUrl);
                HttpURLConnection conn;
                do {
                    conn = (HttpURLConnection) url.openConnection();
                } while (conn.getResponseCode() != 200);
                conn.setRequestMethod("GET");
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    strXml = stringBuilder.toString();
                    setData();
                    return;
                } finally {
                    conn.disconnect();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        /**
         * Fetch image from url and returns drawable.
         * @param strUrl
         * @return
         */
        private Bitmap getImage(String strUrl) {
            try {
                URL url = new URL(strUrl);
                HttpURLConnection conn;
                do {
                    conn = (HttpURLConnection) url.openConnection();
                } while (conn.getResponseCode() != 200);
                InputStream is = conn.getInputStream();
                Bitmap b = BitmapFactory.decodeStream(is);
                return b;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Sets every string that is needed to update screen.
         */
        private void setData(){
            try{
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(new InputSource(new StringReader(strXml)));
                doc.getDocumentElement().normalize();
                Node nNode = doc.getElementsByTagName("location").item(0);
                Node pNode = doc.getElementsByTagName("location").item(1);
                if (nNode.getNodeType() == Node.ELEMENT_NODE && pNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    Element pElement = (Element) pNode;
                    Element tempElement = (Element) eElement.getElementsByTagName("temperature").item(0);
                    Element cloudElement = (Element) eElement.getElementsByTagName("cloudiness").item(0);
                    Element windElement = (Element) eElement.getElementsByTagName("windSpeed").item(0);
                    Element imageElement = (Element) pElement.getElementsByTagName("symbol").item(0);
                    Element precipElement = (Element) pElement.getElementsByTagName("precipitation").item(0);

                    temperatureText = "Temperature: " + tempElement.getAttribute("value") + "°";
                    cloudinessText = "Cloudiness: " + cloudElement.getAttribute("percent")+ "%";
                    windSpeedText = "WindSpeed: " +  windElement.getAttribute("mps") + "mps";
                    precipitationText ="Precipitation: Between " + precipElement.getAttribute("minvalue") + "mm and " + precipElement.getAttribute("maxvalue") + "mm";
                    imageUrl = "https://api.met.no/weatherapi/weathericon/1.1/?symbol=" + imageElement.getAttribute("number") + "&content_type=image/png";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }




}