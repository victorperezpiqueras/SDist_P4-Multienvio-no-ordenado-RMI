/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package centralizedgroups;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedList;

/**
 *
 * @author viktitors
 */
public interface GroupServerInterface extends Remote{

    /**
     * Para crear un nuevo grupo, con identificador textual galias. El
     * propietario del grupo es su creador, con alias oalias, ubicado en
     * ohostname. Se retorna el identificador num´erico del nuevo grupo en el
     * servidor (mayor o igual a 0) o -1 en caso de error (por ejemplo, ya
     * existe un grupo con ese alias).
     *
     * @param galias
     * @param oalias
     * @param ohostname
     * @return
     */
    int createGroup(String galias, String oalias, String ohostname,int port) throws RemoteException;

    /**
     * Para localizar un grupo por su identificador textual. Retorna su
     * identificador o -1 si no existe.
     *
     * @param galias
     * @return
     */
    int findGroup(String galias) throws RemoteException;
    
    /**
     * Para localizar un grupo por su gid, retornando su alias. Devuelve
     * null si no existe.
     *
     * @param gid
     * @return
     */
    String findGroup(int gid)throws RemoteException;
    
    /**
     * Para eliminar el grupo con identi- ficador textual galias, si existe y su
     * propietario es el objeto con identificador textual oalias. Retorna true
     * si pudo borrarse, false en caso contrario.
     *
     * @param galias
     * @param oalias
     * @return
     */
    boolean removeGroup(String galias, String oalias) throws RemoteException;

    /**
     * Para eliminar el grupo con identificador num´erico gid, si existe y su
     * propietario es el objeto con identificador textual oalias. Retorna true
     * si pudo borrarse, false en caso contrario.
     *
     * @param gid
     * @param oalias
     * @return
     */
    //boolean removeGroup(int gid, String oalias) throws RemoteException;

    /**
     * Para añadir como nuevo miembro del grupo al objeto con el alias
     * indicado, que está ubicado en hostname. Retorna null si ya existe.
     *
     * @param gid
     * @param alias
     * @param hostname
     * @return
     */
    GroupMember  addMember(String galias, String alias, String hostname,int port) throws RemoteException;
    
    /**
     * Para añadir
     * como nuevo miembro del grupo galias al objeto con el alias alias indicado, que esta
     * ubicado en hostname. Retorna null si ya existe o grupo galias no existe.
     * 
     * @param galias
     * @param alias
     * @param hostname
     * @return 
     */
    boolean removeMember(String galias, String alias) throws RemoteException;
    
    /**
     * Obtiene un miembro del grupo gid por su alias como miembro del grupo.
     * Retorna null si no existe.
     *
     * @param galias
     * @param alias
     * @return
     */
    GroupMember isMember(String galias, String alias) throws RemoteException;

    /**
     * Se bloquean los intentos de a˜nadir/eliminar miembros del grupo. Devuelve
     * false si no existe ese grupo
     *
     * @param galias
     * @return
     */
   //boolean StopMembers(String galias) throws RemoteException;

    /**
     * Para permitir de nuevo las inserciones y borrados de miembros del grupo.
     * Devuelve false si no existe ese grupo.
     *
     * @param gid
     * @return
     */
    //boolean AllowMembers(String gid) throws RemoteException;
    
//    int numeroGrupos() throws RemoteException;
//    
//    public int numeromiembros(String alias)throws RemoteException;
    
    public LinkedList<String> ListMembers(String galias)throws RemoteException;
    public LinkedList<String> ListGroup()throws RemoteException;
    boolean sendGroupMessage(GroupMember gm, byte[] msg)throws RemoteException;

}
