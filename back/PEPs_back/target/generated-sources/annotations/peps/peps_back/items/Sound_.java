package peps.peps_back.items;

import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import peps.peps_back.items.Interaction;

@Generated(value="EclipseLink-2.7.15.v20240516-rNA", date="2025-11-18T12:00:21")
@StaticMetamodel(Sound.class)
public class Sound_ { 

    public static volatile SingularAttribute<Sound, String> nom;
    public static volatile CollectionAttribute<Sound, Interaction> interactionCollection;
    public static volatile SingularAttribute<Sound, String> typeSon;
    public static volatile SingularAttribute<Sound, Integer> idsound;

}