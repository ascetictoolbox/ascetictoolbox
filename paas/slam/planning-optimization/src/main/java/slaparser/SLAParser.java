/**
 * Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package slaparser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.apache.log4j.Logger;
import org.slasoi.slamodel.core.CompoundConstraintExpr;
import org.slasoi.slamodel.core.CompoundDomainExpr;
import org.slasoi.slamodel.core.ConstraintExpr;
import org.slasoi.slamodel.core.DomainExpr;
import org.slasoi.slamodel.core.FunctionalExpr;
import org.slasoi.slamodel.core.SimpleDomainExpr;
import org.slasoi.slamodel.core.TypeConstraintExpr;
import org.slasoi.slamodel.primitives.BOOL;
import org.slasoi.slamodel.primitives.CONST;
import org.slasoi.slamodel.primitives.Expr;
import org.slasoi.slamodel.primitives.ID;
import org.slasoi.slamodel.primitives.ValueExpr;
import org.slasoi.slamodel.service.Interface;
import org.slasoi.slamodel.service.ResourceType;
import org.slasoi.slamodel.sla.AgreementTerm;
import org.slasoi.slamodel.sla.Customisable;
import org.slasoi.slamodel.sla.Guaranteed;
import org.slasoi.slamodel.sla.InterfaceDeclr;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;
import org.slasoi.slamodel.sla.VariableDeclr;
import org.slasoi.slamodel.vocab.B4Terms;
import org.slasoi.slamodel.vocab.common;
import org.slasoi.slamodel.vocab.core;
import org.slasoi.slamodel.vocab.resources;
import org.slasoi.slamodel.vocab.units;

import eu.ascetic.paas.slam.poc.exceptions.InvalidSLATemplateFormatException;
import eu.ascetic.paas.slam.poc.exceptions.MoreThanOneInterfaceDefinedException;
import eu.ascetic.paas.slam.poc.exceptions.NoVMSpecifiedException;

import utils.Constant;

import datastructure.Bandwidth;
import datastructure.CPU;
import datastructure.Harddisk;
import datastructure.Memory;
import datastructure.Request;
import datastructure.Resource;
import datastructure.VMAccessPoint;

/**
 * The class <code>SLAParser</code> parsers the coming SLA / SLA template.
 * 
 * @author Kuan Lu
 */
public class SLAParser {
    private SLATemplate slat;
    private ValueExpr value;
    private static final Logger LOGGER = Logger.getLogger(SLAParser.class);
    private LinkedHashMap<String, Request> resourceRequest;
    private LinkedHashMap<String, VMAccessPoint> vmAccessPointList;
    private SimpleDomainExpr sde;
    private CONST newValue;
    private LinkedHashMap<String, CONST> variableDeclr = new LinkedHashMap<String, CONST>();

