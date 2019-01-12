/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package centralizedgroups;

import java.io.Serializable;

/**
 *
 * @author usuario
 */
public class GroupMessage implements Serializable{
    GroupMember emisor;
    byte[] mensaje;
    public GroupMessage(GroupMember emisor,byte[] mensaje){
        this.mensaje=mensaje;
        this.emisor=emisor;
    }
    public byte[] getMensaje() {
        return mensaje;
    }

    public void setMensaje(byte[] mensaje) {
        this.mensaje = mensaje;
    }

    public GroupMember getEmisor() {
        return emisor;
    }

    public void setEmisor(GroupMember emisor) {
        this.emisor = emisor;
    }
}
