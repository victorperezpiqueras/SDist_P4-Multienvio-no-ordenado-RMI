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
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author viktitors
 */
public class Client extends UnicastRemoteObject implements ClientInterface {

    //auxiliar attributes:
    int id; // Identificador del grupo
    static GroupServerInterface srv;
    String alias = "", galias = "", ualias = "", hostname = "";
    
    //client attributes:
    String aliasusuario,aliasgrupo;
    int port;
    String serverip;
    
    //utilities:
    public Scanner in;    
    LinkedList<GroupMessage> queue=new LinkedList<GroupMessage>();
    final ReentrantLock mutex = new ReentrantLock(true);
    final Condition control = mutex.newCondition();

    public Client() throws RemoteException {
        super();
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        System.out.println("Introduce el alias del cliente:");
        in = new Scanner(System.in);
        this.aliasusuario = in.next();
        
        System.out.println("Introduce el puerto del cliente:");
        in = new Scanner(System.in);
        this.port=in.nextInt();
        
        System.out.println("Introduce la ip del servidor:");
        in = new Scanner(System.in);
        this.serverip=in.next();
    }
    
    // es un CALLBACK:client objects can act as remote objects,offering some methods to call as ack of the server request
    //if it doesn't implement callbacks, it has no reason to be a remote object.
    public void DepositMessage(GroupMessage m)throws RemoteException{
        
        mutex.lock();
        try{        
            queue.add(m);
            control.signalAll();
            //control.signal();
        }finally{
            mutex.unlock();
        }
    }
    public byte[] receiveGroupMessage(String galias)throws RemoteException{
        mutex.lock();
        try {
            
        int existegrupo=srv.findGroup(galias);
        byte[]msg=null;
        if(existegrupo==-1)return null;
        
        while (true) {
            //if(this.queue.size()>0){
            //if((srv.findGroup(galias)!=-1)&&(srv.isMember(galias, this.aliasusuario)!=null)){
                for(GroupMessage m : this.queue){
                    if(m.emisor.idGroup==(existegrupo)){
                        //msg=m.mensaje.clone();
                        msg=m.mensaje;
                        this.queue.remove(m);
                        return msg;
                    }
                }
            //}
           // else{
                control.await();
            //}
            //} 
        }
        } catch (RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (InterruptedException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        finally{
            mutex.unlock();
        }   
    }
    public void crearGrupo() throws UnknownHostException {
        try {
            System.out.println("Pon nombre de grupo");
            //galias = in.next();
            aliasgrupo = in.next();
            id = srv.createGroup(this.aliasgrupo/*galias*/, this.aliasusuario, InetAddress.getLocalHost().getHostAddress(),this.port);
            if (id != -1) {
                System.out.println("Hemos creado tu grupo, de id: " + id);
                //aliasgrupo=galias;//guardar nombre grupo
            }
        } catch (RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void EliminarGrupo() {
        try {
            System.out.println("Pon nombre del dueño");
            ualias = in.next();
        System.out.println("Pon nombre del grupo");
        galias = in.next();
        if (srv.removeGroup(galias, ualias)) {
            System.out.println("Borrado con exito");
        }
        } catch (RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void AñadirMiembro() {
        try {
            System.out.println("Pon alias de grupo");
            galias = in.next();
            System.out.println("Pon nombre de usuario");
            ualias = in.next();
            System.out.println("Pon nombre de host");
            hostname = in.next();
            System.out.println("Pon puerto de usuario");
            port = in.nextInt();
            
            
            if (srv.addMember(galias, ualias, hostname,port) != null) {
                System.out.println("Añadido con exito");
            } else {
                System.out.println("Ya existia");
            }
        } catch (RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void EliminarMiembro() {
        try {
            System.out.println("Pon nombre de usuario");
            ualias = in.next();
            System.out.println("Pon nombre de grupo");
            galias = in.next();
            if (!srv.removeMember(galias, ualias)) {
                System.out.println("Ya existía");
            } else {
                System.out.println("Todo bien");
            }
        } catch (RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    public void BloquearAltasBajas() {
//        try {
//            System.out.println("Pon alias de grupo");
//            galias = in.next();
//            srv.StopMembers(galias);
//        } catch (RemoteException ex) {
//            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    public void DesbloquearAltasBajas() {
//        try {
//            System.out.println("Pon alias de grupo");
//            galias = in.next();
//            srv.AllowMembers(galias);
//        } catch (RemoteException ex) {
//            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

    public void Terminar() {
        try {
            UnicastRemoteObject.unexportObject(this, true);
        } catch (NoSuchObjectException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void MostrarGrupos(){
        try {
            System.out.println("Lista de Grupos:");
            for(String grupo :srv.ListGroup()){
                System.out.println(grupo);
            }
        } catch (RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void MostrarMiembros(){
        try {
            System.out.println("Introduce el nombre del grupo a listar: ");
            galias=in.next();
            System.out.println("Lista de miembros del grupo: "+galias);
            for(String miembro : srv.ListMembers(galias)){
                System.out.println(miembro);
            }
        } catch (RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     * @author viktitors
     */
    public static void main(String[] args) throws UnknownHostException, MalformedURLException {
        System.setProperty("java.security.policy", "src\\centralizedgroups\\cliente-policy");

        try {
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            Client c = new Client();
            
            int serverPort=1099;
            
            Registry reg = LocateRegistry.getRegistry(c.serverip, serverPort);//Returns a reference to the remote object Registry on the specified host and port.
            srv=(GroupServerInterface) reg.lookup("GroupServer");//Returns the remote reference bound to the specified name in this registry.
            
            LocateRegistry.createRegistry(c.port);    //create a registry with the client's port
            Naming.rebind("rmi://" + InetAddress.getLocalHost().getHostAddress() + ":" + c.port + "/" + c.aliasusuario, c);
            //rebind the client so it can be found by the server, to let it use the client's proxy// Obtaining references to remote objects in a remote object registry.
            System.out.println("Cliente conectado desde: " + InetAddress.getLocalHost().getHostAddress() + ":" + c.port);
            
            
            
            
            int i = 0;

            System.out.println("Elija una accion:");
            while (i < 10) {
                System.out.println("1. Crear grupo");
                System.out.println("2. Eliminar grupo");
                System.out.println("3. Añadir miembro");
                System.out.println("4. Eliminar miembro");
                System.out.println("5. Recibir mensajes");
                System.out.println("6. Enviar mensaje");
                System.out.println("7. Terminar");
                System.out.println("8. Mostrar lista de miembros del grupo");
                System.out.println("9. Mostrar lista de grupos");
                int s=c.in.nextInt();
                String nombregrupo;
                byte[] msg;
                switch (s) {
                    case 1://1. Crear grupo
                        c.crearGrupo();
                        break;
                        
                    case 2://2. Eliminar grupo
                        c.EliminarGrupo();
                        break;
                        
                    case 3://3. Añadir miembro
                        c.AñadirMiembro();
                        break;
                        
                    case 4://4. Eliminar miembro
                        c.EliminarMiembro();
                        break;
                        
                    case 5://5. recibir mensajes             
                        System.out.println("Introduzca el nombre del grupo:");
                        nombregrupo=c.in.next();
                        msg=c.receiveGroupMessage(nombregrupo);
                        if(msg==null){
                            System.out.println("No hay mensajes en el grupo: "+nombregrupo);
                        }
                        else{
                            System.out.println("Nuevo mensaje del grupo \""+nombregrupo+"\" : "+new String(msg));
                        }
                        break;
                        
                    case 6: //6. enviar mensaje
//                        System.out.println("Introduzca el nombre del grupo:");
//                        nombregrupo=c.in.next();
//                        System.out.println("Introduce un mensaje a enviar:");
//                        msg = c.in.next().getBytes();
//                        GroupMessage mensaje =
//                         new GroupMessage(c.srv.isMember(c.aliasusuario,nombregrupo),nombregrupo.getBytes());
//                        c.DepositMessage(mensaje);
                        System.out.println("Introduzca el nombre del grupo: ");
                        nombregrupo=c.in.next();
                        System.out.println("Introduce el mensaje: "); 
                        msg=c.in.next().getBytes();
                        
                        GroupMember gm= srv.isMember(nombregrupo, c.aliasusuario);
                        System.out.println("Es: "+ c.aliasusuario+" miembro del grupo: "+nombregrupo+" ?");
                        if(gm!=null){
                            if(c.srv.sendGroupMessage(gm,msg))System.out.println("Mensaje enviado");         
                            else System.out.println("Error al enviar");
                        }
                        else System.out.println("Estas intentando enviar un mensaje a un grupo al que no perteneces");
                        
                        break;
                        
                    case 7:
                        c.Terminar();
                        i = 10;
                        break;
                        
                    case 8:
                        c.MostrarMiembros();
                        break;
                        
                    case 9:
                        c.MostrarGrupos();
                        break;
                }
                i++;
            }
        } catch (RemoteException ex) {
            Logger.getLogger(GroupServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotBoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
