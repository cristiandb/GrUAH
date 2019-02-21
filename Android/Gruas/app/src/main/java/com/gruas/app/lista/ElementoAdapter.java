package com.gruas.app.lista;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Fragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.gruas.app.R;
import com.gruas.app.lista.Elemento;

public class ElementoAdapter extends BaseAdapter implements Filterable {
    protected Fragment activity;
    protected ArrayList<Object> elementos;
    protected ArrayList<Object> backup_elementos;
    private ValueFilter valueFilter;

    public ElementoAdapter(Fragment activity, ArrayList<Object> elementos) {
        this.activity = activity;
        this.elementos = elementos;
        this.backup_elementos = elementos;
    }

    public void setElementos(ArrayList<Object> elementos){
        this.elementos = elementos;
        backup_elementos = elementos;
    }

    @Override
    public Elemento getItem(int item){
        return (Elemento)elementos.get(item);
    }

    @Override
    public long getItemId(int pos){
        return 0;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return elementos.size();
    }

    @Override
    public View getView(int position, View contentView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = contentView;

        if (contentView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.elemento_lista, null);
        }

        Elemento elemento = (Elemento)elementos.get(position);

        ImageView image = (ImageView) vi.findViewById(R.id.imagen);
        image.setImageDrawable(elemento.icono());

        TextView hora = (TextView) vi.findViewById(R.id.hora);
        hora.setText(elemento.getFecha());

        TextView dia = (TextView) vi.findViewById(R.id.dia);
        dia.setText(elemento.getEstado());
        return vi;
    }

    @Override
    public Filter getFilter() {
        if(valueFilter == null) this.valueFilter = new ValueFilter();
        return valueFilter;
    }

    private class ValueFilter extends Filter {
        //Invoked in a worker thread to filter the data according to the constraint.
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if(constraint != null){
                ArrayList<Object> filterList = new ArrayList();
                Iterator<Object> i = backup_elementos.iterator();

                while(i.hasNext()){
                    //Buscamos cualquier coincidencia con la fecha o con el estado, se puede buscar las dos cosas a la vez
                    Elemento elem = (Elemento)i.next();
                    String cadenaBusq = constraint.toString().toUpperCase().replaceAll("\\s","");
                    String cadenaComp1 = elem.getFecha();
                    String cadenaComp2 = elem.getEstado().toUpperCase();
                    String cadenaComp3 = cadenaComp1 + cadenaComp2;
                    String cadenaComp4 = cadenaComp2 + cadenaComp1;

                    if(cadenaComp1.contains(cadenaBusq) || cadenaComp2.contains(cadenaBusq) || cadenaComp3.contains(cadenaBusq) || cadenaComp4.contains(cadenaBusq))
                        filterList.add(elem);
                }

                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = backup_elementos.size();
                results.values = backup_elementos;
            }

            return results;
        }

        //Invoked in the UI thread to publish the filtering results in the user interface.
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            elementos = (ArrayList<Object>) results.values;
            notifyDataSetChanged();
        }
    }
}
