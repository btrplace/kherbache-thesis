package org.btrplace.scheduler.kherbacheThesis;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.InstanceConverter;
import org.btrplace.model.*;
import org.btrplace.model.constraint.Fence;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.migration.MinMTTRMig;
import org.btrplace.model.constraint.migration.Sync;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.model.view.network.Network;
import org.btrplace.model.view.network.Switch;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.duration.LinearToAResourceActionDuration;
import org.btrplace.scheduler.choco.runner.SolutionStatistics;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;
import org.btrplace.scheduler.choco.transition.MigrateVMTransition;
import org.chocosolver.solver.exception.ContradictionException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * @author Vincent Kherbache
 */
public class RandomVMsScale {

    String path = new File("").getAbsolutePath() +
            "/src/test/java/org/btrplace/scheduler/kherbacheThesis/scale/random_vms_scale/";

    @Test
    public void run_mvm() throws Exception {

        for (int in=1; in<101; in++) {
            StringBuilder res = new StringBuilder("SIZE;DURATION;SCHEDULER;PLAN;RUN\n");
            int nb = 10;
            if (in == 1) nb+=10;
            SolvingStatistics ss = null;
            for (int i = 0; i < nb; i++) {
                ss = schedule_plan("instances/x2/instance_" + in + ".json");
                if (ss != null) { res.append("2;" + duration(ss) + ";mVM;" + planDuration(ss) + ';' + in + "\n"); }
                else { break; }
            }
            if (ss != null) saveToCSV("mvm/x2/single-step_"+in+".csv", res);
        }
        for (int in=1; in<101; in++) {
            StringBuilder res = new StringBuilder("SIZE;DURATION;SCHEDULER;PLAN;RUN\n");
            int nb = 10;
            SolvingStatistics ss = null;
            for (int i = 0; i < nb; i++) {
                ss = schedule_plan("instances/x2/instance_slow_" + in + ".json");
                if (ss != null) { res.append("2;" + duration(ss) + ";mVM;" + planDuration(ss) + ';' + in + "\n"); }
                else { break; }
            }
            if (ss != null) saveToCSV("mvm/x2/single-step_slow_"+in+".csv", res);
        }
        for (int in=1; in<101; in++) {
            StringBuilder res = new StringBuilder("SIZE;DURATION;SCHEDULER;PLAN;RUN\n");
            int nb = 10;
            SolvingStatistics ss = null;
            for (int i = 0; i < nb; i++) {
                ss = schedule_plan("instances/x4/instance_slow_" + in + ".json");
                if (ss != null) { res.append("4;" + duration(ss) + ";mVM;" + planDuration(ss) + ';' + in + "\n"); }
                else { break; }
            }
            if (ss != null) saveToCSV("mvm/x4/single-step_slow_"+in+".csv", res);
        }
        for (int in=1; in<101; in++) {
            StringBuilder res = new StringBuilder("SIZE;DURATION;SCHEDULER;PLAN;RUN\n");
            int nb = 10;
            SolvingStatistics ss = null;
            for (int i = 0; i < nb; i++) {
                ss = schedule_plan("instances/x8/instance_slow_" + in + ".json");
                if (ss != null) { res.append("8;" + duration(ss) + ";mVM;" + planDuration(ss) + ';' + in + "\n"); }
                else { break; }
            }
            if (ss != null) saveToCSV("mvm/x8/single-step_slow_"+in+".csv", res);
        }
        for (int in=1; in<101; in++) {
            StringBuilder res = new StringBuilder("SIZE;DURATION;SCHEDULER;PLAN\n");
            int nb = 10;
            SolvingStatistics ss = null;
            for (int i = 0; i < nb; i++) {
                ss = schedule_plan("instances/x18/instance_slow_" + in + ".json");
                if (ss != null) { res.append("18;" + duration(ss) + ";mVM;" + planDuration(ss) + "\n"); }
                else { i = nb; }
            }
            if (ss != null) saveToCSV("mvm/x18/single-step_"+in+".csv", res);
        }
    }

    @Test
    public void run_btrplace() throws Exception {

        for (int in=1; in<101; in++) {
            StringBuilder res = new StringBuilder("SIZE;DURATION;SCHEDULER;PLAN\n");
            int nb = 10;
            if (in == 1) nb+=10;
            SolvingStatistics ss = null;
            for (int i = 0; i < nb; i++) {
                ss = schedule_btrplace_plan("instances/x1/instance_slow_" + in + ".json");
                if (ss != null) res.append("1;" + duration(ss) + ";BtrPlace;" + planDuration(ss) + "\n");
            }
            if (ss != null) saveToCSV("btrplace/x1/no-share_slow_"+in+".csv", res);
        }
        for (int in=1; in<101; in++) {
            StringBuilder res = new StringBuilder("SIZE;DURATION;SCHEDULER;PLAN\n");
            int nb = 10;
            SolvingStatistics ss = null;
            for (int i = 0; i < nb; i++) {
                ss = schedule_btrplace_plan("instances/x2/instance_slow_" + in + ".json");
                if (ss != null) res.append("2;" + duration(ss) + ";BtrPlace;" + planDuration(ss) + "\n");
            }
            if (ss != null) saveToCSV("btrplace/x2/no-share_slow_"+in+".csv", res);
        }
        for (int in=1; in<101; in++) {
            StringBuilder res = new StringBuilder("SIZE;DURATION;SCHEDULER;PLAN\n");
            int nb = 10;
            SolvingStatistics ss = null;
            for (int i = 0; i < nb; i++) {
                ss = schedule_btrplace_plan("instances/x4/instance_slow_" + in + ".json");
                if (ss != null) res.append("4;" + duration(ss) + ";BtrPlace;" + planDuration(ss) + "\n");
            }
            if (ss != null) saveToCSV("btrplace/x4/no-share_slow_"+in+".csv", res);
        }
        for (int in=1; in<101; in++) {
            StringBuilder res = new StringBuilder("SIZE;DURATION;SCHEDULER;PLAN\n");
            int nb = 10;
            SolvingStatistics ss = null;
            for (int i = 0; i < nb; i++) {
                ss = schedule_btrplace_plan("instances/x8/instance_slow_" + in + ".json");
                if (ss != null) res.append("8;" + duration(ss) + ";BtrPlace;" + planDuration(ss) + "\n");
            }
            if (ss != null) saveToCSV("btrplace/x8/no-share_slow_"+in+".csv", res);
        }
        for (int in=1; in<101; in++) {
            StringBuilder res = new StringBuilder("SIZE;DURATION;SCHEDULER;PLAN\n");
            int nb = 10;
            SolvingStatistics ss = null;
            for (int i = 0; i < nb; i++) {
                ss = schedule_btrplace_plan("instances/x18/instance_slow_" + in + ".json");
                if (ss != null) res.append("18;" + duration(ss) + ";BtrPlace;" + planDuration(ss) + "\n");
            }
            if (ss != null) saveToCSV("btrplace/x18/no-share_slow_"+in+".csv", res);
        }
    }

    @Test
    public void create_plans() throws Exception {
        for (int i=1; i<101; i++) {
            generate_plan_x1(i);
            generate_plan_x2(i);
            generate_plan_x4(i);
            generate_plan_x8(i);
            generate_plan_x18(i);
            //generate_plan_x36(i);
        }
    }

