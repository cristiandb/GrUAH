package com.gruas.app.menu;

/*
* Clase para asignar un nombre y una imagen.
* @Adrián Quirós Godoy
* */

 public class Item_objct {
    private String titulo;
    private int icono;
    public Item_objct(String title, int icon) {
        this.titulo = title;
        this.icono = icon;
    }
    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    public int getIcono() {
        return icono;
    }
    public void setIcono(int icono) {
        this.icono = icono;
    }
}