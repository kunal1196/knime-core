/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Oct 1, 2018 (simon): created
 */
package org.knime.base.node.meta.feature.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.knime.optimization.genetic.ga2.GenerationalRankBasedEvolution;
import org.knime.optimization.genetic.ga2.individuals.combinations.BinaryCombinationUXIndividual;

/**
 *
 * @author simon
 */
public class EvolutionaryStrategy implements FeatureSelectionStrategy {

    private final int m_subsetSize;

    private final List<Integer> m_featureColumns;

    private final Map<Integer, Double> m_scores;

    private final Random m_random;

    private BinaryCombinationUXIndividual m_current;

    private Double m_currentScore;

    private boolean m_isMinimize;

    private double m_currentBestScore;

    private int m_currentIteration;

    private final int m_maxIteration = 3;

    private final int m_popSize = 10;

    private List<BinaryCombinationUXIndividual> m_popToEvaluate;

    private List<BinaryCombinationUXIndividual> m_popEvaluated;

    private boolean m_continueLoop = true;

    /**
     * @param subSetSize subset size at which the search should stop.
     * @param features ids of the features.
     *
     */
    public EvolutionaryStrategy(final int subSetSize, final List<Integer> features) {
        m_subsetSize = subSetSize;
        m_featureColumns = features;
        m_scores = new HashMap<>();
        m_random = new Random();
        createInitialPopulation();
        m_currentIteration = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean continueLoop() {
        return m_continueLoop;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getIncludedFeatures() {
        List<Integer> list = Arrays.stream(m_current.getSelected()).boxed().collect(Collectors.toList());
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addScore(final double score) {
        m_current.getFitness()[0] = score;
        m_scores.put(m_current.hashCode(), score);
        m_currentBestScore = score;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIsMinimize(final boolean isMinimize) {
        m_isMinimize = isMinimize;
        m_currentBestScore = initialBestScore();
    }

    private double initialBestScore() {
        return m_isMinimize ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldAddFeatureLevel() {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getCurrentlyBestScore() {
        return m_currentBestScore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareNewRound() {
        m_popEvaluated.add(m_current);
        if (!m_popToEvaluate.isEmpty()) {
            m_current = m_popToEvaluate.remove(0);
        } else {
            m_currentIteration++;
            if (m_currentIteration >= m_maxIteration) {
                m_continueLoop = false;
                return;
            }
            GenerationalRankBasedEvolution<BinaryCombinationUXIndividual> evolution =
                new GenerationalRankBasedEvolution<>(0); // TODO set elitism factor
            BinaryCombinationUXIndividual[] newPop =
                m_popEvaluated.toArray(new BinaryCombinationUXIndividual[m_popEvaluated.size()]);
            Arrays.sort(newPop);
            evolution.evolve(newPop);
            m_popEvaluated.clear();
            for (final BinaryCombinationUXIndividual ind : newPop) {
                if (m_scores.containsKey(ind.hashCode())) {
                    ind.getFitness()[0] = m_scores.get(ind.hashCode());
                } else {
                    m_popToEvaluate.add(ind);
                }
            }
            if (m_popToEvaluate.isEmpty()) {
                m_continueLoop = false;
            } else {
                //            m_popToEvaluate.addAll(Arrays.asList(newPop));
                m_current = m_popToEvaluate.remove(0);
            }
        }

        //        Factory<Genotype<BitGene>> gtf = Genotype.of(BitChromosome.of(10, 0.5));
        //
        //                Builder<BitGene, Double> builder = Engine.builder(EvolutionaryStrategy::eval, gtf);
        //
        //                Engine<BitGene,Double> build = builder.build();

    }

    private void createInitialPopulation() {
        m_popToEvaluate = new ArrayList<>();
        m_popEvaluated = new ArrayList<>();
        for (int i = 0; i < m_popSize; i++) {
            m_popToEvaluate.add(randomIndividual());
        }
        m_current = m_popToEvaluate.remove(0);
    }

    private BinaryCombinationUXIndividual randomIndividual() {
        int k = m_featureColumns.size();

        BitSet bitSet = new BitSet(k);
        for (int i = 0; i < k; i++) {
            if (m_random.nextBoolean()) {
                bitSet.flip(i);
            }
        }
        return new BinaryCombinationUXIndividual(k, 1, bitSet);
    }
    //
    //    private static double eval(final Genotype<BitGene> gt) {
    //        BitChromosome as = gt.getChromosome().as(BitChromosome.class);
    ////        while (m_currentScore == null) {
    ////
    ////        }
    ////        double score = m_currentScore;
    ////        m_currentScore = null;
    ////        return score;
    //        return 0;
    //    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Integer> getFeatureLevel() {
        return Arrays.stream(m_current.getSelected()).boxed().collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameForLastChange() {
        // column name of output, probably changes in node model necessary...
        return "Selected features";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getLastBestFeature() {
        // can be omitted, output of node must be different for this strategy
        return Arrays.stream(m_current.getSelected()).boxed().collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfIterations() {
        // TODO Auto-generated method stub
        return m_maxIteration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getCurrentFeature() {
        // just need for flow variables, can be omitted
        return 0;
    }

}
