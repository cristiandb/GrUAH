
package com.gruas.app.menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import com.gruas.app.couchBaseLite.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.gruas.app.GruasDelegate;
import com.gruas.app.Login;
import com.gruas.app.R;
import com.gruas.app.couchBaseLite.CouchException;
import com.gruas.app.couchBaseLite.adapter.DocumentsAdapter;
import com.gruas.app.Estadisticas;
import com.gruas.app.fragments.FiltroLista;
import com.gruas.app.fragments.Lista;
import com.gruas.app.notificaciones.Notificacion;
import com.gruas.app.services.gps.GPSCouchDBService;
import com.gruas.app.services.gps.GPSException;
import com.gruas.app.services.gps.GPSObject;
import com.gruas.app.servicio.VistaServicio;

import java.util.ArrayList;


public class Menu extends Activity implements GruasDelegate {
    private String[] titulos;
    private DrawerLayout navDrawerLayout;
    private ListView navList;
    private ArrayList<Item_objct> navItms;
    private TypedArray navIcons;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationAdapter navAdapter;
    private String idUsuario;
    private DocumentsAdapter adaptador;
    private ProgressBar bar;
    private Fragment fragment;
    private String action = "";
    private NotificationManager nm;
    private MenuItem search;
    private MenuItem unlink;
    private android.view.Menu menuOptions;
    private boolean isDisplayFragment = false;
    private boolean realizar_busqueda = false;
    private ProgressDialog progDailog=null;
    private SearchView searchView = null;
    private RelativeLayout rl=null;

    //Variables del GPS
    private GPSCouchDBService gpsservice;
    private ServiceConnection scgps = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            gpsservice = ((GPSCouchDBService.GPSBinder) iBinder).getService();