    //@Test
    public void go_parallel() throws Exception {

        for (int in=1; in<51; in++) {
            StringBuilder res = new StringBuilder("SIZE;DURATION;SCHEDULER;PLAN\n");
            int nb = 10;
            if (in == 1) nb+=10;
            SolvingStatistics ss = null;
            for (int i = 0; i < nb; i++) {
                ss = schedule_parallel_plan("instances/x1/instance_" + in + ".json");
                if (ss != null) res.append("1;" + duration(ss) + ";mVM;" + planDuration(ss) + "\n");
            }
            if (ss != null) saveToCSV("mvm/x1/btrplace_"+in+".csv", res);
        }
        for (int in=1; in<51; in++) {
            StringBuilder res = new StringBuilder("SIZE;DURATION;SCHEDULER;PLAN\n");
            int nb = 10;
            SolvingStatistics ss = null;
            for (int i = 0; i < nb; i++) {
                ss = schedule_parallel_plan("instances/x2/instance_" + in + ".json");
                if (ss != null) res.append("2;" + duration(ss) + ";mVM;" + planDuration(ss) + "\n");
            }
            if (ss != null) saveToCSV("mvm/x2/btrplace_"+in+".csv", res);
        }
        for (int in=1; in<51; in++) {
            StringBuilder res = new StringBuilder("SIZE;DURATION;SCHEDULER;PLAN\n");
            int nb = 10;
            SolvingStatistics ss = null;
            for (int i = 0; i < nb; i++) {
                ss = schedule_parallel_plan("instances/x4/instance_" + in + ".json");
                if (ss != null) res.append("4;" + duration(ss) + ";mVM;" + planDuration(ss) + "\n");
            }
            if (ss != null) saveToCSV("mvm/x4/btrplace_"+in+".csv", res);
        }
        for (int in=1; in<51; in++) {
            StringBuilder res = new StringBuilder("SIZE;DURATION;SCHEDULER;PLAN\n");
            int nb = 10;
            SolvingStatistics ss = null;
            for (int i = 0; i < nb; i++) {
                ss = schedule_parallel_plan("instances/x8/instance_" + in + ".json");
                if (ss != null) res.append("8;" + duration(ss) + ";mVM;" + planDuration(ss) + "\n");
            }
            if (ss != null) saveToCSV("mvm/x8/btrplace_"+in+".csv", res);
        }
        for (int in=1; in<51; in++) {
            StringBuilder res = new StringBuilder("SIZE;DURATION;SCHEDULER;PLAN\n");
            int nb = 10;
            SolvingStatistics ss = null;
            for (int i = 0; i < nb; i++) {
                ss = schedule_parallel_plan("instances/x18/instance_" + in + ".json");
                if (ss != null) res.append("18;" + duration(ss) + ";mVM;" + planDuration(ss) + "\n");
            }
            if (ss != null) saveToCSV("mvm/x18/btrplace_"+in+".csv", res);
        }
    }

    public double duration(SolvingStatistics s) throws Exception {
        SolutionStatistics x = s.getSolutions().get(0);
        return x.getTime() + s.getCoreRPBuildDuration() + s.getSpeRPDuration();
    }

    public double planDuration(SolvingStatistics s) throws Exception {
        SolutionStatistics x= s.getSolutions().get(0);
        return x.getReconfigurationPlan().getDuration();
    }

