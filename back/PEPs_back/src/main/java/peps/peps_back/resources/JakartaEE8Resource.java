/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file defines the JakartaEE8Resource class, which is a simple REST resource for pinging the server.
 */
package peps.peps_back.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
//apparemment on a le droit de supprimer ce file mais j'ose pas pour le moment


@Path("rest")
public class JakartaEE8Resource {
    
    @GET
    public Response ping(){
        return Response
                .ok("ping")
                .build();
    }
}