            if(!gpsservice.isServiceLaunched()){
                gpsservice.createGPSManager((LocationManager) getSystemService(Context.LOCATION_SERVICE));

                try {
                    gpsservice.setDelegate(getActivity());
                    gpsservice.launchGPS(getIdUsuario());
                } catch (GPSException e) {
                    Log.d("GPSError", e.getMessage());
                }
            } else
                gpsservice.checkNotification(getActivity(),action);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            gpsservice = null;
        }
    };

    public Activity getActivity(){
        return this;
    }

    @Override
    public void handlerFatalError(GPSException.TypeErrors typeGPS, CouchException.TypeErrors typeCouch, String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handlerErrorCouch(CouchException.TypeErrors type,String error){
        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public DocumentsAdapter getDocumentsAdapter(){
        return adaptador;
    }

    @Override
    public void startedServiceCouch(DocumentsAdapter adaptador,boolean gps_enabled) {
        //Esta lanzado couch, se puede acceder a los fragment, pero antes debemos habilitar el gps si no lo esta
        this.adaptador = adaptador;

        if(!gps_enabled){
            runOnUiThread(new Runnable() {
                // No se pueden lanzar desde un hilo que no sea el hilo principal, cambios en un view
                @Override
                public void run() {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(Menu.this,AlertDialog.THEME_HOLO_DARK);

                    alertDialog.setTitle("Activar GPS")
                            .setMessage("El GPS está desactivado. Debe activarlo para el correcto funcionamiento de la aplicación.")
                            .setIcon(R.drawable.ic_launcher);

                    alertDialog.setPositiveButton("Activar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int which) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    });
                    alertDialog.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialog.show();
                }
            });
        }

        disappearBar();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        rl=(RelativeLayout) this.findViewById(R.id.imagenComienzo);
        rl.setVisibility(View.GONE);
        bar = (ProgressBar) this.findViewById(R.id.progressBar);
        bar.setVisibility(View.GONE);
        bar.setVisibility(View.VISIBLE);

        Bundle extras = getIntent().getExtras();
        if(extras != null)
            this.idUsuario = extras.getString("idUsuario");

        //Lanzar GPS
        bindService(new Intent(this, GPSCouchDBService.class), scgps, Context.BIND_AUTO_CREATE);

        navDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navList = (ListView) findViewById(R.id.left_drawer);
        navList.addHeaderView(getLayoutInflater().inflate(R.layout.header, null));

        //Tomamos listado  de imgs desde drawable
        navIcons = getResources().obtainTypedArray(R.array.navigation_iconos);
        //Tomamos listado  de titulos desde el string-array de los recursos @string/nav_options
        titulos = getResources().getStringArray(R.array.nav_options);
        //Listado de titulos de barra de navegacion
        navItms = new ArrayList<Item_objct>();

        //Agregamos objetos Item_objct al array
        for(int i=0;i<titulos.length;i++){
            navItms.add(new Item_objct(titulos[i], navIcons.getResourceId(i, -1)));
        }

        //Declaramos y seteamos nuestrp adaptador al cual le pasamos el array con los titulos
        navAdapter = new NavigationAdapter(this, navItms);
        navList.setAdapter(navAdapter);

        //Establecemos la accion al clickear sobre cualquier item del menu.
        navList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                mostrarFragment(position);
            }
        });

        //Declaramos el mDrawerToggle y las imgs a utilizar
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                navDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_navigation_drawer,  /* Icono de navegacion*/
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        // Establecemos que mDrawerToggle declarado anteriormente sea el DrawerListener
        navDrawerLayout.setDrawerListener(mDrawerToggle);
        action = getIntent().getAction();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        if(search == null) search = menu.findItem(R.id.search);
        if(unlink == null) unlink = menu.findItem(R.id.unlinked);
        if(isDisplayFragment) search.setVisible(true);
        prepareSearch(menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void prepareSearch(android.view.Menu menu){
        menuOptions = menu;
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
		searchView.setQueryHint("Buscar");
        searchView.setSubmitButtonEnabled(false);
		final SearchView searchViewAux = searchView;
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if (realizar_busqueda) {
                    ((Lista) fragment).filterList((CharSequence) newText);
                    return true;
                } else {
                    realizar_busqueda = true;
                    return false;
                }
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                searchViewAux.setQuery("", false);
                return true;
            }
        });
    }

    /*Pasando la posicion de la opcion en el menu nos mostrara el Fragment correspondiente*/
    private void mostrarFragment(int position) {
        boolean show_fragment = false;
        rl.setVisibility(View.GONE);
        switch (position) {
            case 1:
                Service servicio = gpsservice.obtenerServicioEnCurso();
                if(servicio == null) {
                    if(search != null && fragment != null) search.setVisible(isDisplayFragment);
                    Toast.makeText(getApplicationContext(), "No hay ningún servicio en curso.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), VistaServicio.class);
                    intent.putExtra("Servicio", servicio);
                    startActivity(intent);
                }
                break;
            case 5:
                Intent intent = new Intent(getApplicationContext(), Estadisticas.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "Acceso a  " + titulos[position - 1], Toast.LENGTH_SHORT).show();
                break;
            case 6:
                startlogout("Cerrando sesión...",false); //Logout
                break;
            default:
                prepararFragment(position);
                show_fragment = true;
        }

        navDrawerLayout.closeDrawer(navList);
        if (show_fragment) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

            // Actualizamos el contenido segun la opcion elegida
            navList.setItemChecked(position, true);
            navList.setSelection(position);

            setTitle(titulos[position-1]); //Cambiamos el titulo en donde decia "
            navDrawerLayout.closeDrawer(navList); //Cerramos el menu deslizable
        }

        if(search != null) search.setVisible(isDisplayFragment); //Mostramos o no la barra de busqueda
    }

    private FiltroLista obtenerFiltro(int position){
        FiltroLista filtro;
        switch (position){
            case 2: filtro = FiltroLista.LISTA_PENDIENTES; break;
            case 3: filtro = FiltroLista.LISTA_ESPERANDO_APROBACION; break;
            default: filtro = FiltroLista.LISTA_HISTORIAL;
        }
        return filtro;
    }
	
	public void onBackPressed() {
            moveTaskToBack(false);
    }

    private void prepararFragment(int position){
        isDisplayFragment = true;
        Toast.makeText(getApplicationContext(), "Acceso a  " + titulos[position - 1], Toast.LENGTH_SHORT).show();
        if(fragment == null) fragment = new Lista(adaptador);
        if(position == 4) cancelarNotificacion(); //Si hay alguna notificacion para este activity la quitamos
        ((Lista)fragment).setFiltro(obtenerFiltro(position));
        ((Lista)fragment).refresh();
    }

    @Override
    public void cancelarNotificacion(){
        if(Notificacion.isMULTIPLE_NOTIFICATION()){
            if(nm == null) nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int notificactionID = 1;
            nm.cancel(notificactionID);
            gpsservice.clearCacheAdapter();
        }
    }

    @Override
    public void pressBackAnotherActivity() {
        if(isDisplayFragment) ((Lista)fragment).refresh();
    }

    @Override
    public void clearTextSearch() {
        if(searchView != null){
            realizar_busqueda = false;
            searchView.clearFocus();
            searchView.onActionViewCollapsed();
            searchView.setQuery("", false);
        }
    }

    @Override
    public void stopedGPSCouchDBService() {
        unbindService(scgps);
        runOnUiThread(new Runnable() {
                          // No se pueden lanzar desde un hilo que no sea el hilo principal, cambios en un view
                          @Override
                          public void run() {
                              navDrawerLayout.closeDrawers();
                          }
                      });

        if(progDailog != null)
            progDailog.dismiss();

        //Visualizamos el activity de Login
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);

    }

    private void disappearBar(){
        bar.setVisibility(View.GONE); //Quitamos la barra de carga
        getActionBar().setDisplayHomeAsUpEnabled(true); //Mostramos el botón del actionBar
        getActionBar().setHomeButtonEnabled(true);
        rl.setVisibility(View.VISIBLE);
    }

    private void startlogout(String message, boolean desvincular){
        progDailog= new ProgressDialog(Menu.this);
        progDailog.setMessage(message);
        progDailog.setIndeterminate(false);
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDailog.setCancelable(false);
        progDailog.setCanceledOnTouchOutside(false);
        progDailog.show();
        gpsservice.stopServices(desvincular);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Called by the system when the device configuration changes while your
        // activity is running
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item))  return true;
        else if(item.getItemId() == R.id.unlinked){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Menu.this,AlertDialog.THEME_HOLO_DARK);
            alertDialog.setTitle("Desvincular cuenta");
            alertDialog.setMessage("¿Desea desvincular la cuenta " + idUsuario + "?");
            alertDialog.setIcon(R.drawable.ic_launcher);

            alertDialog.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            alertDialog.setNegativeButton("Desvincular", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startlogout("Desvinculando cuenta ...", true); //Desvincular
                }
            });

            alertDialog.show();
        }

        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void cambioEstadoGPS(int i) {
        /*
            * Cuando se produce un cambio en el proveedor.Estados del proveedor (class LocationProvider):
            * OUT_OF_SERVICE. Fuera de Servicio
            * TEMPORARILY_UNAVAILABLE. Temporalmente disponible, se espera que este pronto disponible
            * AVALAIBLE. Disponible
        */
    }

    @Override
    public void cambioLocalizacion(GPSObject gpsobject) {

    }

    @Override
    public void enabledGPS(boolean state) {

    }

    @Override
    public String getIdUsuario() {
        return idUsuario;
    }

    @Override
    public GPSObject getLastLocation() {
        return gpsservice.getLastLocation();
    }

    @Override
    public void notificationReceived(DocumentsAdapter da, String idUser) {
        adaptador = da;
        idUsuario = idUser;
        disappearBar();
        cancelarNotificacion();
        mostrarFragment(3);
    }
}


