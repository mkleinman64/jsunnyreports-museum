package nl.mk.jsunnyreports.loaders.inverterdataloaders.processors;

import java.io.File;

import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.cache.FileCache;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.inverters.BaseInverter;

class BaseProcessor {
    public BaseProcessor() {
    }
    
    protected BaseInverter baseInverter;
    protected File theFile;
    protected InverterData inverterData;    
    protected Settings settings;
    protected boolean init;
    protected Integer year;
    protected FileCache fc;
}
