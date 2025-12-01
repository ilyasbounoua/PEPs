/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file defines the Sound_ class, which is a metamodel for the Sound entity.
 */

package peps.peps_back.items;

import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.15.v20240516-rNA", date="2025-12-01T17:08:36")
@StaticMetamodel(Sound.class)
public class Sound_ { 

    public static volatile SingularAttribute<Sound, String> extension;
    public static volatile SingularAttribute<Sound, String> chemin;
    public static volatile SingularAttribute<Sound, String> nom;
    public static volatile CollectionAttribute<Sound, Interaction> interactionCollection;
    public static volatile SingularAttribute<Sound, String> typeSon;
    public static volatile SingularAttribute<Sound, Integer> idsound;

}