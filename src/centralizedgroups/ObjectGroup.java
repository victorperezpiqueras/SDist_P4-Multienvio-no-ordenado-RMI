/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package centralizedgroups;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author viktitors
 */
public class ObjectGroup {

    int gid;
    String alias;
    LinkedList<GroupMember> miembros = new LinkedList<GroupMember>();
    GroupMember propietario;
    int idUserContador = 0;
    boolean stop = false;
    final ReentrantLock mutex = new ReentrantLock(true);
    final Condition control = mutex.newCondition();
    
    private int numEnvios=0;//COMPROBAR ESTO

    /**
     * Crea un nuevo objeto de grupo cuyo alias e identificador numérico se
     * indica en los argumentos, y cuyo primer miembro es el indicado en los
     * argumentos (alias y hostname). Este último se convierte además en
     * propietario del grupo. En este caso no es necesario el bloqueo de
     * exclusión mutua.
     *
     * @param gid
     * @param galias
     * @param ualias
     * @param uhostname
     */
    public ObjectGroup(int gid, String galias, String ualias, String uhostname,int port) {
        this.gid = gid;
        this.alias = galias;
        this.propietario = new GroupMember(ualias,uhostname,0,gid,port);
        this.miembros.add(propietario);        
        //this.addMember(ualias, uhostname, port);
    }
    
    public void Sending(){
        mutex.lock();
        try{
            
            numEnvios+=this.miembros.size()-1;
            //numEnvios=this.miembros.size()-1;
            /*if(numEnvios>0)*/stop=true;
        }finally{
            mutex.unlock();
        }
    }
    public void EndSending(){
        mutex.lock();
        try{           
            numEnvios--;
            //stop=false;//stop outside the if
            if(numEnvios==0){    
                stop=false;
                control.signalAll();
            }
        }finally{
            mutex.unlock();
        }
    }
    public boolean sendGroupMessage(GroupMember gm, byte msg[]) throws RemoteException{               
        mutex.lock();
        //try {
            
            SendingMessage thread;
            GroupMessage mensaje;
            this.Sending();//1 send for all messages
            for(GroupMember miembro : this.miembros){
                if(!miembro.alias.equals(gm.alias)){//                   COMPROBAR SI DA ERROR AL COMPARAR MIEMBROS!!!!!!!!!!!!!!!!!!
                    mensaje=new GroupMessage(gm,msg);//creamos un mensaje con el texto y emisor
                    //Sending();             
                    thread=new SendingMessage(miembro,this,mensaje);//creamos el sendingmessage con destinatarios los miembros del grupo               
                    //thread.start();               
                }
            }
            mutex.unlock();
            return true;
        } //finally {
           // mutex.unlock();
        //}       
    //}

    
    
    /**
     * Se comprueba si un objeto cuyo alias se indica como argumento es miembro
     * del grupo. Si lo es retorna su correspondiente objeto de clase
     * GroupMember, en caso contrario retorna null.
     *
     */
    GroupMember isMember(String ualias) {
        mutex.lock();
        GroupMember miembro = null;
        Iterator it = miembros.iterator();
        while (it.hasNext()) {
            miembro = (GroupMember) it.next();
            if (miembro.alias.equals(ualias)) {
                mutex.unlock();
                return miembro;
            }
        }
        mutex.unlock();//si no entra en el if
        return null;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        mutex.lock();
        this.gid = gid;
        mutex.unlock();
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        mutex.lock();
        this.alias = alias;
        mutex.unlock();
    }

    public LinkedList<GroupMember> getMiembros() {
        return miembros;
    }

    public void setMiembros(LinkedList<GroupMember> miembros) {
        mutex.lock();
        this.miembros = miembros;
        mutex.unlock();
    }

    /**
     * Se incluye el objeto cuyo alias se indica en los argumentos como nuevo
     * miembro del grupo, salvo que ya existiese en dicho grupo uno con el mismo
     * alias. Al nuevo miembro se le asignar´a un nuevo identificador. Retorna
     * null si ya existe un miembro con el alias indicado. Debe bloquearse al
     * invocador si las inserciones y borrados de sus miembros est´an
     * bloqueadas.
     *
     * @param alias
     * @param hostname
     * @return
     */
    public GroupMember addMember(String alias, String hostname, int port) {
       mutex.lock();
        try {  
            
            //while (stop) {
            if(stop) {
                control.await();
            }
            
            if (this.isMember(alias) == null) {
                GroupMember nuevo = new GroupMember(alias, hostname, this.miembros.size()/*idUserContador++*/, gid, port);
                this.miembros.add(nuevo);
                System.out.println("Miembro añadido.");
                //mutex.unlock();
                return nuevo;
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ObjectGroup.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            mutex.unlock();
        }
        return null;
    }

    /**
     * Se elimina del grupo al objeto cuyo alias se indica como argumento. No
     * puede eliminarse al propietario del grupo ni un objeto que no es miembro
     * del grupo, retornando false
     *
     * @return
     */
    boolean removeMember(String alias) {
        mutex.lock();
        try {            
            //while (stop) {
            if(stop) {
                control.await();
            }
            
            GroupMember miembro = this.isMember(alias);
            if( (miembro != null) && !(propietario.alias.equals(alias)) ) {
                //mutex.unlock();
                return miembros.remove(miembro);
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(ObjectGroup.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            mutex.unlock();
        }
        return false;
    }

    public GroupMember getPropietario() {
        return propietario;
    }

    public void setPropietario(GroupMember propietario) {
        mutex.lock();
        this.propietario = propietario;
        mutex.unlock();
    }

//    public void StopMembers() {
//        mutex.lock();
//        try {
//            stop = true;
//        } finally {
//            mutex.unlock();
//        }
//    }
//
//    public void AllowMembers() {
//        mutex.lock();
//        try {
//            stop = false;
//            control.signalAll();
//        } finally {
//            mutex.unlock();
//        }
//    }
    public LinkedList<String> ListMembers(){
        mutex.lock();
        LinkedList<String> listamiembros=new LinkedList<String>();
        for(GroupMember miembro : this.miembros){
            listamiembros.add(miembro.alias);
        }
        mutex.unlock();
        return listamiembros;
    }

}
