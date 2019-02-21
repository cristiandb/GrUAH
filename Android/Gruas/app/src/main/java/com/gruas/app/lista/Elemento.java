package com.gruas.app.lista;

import android.graphics.Color;

import com.gruas.app.UtilDateFormat;
import com.gruas.app.couchBaseLite.Service;
import com.gruas.app.couchBaseLite.adapter.AdapterObject;

import java.util.Date;

public class Elemento implements AdapterObject{
	private String fecha;
    private String estado;
    private String idDoc;

	public Elemento(long date, String estado, String idDocument ){
		this.fecha = UtilDateFormat.getStringDateFormatWithLongTime(date);
        this.idDoc = idDocument;
        this.estado=estado;
	}

    @Override
    public String getIdDoc(){
        return this.idDoc;
    }

	public String getFecha(){
		return this.fecha;
	}

    public String getEstado(){
        return this.estado;
    }

    public CharacterDrawable icono() {
        CharacterDrawable cd;
        if(estado.equals(Service.StateService.ESPERA.getDescription()))
            cd = new CharacterDrawable('W', Color.parseColor("#EEEEEE"));
        else if(estado.equals(Service.StateService.ACEPTADO.getDescription()))
            cd = new CharacterDrawable('A', Color.parseColor("#FFBD21"));
        else if(estado.equals(Service.StateService.RECHAZADO.getDescription()))
            cd = new CharacterDrawable('R', Color.parseColor("#CC0000"));
        else if(estado.equals(Service.StateService.PAUSADO.getDescription()))
            cd = new CharacterDrawable('P', Color.parseColor("#9933CC"));
        else if(estado.equals(Service.StateService.CURSO.getDescription()))
            cd = new CharacterDrawable('C', Color.parseColor("#50C0E9"));
        else if(estado.equals(Service.StateService.FINALIZADO.getDescription()))
            cd = new CharacterDrawable('F', Color.parseColor("#6DA000"));
        else
            cd = new CharacterDrawable('E', Color.parseColor("#9B0600"));

      return cd;
    }
}
