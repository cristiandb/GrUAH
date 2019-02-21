package com.gruas.app;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;


public class Registro extends FragmentActivity {

    private static SectionsPagerAdapter registroPagerAdapter;
    private static ViewPager registroViewPager;
    private static String idUsuario="", passUsuario="", nombreUsuario="",apellidosUsuario="";
    private static double longitud=0, latitud=0;
    private static short radio=0;
    private static Fragment inicio,datos,mapa,finalizar;
    private static Context context;
    private static Registro reg;


    public void setContext(){
       context=getApplicationContext();
    }

    public void setRegistro(){
       reg=this;
    }

    public static void setCurrentItem (int item, boolean smoothScroll) {
        registroViewPager.setCurrentItem(item, smoothScroll);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        setContext();
        setRegistro();
        // Crea el Adapter que devuelve un Fragment por cada sección
        registroPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Establece el ViewPager con las secciones del Adapter
        registroViewPager = (ViewPager) findViewById(R.id.pagerRegistro);
        registroViewPager.setAdapter(registroPagerAdapter);
        registroViewPager.setPageTransformer(true, new ZoomOutPageTransformer());

    }

    @Override
    protected void onStart() {
        super.onStart();
        registroViewPager.setCurrentItem(0);
    }

    /**
     * Devuelve un Fragment Correspondiente en cada Seccion/Tab/Pagina
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            inicio=new Inicio();
            datos=new Datos();
            mapa=new Mapa();
            finalizar=new Finalizar();
        }
        @Override
        public Fragment getItem(int position) {

            Fragment fragment=new Fragment();
            Bundle args = new Bundle();

            switch (position) {
                case 0:
                    fragment=new Inicio();
                    args.putInt(Inicio.ARG_FRAGMENT_1, position + 1);
                    fragment.setArguments(args);
                    break;
                case 1:
                    fragment=new Datos();
                    args.putInt(Datos.ARG_FRAGMENT_2, position + 1);
                    fragment.setArguments(args);
                    break;
                case 2:
                    fragment=new Mapa();
                    args.putInt(Mapa.ARG_FRAGMENT_3, position + 1);
                    fragment.setArguments(args);
                    break;
                case 3:
                    fragment=new Finalizar();
                    args.putInt(Finalizar.ARG_FRAGMENT_3, position + 1);
                    fragment.setArguments(args);
                    break;
                default:
                    break;
            }
            return fragment;
        }


        @Override
        public int getCount() {
            return 4;//Total paginas
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "Inicio";
                case 1:
                    return "Introducir datos";
                case 2:
                    return "Selecionar zona de actuación";
                case 3:
                    return "Finalizar registro";
            }
            return null;
        }


    }
    public static class Inicio extends Fragment {

        // Argumento del Fragment_1 que representa el número de sección
        public static final String ARG_FRAGMENT_1 = "section_number_1";

        public Inicio() { }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {


            View rootView = inflater.inflate(R.layout.registroinicio, container,false);
            (rootView.findViewById(R.id.SiguienteInicio)).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Registro.setCurrentItem(1,true);
                }
            });
            (rootView.findViewById(R.id.noRegistro)).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent returnIntent = new Intent();
                    reg.setResult(RESULT_CANCELED, returnIntent);
                    reg. finish();
                }
            });
            return rootView;
        }
    }

    public static class Datos extends Fragment {

        // Argumento del Fragment_2 que representa el número de sección
        public static final String ARG_FRAGMENT_2 = "section_number_2";

        public Datos() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.registrodatos, container, false);

            (rootView.findViewById(R.id.SiguienteDatos)).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Registro.setCurrentItem(2,true);
                }
            });

            final EditText id=(EditText)(rootView.findViewById(R.id.idUsuario));
            final EditText pass=(EditText)(rootView.findViewById(R.id.passUsuario));
            final EditText nombre=(EditText)(rootView.findViewById(R.id.nombreUsuario));
            final EditText apellidos=(EditText)(rootView.findViewById(R.id.apellidosUsuario));
            idUsuario = "";
            nombreUsuario = "";
            apellidosUsuario= "";
            passUsuario = "";

            id.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    try {
                        idUsuario = id.getText().toString();
                    } catch (Exception ex) {
                        idUsuario = "";
                    }
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after){}
                public void onTextChanged(CharSequence s, int start, int before, int count){}
            });
            pass.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    try {
                        passUsuario = pass.getText().toString();
                    } catch (Exception ex) {
                        passUsuario = "";
                    }
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after){}
                public void onTextChanged(CharSequence s, int start, int before, int count){}
            });
            nombre.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    try {
                        nombreUsuario = nombre.getText().toString();
                    } catch (Exception ex) {
                        nombreUsuario = "";
                    }
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after){}
                public void onTextChanged(CharSequence s, int start, int before, int count){}
            });
            apellidos.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    try {
                        apellidosUsuario = apellidos.getText().toString();
                    } catch (Exception ex) {
                        apellidosUsuario = "";
                    }
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after){}
                public void onTextChanged(CharSequence s, int start, int before, int count){}
            });
            return rootView;
        }
    }

    public static class Mapa extends Fragment {

        private SeekBar seekBar;
        private TextView textView;
        private GoogleMap mapa;
        private CircleOptions circleOptions = new CircleOptions()
                .strokeColor(R.color.gruas_color)
                .strokeWidth(5);
        private LatLng posicionActual;

        // Argumento del Fragment_3 que representa el número de sección
        public static final String ARG_FRAGMENT_3 = "section_number_3";
        private View rootView;

        public Mapa() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            if (rootView != null) {
                ViewGroup parent = (ViewGroup) rootView.getParent();
                if (parent != null)
                    parent.removeView(rootView);
            }
            try {
                rootView = inflater.inflate(R.layout.registromapa, container, false);
                (rootView.findViewById(R.id.SiguienteMapas)).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Registro.setCurrentItem(3,true);
                    }
                });

                seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
                textView = (TextView) rootView.findViewById(R.id.radioMapa);

                if(mapa==null) {
                    mapa = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
                }
                mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.4378271, -3.6795367), 5));

                textView.setText("Radio: "+seekBar.getProgress() + "km");

                mapa.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                    @Override
                    public void onMapClick(LatLng point) {
                        mapa.clear();
                        posicionActual=point;

                        final Marker m = mapa.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.grua))
                                .anchor(0.5f, 1.0f)
                                .position(posicionActual));
                        final ValueAnimator va = ValueAnimator.ofFloat(10, 1);
                        va.setDuration(1500);
                        va.setInterpolator(new BounceInterpolator());
                        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                m.setAnchor(0.5f, (Float) animation.getAnimatedValue());
                            }
                        });
                        va.start();
                        circleOptions.center(posicionActual);
                        circleOptions.radius(seekBar.getProgress() * 1000);
                        longitud=posicionActual.longitude;
                        latitud=posicionActual.latitude;
                        radio=(short)seekBar.getProgress();
                        mapa.addCircle(circleOptions);
                    }
                });

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar,int progresValue, boolean fromUser) {
                        textView.setText("Radio: " + progresValue + "km");
                        if(posicionActual!=null){
                            mapa.clear();
                            final Marker m = mapa.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.grua))
                                    .position(posicionActual));
                            circleOptions.center(posicionActual);
                            circleOptions.radius(progresValue*1000);
                            longitud=posicionActual.longitude;
                            latitud=posicionActual.latitude;
                            radio=(short)seekBar.getProgress();
                            mapa.addCircle(circleOptions);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
            } catch (Exception e) {
            }

            return rootView;
        }
    }

    public static class Finalizar extends Fragment {

        // Argumento del Fragment_3 que representa el número de sección
        public static final String ARG_FRAGMENT_3 = "section_number_3";

        public Finalizar() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.registrofinalizar, container,false);
            TextView nombre = (TextView)rootView.findViewById(R.id.nombre);
            TextView usuario = (TextView)rootView.findViewById(R.id.usuario);
            if(nombreUsuario.equals("")||apellidosUsuario.equals("")){
                nombre.setText("No Establecido");
            } else {
                nombre.setText(nombreUsuario+" "+apellidosUsuario);
            }
            if(idUsuario.equals("")){
                usuario.setText("No Establecido");
            }else{
                usuario.setText(idUsuario);
            }


            (rootView.findViewById(R.id.FinalizarRegistro)).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(idUsuario.equals("")||passUsuario.equals("")||nombreUsuario.equals("")||apellidosUsuario.equals("")){
                        (Toast.makeText(Registro.context, "Deber rellenar todos los datos del registro", Toast.LENGTH_SHORT)).show();
                        Registro.setCurrentItem(1,true);
                    }else if(latitud==0||longitud==0||radio==0){
                        (Toast.makeText(Registro.context, "Debe seleccionar una posición en el mapa y un radio de actuación", Toast.LENGTH_SHORT)).show();
                        Registro.setCurrentItem(2,true);
                    }else{
                        new PeticionRegistro().execute(getString(R.string.urlGestionRegistro),getString(R.string.idRegistro),idUsuario,getString(R.string.passRegistro),((SHA1)new SHA1()).encriptar(passUsuario),getString(R.string.nombreRegistro),nombreUsuario,getString(R.string.apellidosRegistro),apellidosUsuario,getString(R.string.categoriaRegistro),getString(R.string.categoriaRegistroGruas),getString(R.string.latRegistro),String.valueOf(latitud),getString(R.string.longRegistro),String.valueOf(longitud),getString(R.string.radioRegistro),String.valueOf(radio),getString(R.string.resultadoRegistro),getString(R.string.respuestaEsperadaRegistro));
                    }
                }
            });
            return rootView;
        }
    }

    public static  class PeticionRegistro extends AsyncTask<String, Integer, String> {
        ProgressDialog progDailog = new ProgressDialog(Registro.reg);

        @Override
        protected void onPreExecute() {
            progDailog.setMessage("Enviando datos...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(false);
            progDailog.setCanceledOnTouchOutside(false);
            progDailog.show();
        }
        @Override
        protected String doInBackground(String... params) {

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost(params[0]);
            post.setHeader("content-type", "application/json");

            try{
                //Construimos el objeto cliente en formato JSON y enviamos la peticion
                JSONObject dato = new JSONObject();
                dato.put(params[1],params[2]);
                dato.put(params[3],params[4]);
                dato.put(params[5],params[6]);
                dato.put(params[7],params[8]);
                dato.put(params[9],params[10]);
                dato.put(params[11],params[12]);
                dato.put(params[13],params[14]);
                dato.put(params[15],params[16]);
                StringEntity entity = new StringEntity(dato.toString());
                post.setEntity(entity);
                HttpResponse resp = httpClient.execute(post);
                String respStr = EntityUtils.toString(resp.getEntity());
                String json=respStr.substring(1,respStr.length()-1);
                JSONObject jsonObject = new JSONObject(json);
                String resultado = jsonObject.getString(params[17]);
                if(resultado.equals(params[18])){
                    return "OK";
                }
                return "KO";
            }catch(Exception ex){
                Log.d("Error", "Error Login", ex);
            }
            return "Error Login";
        }

        protected void onPostExecute(String result){
            progDailog.dismiss();

            if(result.equals("OK")){
                (Toast.makeText(Registro.context, "El registro se ha llevado acabo correctamente", Toast.LENGTH_SHORT)).show();
                Intent returnIntent = new Intent();
                reg.setResult(RESULT_CANCELED, returnIntent);
                reg.finish();
            }
            else if(result.equals("KO")){
                (Toast.makeText(Registro.context, "Los datos son incorrectos. Contacte con el administrador", Toast.LENGTH_SHORT)).show();
            }
            else{
                (Toast.makeText(Registro.context, "Se ha producido un error con el servidor de registro", Toast.LENGTH_SHORT)).show();
            }
        }
        protected void onProgressUpdate(Integer... progress){
        }
    }

    public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}