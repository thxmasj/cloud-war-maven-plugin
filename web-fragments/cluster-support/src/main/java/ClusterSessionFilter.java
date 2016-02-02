import javax.servlet.DispatcherType;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

/**
 * @author thomas@thomasjohansen.it
 */
//@WebFilter(
//        filterName = "hazelcast",
//        initParams = {
//                @WebInitParam(name = "map-name", value = "web-sessions"),
//                @WebInitParam(name = "sticky-sessions", value = "true")
//        },
//        dispatcherTypes = {
//                DispatcherType.REQUEST,
//                DispatcherType.FORWARD,
//                DispatcherType.INCLUDE
//        },
//        urlPatterns = "/*"
//)
public class ClusterSessionFilter extends com.hazelcast.web.WebFilter {
}
