package nl.mk.jsunnyreports.loaders.inverterdataloaders;

import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.cache.Files;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;

import nl.mk.jsunnyreports.interfaces.LoaderInterface;

import nl.mk.jsunnyreports.inverters.BaseInverter;

import org.apache.log4j.Logger;

/**
 * Loads and processes Manual.xls exclusively for one baseInverter.
 *
 * Date         Version     Who     What
 * 19-10-2010   1.1.2.0     MKL     Big code improvements.
 * 27-11-2011   1.3.0.2A    MKL     Added filecache.
 * 28-11-2011   1.3.0.2A    MKL     Filecache is operational.
 * 03-12-2011   1.3.1.1     MKL     Updated filecache handling added invertername.
 * 09-12-2011   1.3.2.0     MKL     Updated to use Wh and long instead of kWh.
 * 12-12-2011   1.3.2.0     MKL     Moved Manual Loading to parent class.
 * 14-01-2012   1.3.2.0     MKL     Updated Exception handling, it will now continue if an error occurs in a file instead of crashing completely.
 *
 * @author Martin Kleinman ( martin@familie-kleinman.nl )
 * @version 1.3.2.0
 * @since 0.8.0.0
 */
public class CustomDataLoader extends BaseLoader implements LoaderInterface {

    public CustomDataLoader(BaseInverter inverter, InverterData inverterData, Files fileCache, Settings s) {
        super(inverter, inverterData, fileCache, s);

    }

    /**
     * Dataloader, actual method that is called for loading data.
     *
     * @throws DataLoadException when manual.xls is not found.
     * @since 0.8.0.0
     */
    public void dataLoader( boolean init, Integer year ) {
        super.dataLoader( init,year );
    }

}
