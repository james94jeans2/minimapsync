
package reifnsk.minimap;

/**
 *
 * @author james94jeans2 (Jens Leicht)
 *
 */

public class MinimapsyncUpdaterRei {
    
    public MinimapsyncUpdaterRei()
    {
        
    }
    
    public void updateWaypoints()
    {
        ReiMinimap.instance.loadWaypoints();
    }

}
