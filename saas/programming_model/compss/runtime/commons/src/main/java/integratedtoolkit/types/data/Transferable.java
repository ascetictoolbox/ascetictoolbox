/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package integratedtoolkit.types.data;

import integratedtoolkit.api.ITExecution.ParamType;

/**
 *
 * @author flordan
 */
public interface Transferable {

    Object getDataSource();

    void setDataSource(Object dataSource);

    String getDataTarget();

    void setDataTarget(String target);
    
    ParamType getType();
}
