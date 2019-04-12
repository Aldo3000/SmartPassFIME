package com.smartpassfime.smartpassfime;

import android.provider.ContactsContract;

public class GettersDeUsuarios {
    public String Matriculaa;
    public String Email;
    public String Contraseña;
    public String CuentaAbierta;

    //public String Contraseña;

    public GettersDeUsuarios(){

    }

    public GettersDeUsuarios(String Matriculaa, String Email, String Contraseña, String CuentaAbierta){
        this.Matriculaa = Matriculaa;
        this.Email = Email;
        this.Contraseña = Contraseña;
        this.CuentaAbierta = CuentaAbierta;

    }

    /*private String Uid;
    private String Usuario;
    private String Correo;
    private String Contraseña;

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getUsuario() {
        return Usuario;
    }

    public void setUsuario(String usuario) {
        Usuario = usuario;
    }

    public String getCorreo() {
        return Correo;
    }

    public void setCorreo(String correo) {
        Correo = correo;
    }

    public String getContraseña() {
        return Contraseña;
    }

    public void setContraseña(String contraseña) {
        Contraseña = contraseña;
    }*/


}
