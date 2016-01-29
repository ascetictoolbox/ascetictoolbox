package es.bsc.demiurge.renewit.scheduler.clopla;

import es.bsc.demiurge.core.clopla.domain.ClusterState;
import es.bsc.demiurge.core.clopla.domain.Host;
import es.bsc.demiurge.core.clopla.placement.scorecalculators.ScoreCalculatorCommon;
import es.bsc.demiurge.core.configuration.Config;
import es.bsc.demiurge.renewit.modellers.PowerModeller;
import org.optaplanner.core.api.score.buildin.hardsoftdouble.HardSoftDoubleScore;
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;

/**
 * Created by mmacias on 14/12/15.
 */
public class ScoreCalculatorPowerAware implements SimpleScoreCalculator<ClusterState> {
	private PowerModeller powerModeller;

	public ScoreCalculatorPowerAware() {
		powerModeller = Config.INSTANCE.getVmManager().getEstimatesManager().get(PowerModeller.class);
	}
	@Override
	public HardSoftDoubleScore calculateScore(ClusterState solution) {
		// dummy score based only in estimated power. TODO: implement good policies
		double hardScore = calculateHardScore(solution);
		double softScore = 0;

		for(Host h : solution.getHosts()) {
			softScore -= powerModeller.getCloplaEstimation(h, solution.getVmsDeployedInHost(h));
		}

		return HardSoftDoubleScore.valueOf(hardScore,softScore);
	}

	private int calculateHardScore(ClusterState solution) {
		return (int) (ScoreCalculatorCommon.getClusterOverCapacityScore(solution)
				+ ScoreCalculatorCommon.getClusterPenaltyScoreForFixedVms(solution));
	}
}