    public SolvingStatistics schedule_plan(String instanceName) throws SchedulerException,ContradictionException {

        ReconfigurationPlan p; // = loadPlanFromJSON("plan_1.json");
        Instance i = loadInstanceFromJSON(instanceName);
        
        //Instance i = new Instance(ii.getModel(), ii.getSatConstraints(), new MinMTTR());

        if (i == null) return null;

        // Set parameters
        DefaultParameters ps = new DefaultParameters();
        ps.setVerbosity(0);
        ps.setTimeLimit(60);
        //ps.setMaxEnd(7200);
        ps.doOptimize(false);

        // Set the custom migration transition
        ps.getTransitionFactory().remove(ps.getTransitionFactory().getBuilder(VMState.RUNNING, VMState.RUNNING));
        ps.getTransitionFactory().add(new MigrateVMTransition.Builder());

        // Set a custom objective
        DefaultChocoScheduler sc = new DefaultChocoScheduler(ps);

        try {
            p = sc.solve(i);
            Assert.assertNotNull(p);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        //finally {
        return sc.getStatistics();
        //}
    }

    public SolvingStatistics schedule_parallel_plan(String instanceName) throws SchedulerException,ContradictionException {

        ReconfigurationPlan p; // = loadPlanFromJSON("plan_1.json");
        Instance i = loadInstanceFromJSON(instanceName);

        if (i == null) return null;
        
        // Synchronise all the migrations
        i.getSatConstraints().add(new Sync(i.getModel().getMapping().getAllVMs()));

        // Add PostCopy attribute to synchronise the VMs start time
        for (VM vm: i.getModel().getMapping().getAllVMs()) {
            i.getModel().getAttributes().put(vm, "postCopy", true);
        }

        // Set parameters
        DefaultParameters ps = new DefaultParameters();
        ps.setVerbosity(0);
        ps.setTimeLimit(0);
        //ps.setMaxEnd(600);
        ps.doOptimize(false);

        // Set the custom migration transition
        ps.getTransitionFactory().remove(ps.getTransitionFactory().getBuilder(VMState.RUNNING, VMState.RUNNING));
        ps.getTransitionFactory().add(new MigrateVMTransition.Builder());

        // Set a custom objective
        DefaultChocoScheduler sc = new DefaultChocoScheduler(ps);

        try {
            p = sc.solve(i);
            Assert.assertNotNull(p);
        } catch(Exception e) {
            e.printStackTrace();
        }
        //finally {
        return sc.getStatistics();
        //}
    }

    public SolvingStatistics schedule_btrplace_plan(String instanceName) throws SchedulerException,ContradictionException {

        ReconfigurationPlan p; // = loadPlanFromJSON("plan_1.json");
        Instance ii = loadInstanceFromJSON(instanceName);

        if (ii == null) return null;

        Instance i = new Instance(ii.getModel(), ii.getSatConstraints(), new MinMTTR());

        // Set parameters
        DefaultParameters ps = new DefaultParameters();
        ps.setVerbosity(0);
        ps.setTimeLimit(0);
        //ps.setMaxEnd(600);
        ps.doOptimize(false);

        // Set custom duration evaluator
        ps.getDurationEvaluators().register(MigrateVM.class, new LinearToAResourceActionDuration<>("mem", 8));

        // Detach the network view
        i.getModel().detach(i.getModel().getView(Network.VIEW_ID));

        // Set a custom objective
        DefaultChocoScheduler sc = new DefaultChocoScheduler(ps);

        try {
            p = sc.solve(i);
            Assert.assertNotNull(p);
        } catch(Exception e) {
            e.printStackTrace();
        }
        //finally {
        return sc.getStatistics();
        //}
    }

    public void generate_plan_x1(int nb) throws SchedulerException,ContradictionException {

        // Set nb of nodes and vms
        int nbNodesRack = 24;
        int nbSrcNodes = nbNodesRack * 2;
        int nbDstNodes = nbNodesRack * 1;
        int nbVMs = nbSrcNodes * 2;

        // Set mem + cpu for VMs and Nodes
        int memVMtpl1 = 2, memVMtpl2 = 4, cpuVMs = 1;
        int memSrcNode = 16, cpuSrcNode = 4;
        int memDstNode = 16, cpuDstNode = 4;

        // Maintain a list of actual nodes resources
        List<Integer> nodesRAM = new ArrayList<>();
        List<Integer> nodesCPU = new ArrayList<>();
        for (int i=0; i<nbSrcNodes+nbDstNodes; i++) {
            if (i<nbSrcNodes) {
                nodesRAM.add(i, memSrcNode);
                nodesCPU.add(i, cpuSrcNode);
            }
            else {
                nodesRAM.add(i, memDstNode);
                nodesCPU.add(i, cpuDstNode);
            }
        }

        // Set memoryUsed and dirtyRate (for all VMs)
        int tpl1MemUsed = 2000, tpl1MaxDirtySize = 5, tpl1MaxDirtyDuration = 3; double tpl1DirtyRate = 0; // idle vm
        int tpl2MemUsed = 4000, tpl2MaxDirtySize = 96, tpl2MaxDirtyDuration = 2; double tpl2DirtyRate = 3; // stress --vm 1000 --bytes 70K
        int tpl3MemUsed = 2000, tpl3MaxDirtySize = 96, tpl3MaxDirtyDuration = 2; double tpl3DirtyRate = 3; // stress --vm 1000 --bytes 70K
        int tpl4MemUsed = 4000, tpl4MaxDirtySize = 5, tpl4MaxDirtyDuration = 3; double tpl4DirtyRate = 0; // idle vm

        // New default model
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();

        // Create online source nodes and destination nodes
        List<Node> srcNodes = new ArrayList<>(), dstNodes = new ArrayList<>(), nodes = new ArrayList<>();
        for (int i=0; i<nbSrcNodes; i++) { srcNodes.add(mo.newNode()); ma.addOnlineNode(srcNodes.get(i)); }
        for (int i=0; i<nbDstNodes; i++) { dstNodes.add(mo.newNode()); ma.addOnlineNode(dstNodes.get(i)); }
        for (Node n : srcNodes) { nodes.add(srcNodes.indexOf(n), n); }
        for (Node n : dstNodes) { nodes.add(srcNodes.size() + dstNodes.indexOf(n), n); }

        // Set boot and shutdown time
        for (Node n : dstNodes) { mo.getAttributes().put(n, "boot", 120); /*~2 minutes to boot*/ }
        for (Node n : srcNodes) {  mo.getAttributes().put(n, "shutdown", 17); /*~17 seconds to shutdown*/ }

        // Create running VMs on src nodes
        List<VM> vms = new ArrayList<>();

        // Add resource views
        ShareableResource rcMem = new ShareableResource("mem", 0, 0);
        ShareableResource rcCPU = new ShareableResource("cpu", 0, 0);
        mo.attach(rcMem);
        mo.attach(rcCPU);
        for (Node n : srcNodes) { rcMem.setCapacity(n, memSrcNode); rcCPU.setCapacity(n, cpuSrcNode); }
        for (Node n : dstNodes) { rcMem.setCapacity(n, memDstNode); rcCPU.setCapacity(n, cpuDstNode); }

        // Do a random global placement while being fair with VM templates
        java.util.Random r = new java.util.Random();
        for (int i=0; i<nbVMs; i+=4) {
            Node n; VM v;

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl1MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl1DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl1MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl1MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl1);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl1 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl2MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl2DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl2MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl2MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl2);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl2 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl3MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl3DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl3MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl3MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl1);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl1 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl4MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl4DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl4MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl4MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl2);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl2 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));
        }

        // Add a NetworkView view
        Network net = new Network();
        Switch swSrcRack1 = net.newSwitch();
        Switch swSrcRack2 = net.newSwitch();
        Switch swDstRack1 = net.newSwitch();
        Switch swMain = net.newSwitch();
        net.connect(1000, swSrcRack1, srcNodes.subList(0,nbNodesRack));
        net.connect(1000, swSrcRack2, srcNodes.subList(nbNodesRack,nbNodesRack*2));
        net.connect(1000, swDstRack1, dstNodes.subList(0,nbNodesRack));
        net.connect(10000, swMain, swSrcRack1, swSrcRack2, swDstRack1);
        mo.attach(net);
        //net.generateDot(path + "topology.dot", false);

        // Set parameters
        DefaultParameters ps = new DefaultParameters();
        ps.setVerbosity(0);
        ps.setTimeLimit(60);
        //ps.setMaxEnd(600);
        ps.doOptimize(false);

        // Set the custom migration transition
        ps.getTransitionFactory().remove(ps.getTransitionFactory().getBuilder(VMState.RUNNING, VMState.RUNNING));
        ps.getTransitionFactory().add(new MigrateVMTransition.Builder());

        // Migrate all VMs to random nodes
        List<SatConstraint> cstrs = new ArrayList<>();
        for (int i=0; i<nbVMs; i++) {
            Node n;

            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (n.id() == ma.getVMLocation(vms.get(i)).id() ||
                    rcMem.getConsumption(vms.get(i)) > nodesRAM.get(nodes.indexOf(n)) ||
                    rcCPU.getConsumption(vms.get(i)) > nodesCPU.get(nodes.indexOf(n)));
            cstrs.add(new Fence(vms.get(i), Collections.singleton(n)));
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(vms.get(i)));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(vms.get(i)));

            nodesRAM.set(nodes.indexOf(ma.getVMLocation(vms.get(i))),
                    nodesRAM.get(nodes.indexOf(ma.getVMLocation(vms.get(i))))+rcMem.getConsumption(vms.get(i)));
            nodesCPU.set(nodes.indexOf(ma.getVMLocation(vms.get(i))),
                    nodesCPU.get(nodes.indexOf(ma.getVMLocation(vms.get(i))))+rcCPU.getConsumption(vms.get(i)));
        }

        // Set a custom objective
        DefaultChocoScheduler sc = new DefaultChocoScheduler(ps);
        Instance i = new Instance(mo, cstrs, new MinMTTRMig());

        ReconfigurationPlan p;
        try {
            p = sc.solve(i);
            //Assert.assertNotNull(p);
            if (p != null) {
                saveInstanceToJSON(i, "instances/x1/instance_" + nb + ".json");
            }
        } catch(Exception e) {
            e.printStackTrace();
            saveInstanceToJSON(i, "instances/x1/instance_slow_" + nb + ".json");
        }
    }

    public void generate_plan_x2(int nb) throws SchedulerException,ContradictionException {

        // Set nb of nodes and vms
        int nbNodesRack = 24;
        int nbSrcNodes = nbNodesRack * 2;
        int nbDstNodes = nbNodesRack * 1;
        int nbVMs = nbSrcNodes * 4;

        // Set mem + cpu for VMs and Nodes
        int memVMtpl1 = 2, memVMtpl2 = 4, cpuVMs = 1;
        int memSrcNode = 32, cpuSrcNode = 8;
        int memDstNode = 32, cpuDstNode = 8;

        // Maintain a list of actual nodes resources
        List<Integer> nodesRAM = new ArrayList<>();
        List<Integer> nodesCPU = new ArrayList<>();
        for (int i=0; i<nbSrcNodes+nbDstNodes; i++) {
            if (i<nbSrcNodes) {
                nodesRAM.add(i, memSrcNode);
                nodesCPU.add(i, cpuSrcNode);
            }
            else {
                nodesRAM.add(i, memDstNode);
                nodesCPU.add(i, cpuDstNode);
            }
        }

        // Set memoryUsed and dirtyRate (for all VMs)
        int tpl1MemUsed = 2000, tpl1MaxDirtySize = 5, tpl1MaxDirtyDuration = 3; double tpl1DirtyRate = 0; // idle vm
        int tpl2MemUsed = 4000, tpl2MaxDirtySize = 96, tpl2MaxDirtyDuration = 2; double tpl2DirtyRate = 3; // stress --vm 1000 --bytes 70K
        int tpl3MemUsed = 2000, tpl3MaxDirtySize = 96, tpl3MaxDirtyDuration = 2; double tpl3DirtyRate = 3; // stress --vm 1000 --bytes 70K
        int tpl4MemUsed = 4000, tpl4MaxDirtySize = 5, tpl4MaxDirtyDuration = 3; double tpl4DirtyRate = 0; // idle vm

        // New default model
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();

        // Create online source nodes and destination nodes
        List<Node> srcNodes = new ArrayList<>(), dstNodes = new ArrayList<>(), nodes = new ArrayList<>();
        for (int i=0; i<nbSrcNodes; i++) { srcNodes.add(mo.newNode()); ma.addOnlineNode(srcNodes.get(i)); }
        for (int i=0; i<nbDstNodes; i++) { dstNodes.add(mo.newNode()); ma.addOnlineNode(dstNodes.get(i)); }
        for (Node n : srcNodes) { nodes.add(srcNodes.indexOf(n), n); }
        for (Node n : dstNodes) { nodes.add(srcNodes.size() + dstNodes.indexOf(n), n); }

        // Set boot and shutdown time
        for (Node n : dstNodes) { mo.getAttributes().put(n, "boot", 120); /*~2 minutes to boot*/ }
        for (Node n : srcNodes) {  mo.getAttributes().put(n, "shutdown", 17); /*~17 seconds to shutdown*/ }

        // Create running VMs on src nodes
        List<VM> vms = new ArrayList<>();

        // Add resource views
        ShareableResource rcMem = new ShareableResource("mem", 0, 0);
        ShareableResource rcCPU = new ShareableResource("cpu", 0, 0);
        mo.attach(rcMem);
        mo.attach(rcCPU);
        for (Node n : srcNodes) { rcMem.setCapacity(n, memSrcNode); rcCPU.setCapacity(n, cpuSrcNode); }
        for (Node n : dstNodes) { rcMem.setCapacity(n, memDstNode); rcCPU.setCapacity(n, cpuDstNode); }

        // Do a random global placement while being fair with VM templates
        java.util.Random r = new java.util.Random();
        for (int i=0; i<nbVMs; i+=4) {
            Node n; VM v;

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl1MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl1DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl1MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl1MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl1);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl1 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl2MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl2DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl2MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl2MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl2);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl2 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl3MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl3DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl3MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl3MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl1);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl1 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl4MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl4DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl4MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl4MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl2);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl2 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));
        }

        // Add a NetworkView view
        Network net = new Network();
        Switch swSrcRack1 = net.newSwitch();
        Switch swSrcRack2 = net.newSwitch();
        Switch swDstRack1 = net.newSwitch();
        Switch swMain = net.newSwitch();
        net.connect(1000, swSrcRack1, srcNodes.subList(0,nbNodesRack));
        net.connect(1000, swSrcRack2, srcNodes.subList(nbNodesRack,nbNodesRack*2));
        net.connect(1000, swDstRack1, dstNodes.subList(0,nbNodesRack));
        net.connect(10000, swMain, swSrcRack1, swSrcRack2, swDstRack1);
        mo.attach(net);
        //net.generateDot(path + "topology.dot", false);

        // Set parameters
        DefaultParameters ps = new DefaultParameters();
        ps.setVerbosity(0);
        ps.setTimeLimit(60);
        //ps.setMaxEnd(600);
        ps.doOptimize(false);

        // Set the custom migration transition
        ps.getTransitionFactory().remove(ps.getTransitionFactory().getBuilder(VMState.RUNNING, VMState.RUNNING));
        ps.getTransitionFactory().add(new MigrateVMTransition.Builder());

        // Migrate all VMs to random nodes
        List<SatConstraint> cstrs = new ArrayList<>();
        for (int i=0; i<nbVMs; i++) {
            Node n;

            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (n.id() == ma.getVMLocation(vms.get(i)).id() ||
                    rcMem.getConsumption(vms.get(i)) > nodesRAM.get(nodes.indexOf(n)) ||
                    rcCPU.getConsumption(vms.get(i)) > nodesCPU.get(nodes.indexOf(n)));
            cstrs.add(new Fence(vms.get(i), Collections.singleton(n)));
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(vms.get(i)));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(vms.get(i)));

            nodesRAM.set(nodes.indexOf(ma.getVMLocation(vms.get(i))),
                    nodesRAM.get(nodes.indexOf(ma.getVMLocation(vms.get(i))))+rcMem.getConsumption(vms.get(i)));
            nodesCPU.set(nodes.indexOf(ma.getVMLocation(vms.get(i))),
                    nodesCPU.get(nodes.indexOf(ma.getVMLocation(vms.get(i))))+rcCPU.getConsumption(vms.get(i)));
        }

        // Set a custom objective
        DefaultChocoScheduler sc = new DefaultChocoScheduler(ps);
        Instance i = new Instance(mo, cstrs, new MinMTTRMig());

        ReconfigurationPlan p;
        try {
            p = sc.solve(i);
            //Assert.assertNotNull(p);
            if (p != null) {
                saveInstanceToJSON(i, "instances/x2/instance_" + nb + ".json");
            }
        } catch(Exception e) {
            e.printStackTrace();
            saveInstanceToJSON(i, "instances/x2/instance_slow_" + nb + ".json");
        }
    }

    public void generate_plan_x4(int nb) throws SchedulerException,ContradictionException {

        // Set nb of nodes and vms
        int nbNodesRack = 24;
        int nbSrcNodes = nbNodesRack * 2;
        int nbDstNodes = nbNodesRack * 1;
        int nbVMs = nbSrcNodes * 8;

        // Set mem + cpu for VMs and Nodes
        int memVMtpl1 = 2, memVMtpl2 = 4, cpuVMs = 1;
        int memSrcNode = 64, cpuSrcNode = 16;
        int memDstNode = 64, cpuDstNode = 16;

        // Maintain a list of actual nodes resources
        List<Integer> nodesRAM = new ArrayList<>();
        List<Integer> nodesCPU = new ArrayList<>();
        for (int i=0; i<nbSrcNodes+nbDstNodes; i++) {
            if (i<nbSrcNodes) {
                nodesRAM.add(i, memSrcNode);
                nodesCPU.add(i, cpuSrcNode);
            }
            else {
                nodesRAM.add(i, memDstNode);
                nodesCPU.add(i, cpuDstNode);
            }
        }

        // Set memoryUsed and dirtyRate (for all VMs)
        int tpl1MemUsed = 2000, tpl1MaxDirtySize = 5, tpl1MaxDirtyDuration = 3; double tpl1DirtyRate = 0; // idle vm
        int tpl2MemUsed = 4000, tpl2MaxDirtySize = 96, tpl2MaxDirtyDuration = 2; double tpl2DirtyRate = 3; // stress --vm 1000 --bytes 70K
        int tpl3MemUsed = 2000, tpl3MaxDirtySize = 96, tpl3MaxDirtyDuration = 2; double tpl3DirtyRate = 3; // stress --vm 1000 --bytes 70K
        int tpl4MemUsed = 4000, tpl4MaxDirtySize = 5, tpl4MaxDirtyDuration = 3; double tpl4DirtyRate = 0; // idle vm

        // New default model
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();

        // Create online source nodes and destination nodes
        List<Node> srcNodes = new ArrayList<>(), dstNodes = new ArrayList<>(), nodes = new ArrayList<>();
        for (int i=0; i<nbSrcNodes; i++) { srcNodes.add(mo.newNode()); ma.addOnlineNode(srcNodes.get(i)); }
        for (int i=0; i<nbDstNodes; i++) { dstNodes.add(mo.newNode()); ma.addOnlineNode(dstNodes.get(i)); }
        for (Node n : srcNodes) { nodes.add(srcNodes.indexOf(n), n); }
        for (Node n : dstNodes) { nodes.add(srcNodes.size() + dstNodes.indexOf(n), n); }

        // Set boot and shutdown time
        for (Node n : dstNodes) { mo.getAttributes().put(n, "boot", 120); /*~2 minutes to boot*/ }
        for (Node n : srcNodes) {  mo.getAttributes().put(n, "shutdown", 17); /*~17 seconds to shutdown*/ }

        // Create running VMs on src nodes
        List<VM> vms = new ArrayList<>();

        // Add resource views
        ShareableResource rcMem = new ShareableResource("mem", 0, 0);
        ShareableResource rcCPU = new ShareableResource("cpu", 0, 0);
        mo.attach(rcMem);
        mo.attach(rcCPU);
        for (Node n : srcNodes) { rcMem.setCapacity(n, memSrcNode); rcCPU.setCapacity(n, cpuSrcNode); }
        for (Node n : dstNodes) { rcMem.setCapacity(n, memDstNode); rcCPU.setCapacity(n, cpuDstNode); }

        // Do a random global placement while being fair with VM templates
        java.util.Random r = new java.util.Random();
        for (int i=0; i<nbVMs; i+=4) {
            Node n; VM v;

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl1MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl1DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl1MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl1MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl1);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl1 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl2MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl2DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl2MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl2MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl2);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl2 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl3MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl3DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl3MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl3MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl1);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl1 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl4MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl4DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl4MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl4MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl2);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl2 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));
        }

        // Add a NetworkView view
        Network net = new Network();
        Switch swSrcRack1 = net.newSwitch();
        Switch swSrcRack2 = net.newSwitch();
        Switch swDstRack1 = net.newSwitch();
        Switch swMain = net.newSwitch();
        net.connect(1000, swSrcRack1, srcNodes.subList(0,nbNodesRack));
        net.connect(1000, swSrcRack2, srcNodes.subList(nbNodesRack,nbNodesRack*2));
        net.connect(1000, swDstRack1, dstNodes.subList(0,nbNodesRack));
        net.connect(10000, swMain, swSrcRack1, swSrcRack2, swDstRack1);
        mo.attach(net);
        //net.generateDot(path + "topology.dot", false);

        // Set parameters
        DefaultParameters ps = new DefaultParameters();
        ps.setVerbosity(0);
        ps.setTimeLimit(60);
        //ps.setMaxEnd(600);
        ps.doOptimize(false);

        // Set the custom migration transition
        ps.getTransitionFactory().remove(ps.getTransitionFactory().getBuilder(VMState.RUNNING, VMState.RUNNING));
        ps.getTransitionFactory().add(new MigrateVMTransition.Builder());

        // Migrate all VMs to random nodes
        List<SatConstraint> cstrs = new ArrayList<>();
        for (int i=0; i<nbVMs; i++) {
            Node n;

            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (n.id() == ma.getVMLocation(vms.get(i)).id() ||
                    rcMem.getConsumption(vms.get(i)) > nodesRAM.get(nodes.indexOf(n)) ||
                    rcCPU.getConsumption(vms.get(i)) > nodesCPU.get(nodes.indexOf(n)));
            cstrs.add(new Fence(vms.get(i), Collections.singleton(n)));
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(vms.get(i)));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(vms.get(i)));

            nodesRAM.set(nodes.indexOf(ma.getVMLocation(vms.get(i))),
                    nodesRAM.get(nodes.indexOf(ma.getVMLocation(vms.get(i))))+rcMem.getConsumption(vms.get(i)));
            nodesCPU.set(nodes.indexOf(ma.getVMLocation(vms.get(i))),
                    nodesCPU.get(nodes.indexOf(ma.getVMLocation(vms.get(i))))+rcCPU.getConsumption(vms.get(i)));
        }

        // Set a custom objective
        DefaultChocoScheduler sc = new DefaultChocoScheduler(ps);
        Instance i = new Instance(mo, cstrs, new MinMTTRMig());

        ReconfigurationPlan p;
        try {
            p = sc.solve(i);
            //Assert.assertNotNull(p);
            if (p != null) {
                saveInstanceToJSON(i, "instances/x4/instance_" + nb + ".json");
            }
        } catch(Exception e) {
            e.printStackTrace();
            saveInstanceToJSON(i, "instances/x4/instance_slow_" + nb + ".json");
        }
    }

    public void generate_plan_x8(int nb) throws SchedulerException,ContradictionException {

        // Set nb of nodes and vms
        int nbNodesRack = 24;
        int nbSrcNodes = nbNodesRack * 2;
        int nbDstNodes = nbNodesRack * 1;
        int nbVMs = nbSrcNodes * 16;

        // Set mem + cpu for VMs and Nodes
        int memVMtpl1 = 2, memVMtpl2 = 4, cpuVMs = 1;
        int memSrcNode = 128, cpuSrcNode = 32;
        int memDstNode = 128, cpuDstNode = 32;

        // Maintain a list of actual nodes resources
        List<Integer> nodesRAM = new ArrayList<>();
        List<Integer> nodesCPU = new ArrayList<>();
        for (int i=0; i<nbSrcNodes+nbDstNodes; i++) {
            if (i<nbSrcNodes) {
                nodesRAM.add(i, memSrcNode);
                nodesCPU.add(i, cpuSrcNode);
            }
            else {
                nodesRAM.add(i, memDstNode);
                nodesCPU.add(i, cpuDstNode);
            }
        }

        // Set memoryUsed and dirtyRate (for all VMs)
        int tpl1MemUsed = 2000, tpl1MaxDirtySize = 5, tpl1MaxDirtyDuration = 3; double tpl1DirtyRate = 0; // idle vm
        int tpl2MemUsed = 4000, tpl2MaxDirtySize = 96, tpl2MaxDirtyDuration = 2; double tpl2DirtyRate = 3; // stress --vm 1000 --bytes 70K
        int tpl3MemUsed = 2000, tpl3MaxDirtySize = 96, tpl3MaxDirtyDuration = 2; double tpl3DirtyRate = 3; // stress --vm 1000 --bytes 70K
        int tpl4MemUsed = 4000, tpl4MaxDirtySize = 5, tpl4MaxDirtyDuration = 3; double tpl4DirtyRate = 0; // idle vm

        // New default model
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();

        // Create online source nodes and destination nodes
        List<Node> srcNodes = new ArrayList<>(), dstNodes = new ArrayList<>(), nodes = new ArrayList<>();
        for (int i=0; i<nbSrcNodes; i++) { srcNodes.add(mo.newNode()); ma.addOnlineNode(srcNodes.get(i)); }
        for (int i=0; i<nbDstNodes; i++) { dstNodes.add(mo.newNode()); ma.addOnlineNode(dstNodes.get(i)); }
        for (Node n : srcNodes) { nodes.add(srcNodes.indexOf(n), n); }
        for (Node n : dstNodes) { nodes.add(srcNodes.size() + dstNodes.indexOf(n), n); }

        // Set boot and shutdown time
        for (Node n : dstNodes) { mo.getAttributes().put(n, "boot", 120); /*~2 minutes to boot*/ }
        for (Node n : srcNodes) {  mo.getAttributes().put(n, "shutdown", 17); /*~17 seconds to shutdown*/ }

        // Create running VMs on src nodes
        List<VM> vms = new ArrayList<>();

        // Add resource views
        ShareableResource rcMem = new ShareableResource("mem", 0, 0);
        ShareableResource rcCPU = new ShareableResource("cpu", 0, 0);
        mo.attach(rcMem);
        mo.attach(rcCPU);
        for (Node n : srcNodes) { rcMem.setCapacity(n, memSrcNode); rcCPU.setCapacity(n, cpuSrcNode); }
        for (Node n : dstNodes) { rcMem.setCapacity(n, memDstNode); rcCPU.setCapacity(n, cpuDstNode); }

        // Do a random global placement while being fair with VM templates
        java.util.Random r = new java.util.Random();
        for (int i=0; i<nbVMs; i+=4) {
            Node n; VM v;

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl1MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl1DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl1MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl1MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl1);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl1 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl2MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl2DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl2MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl2MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl2);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl2 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl3MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl3DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl3MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl3MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl1);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl1 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl4MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl4DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl4MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl4MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl2);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl2 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));
        }

        // Add a NetworkView view
        Network net = new Network();
        Switch swSrcRack1 = net.newSwitch();
        Switch swSrcRack2 = net.newSwitch();
        Switch swDstRack1 = net.newSwitch();
        Switch swMain = net.newSwitch();
        net.connect(1000, swSrcRack1, srcNodes.subList(0,nbNodesRack));
        net.connect(1000, swSrcRack2, srcNodes.subList(nbNodesRack,nbNodesRack*2));
        net.connect(1000, swDstRack1, dstNodes.subList(0,nbNodesRack));
        net.connect(10000, swMain, swSrcRack1, swSrcRack2, swDstRack1);
        mo.attach(net);
        //net.generateDot(path + "topology.dot", false);

        // Set parameters
        DefaultParameters ps = new DefaultParameters();
        ps.setVerbosity(0);
        ps.setTimeLimit(60);
        //ps.setMaxEnd(600);
        ps.doOptimize(false);

        // Set the custom migration transition
        ps.getTransitionFactory().remove(ps.getTransitionFactory().getBuilder(VMState.RUNNING, VMState.RUNNING));
        ps.getTransitionFactory().add(new MigrateVMTransition.Builder());

        // Migrate all VMs to random nodes
        List<SatConstraint> cstrs = new ArrayList<>();
        for (int i=0; i<nbVMs; i++) {
            Node n;

            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (n.id() == ma.getVMLocation(vms.get(i)).id() ||
                    rcMem.getConsumption(vms.get(i)) > nodesRAM.get(nodes.indexOf(n)) ||
                    rcCPU.getConsumption(vms.get(i)) > nodesCPU.get(nodes.indexOf(n)));
            cstrs.add(new Fence(vms.get(i), Collections.singleton(n)));
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(vms.get(i)));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(vms.get(i)));

            nodesRAM.set(nodes.indexOf(ma.getVMLocation(vms.get(i))),
                    nodesRAM.get(nodes.indexOf(ma.getVMLocation(vms.get(i))))+rcMem.getConsumption(vms.get(i)));
            nodesCPU.set(nodes.indexOf(ma.getVMLocation(vms.get(i))),
                    nodesCPU.get(nodes.indexOf(ma.getVMLocation(vms.get(i))))+rcCPU.getConsumption(vms.get(i)));
        }

        // Set a custom objective
        DefaultChocoScheduler sc = new DefaultChocoScheduler(ps);
        Instance i = new Instance(mo, cstrs, new MinMTTRMig());

        ReconfigurationPlan p;
        try {
            p = sc.solve(i);
            //Assert.assertNotNull(p);
            if (p != null) {
                saveInstanceToJSON(i, "instances/x8/instance_" + nb + ".json");
            }
        } catch(Exception e) {
            e.printStackTrace();
            saveInstanceToJSON(i, "instances/x8/instance_slow_" + nb + ".json");
        }
    }

    public void generate_plan_x18(int nb) throws SchedulerException,ContradictionException {

        // Set nb of nodes and vms
        int nbNodesRack = 24;
        int nbSrcNodes = nbNodesRack * 2;
        int nbDstNodes = nbNodesRack * 1;
        int nbVMs = nbSrcNodes * 36;

        // Set mem + cpu for VMs and Nodes
        int memVMtpl1 = 2, memVMtpl2 = 4, cpuVMs = 1;
        int memSrcNode = 300, cpuSrcNode = 72;
        int memDstNode = 300, cpuDstNode = 72;

        // Maintain a list of actual nodes resources
        List<Integer> nodesRAM = new ArrayList<>();
        List<Integer> nodesCPU = new ArrayList<>();
        for (int i=0; i<nbSrcNodes+nbDstNodes; i++) {
            if (i<nbSrcNodes) {
                nodesRAM.add(i, memSrcNode);
                nodesCPU.add(i, cpuSrcNode);
            }
            else {
                nodesRAM.add(i, memDstNode);
                nodesCPU.add(i, cpuDstNode);
            }
        }

        // Set memoryUsed and dirtyRate (for all VMs)
        int tpl1MemUsed = 2000, tpl1MaxDirtySize = 5, tpl1MaxDirtyDuration = 3; double tpl1DirtyRate = 0; // idle vm
        int tpl2MemUsed = 4000, tpl2MaxDirtySize = 96, tpl2MaxDirtyDuration = 2; double tpl2DirtyRate = 3; // stress --vm 1000 --bytes 70K
        int tpl3MemUsed = 2000, tpl3MaxDirtySize = 96, tpl3MaxDirtyDuration = 2; double tpl3DirtyRate = 3; // stress --vm 1000 --bytes 70K
        int tpl4MemUsed = 4000, tpl4MaxDirtySize = 5, tpl4MaxDirtyDuration = 3; double tpl4DirtyRate = 0; // idle vm

        // New default model
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();

        // Create online source nodes and destination nodes
        List<Node> srcNodes = new ArrayList<>(), dstNodes = new ArrayList<>(), nodes = new ArrayList<>();
        for (int i=0; i<nbSrcNodes; i++) { srcNodes.add(mo.newNode()); ma.addOnlineNode(srcNodes.get(i)); }
        for (int i=0; i<nbDstNodes; i++) { dstNodes.add(mo.newNode()); ma.addOnlineNode(dstNodes.get(i)); }
        for (Node n : srcNodes) { nodes.add(srcNodes.indexOf(n), n); }
        for (Node n : dstNodes) { nodes.add(srcNodes.size() + dstNodes.indexOf(n), n); }

        // Set boot and shutdown time
        for (Node n : dstNodes) { mo.getAttributes().put(n, "boot", 120); /*~2 minutes to boot*/ }
        for (Node n : srcNodes) {  mo.getAttributes().put(n, "shutdown", 17); /*~17 seconds to shutdown*/ }

        // Create running VMs on src nodes
        List<VM> vms = new ArrayList<>();

        // Add resource views
        ShareableResource rcMem = new ShareableResource("mem", 0, 0);
        ShareableResource rcCPU = new ShareableResource("cpu", 0, 0);
        mo.attach(rcMem);
        mo.attach(rcCPU);
        for (Node n : srcNodes) { rcMem.setCapacity(n, memSrcNode); rcCPU.setCapacity(n, cpuSrcNode); }
        for (Node n : dstNodes) { rcMem.setCapacity(n, memDstNode); rcCPU.setCapacity(n, cpuDstNode); }

        // Do a random global placement while being fair with VM templates
        java.util.Random r = new java.util.Random();
        for (int i=0; i<nbVMs; i+=4) {
            Node n; VM v;

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl1MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl1DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl1MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl1MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl1);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl1 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl2MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl2DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl2MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl2MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl2);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl2 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl3MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl3DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl3MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl3MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl1);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl1 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl4MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl4DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl4MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl4MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl2);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl2 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));
        }

        // Add a NetworkView view
        Network net = new Network();
        Switch swSrcRack1 = net.newSwitch();
        Switch swSrcRack2 = net.newSwitch();
        Switch swDstRack1 = net.newSwitch();
        Switch swMain = net.newSwitch();
        net.connect(1000, swSrcRack1, srcNodes.subList(0,nbNodesRack));
        net.connect(1000, swSrcRack2, srcNodes.subList(nbNodesRack,nbNodesRack*2));
        net.connect(1000, swDstRack1, dstNodes.subList(0,nbNodesRack));
        net.connect(10000, swMain, swSrcRack1, swSrcRack2, swDstRack1);
        mo.attach(net);
        //net.generateDot(path + "topology.dot", false);

        // Set parameters
        DefaultParameters ps = new DefaultParameters();
        ps.setVerbosity(0);
        ps.setTimeLimit(60);
        ps.setMaxEnd(7200);
        ps.doOptimize(false);

        // Set the custom migration transition
        ps.getTransitionFactory().remove(ps.getTransitionFactory().getBuilder(VMState.RUNNING, VMState.RUNNING));
        ps.getTransitionFactory().add(new MigrateVMTransition.Builder());

        // Migrate all VMs to random nodes
        List<SatConstraint> cstrs = new ArrayList<>();
        for (int i=0; i<nbVMs; i++) {
            Node n;

            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (n.id() == ma.getVMLocation(vms.get(i)).id() ||
                    rcMem.getConsumption(vms.get(i)) > nodesRAM.get(nodes.indexOf(n)) ||
                    rcCPU.getConsumption(vms.get(i)) > nodesCPU.get(nodes.indexOf(n)));
            cstrs.add(new Fence(vms.get(i), Collections.singleton(n)));
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(vms.get(i)));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(vms.get(i)));

            nodesRAM.set(nodes.indexOf(ma.getVMLocation(vms.get(i))),
                    nodesRAM.get(nodes.indexOf(ma.getVMLocation(vms.get(i))))+rcMem.getConsumption(vms.get(i)));
            nodesCPU.set(nodes.indexOf(ma.getVMLocation(vms.get(i))),
                    nodesCPU.get(nodes.indexOf(ma.getVMLocation(vms.get(i))))+rcCPU.getConsumption(vms.get(i)));
        }

        // Set a custom objective
        DefaultChocoScheduler sc = new DefaultChocoScheduler(ps);
        Instance i = new Instance(mo, cstrs, new MinMTTRMig());

        ReconfigurationPlan p;
        try {
            p = sc.solve(i);
            //Assert.assertNotNull(p);
            if (p != null) {
                saveInstanceToJSON(i, "instances/x18/instance_" + nb + ".json");
            }
        } catch(Exception e) {
            e.printStackTrace();
            saveInstanceToJSON(i, "instances/x18/instance_slow_" + nb + ".json");
        }
    }

    // 144 cores' server doesn't exists yet !
    public void generate_plan_x36(int nb) throws SchedulerException,ContradictionException {

        // Set nb of nodes and vms
        int nbNodesRack = 24;
        int nbSrcNodes = nbNodesRack * 2;
        int nbDstNodes = nbNodesRack * 1;
        int nbVMs = nbSrcNodes * 72;

        // Set mem + cpu for VMs and Nodes
        int memVMtpl1 = 2, memVMtpl2 = 4, cpuVMs = 1;
        int memSrcNode = 600, cpuSrcNode = 144;
        int memDstNode = 600, cpuDstNode = 144;

        // Maintain a list of actual nodes resources
        List<Integer> nodesRAM = new ArrayList<>();
        List<Integer> nodesCPU = new ArrayList<>();
        for (int i=0; i<nbSrcNodes+nbDstNodes; i++) {
            if (i<nbSrcNodes) {
                nodesRAM.add(i, memSrcNode);
                nodesCPU.add(i, cpuSrcNode);
            }
            else {
                nodesRAM.add(i, memDstNode);
                nodesCPU.add(i, cpuDstNode);
            }
        }

        // Set memoryUsed and dirtyRate (for all VMs)
        int tpl1MemUsed = 2000, tpl1MaxDirtySize = 5, tpl1MaxDirtyDuration = 3; double tpl1DirtyRate = 0; // idle vm
        int tpl2MemUsed = 4000, tpl2MaxDirtySize = 96, tpl2MaxDirtyDuration = 2; double tpl2DirtyRate = 3; // stress --vm 1000 --bytes 70K
        int tpl3MemUsed = 2000, tpl3MaxDirtySize = 96, tpl3MaxDirtyDuration = 2; double tpl3DirtyRate = 3; // stress --vm 1000 --bytes 70K
        int tpl4MemUsed = 4000, tpl4MaxDirtySize = 5, tpl4MaxDirtyDuration = 3; double tpl4DirtyRate = 0; // idle vm

        // New default model
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();

        // Create online source nodes and destination nodes
        List<Node> srcNodes = new ArrayList<>(), dstNodes = new ArrayList<>(), nodes = new ArrayList<>();
        for (int i=0; i<nbSrcNodes; i++) { srcNodes.add(mo.newNode()); ma.addOnlineNode(srcNodes.get(i)); }
        for (int i=0; i<nbDstNodes; i++) { dstNodes.add(mo.newNode()); ma.addOnlineNode(dstNodes.get(i)); }
        for (Node n : srcNodes) { nodes.add(srcNodes.indexOf(n), n); }
        for (Node n : dstNodes) { nodes.add(srcNodes.size() + dstNodes.indexOf(n), n); }

        // Set boot and shutdown time
        for (Node n : dstNodes) { mo.getAttributes().put(n, "boot", 120); /*~2 minutes to boot*/ }
        for (Node n : srcNodes) {  mo.getAttributes().put(n, "shutdown", 17); /*~17 seconds to shutdown*/ }

        // Create running VMs on src nodes
        List<VM> vms = new ArrayList<>();

        // Add resource views
        ShareableResource rcMem = new ShareableResource("mem", 0, 0);
        ShareableResource rcCPU = new ShareableResource("cpu", 0, 0);
        mo.attach(rcMem);
        mo.attach(rcCPU);
        for (Node n : srcNodes) { rcMem.setCapacity(n, memSrcNode); rcCPU.setCapacity(n, cpuSrcNode); }
        for (Node n : dstNodes) { rcMem.setCapacity(n, memDstNode); rcCPU.setCapacity(n, cpuDstNode); }

        // Do a random global placement while being fair with VM templates
        java.util.Random r = new java.util.Random();
        for (int i=0; i<nbVMs; i+=4) {
            Node n; VM v;

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl1MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl1DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl1MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl1MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl1);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl1 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl2MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl2DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl2MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl2MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl2);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl2 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl3MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl3DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl3MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl3MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl1);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl1 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));

            v = mo.newVM(); vms.add(v);
            mo.getAttributes().put(v, "memUsed", tpl4MemUsed);
            mo.getAttributes().put(v, "coldDirtyRate", tpl4DirtyRate);
            mo.getAttributes().put(v, "hotDirtySize", tpl4MaxDirtySize);
            mo.getAttributes().put(v, "hotDirtyDuration", tpl4MaxDirtyDuration);
            rcMem.setConsumption(v, memVMtpl2);
            rcCPU.setConsumption(v, cpuVMs);
            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (memVMtpl2 > nodesRAM.get(nodes.indexOf(n)) || cpuVMs > nodesCPU.get(nodes.indexOf(n)));
            ma.addRunningVM(v, n);
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(v));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(v));
        }

        // Add a NetworkView view
        Network net = new Network();
        Switch swSrcRack1 = net.newSwitch();
        Switch swSrcRack2 = net.newSwitch();
        Switch swDstRack1 = net.newSwitch();
        Switch swMain = net.newSwitch();
        net.connect(1000, swSrcRack1, srcNodes.subList(0,nbNodesRack));
        net.connect(1000, swSrcRack2, srcNodes.subList(nbNodesRack,nbNodesRack*2));
        net.connect(1000, swDstRack1, dstNodes.subList(0,nbNodesRack));
        net.connect(10000, swMain, swSrcRack1, swSrcRack2, swDstRack1);
        mo.attach(net);
        //net.generateDot(path + "topology.dot", false);

        // Set parameters
        DefaultParameters ps = new DefaultParameters();
        ps.setVerbosity(0);
        ps.setTimeLimit(60);
        //ps.setMaxEnd(600);
        ps.doOptimize(false);

        // Set the custom migration transition
        ps.getTransitionFactory().remove(ps.getTransitionFactory().getBuilder(VMState.RUNNING, VMState.RUNNING));
        ps.getTransitionFactory().add(new MigrateVMTransition.Builder());

        // Migrate all VMs to random nodes
        List<SatConstraint> cstrs = new ArrayList<>();
        for (int i=0; i<nbVMs; i++) {
            Node n;

            do { n = nodes.get(r.nextInt(nodes.size())); }
            while (n.id() == ma.getVMLocation(vms.get(i)).id() ||
                    rcMem.getConsumption(vms.get(i)) > nodesRAM.get(nodes.indexOf(n)) ||
                    rcCPU.getConsumption(vms.get(i)) > nodesCPU.get(nodes.indexOf(n)));
            cstrs.add(new Fence(vms.get(i), Collections.singleton(n)));
            nodesRAM.set(nodes.indexOf(n), nodesRAM.get(nodes.indexOf(n))-rcMem.getConsumption(vms.get(i)));
            nodesCPU.set(nodes.indexOf(n), nodesCPU.get(nodes.indexOf(n))-rcCPU.getConsumption(vms.get(i)));

            nodesRAM.set(nodes.indexOf(ma.getVMLocation(vms.get(i))),
                    nodesRAM.get(nodes.indexOf(ma.getVMLocation(vms.get(i))))+rcMem.getConsumption(vms.get(i)));
            nodesCPU.set(nodes.indexOf(ma.getVMLocation(vms.get(i))),
                    nodesCPU.get(nodes.indexOf(ma.getVMLocation(vms.get(i))))+rcCPU.getConsumption(vms.get(i)));
        }

        // Set a custom objective
        DefaultChocoScheduler sc = new DefaultChocoScheduler(ps);
        Instance i = new Instance(mo, cstrs, new MinMTTRMig());

        ReconfigurationPlan p;
        try {
            p = sc.solve(i);
            //Assert.assertNotNull(p);
            if (p != null) {
                saveInstanceToJSON(i, "instances/x36/instance_" + nb + ".json");
            }
        } catch(Exception e) {
            e.printStackTrace();
            saveInstanceToJSON(i, "instances/x36/instance_slow_" + nb + ".json");
        }
    }

    public void saveInstanceToJSON(Instance i, String fileName) {

        InstanceConverter instanceConverter = new InstanceConverter();
        JSONObject obj = null;

        try {
            obj =  instanceConverter.toJSON(i);
        } catch (JSONConverterException e) {
            System.err.println("Error while converting the plan: " + e.toString());
            e.printStackTrace();
        }

        try {
            FileWriter file = new FileWriter(path + fileName);
            file.write(obj.toJSONString());
            file.flush();
            file.close();
        } catch (IOException e) {
            System.err.println("Error while writing the instance: " + e.toString());
            e.printStackTrace();
        }
    }

    public Instance loadInstanceFromJSON(String fileName) {

        // Read the input JSON file
        JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
        Object obj = null;
        try {
            // Check for gzip extension
            if (fileName.endsWith(".gz")) {
                obj = parser.parse(new InputStreamReader(new GZIPInputStream(new FileInputStream(path + fileName))));
            } else {
                obj = parser.parse(new FileReader(path + fileName));
            }
        } catch (ParseException e) {
            System.err.println("Error during XML file parsing: " + e.toString());
            return null;
        } catch (FileNotFoundException e) {
            System.err.println("File '"+fileName+"' not found (" + e.toString() + ")");
            return null;
        } catch (IOException e) {
            System.err.println("IO error while loading plan: " + e.toString());
            return null;
        }
        JSONObject o = (JSONObject) obj;

        InstanceConverter instanceConverter = new InstanceConverter();
        try {
            return instanceConverter.fromJSON(o);
        } catch (JSONConverterException e) {
            System.err.println("Error while converting plan: " + e.toString());
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    public void saveToCSV(String fileName, StringBuilder fileContent) throws Exception {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + fileName), "utf-8"));
            writer.write(String.valueOf(fileContent));
            writer.flush();
        } catch (IOException ex) {
            System.err.println("IO error occurs when trying to write '" + fileName + "': " + ex.toString());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Unable to close the file '" + fileName + "': " + e.toString());
                }
            }
        }
    }
}
