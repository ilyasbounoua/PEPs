/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file defines the Module_ class, which is a metamodel for the Module entity.
 */
package peps.peps_back.items;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.15.v20240516-rNA", date="2025-12-01T17:02:56")
@StaticMetamodel(Module.class)
public class Module_ { 

    public static volatile SingularAttribute<Module, Integer> volume;
    public static volatile SingularAttribute<Module, Integer> idmodule;
    public static volatile SingularAttribute<Module, Date> lastSeen;
    public static volatile SingularAttribute<Module, Boolean> actif;
    public static volatile SingularAttribute<Module, String> ipAdress;
    public static volatile SingularAttribute<Module, String> nom;
    public static volatile CollectionAttribute<Module, Interaction> interactionCollection;
    public static volatile SingularAttribute<Module, String> status;
    public static volatile SingularAttribute<Module, String> currentMode;

}