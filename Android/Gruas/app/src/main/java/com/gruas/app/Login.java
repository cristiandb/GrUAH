package com.gruas.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gruas.app.couchBaseLite.CouchException;
import com.gruas.app.couchBaseLite.manager.ManagerGruasCouchDB;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class Login extends Activity {

    private int contador = 0;
    private String usuarioVinculado="";
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        try{
            usuarioVinculado=ManagerGruasCouchDB.accountLinked(getActivity(), "");
            if(usuarioVinculado.isEmpty()){
                Intent intent= new Intent(this.getApplicationContext(), Registro.class);
                startActivityForResult(intent,1);
            }
        }catch(Exception ex){

        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        (findViewById(R.id.botonLogin)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(((EditText)findViewById(R.id.usuarioLogin)).getText().toString().equals("") || ((EditText)findViewById(R.id.passLogin)).getText().toString().equals("")){
                    (Toast.makeText(getApplicationContext(), "Deber rellenar el id de Usuario y el Password", Toast.LENGTH_SHORT)).show();
                }else{
                    new PeticionLoginGET().execute(getString(R.string.urlGestionLogin),getString(R.string.idLogin),((EditText)findViewById(R.id.usuarioLogin)).getText().toString(),getString(R.string.passLogin),((SHA1)new SHA1()).encriptar(((EditText)findViewById(R.id.passLogin)).getText().toString()),getString(R.string.resultadoLogin),getString(R.string.respuestaEsperadaLogin));
                }



            }
        });

        (findViewById(R.id.registroLink)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent= new Intent(getApplicationContext(), Registro.class);
                startActivityForResult(intent,1);
                overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
            }
        });



        //Se obtienen el tamaño de la pantalla del dispositivo
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);



        TextView logo = (TextView) findViewById(R.id.logo);

        //Animación que va a realizar el logo
        Animation movimiento = new TranslateAnimation(0, 0, (displaymetrics.heightPixels/2), 0);
        movimiento.setDuration(2000);
        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(movimiento);

        //Se establecen como invisible los elementos del login
        (findViewById(R.id.usuarioLogin)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.passLogin)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.botonLogin)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.registroLink)).setVisibility(View.INVISIBLE);

        logo.setAnimation(animation);
        animation.setAnimationListener(new MyAnimationListener());
        logo.startAnimation(animation);

    }

    //Animación del logo
    public class MyAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationEnd(Animation animation) {
            (findViewById(R.id.usuarioLogin)).setVisibility(View.VISIBLE);
            (findViewById(R.id.passLogin)).setVisibility(View.VISIBLE);
            (findViewById(R.id.botonLogin)).setVisibility(View.VISIBLE);

            //Si ya hay un usuario vinculado se oculta el registro
            if(usuarioVinculado.isEmpty()){
                (findViewById(R.id.registroLink)).setVisibility(View.VISIBLE);
            }


            //Animacion FadeIn
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new AccelerateInterpolator());
            fadeIn.setDuration(1000);
            AnimationSet animacion = new AnimationSet(false);
            animacion.addAnimation(fadeIn);

            //Se establece las animaciones a los componentes
            (findViewById(R.id.usuarioLogin)).startAnimation(animacion);
            (findViewById(R.id.passLogin)).startAnimation(animacion);
            (findViewById(R.id.botonLogin)).startAnimation(animacion);
            if(usuarioVinculado.isEmpty()){
                (findViewById(R.id.registroLink)).startAnimation(animacion);
            }

        }
        @Override
        public void onAnimationRepeat(Animation animation) {}
        @Override
        public void onAnimationStart(Animation animation) {}
    }

    public void onBackPressed() {
        if (contador == 1)
        {
            moveTaskToBack(true);
            contador = 0;
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Pulse otra vez para salir", Toast.LENGTH_SHORT).show();
            contador++;
        }
    }

    private class PeticionLoginGET extends AsyncTask<String, Integer, String> {
        ProgressDialog progDailog = new ProgressDialog(Login.this);

        @Override
        protected void onPreExecute() {
            progDailog.setMessage("Autentificando...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(false);
            progDailog.setCanceledOnTouchOutside(false);
            progDailog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            try{


                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(params[0]+"?"+params[1]+"="+params[2]+"&"+params[3]+"="+params[4]);

                HttpResponse response = httpClient.execute(httpget);
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    String respStr = EntityUtils.toString(entity);
                    String json=respStr.substring(1,respStr.length()-1);
                    JSONObject jsonObject = new JSONObject(json);
                    String resultado = jsonObject.getString(params[5]);
                    if(resultado.equals(params[6])){
                        httpClient.getConnectionManager().shutdown();
                        return "OK";
                    }
                    httpClient.getConnectionManager().shutdown();
                    return "KO";

                }
                if (entity != null) {
                    entity.consumeContent();
                }
                httpClient.getConnectionManager().shutdown();

                return "KO";

            }catch(Exception ex){
                Log.d("Error","Error Login",ex);
            }
            return "Error Login";

        }

        protected void onPostExecute(String result){
            progDailog.dismiss();

            if(result.equals("OK")){
                try {
                    final String idUsuario = ((EditText)findViewById(R.id.usuarioLogin)).getText().toString();
                    String idVinculed = ManagerGruasCouchDB.accountLinked(getActivity(), idUsuario);

                    if(idVinculed.isEmpty()) launchMenu(idUsuario);
                    else if(idUsuario.equals(idVinculed)) launchMenu(idUsuario);
                    else {
                        //notificar al usuario de que existe otra cuenta vinculada
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Login.this,AlertDialog.THEME_HOLO_DARK);
                        alertDialog.setTitle("Cuenta vinculada");
                        alertDialog.setMessage("Existe una cuenta vinculada con el dispositivo, para acceder debe vincular una nueva cuenta o acceder con la que está vinculada.");
                        alertDialog.setIcon(R.drawable.ic_launcher);

                        alertDialog.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        alertDialog.setNegativeButton("Vincular", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    ManagerGruasCouchDB.createLink(getActivity(), idUsuario);
                                    launchMenu(idUsuario);
                                } catch (CouchException e) {
                                    (Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT)).show();
                                }
                            }
                        });

                        alertDialog.show();
                    }
                } catch (CouchException e) {
                    (Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT)).show();
                }
            }
            else if(result.equals("KO")){
                (Toast.makeText(getApplicationContext(), "El id de Usuario y el Password es incorrecto", Toast.LENGTH_SHORT)).show();
            }
            else{
                (Toast.makeText(getApplicationContext(), "Se ha producido un error con el servidor de autentificación", Toast.LENGTH_SHORT)).show();
            }
        }

        protected void onProgressUpdate(Integer... progress){

        }

        protected void launchMenu(String idUsuario){
            contador = 0;
            Intent intent = new Intent(getApplicationContext(), com.gruas.app.menu.Menu.class);
            intent.putExtra("idUsuario",idUsuario);
            startActivity(intent);
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        }
    }

    protected Activity getActivity(){
        return this;
    }


}
