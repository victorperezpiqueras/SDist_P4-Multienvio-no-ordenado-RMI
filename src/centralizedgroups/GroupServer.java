/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package centralizedgroups;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author viktitors
 */
public class GroupServer extends UnicastRemoteObject implements GroupServerInterface {

    LinkedList<ObjectGroup> objetos = new LinkedList<ObjectGroup>();
    ReentrantLock lock = new ReentrantLock(true);
    int idGroup = 0;

    public GroupServer() throws RemoteException {
        super();//export the constructor and permit to attend requests
        lock.lock();
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        lock.unlock();

    }

    public boolean sendGroupMessage(GroupMember gm, byte[] msg) throws RemoteException{
        lock.lock();
        System.out.println(gm.alias+" IS SENDING A MESSAGE");
        if((this.findGroup(gm.idGroup))==null){    
            lock.unlock();
            return false;
        }
        
        for(ObjectGroup grupo : this.objetos){
            if(grupo.gid==gm.idGroup){
                lock.unlock();
               return grupo.sendGroupMessage(gm, msg);  
               
            }
        }
        lock.unlock();
        return false;
        //finally{
           // lock.unlock();
        //}      
    }

    @Override
    public int createGroup(String galias, String oalias, String ohostname,int port) {
        lock.lock();
        System.out.println("CREATE GROUP");
        System.out.println("Creando grupo "+galias+"...");
        if (this.findGroup(galias) == -1) {
            ObjectGroup grupo = new ObjectGroup(++idGroup, galias, oalias, ohostname,port);
            objetos.add(grupo);
        }
        lock.unlock();
        System.out.print("Creado.");
        return idGroup;
    }

    @Override
    public int findGroup(String galias) {
        lock.lock();
        System.out.println("FIND GROUP: "+galias);
        try{
        System.out.println("Buscando grupo "+galias+"...");
        for (ObjectGroup grupo : objetos) {
            if (grupo.alias.equals(galias)) {
                System.out.print("Encontrado grupo "+galias+".");
                return grupo.getGid();
            }
        }
        System.out.println("Grupo "+galias+" no encontrado");
        return -1;
        }finally{
            lock.unlock();
        }
    }
    @Override
    public String findGroup(int gid) {
        System.out.println("FIND GROUP: "+gid);
        try{
            lock.lock();
            System.out.println("Buscando grupo "+gid+"...");
            for (ObjectGroup grupo : objetos) {
                if (grupo.gid==gid) {
                    System.out.print("Encontrado grupo "+gid+".");
                    return grupo.getAlias();
                }
            }
            System.out.println("Grupo "+gid+" no encontrado");
            return null;
        }finally{
            lock.unlock();
        }
    }
    

    @Override
    public boolean removeGroup(String galias, String oalias) {
        lock.lock();
        System.out.println("REMOVE GROUP: "+galias);
        System.out.println("Eliminado grupo "+galias+"...");
        int gdelete = this.findGroup(galias);
        if (gdelete != -1) {
            for (ObjectGroup grupo : objetos) {
                if (grupo.getGid() == gdelete && grupo.getPropietario().alias.equals(oalias)) {
                    objetos.remove(grupo);
                    System.out.print(" Grupo "+galias+" eliminado.");
                    lock.unlock();
                    return true;
                }
            }
            System.out.println("Existe pero el dueño no es ese.");
        }
        lock.unlock();//si no entra en el if
        return false;
    }

    @Override
    public GroupMember addMember(String galias, String alias, String hostname,int port) {
        lock.lock();
        System.out.println("ADD MEMBER: "+alias+ " TO GROUP: "+galias);
        System.out.print("Añadiendo miembro "+alias+"...");        
        for (ObjectGroup grupo : objetos) {
            if (grupo.alias.equals(galias) && grupo.isMember(alias) == null) {
                lock.unlock();
                
                return grupo.addMember(alias, hostname,port);
                
            }
        }
        lock.unlock();//si no entra en el if
        return null;
    }

