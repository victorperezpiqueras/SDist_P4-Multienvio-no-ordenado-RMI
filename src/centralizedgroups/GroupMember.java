/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package centralizedgroups;

import java.io.Serializable;

/**
 *
 * @author viktitors
 */
public class GroupMember implements Serializable {

    String alias;
    String hostname;
    int idUser;
    int idGroup;
    int port;
 
    public GroupMember(String alias, String hostname, int idUser, int idGroup, int port) {
        this.alias = alias;
        this.hostname = hostname;
        this.idUser = idUser;
        this.idGroup = idGroup;
        this.port=port;
    }
    public boolean equals(Object o){
        GroupMember m=(GroupMember)o;
        if(this.alias.equals(m.alias)&&this.hostname.equals(m.hostname)&&
                this.idGroup==m.idGroup &&this.idUser==m.idUser&&this.port==m.port)return true;
        return false;
    }
    //SETTER:
    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public void setIdGroup(int idGroup) {
        this.idGroup = idGroup;
    }

    public void setPort(int port) {
        this.port = port;
    }

 //GETTER:   
    
    public String getAlias() {
        return alias;
    }

    public String getHostname() {
        return hostname;
    }

    public int getIdUser() {
        return idUser;
    }

    public int getIdGroup() {
        return idGroup;
    }

    public int getPort() {
        return port;
    }

}
