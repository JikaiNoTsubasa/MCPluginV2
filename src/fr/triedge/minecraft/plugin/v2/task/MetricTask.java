package fr.triedge.minecraft.plugin.v2.task;

import java.io.IOException;
import java.util.logging.Level;
import fr.triedge.minecraft.plugin.v2.MCPluginV2;
import fr.triedge.minecraft.plugin.v2.metrics.PluginMetrics;

public class MetricTask implements Runnable{

	protected MCPluginV2 plugin;

	public MetricTask(MCPluginV2 plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		this.plugin.getLogger().log(Level.INFO,"[SCHEDULED TASK] Saving Metrics");
		try {
			PluginMetrics.storeMetricsTimed(MCPluginV2.METRIC_INFO);
		} catch (IOException e) {
			this.plugin.getLogger().log(Level.SEVERE,"Cannot save metrics",e);
		}
		this.plugin.getLogger().log(Level.INFO,"[SCHEDULED TASK] Metrics saved");
	}
}