    /**
     * Starts parser.
     */
    public SLAParser(SLATemplate slat) {
        try {
            this.resourceRequest = new LinkedHashMap<String, Request>();
            this.vmAccessPointList = new LinkedHashMap<String, VMAccessPoint>();
            if (slat instanceof SLA) {
                this.slat = slat;
                this.render();

            }
            else if (slat instanceof SLATemplate) {
                this.slat = slat;
                this.render();
            }
        }
        catch (NoVMSpecifiedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (MoreThanOneInterfaceDefinedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvalidSLATemplateFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void render() throws NoVMSpecifiedException, MoreThanOneInterfaceDefinedException,
            InvalidSLATemplateFormatException {
        if (this.slat == null)
            throw new IllegalArgumentException("No SLA(T) specified");
        if (this.slat instanceof SLA) {
            this.x_SLA();
        }
        else if (this.slat instanceof SLATemplate) {
            this.x_SLA();
        }
    }
    // ----SLA-------------------------------------------------------------------------------------------

    private void x_SLA() throws NoVMSpecifiedException, MoreThanOneInterfaceDefinedException,
            InvalidSLATemplateFormatException {
        if (slat == null)
            throw new IllegalArgumentException("No SLA(T) specified");
        x_SLA_CONTENT();
    }

    // ----SLAT CONTENT-------------------------------------------------------------------------------------------

    private void x_SLA_CONTENT() throws NoVMSpecifiedException, MoreThanOneInterfaceDefinedException,
            InvalidSLATemplateFormatException {
        if (this.slat == null)
            throw new IllegalArgumentException("No SLA specified");
        // ---- PARTIES -------

        InterfaceDeclr[] idecs = this.slat.getInterfaceDeclrs();
        // ---- INTERFACE DECLARATIONS-------------------------------------------------
        if (idecs != null) {
            for (InterfaceDeclr idec : idecs) {
                this.x_RENDER(idec);
            }
        }

        // ---- AGREEMENT TERMS--------------------------------------------------------
        AgreementTerm[] terms = this.slat.getAgreementTerms();
        if (terms != null)
            for (AgreementTerm t : terms) {
                x_RENDER(t);
                this.variableDeclr.clear();
            }
    }

    // ----INTERFACE DECLR -------------------------------------------------------------------------------------------
    private void x_RENDER(InterfaceDeclr idec) {
        Interface iface = idec.getInterface();
        if (iface instanceof ResourceType) {
            if (((ResourceType) iface).getName().equalsIgnoreCase("VirtualMachine")||((ResourceType) iface).getName().equalsIgnoreCase("VirtualMachines")||((ResourceType) iface).getName().equalsIgnoreCase("VM")||((ResourceType) iface).getName().equalsIgnoreCase("VMs")) {
                this.vmAccessPointList.put(idec.getId().getValue(), new VMAccessPoint());
            }
        }
    }


    private void x_RENDER(AgreementTerm term) throws NoVMSpecifiedException, InvalidSLATemplateFormatException {
        VariableDeclr[] vdecs = term.getVariableDeclrs();
        if (vdecs != null)
            for (VariableDeclr v : vdecs) {
                x_RENDER(v);
            }

        Guaranteed[] gs = term.getGuarantees();
        for (Guaranteed g : gs) {
            if (g instanceof Guaranteed.State)
                x_RENDER((Guaranteed.State) g);
        }
    }

    // ----VAR-------------------------------------------------------------------------------------------
    private void x_RENDER(VariableDeclr vdec) throws InvalidSLATemplateFormatException {
        if (vdec instanceof Customisable) {
            Customisable c = (Customisable) vdec;
            // ID for Var
            ID var = c.getVar();
            // CONST for Value
            CONST valu = c.getValue();
            this.variableDeclr.put(var.getValue(), valu);
        }
        // create VM
        else if (!(vdec instanceof Customisable)) {
            String term = "";
            String var = vdec.getVar().getValue();
            Expr exp = vdec.getExpr();
            if (exp instanceof FunctionalExpr) {
                term = ((FunctionalExpr) exp).getOperator().toString();
                ValueExpr[] parameter = ((FunctionalExpr) exp).getParameters();
                // one VM points to only one VM-Access-point
                if ((parameter[0] instanceof ID) & term.equalsIgnoreCase(core.subset_of.toString())) {
                    Request request = new Request(Constant.Client_Type_Gold);
                    this.copyVMAccessPoint2Request(request, this.vmAccessPointList.get(((ID) parameter[0]).getValue()));
                    request.setVmName(var);
                    this.resourceRequest.put(var, request);
                }
                // SLA without customisable
                else if ((parameter[0] instanceof CONST) & !term.equalsIgnoreCase(core.subset_of.toString())) {
                    this.variableDeclr.put(var, (CONST) parameter[0]);
                }
            }

            else if (exp instanceof CompoundDomainExpr) {
                LOGGER.error("Sorry, POC dose not support more than one constraints to a specific term.");
                throw new InvalidSLATemplateFormatException();
            }
        }
    }

    private void copyVMAccessPoint2Request(Request request, VMAccessPoint vmAccessPoint) {
        request.setAvailability(vmAccessPoint.getAvailability());
    }

    // ----STATE-------------------------------------------------------------------------------------------
    private void x_RENDER(Guaranteed.State gs) throws NoVMSpecifiedException {
        ConstraintExpr state = gs.getState();
        x_RENDER(state);
    }


    private void x_RENDER(ConstraintExpr ce) throws NoVMSpecifiedException {
        String term = "";
        ArrayList<String> VMList = new ArrayList<String>();
        if (ce instanceof TypeConstraintExpr) {
            // TypeConstraintExpr
            TypeConstraintExpr tce = (TypeConstraintExpr) ce;
            ValueExpr valu = tce.getValue();
            if (valu instanceof FunctionalExpr) {
                term = ((FunctionalExpr) valu).getOperator().toString();
                ValueExpr[] parameter = ((FunctionalExpr) valu).getParameters();
                for (ValueExpr v : parameter) {
                    if (v instanceof ID) {
                        String id = ((ID) v).getValue();
                        if (id.equalsIgnoreCase("")) {
                            LOGGER.error("No interface information specified in agreement term : " + term);
                            throw new NoVMSpecifiedException();
                        }
                        VMList.add(id);
                    }
                }
            }
            // SimpleDomainExpr
            DomainExpr de = tce.getDomain();
            if (de instanceof SimpleDomainExpr) {
                sde = (SimpleDomainExpr) de;
                value = sde.getValue();
            }
            else if (de instanceof CompoundDomainExpr) {
                LOGGER.error("Sorry, POC dose not support more than one constraints to a specific term.");
                return;
            }

            for (String vm : VMList) {
                VMAccessPoint request;
                if (this.resourceRequest.containsKey(vm)) {
                    request = this.resourceRequest.get(vm);
                }
                else if (this.vmAccessPointList.containsKey(vm)) {
                    request = this.vmAccessPointList.get(vm);
                }
                else {
                    LOGGER.error("The required VM: " + vm + " is either not in VM list nor in VM Access Point list.");
                    return;
                }
                
                if(request instanceof Request){
                    // set starting time
                    if(this.variableDeclr.containsKey(Constant.startTtime)){
                        ((Request) request).setStartTime(((CONST)this.variableDeclr.get("START_TIME_VAR")).getValue());
                    }
                    
                    // set ending time
                    if(this.variableDeclr.containsKey(Constant.endTime)){
                        ((Request) request).setFreeAt(((CONST)this.variableDeclr.get("END_TIME_VAR")).getValue());
                    } 
                }
                
                if (term.equalsIgnoreCase(common.availability.toString())) {
                    if (value instanceof CONST) {
                        double value_trans = Double.parseDouble(((CONST) value).getValue());
                        if (value_trans >= Constant.basicAvailability) {
                            request.setAvailability(value_trans);
                        }
                        else {
                            request.setAvailability(Constant.basicAvailability);
                            newValue = new CONST(String.valueOf(Constant.basicAvailability), units.percentage);
                            sde.setValue(newValue);
                        }
                        continue;
                    }
                    // point to variable
                    else if (value instanceof ID) {
                        CONST temp = this.variableDeclr.get(((ID) value).getValue());
                        if (temp != null) {
                            double value_trans = Double.parseDouble(((CONST) temp).getValue());
                            if (value_trans >= Constant.basicAvailability) {
                                request.setAvailability(value_trans);
                            }
                            else
                                request.setAvailability(Constant.basicAvailability);
                            continue;
                        }
                        else {
                            LOGGER.error("Unable to get the value of Variable of " + value.toString() + ".");
                            return;
                        }
                    }
                    else {
                        LOGGER.error("Invalid datastructue format of value that should be CONST.(availability)");
                        return;
                    }
                }
                else if (term.equalsIgnoreCase(common.isolation.toString()) & (request instanceof Request)) {
                    if (value instanceof BOOL) {
                        ((Request) request).setIsolation(((BOOL) value).getValue());
                        continue;
                    }
                    // point to variable
                    else if (value instanceof ID) {
                        CONST temp = this.variableDeclr.get(((ID) value).getValue());
                        if (temp != null) {
                            ((Request) request).setIsolation(Boolean.parseBoolean(temp.getValue()));
                        }
                        else {
                            LOGGER.error("Unable to get the value of Variable of " + value.toString() + ".");
                            return;
                        }
                    }
                    else {
                        LOGGER.error("Invalid datastructue format of value that should be BOOL.(isolation)");
                        return;
                    }
                }
                
                
                else if (term.equalsIgnoreCase(resources.vm_cores.toString()) & (request instanceof Request)) {
                    if (value instanceof CONST) {
                        ((Request) request).setCpuNr((CONST) value);
                        int value_trans = Integer.parseInt(((CONST) value).getValue());
                        LinkedHashMap<String, Resource> resource = ((Request) request).getResource();
                        if (resource.containsKey(Constant.CPU) & resource.get(Constant.CPU) instanceof CPU) {
                            if (value_trans >= Constant.CPU_Core_Mini_Nr_VM0
                                    & value_trans <= Constant.CPU_Core_Max_Nr_VM0) {
                                Resource cpu = resource.get(Constant.CPU);
                                cpu.setAmount(value_trans);
                            }
                            else {
                                LOGGER.error("Unsupported CPU core number for VM: " + value_trans
                                        + ". The lowest CPU core number setting is : " + Constant.CPU_Core_Mini_Nr_VM0
                                        + ". The highest CPU core number setting is : " + Constant.CPU_Core_Max_Nr_VM0);
                                return;
                            }
                        }
                        else {
                            if (value_trans >= Constant.CPU_Core_Mini_Nr_VM0
                                    & value_trans <= Constant.CPU_Core_Max_Nr_VM0) {
                                CPU cpu = new CPU();
                                cpu.setAmount(value_trans);
                                resource.put(Constant.CPU, cpu);
                            }
                            else {
                                LOGGER.error("Unsupported CPU core number for VM: " + value_trans
                                        + ". The lowest CPU core number setting is : " + Constant.CPU_Core_Mini_Nr_VM0
                                        + ". The highest CPU core number setting is : " + Constant.CPU_Core_Max_Nr_VM0);
                                return;
                            }
                        }
                        continue;
                    }
                    // point to variable
                    else if (value instanceof ID) {
                        CONST temp = this.variableDeclr.get(((ID) value).getValue());
                        if (temp != null) {
                            ((Request) request).setCpuNr(temp);
                            int value_trans = Integer.parseInt(temp.getValue());
                            LinkedHashMap<String, Resource> resource = ((Request) request).getResource();
                            if (resource.containsKey(Constant.CPU) & resource.get(Constant.CPU) instanceof CPU) {
                                if (value_trans >= Constant.CPU_Core_Mini_Nr_VM0
                                        & value_trans <= Constant.CPU_Core_Max_Nr_VM0) {
                                    Resource cpu = resource.get(Constant.CPU);
                                    cpu.setAmount(value_trans);
                                }
                                else {
                                    LOGGER.error("Unsupported CPU core number for VM: " + value_trans
                                            + ". The lowest CPU core number setting is : "
                                            + Constant.CPU_Core_Mini_Nr_VM0
                                            + ". The highest CPU core number setting is : "
                                            + Constant.CPU_Core_Max_Nr_VM0);
                                    return;
                                }
                            }
                            else {
                                if (value_trans >= Constant.CPU_Core_Mini_Nr_VM0
                                        & value_trans <= Constant.CPU_Core_Max_Nr_VM0) {
                                    CPU cpu = new CPU();
                                    cpu.setAmount(value_trans);
                                    resource.put(Constant.CPU, cpu);
                                }
                                else {
                                    LOGGER.error("Unsupported CPU core number for VM: " + value_trans
                                            + ". The lowest CPU core number setting is : "
                                            + Constant.CPU_Core_Mini_Nr_VM0
                                            + ". The highest CPU core number setting is : "
                                            + Constant.CPU_Core_Max_Nr_VM0);
                                    return;
                                }
                            }
                            continue;
                        }
                        else {
                            LOGGER.error("Unable to get the value of Variable of " + value.toString() + ".");
                            return;
                        }
                    }
                    else {
                        LOGGER.error("Invalid datastructue format of value that should be CONST.(CPU_cores)");
                        return;
                    }
                }
                else if (term.equalsIgnoreCase(resources.cpu_speed.toString()) & (request instanceof Request)) {
                    if (value instanceof CONST) {
                        float value_trans = Float.parseFloat(((CONST) value).getValue());
                        LinkedHashMap<String, Resource> resource = ((Request) request).getResource();
                        if (resource.containsKey(Constant.CPU) & resource.get(Constant.CPU) instanceof CPU) {
                            if (value_trans >= Constant.CPU_Mini_Speed_VM0 & value_trans <= Constant.CPU_Max_Speed_VM0) {
                                CPU cpu = (CPU) resource.get(Constant.CPU);
                                double price =
                                        Constant.CPU_Basic_Price + (value_trans - Constant.CPU_Mini_Speed_VM0) * 10
                                                * Constant.CPU_Price_Increasement;
                                cpu.setPrice(price);
                                cpu.setSpeed(value_trans);
                            }
                            else {
                                LOGGER.error("Unsupported CPU speed for VM: " + value_trans
                                        + ". The lowest CPU speed setting is " + Constant.CPU_Mini_Speed_VM0
                                        + " GHz. The highest CPU speed setting is " + Constant.CPU_Max_Speed_VM0
                                        + " GHz.");
                                return;
                            }
                        }
                        else {
                            if (value_trans >= Constant.CPU_Mini_Speed_VM0 & value_trans <= Constant.CPU_Max_Speed_VM0) {
                                CPU cpu = new CPU();
                                double price =
                                        Constant.CPU_Basic_Price + (value_trans - Constant.CPU_Mini_Speed_VM0) * 10
                                                * Constant.CPU_Price_Increasement;
                                cpu.setPrice(price);
                                cpu.setSpeed(value_trans);
                                resource.put(Constant.CPU, cpu);
                            }
                            else {
                                LOGGER.error("Unsupported CPU speed for VM: " + value_trans
                                        + ". The lowest CPU speed setting is " + Constant.CPU_Mini_Speed_VM0
                                        + " GHz. The highest CPU speed setting is " + Constant.CPU_Max_Speed_VM0
                                        + " GHz.");
                                return;
                            }
                        }
                        continue;

                    }
                    else if (value instanceof ID) {
                        CONST temp = this.variableDeclr.get(((ID) value).getValue());
                        if (temp != null) {
                            float value_trans = Float.parseFloat(temp.getValue());
                            LinkedHashMap<String, Resource> resource = ((Request) request).getResource();
                            if (resource.containsKey(Constant.CPU) & resource.get(Constant.CPU) instanceof CPU) {
                                if (value_trans >= Constant.CPU_Mini_Speed_VM0
                                        & value_trans <= Constant.CPU_Max_Speed_VM0) {
                                    CPU cpu = (CPU) resource.get(Constant.CPU);
                                    double price =
                                            Constant.CPU_Basic_Price + (value_trans - Constant.CPU_Mini_Speed_VM0) * 10
                                                    * Constant.CPU_Price_Increasement;
                                    cpu.setPrice(price);
                                    cpu.setSpeed(value_trans);
                                }
                                else {
                                    LOGGER.error("Unsupported CPU speed for VM: " + value_trans
                                            + ". The lowest CPU speed setting is " + Constant.CPU_Mini_Speed_VM0
                                            + " GHz. The highest CPU speed setting is " + Constant.CPU_Max_Speed_VM0
                                            + " GHz.");
                                    return;
                                }
                            }
                            else {
                                if (value_trans >= Constant.CPU_Mini_Speed_VM0
                                        & value_trans <= Constant.CPU_Max_Speed_VM0) {
                                    CPU cpu = new CPU();
                                    double price =
                                            Constant.CPU_Basic_Price + (value_trans - Constant.CPU_Mini_Speed_VM0) * 10
                                                    * Constant.CPU_Price_Increasement;
                                    cpu.setPrice(price);
                                    cpu.setSpeed(value_trans);
                                    resource.put(Constant.CPU, cpu);
                                }
                                else {
                                    LOGGER.error("Unsupported CPU speed for VM: " + value_trans
                                            + ". The lowest CPU speed setting is " + Constant.CPU_Mini_Speed_VM0
                                            + " GHz. The highest CPU speed setting is " + Constant.CPU_Max_Speed_VM0
                                            + " GHz.");
                                    return;
                                }
                            }
                            continue;

                        }
                        else {
                            LOGGER.error("Unable to get the value of Variable of " + value.toString() + ".");
                            return;
                        }
                    }
                    else {
                        LOGGER.error("Invalid datastructue format of value that should be CONST.(CPU_speed)");
                        return;
                    }
                }
                else if (term.equalsIgnoreCase(resources.memory.toString()) & (request instanceof Request)) {
                    if (value instanceof CONST) {
                        ((Request) request).setMemoryNr((CONST) value);
                        int value_trans = Integer.parseInt(((CONST) value).getValue());
                        LinkedHashMap<String, Resource> resource = ((Request) request).getResource();
                        if (resource.containsKey(Constant.Memory) & resource.get(Constant.Memory) instanceof Memory) {
                            Memory memory = (Memory) resource.get(Constant.Memory);
                                double amount = value_trans / Constant.Memory_Mini_Size;
                                memory.setPrice(Constant.Memory_Basic_Price);
                                memory.setAmount((int) amount);
                        }
                        else {
                            Memory memory = new Memory();
                                double amount = value_trans / Constant.Memory_Mini_Size;
                                memory.setPrice(Constant.Memory_Basic_Price);
                                memory.setAmount((int) amount);
                                resource.put(Constant.Memory, memory);

                        }
                        continue;
                    }
                    else if (value instanceof ID) {
                        CONST temp = this.variableDeclr.get(((ID) value).getValue());
                        if (temp != null) {
                            ((Request) request).setMemoryNr(temp);
                            int value_trans = Integer.parseInt(temp.getValue());
                            LinkedHashMap<String, Resource> resource = ((Request) request).getResource();
                            if (resource.containsKey(Constant.Memory) & resource.get(Constant.Memory) instanceof Memory) {
                                Memory memory = (Memory) resource.get(Constant.Memory);
                                    double amount = value_trans / Constant.Memory_Mini_Size;
                                    memory.setPrice(Constant.Memory_Basic_Price);
                                    memory.setAmount((int) amount);
                            }
                            else {
                                Memory memory = new Memory();
                                    double amount = value_trans / Constant.Memory_Mini_Size;
                                    memory.setPrice(Constant.Memory_Basic_Price);
                                    memory.setAmount((int) amount);
                                    resource.put(Constant.Memory, memory);

                            }
                            continue;
                        }
                        else {
                            LOGGER.error("Unable to get the value of Variable of " + value.toString() + ".");
                            return;
                        }
                    }
                    else {
                        LOGGER.error("Invalid datastructue format of value that should be CONST.(memory)");
                        return;
                    }
                }
                // here accuracy is hard disk
                else if (term.equalsIgnoreCase(common.accuracy.toString()) & (request instanceof Request)) {
                    if (value instanceof CONST) {
                        ((Request) request).setHarddiskNr((CONST) value);
                        int value_trans = Integer.parseInt(((CONST) value).getValue());
                        LinkedHashMap<String, Resource> resource = ((Request) request).getResource();
                        if (resource.containsKey(Constant.Harddisk) & resource.get(Constant.Harddisk) instanceof Harddisk) {
                            Harddisk disk = (Harddisk) resource.get(Constant.Harddisk);
                                double amount = value_trans;
                                disk.setPrice(Constant.Harddisk_Basic_Price);
                                disk.setAmount((int) amount);
                        }
                        else {
                            Harddisk disk = new Harddisk();
                                double amount = value_trans;
                                disk.setPrice(Constant.Harddisk_Basic_Price);
                                disk.setAmount((int) amount);
                                resource.put(Constant.Harddisk, disk);

                        }
                        continue;
                    }
                    else if (value instanceof ID) {
                        CONST temp = this.variableDeclr.get(((ID) value).getValue());
                        if (temp != null) {
                            ((Request) request).setHarddiskNr(temp);
                            int value_trans = Integer.parseInt(temp.getValue());
                            LinkedHashMap<String, Resource> resource = ((Request) request).getResource();
                            if (resource.containsKey(Constant.Harddisk) & resource.get(Constant.Harddisk) instanceof Harddisk) {
                                Harddisk disk = (Harddisk) resource.get(Constant.Harddisk);
                                    double amount = value_trans;
                                    disk.setPrice(Constant.Harddisk_Basic_Price);
                                    disk.setAmount((int) amount);
                            }
                            else {
                                Harddisk disk = new Harddisk();
                                    double amount = value_trans;
                                    disk.setPrice(Constant.Harddisk_Basic_Price);
                                    disk.setAmount((int) amount);
                                    resource.put(Constant.Harddisk, disk);

                            }
                            continue;
                        }
                        else {
                            LOGGER.error("Unable to get the value of Variable of " + value.toString() + ".");
                            return;
                        }
                    }
                    else {
                        LOGGER.error("Invalid datastructue format of value that should be CONST.(harddisk)");
                        return;
                    }
                }

                //bandwidth
                else if (term.equalsIgnoreCase(B4Terms.vm_network_bandwidth.toString()) & (request instanceof Request)) {
                    if (value instanceof CONST) {
                        ((Request) request).setBandwidth((CONST) value);
                        int value_trans = Integer.parseInt(((CONST) value).getValue());
                        LinkedHashMap<String, Resource> resource = ((Request) request).getResource();
                        if (resource.containsKey(Constant.Bandwidth) & resource.get(Constant.Bandwidth) instanceof Bandwidth) {
                            Bandwidth bandwidth = (Bandwidth) resource.get(Constant.Bandwidth);
                                double amount = value_trans;
                                bandwidth.setPrice(Constant.Bandwidth_Basic_Price);
                                bandwidth.setAmount((int) amount);
                        }
                        else {
                            Bandwidth bandwidth = new Bandwidth();
                                double amount = value_trans;
                                bandwidth.setPrice(Constant.Bandwidth_Basic_Price);
                                bandwidth.setAmount((int) amount);
                                resource.put(Constant.Bandwidth, bandwidth);

                        }
                        continue;
                    }
                    else if (value instanceof ID) {
                        CONST temp = this.variableDeclr.get(((ID) value).getValue());
                        if (temp != null) {
                            ((Request) request).setBandwidth(temp);
                            int value_trans = Integer.parseInt(temp.getValue());
                            LinkedHashMap<String, Resource> resource = ((Request) request).getResource();
                            if (resource.containsKey(Constant.Bandwidth) & resource.get(Constant.Bandwidth) instanceof Bandwidth) {
                                Bandwidth bandwidth = (Bandwidth) resource.get(Constant.Bandwidth);
                                    double amount = value_trans;
                                    bandwidth.setPrice(Constant.Bandwidth_Basic_Price);
                                    bandwidth.setAmount((int) amount);
                            }
                            else {
                                Bandwidth bandwidth = new Bandwidth();
                                    double amount = value_trans;
                                    bandwidth.setPrice(Constant.Bandwidth_Basic_Price);
                                    bandwidth.setAmount((int) amount);
                                    resource.put(Constant.Bandwidth, bandwidth);

                            }
                            continue;
                        }
                        else {
                            LOGGER.error("Unable to get the value of Variable of " + value.toString() + ".");
                            return;
                        }
                    }
                    else {
                        LOGGER.error("Invalid datastructue format of value that should be CONST.(bandwidth)");
                        return;
                    }
                }
                else if (term.equalsIgnoreCase(resources.persistence.toString()) & (request instanceof Request)) {
                    if (value instanceof BOOL) {
                        ((Request) request).setPersistence(((BOOL) value).getValue());
                        continue;
                    }
                    else if (value instanceof ID) {
                        CONST temp = this.variableDeclr.get(((ID) value).getValue());
                        if (temp != null) {
                            ((Request) request).setPersistence(Boolean.parseBoolean(temp.getValue()));
                            continue;
                        }
                        else {
                            LOGGER.error("Unable to get the value of Variable of " + value.toString() + ".");
                            return;
                        }
                    }
                    else {
                        LOGGER.error("Invalid datastructue format of value that should be BOOL.(persistence)");
                        return;
                    }
                }
                else if (term.equalsIgnoreCase(core.count.toString()) & (request instanceof Request)) {
                    if (value instanceof CONST) {
                        ((Request) request).setVmNumber((CONST) value);
                        continue;
                    }
                    else if (value instanceof ID) {
                        CONST temp = this.variableDeclr.get(((ID) value).getValue());
                        if (temp != null) {
                            ((Request) request).setVmNumber(temp);
                            continue;
                        }
                        else {
                            LOGGER.error("Unable to get the value of Variable of " + value.toString() + ".");
                            return;
                        }
                    }
                    else {
                        LOGGER.error("Invalid datastructue format of value that should be CONST.(vm_number)");
                        return;
                    }
                }
                else if (term.equalsIgnoreCase(resources.vm_image.toString()) & (request instanceof Request)) {
                    if (value instanceof CONST) {
                        ((Request) request).setImage(((CONST) value).getValue());
                        continue;
                    }
                    else if (value instanceof ID) {
                        CONST temp = this.variableDeclr.get(((ID) value).getValue());
                        if (temp != null) {
                            ((Request) request).setImage(temp.getValue());
                            continue;
                        }
                        else {
                            LOGGER.error("Unable to get the value of Variable of " + value.toString() + ".");
                            return;
                        }
                    }
                    else {
                        LOGGER.error("Invalid datastructue format of value that should be CONST.(vm_image)");
                        return;
                    }
                }
                else if (term.equalsIgnoreCase(Constant.$cup_architecture) & (request instanceof Request)) {
                    if (value instanceof CONST) {
                        LinkedHashMap<String, Resource> resource = ((Request) request).getResource();
                        if (resource.containsKey(Constant.CPU) & resource.get(Constant.CPU) instanceof CPU) {
                            CPU cpu = (CPU) resource.get(Constant.CPU);
                            cpu.setArchitecture(((CONST) value).getValue());
                        }
                        else {
                            CPU cpu = new CPU();
                            cpu.setArchitecture(((CONST) value).getValue());
                            resource.put(Constant.CPU, cpu);
                        }
                        continue;
                    }
                    else if (value instanceof ID) {
                        CONST temp = this.variableDeclr.get(((ID) value).getValue());
                        if (temp != null) {
                            LinkedHashMap<String, Resource> resource = ((Request) request).getResource();
                            if (resource.containsKey(Constant.CPU) & resource.get(Constant.CPU) instanceof CPU) {
                                CPU cpu = (CPU) resource.get(Constant.CPU);
                                cpu.setArchitecture(temp.getValue());
                            }
                            else {
                                CPU cpu = new CPU();
                                cpu.setArchitecture(temp.getValue());
                                resource.put(Constant.CPU, cpu);
                            }
                            continue;
                        }
                        else {
                            LOGGER.error("Unable to get the value of Variable of " + value.toString() + ".");
                            return;
                        }
                    }
                    else {
                        LOGGER.error("Invalid datastructue format of value that should be CONST.(architecture)");
                        return;
                    }
                }
                else if (term.equalsIgnoreCase(Constant.$memory_redundancy) & (request instanceof Request)) {
                    if (value instanceof BOOL) {
                        LinkedHashMap<String, Resource> resource = ((Request) request).getResource();
                        if (resource.containsKey(Constant.Memory) & resource.get(Constant.Memory) instanceof Memory) {
                            Memory memory = (Memory) resource.get(Constant.Memory);
                            memory.setMemory_redundancy(((BOOL) value).getValue());
                        }
                        else {
                            Memory memory = new Memory();
                            memory.setMemory_redundancy(((BOOL) value).getValue());
                            resource.put(Constant.Memory, memory);
                        }
                        continue;
                    }
                    else if (value instanceof ID) {
                        CONST temp = this.variableDeclr.get(((ID) value).getValue());
                        if (temp != null) {
                            LinkedHashMap<String, Resource> resource = ((Request) request).getResource();
                            if (resource.containsKey(Constant.Memory) & resource.get(Constant.Memory) instanceof Memory) {
                                Memory memory = (Memory) resource.get(Constant.Memory);
                                memory.setMemory_redundancy(Boolean.parseBoolean(temp.getValue()));
                            }
                            else {
                                Memory memory = new Memory();
                                memory.setMemory_redundancy(Boolean.parseBoolean(temp.getValue()));
                                resource.put(Constant.Memory, memory);
                            }
                            continue;
                        }
                        else {
                            LOGGER.error("Unable to get the value of Variable of " + value.toString() + ".");
                            return;
                        }
                    }
                    else {
                        LOGGER.error("Invalid datastructue format of value that should be CONST.(memory_redundancy)");
                        return;
                    }
                }

            }

        }
        else if (ce instanceof CompoundConstraintExpr) {
            LOGGER.error("Sorry, POC dose not support more than one constraint to a specific term.");
            return;
        }
    }

    /**
     * Gets the resource request.
     */
    public LinkedHashMap<String, Request> getResourceRequest() {
        return resourceRequest;
    }
}
