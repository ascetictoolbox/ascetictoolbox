package es.bsc.vmm.ascetic.scheduler.clopla;

import es.bsc.vmm.ascetic.modellers.price.ascetic.AsceticPricingModellerAdapter;
import es.bsc.demiurge.core.clopla.domain.ClusterState;
import es.bsc.demiurge.core.clopla.domain.Host;
import es.bsc.demiurge.core.clopla.placement.config.VmPlacementConfig;
import es.bsc.demiurge.core.clopla.placement.scorecalculators.ScoreCalculatorCommon;
import es.bsc.demiurge.core.configuration.Config;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;

/**
 * This class defines the score used in the cost-aware policy.
 * The score in this case contains a hard, a medium, and a soft score.
 * Hard score: overcapacity of the servers of the cluster
 *             plus number of fixed VMs that were moved. (minimize)
 * Medium score: price of running the VMs in the hosts indicated. (minimize)
 * Soft score: number of migrations needed from initial state (minimize)
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz (david.ortiz@bsc.es)
 */
public class ScoreCalculatorAsceticPriceAware implements SimpleScoreCalculator<ClusterState> {

	@Override
	public HardMediumSoftScore calculateScore(ClusterState solution) {
		return HardMediumSoftScore.valueOf(
				calculateHardScore(solution),
				calculateSoftScore(solution),
				-VmPlacementConfig.initialClusterState.get().countVmMigrationsNeeded(solution));
	}

	private int calculateHardScore(ClusterState solution) {
		return (int) (ScoreCalculatorCommon.getClusterOverCapacityScore(solution)
				+ ScoreCalculatorCommon.getClusterPenaltyScoreForFixedVms(solution));
	}

	private int calculateSoftScore(ClusterState solution) {
		double result = 0;
		for (Host host: solution.getHosts()) {
			// TO DO: make sure the next function, for all the implementations, performs as:
			// "higher values (price, energy) are less desirable"
			result -= Config.INSTANCE.getVmManager().getEstimatesManager()
					.get(AsceticPricingModellerAdapter.class)
					.getCloplaEstimation(host, solution.getVmsDeployedInHost(host));
		}
		return (int) result;
	}

}
