/* Created on Jun 19, 2006 5:08:56 PM by thor
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 */
package de.unikn.knime.core.node.meta;

import de.unikn.knime.core.data.DataTableSpec;
import de.unikn.knime.core.node.BufferedDataTable;
import de.unikn.knime.core.node.ExecutionMonitor;
import de.unikn.knime.core.node.InvalidSettingsException;
import de.unikn.knime.core.node.ModelContent;
import de.unikn.knime.core.node.NodeSettings;

/**
 * This model is for injecting models into a meta workflow. It should not be
 * used for anything else.
 * 
 * @author Thorsten Meinl, University of Konstanz
 */
class ModelInputNodeModel extends MetaInputModel {
    private ModelContent m_predictorParams;
    
    /**
     * Creates a new data table input model with no input ports and one 
     * data output node. 
     */
    public ModelInputNodeModel() {
        super(0, 1);
    }

    /**
     * Does nothing but return an empty datatable array.
     * 
     * @param inData the input data table array
     * @param exec the execution monitor
     * @return an empty (zero-length) array
     * @throws Exception actually, no exception is thrown
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionMonitor exec) throws Exception {
        return new BufferedDataTable[0];
    }

    /**
     * Does nothing but return an empty datatable spec array.
     * 
     * @param inSpecs the input specs
     * @return an empty (zero-length) array
     * @throws InvalidSettingsException actually, no exception is thrown
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        return new DataTableSpec[0];
    }

    /**
     * @see de.unikn.knime.core.node.meta.MetaInputModel#canBeExecuted()
     */
    @Override
    public boolean canBeExecuted() {
        return (m_predictorParams != null);
    }

    /**
     * @see de.unikn.knime.core.node.NodeModel
     *  #saveSettingsTo(de.unikn.knime.core.node.NodeSettings)
     */
    @Override
    protected void saveSettingsTo(final NodeSettings settings) {
        // nothing to save here        
    }

    /**
     * @see de.unikn.knime.core.node.NodeModel
     *  #validateSettings(de.unikn.knime.core.node.NodeSettings)
     */
    @Override
    protected void validateSettings(final NodeSettings settings)
    throws InvalidSettingsException {
        // nothing to do here        
    }

    /**
     * @see de.unikn.knime.core.node.NodeModel
     *  #loadValidatedSettingsFrom(de.unikn.knime.core.node.NodeSettings)
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettings settings)
    throws InvalidSettingsException {
        // nothing to do here
    }

    /**
     * @see de.unikn.knime.core.node.NodeModel#reset()
     */
    @Override
    protected void reset() {
        // nothing to do here
    }
    
    /**
     * Sets the predictor params that should be passed on in
     * {@link #savePredictorParams(int, ModelContent)}.
     * 
     * @param predParams the predictor parameters
     */
    public void setPredictorParams(final ModelContent predParams) {
        m_predictorParams = predParams;
    }

    /** 
     * @see de.unikn.knime.core.node.NodeModel
     *  #savePredictorParams(int, de.unikn.knime.core.node.ModelContent)
     */
    @Override
    protected void savePredictorParams(final int index,
            final ModelContent predParams) throws InvalidSettingsException {
        m_predictorParams.copyTo(predParams);
    }    
}
