package peps.peps_back.items;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.15.v20240516-rNA", date="2025-12-01T17:08:36")
@StaticMetamodel(Interaction.class)
public class Interaction_ { 

    public static volatile SingularAttribute<Interaction, Module> idmodule;
    public static volatile SingularAttribute<Interaction, Date> timeLancement;
    public static volatile SingularAttribute<Interaction, Integer> idinteraction;
    public static volatile SingularAttribute<Interaction, String> typeinteraction;
    public static volatile SingularAttribute<Interaction, Sound> idsound;

}