    /**
     * Eliminamos al usuario con alias "alias" que pertenece al grupo con id
     * "gid". En principio no se puede borrar al propietario, si hay tiempo
     * hacemos una version elaborada.
     *
     * @param gid
     * @param alias
     * @return
     */
    @Override
    public boolean removeMember(String galias, String alias) {
        lock.lock();
        System.out.println("REMOVE MEMBER: "+alias+ " FROM GROUP: "+galias);
        System.out.println("Eliminado miembro "+alias+"...");
        for (ObjectGroup grupo : objetos) {
            if (grupo.alias.equals(galias) && grupo.isMember(alias) != null) {
                lock.unlock();
                return grupo.removeMember(alias);
           }
        }
        lock.unlock();//si no entra en el if
        return false;
    }

    @Override
    public GroupMember isMember(String galias, String alias) {
        lock.lock();
        System.out.println("IS MEMBER: "+alias+ " FROM GROUP: "+galias+" ?");
        System.out.println("Comprobando pertenencia miembro "+alias+" a grupo "+galias+"...");
        int groupid=findGroup(galias);
        if(groupid!=-1){
            for(ObjectGroup grupo : objetos){
                if (grupo.gid == groupid) {                  
                    if(grupo.isMember(alias)==null)System.out.println("No es miembro");
                    else System.out.println("Es miembro.");
                    lock.unlock();
                    return grupo.isMember(alias);
                }
            }
        }
        lock.unlock();//si no entra en el if
        return null;
    }

//    @Override
//    public boolean StopMembers(String galias) {
//        lock.lock();
//        System.out.println("Bloqueando altas y bajas de miembros al grupo "+galias+"...");              
//        for (ObjectGroup grupo : objetos) { 
//             if (grupo.getAlias().equals(galias)) {
//                 lock.unlock();
//                 grupo.StopMembers();
//                 System.out.println("Bloqueado");
//                 return true;
//             }  
//        }
//        lock.unlock();//si no entra en el if
//        return false;
//    }
//    @Override
//    public boolean AllowMembers(String gid) {
//        lock.lock();
//        System.out.println("Desbloqueando altas y bajas de miembros al grupo "+gid+"...");
//        for (ObjectGroup grupo : objetos) {
//            if (grupo.getAlias().equals(gid)) {
//                lock.unlock();
//                grupo.AllowMembers();
//                System.out.println("Desbloqueado");
//                return true;
//            }
//        }
//        lock.unlock();//si no entra en el if
//        return false;
//    }

    
    //MUESTRA TODOS LOS MIEMBROS DE UN GRUPO
    public LinkedList<String> ListMembers(String galias){
        System.out.println("LIST MEMBERS FROM GROUP: "+galias);
        System.out.println("Miembros del grupo "+galias+": \n");
        for (ObjectGroup grupo : objetos) {
            if (grupo.getAlias().equals(galias)) {  
               return grupo.ListMembers();  
            }
        }
        return new LinkedList<String>();        
    }
    
    public LinkedList<String>ListGroup() {
        System.out.println("LIST GROUPS: ");
        LinkedList<String> grupos= new LinkedList<String>();
        System.out.println("Lista de grupos:");
        for (ObjectGroup grupo : objetos) {             
             grupos.add(grupo.getAlias()); 
        }        
        return grupos;
    }
    
    
    /**
     * @param args the command line arguments
     * @author viktitors
     */
    public static void main(String[] args) throws UnknownHostException, MalformedURLException {
        System.setProperty("java.security.policy", "src\\centralizedgroups\\server-policy");
        try {
            GroupServer srv = new GroupServer();//creates the server of type GroupServer
            
            String ip=InetAddress.getLocalHost().getHostAddress();//IP
            int port = 1099; //port
            LocateRegistry.createRegistry(port);//creates the registry with the desired port 
            Naming.rebind("GroupServer",srv);//assigns a name for the proxy to allow clients to find it
            
            System.out.println("Servidor iniciado en: "+ip+" : "+port);
            
  //          Naming.rebind("rmi://" + ip + ":" + port + "/GroupServer",srv);
            //Registry registry = LocateRegistry.createRegistry(port);//gets the registry created                 //VERSION CON REGISTRO           
            //registry.rebind("rmi://" + ip + ":" + port + "/GroupServer",srv);                                   //VERSION CON REGISTRO
            
                    
        } catch (RemoteException ex) {
            Logger.getLogger(GroupServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
