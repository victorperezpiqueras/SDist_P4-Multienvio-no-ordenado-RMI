/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package centralizedgroups;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author usuario
 */
public class SendingMessage extends Thread {
    GroupMessage mensaje;   
    ObjectGroup grupo;
    GroupMember miembro;
    
    ClientInterface destinatario;
       
    public SendingMessage(GroupMember miembro,ObjectGroup grupo,GroupMessage mensaje){
        this.mensaje=mensaje;
        this.miembro=miembro;
        this.grupo=grupo;
        //this.run();
        this.start();
    }
    
    @Override
    public void run(){
        System.setProperty("java.security.policy", "src\\centralizedgroups\\server-policy");
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());//gestor de seguridad
        }
        //for(GroupMember miembro:grupo.getMiembros()){//////////////////////////////////////////////comentar o no
            //if(!miembro.alias.equals(mensaje.emisor.alias)){/////////////////////////
                try {
                    destinatario=(ClientInterface)Naming.lookup("rmi://"+miembro.hostname+":"+miembro.port+"/"+miembro.alias);
                    //permit us to localizate the registry and to obtain the server reference, looking for the proxy of the server
                    
                    
                    
                    //buscar el objeto miembro en el registro 
                    if(destinatario!=null){
                        //Thread.sleep((int)(Math.random()*(60000-30000)) + 30000);
                        //Thread.sleep(30 + (int)(Math.random()*60));
                        
                       
                        Random random= new Random();
                        int randomNum = random.nextInt(30000);
                        System.out.println("Delay  "+ miembro.alias +" Random: "+randomNum+30000);
                        SendingMessage.sleep(30000 + randomNum);
                        
                        
                        destinatario.DepositMessage(mensaje);
                    }
                } catch (RemoteException | NotBoundException ex) {
                    Logger.getLogger(SendingMessage.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Excepción en el thread.run()");
                } catch (InterruptedException ex) {
                    Logger.getLogger(SendingMessage.class.getName()).log(Level.SEVERE, null, ex);
                //}
            //}/////////////
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(SendingMessage.class.getName()).log(Level.SEVERE, null, ex);
        }/////////////////////////////////////////////////
        finally{
            grupo.EndSending();
        }
    }            
}
