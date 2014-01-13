package fi.nls.oskari.domain.map.stats;

import java.util.ArrayList;
import java.util.List;

import fi.nls.oskari.domain.map.Layer;

@Deprecated
public class StatsLayer extends Layer {
	private List<StatsVisualization> visualizations = new ArrayList<StatsVisualization>();

	public StatsLayer() {
		super.setType(Layer.TYPE_STATS);
	}

    public List<StatsVisualization> getVisualizations() {
        return visualizations;
    }

    public void setVisualizations(final List<StatsVisualization> visualizations) {
        this.visualizations = visualizations;
    }
}
